-- Borrow and rent listings already require a server-owned default_duration_days.
-- The request RPC must persist the corresponding due_at value in the same transaction.
create or replace function public.request_listing_transaction(
  p_listing_id uuid,
  p_transaction_type public.transaction_type,
  p_quantity numeric,
  p_counter_resource_id uuid,
  p_request_reason text,
  p_idempotency_key uuid
)
returns jsonb
language plpgsql security definer set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  replay jsonb;
  listing_row public.marketplace_listings;
  resource_row public.resource_items;
  counter_row public.resource_items;
  transaction_row public.circular_transactions;
  unit_amount bigint := 0;
  total_amount bigint := 0;
  payer_id uuid;
  payee_id uuid;
  due_timestamp timestamptz;
  request_value jsonb := jsonb_build_object(
    'listing_id', p_listing_id,
    'transaction_type', p_transaction_type,
    'quantity', p_quantity,
    'counter_resource_id', p_counter_resource_id,
    'request_reason', p_request_reason
  );
begin
  replay := public.begin_idempotent_command('request_listing_transaction', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;

  if p_transaction_type not in ('BORROW', 'RENT', 'BUY', 'DONATE', 'EXCHANGE') then
    raise exception using errcode = '22023', message = 'LISTING_ACTION_REQUIRED';
  end if;
  if p_quantity is null or p_quantity <= 0 then
    raise exception using errcode = '22023', message = 'INVALID_QUANTITY';
  end if;

  select * into strict listing_row from public.marketplace_listings where id = p_listing_id for update;
  perform id from public.resource_items
  where id in (listing_row.resource_id, p_counter_resource_id)
  order by id for update;
  select * into strict resource_row from public.resource_items where id = listing_row.resource_id;

  if listing_row.status <> 'PUBLISHED' or resource_row.status <> 'ACTIVE' then
    raise exception using errcode = '55000', message = 'LISTING_NOT_AVAILABLE';
  end if;
  if listing_row.seller_id is null or listing_row.seller_id <> resource_row.current_owner_id then
    raise exception using errcode = '55000', message = 'LISTING_OWNER_MISMATCH';
  end if;
  if actor_id = listing_row.seller_id then
    raise exception using errcode = '42501', message = 'SELF_DEALING_FORBIDDEN';
  end if;
  if not (p_transaction_type = any(listing_row.allowed_actions)) then
    raise exception using errcode = '22023', message = 'ACTION_NOT_ALLOWED';
  end if;
  if resource_row.unit <> 'KG' and p_quantity <> trunc(p_quantity) then
    raise exception using errcode = '22023', message = 'FRACTIONAL_DISCRETE_QUANTITY';
  end if;
  if p_quantity > listing_row.published_quantity or p_quantity > public.available_resource_quantity(resource_row.id) then
    raise exception using errcode = '22023', message = 'QUANTITY_UNAVAILABLE';
  end if;

  if p_transaction_type = 'EXCHANGE' then
    if p_counter_resource_id is null then
      raise exception using errcode = '22004', message = 'COUNTER_RESOURCE_REQUIRED';
    end if;
    select * into strict counter_row from public.resource_items where id = p_counter_resource_id;
    if counter_row.id = resource_row.id or counter_row.current_owner_id <> actor_id or counter_row.status <> 'ACTIVE' then
      raise exception using errcode = '42501', message = 'COUNTER_RESOURCE_NOT_OWNED';
    end if;
    if p_quantity <> resource_row.quantity
      or public.available_resource_quantity(resource_row.id) <> resource_row.quantity
      or public.available_resource_quantity(counter_row.id) <> counter_row.quantity then
      raise exception using errcode = '22023', message = 'EXCHANGE_REQUIRES_WHOLE_AVAILABLE_LOTS';
    end if;
    if exists(
      select 1 from public.marketplace_listings
      where resource_id = counter_row.id and status in ('DRAFT', 'PUBLISHED', 'RESERVED')
    ) then
      raise exception using errcode = '55000', message = 'COUNTER_RESOURCE_HAS_OPEN_LISTING';
    end if;
  elsif p_counter_resource_id is not null then
    raise exception using errcode = '22023', message = 'COUNTER_RESOURCE_NOT_ALLOWED';
  end if;

  if p_transaction_type = 'BUY' then unit_amount := listing_row.unit_coin_price_buy;
  elsif p_transaction_type = 'RENT' then unit_amount := listing_row.unit_coin_price_rent;
  end if;
  if unit_amount > 0 then
    total_amount := ceil(unit_amount * p_quantity)::bigint;
    payer_id := actor_id;
    payee_id := listing_row.seller_id;
  end if;
  if p_transaction_type in ('BORROW', 'RENT') then
    due_timestamp := now() + make_interval(days => listing_row.default_duration_days);
  end if;

  insert into public.circular_transactions(
    listing_id, origin_event_id, resource_id, counter_resource_id, requester_id,
    sender_id, receiver_id, transaction_type, quantity, unit,
    coin_payer_id, coin_payee_id, unit_coin_amount, total_coin_amount, due_at, request_reason
  ) values (
    listing_row.id, resource_row.origin_event_id, resource_row.id, p_counter_resource_id, actor_id,
    listing_row.seller_id, actor_id, p_transaction_type, p_quantity, resource_row.unit,
    payer_id, payee_id, unit_amount, total_amount, due_timestamp, nullif(btrim(p_request_reason), '')
  ) returning * into transaction_row;

  return public.finish_idempotent_command(
    'request_listing_transaction', p_idempotency_key,
    jsonb_build_object('transaction', to_jsonb(transaction_row), 'replayed', false)
  );
end;
$$;
