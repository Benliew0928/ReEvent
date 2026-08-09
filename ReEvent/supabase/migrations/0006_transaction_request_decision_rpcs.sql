-- Server-authoritative request and decision commands for the ReEvent 1.0 lifecycle.

alter table public.circular_transactions drop constraint transactions_due_check;
alter table public.circular_transactions add constraint transactions_due_check check (
  (transaction_type in ('BORROW', 'RENT') and (
    (status in ('REQUESTED', 'REJECTED', 'CANCELLED')) or
    (status in ('APPROVED', 'IN_TRANSIT', 'ACTIVE', 'RETURN_IN_PROGRESS', 'COMPLETED') and due_at is not null)
  )) or
  (transaction_type not in ('BORROW', 'RENT') and due_at is null)
);

create or replace function public.require_verified_actor()
returns uuid
language plpgsql stable security definer set search_path = public
as $$
declare
  actor_id uuid := auth.uid();
begin
  if actor_id is null then
    raise exception using errcode = '28000', message = 'AUTH_REQUIRED';
  end if;
  if not exists(select 1 from public.profiles where id = actor_id and role is not null and role_frozen_at is not null) then
    raise exception using errcode = '28000', message = 'VERIFIED_PROFILE_REQUIRED';
  end if;
  return actor_id;
end;
$$;

create or replace function public.begin_idempotent_command(
  command_name text,
  command_key uuid,
  command_request jsonb
)
returns jsonb
language plpgsql security definer set search_path = public
as $$
declare
  current_actor_id uuid := public.require_verified_actor();
  request_digest text := encode(digest(command_request::text, 'sha256'), 'hex');
  existing_record public.idempotency_records;
  inserted_count integer;
begin
  if command_key is null then
    raise exception using errcode = '22004', message = 'IDEMPOTENCY_KEY_REQUIRED';
  end if;

  insert into public.idempotency_records(actor_id, function_name, idempotency_key, request_hash)
  values (current_actor_id, command_name, command_key, request_digest)
  on conflict do nothing;
  get diagnostics inserted_count = row_count;
  if inserted_count = 1 then
    return null;
  end if;

  select * into strict existing_record
  from public.idempotency_records record
  where record.actor_id = current_actor_id
    and record.function_name = command_name
    and record.idempotency_key = command_key
  for update;

  if existing_record.request_hash <> request_digest then
    raise exception using errcode = '22000', message = 'IDEMPOTENCY_KEY_REUSED';
  end if;
  if existing_record.response_json is null then
    raise exception using errcode = '55000', message = 'COMMAND_IN_PROGRESS';
  end if;
  return existing_record.response_json;
end;
$$;

create or replace function public.finish_idempotent_command(
  command_name text,
  command_key uuid,
  command_response jsonb
)
returns jsonb
language plpgsql security definer set search_path = public
as $$
begin
  update public.idempotency_records
  set response_json = command_response
  where actor_id = auth.uid() and function_name = command_name and idempotency_key = command_key;
  if not found then
    raise exception using errcode = '55000', message = 'IDEMPOTENCY_CLAIM_MISSING';
  end if;
  return command_response;
end;
$$;

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
    -- Both resource rows are locked before the request is committed. Approval repeats the
    -- locks in UUID order to serialize competing allocations.
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

  insert into public.circular_transactions(
    listing_id, origin_event_id, resource_id, counter_resource_id, requester_id,
    sender_id, receiver_id, transaction_type, quantity, unit,
    coin_payer_id, coin_payee_id, unit_coin_amount, total_coin_amount, request_reason
  ) values (
    listing_row.id, resource_row.origin_event_id, resource_row.id, p_counter_resource_id, actor_id,
    listing_row.seller_id, actor_id, p_transaction_type, p_quantity, resource_row.unit,
    payer_id, payee_id, unit_amount, total_amount, nullif(btrim(p_request_reason), '')
  ) returning * into transaction_row;

  return public.finish_idempotent_command(
    'request_listing_transaction', p_idempotency_key,
    jsonb_build_object('transaction', to_jsonb(transaction_row), 'replayed', false)
  );
end;
$$;

create or replace function public.request_programme_transaction(
  p_programme_id uuid,
  p_resource_id uuid,
  p_quantity numeric,
  p_request_reason text,
  p_idempotency_key uuid
)
returns jsonb
language plpgsql security definer set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  replay jsonb;
  programme_row public.circular_programmes;
  resource_row public.resource_items;
  transaction_row public.circular_transactions;
  target_type public.transaction_type;
  unit_amount bigint := 0;
  total_amount bigint := 0;
  payer_id uuid;
  payee_id uuid;
  request_value jsonb := jsonb_build_object(
    'programme_id', p_programme_id,
    'resource_id', p_resource_id,
    'quantity', p_quantity,
    'request_reason', p_request_reason
  );
begin
  replay := public.begin_idempotent_command('request_programme_transaction', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;
  if p_quantity is null or p_quantity <= 0 then
    raise exception using errcode = '22023', message = 'INVALID_QUANTITY';
  end if;

  select * into strict resource_row from public.resource_items where id = p_resource_id for update;
  select * into strict programme_row from public.circular_programmes where id = p_programme_id for update;
  if resource_row.current_owner_id <> actor_id or resource_row.status <> 'ACTIVE' then
    raise exception using errcode = '42501', message = 'RESOURCE_NOT_OWNED';
  end if;
  if not programme_row.active or programme_row.partner_id is null then
    raise exception using errcode = '55000', message = 'PROGRAMME_NOT_AVAILABLE';
  end if;
  if programme_row.partner_id = actor_id then
    raise exception using errcode = '42501', message = 'SELF_DEALING_FORBIDDEN';
  end if;
  if programme_row.unit is not null and programme_row.unit <> resource_row.unit then
    raise exception using errcode = '22023', message = 'UNIT_NOT_ACCEPTED';
  end if;
  if resource_row.unit <> 'KG' and p_quantity <> trunc(p_quantity) then
    raise exception using errcode = '22023', message = 'FRACTIONAL_DISCRETE_QUANTITY';
  end if;
  if p_quantity > public.available_resource_quantity(resource_row.id)
    or (programme_row.minimum_quantity is not null and p_quantity < programme_row.minimum_quantity)
    or (programme_row.maximum_quantity is not null and p_quantity > programme_row.maximum_quantity)
    or (programme_row.remaining_capacity is not null and p_quantity > programme_row.remaining_capacity) then
    raise exception using errcode = '22023', message = 'QUANTITY_NOT_ELIGIBLE';
  end if;
  if cardinality(programme_row.accepted_categories) > 0
    and not (lower(resource_row.category) = any(select lower(value) from unnest(programme_row.accepted_categories) value)) then
    raise exception using errcode = '22023', message = 'CATEGORY_NOT_ACCEPTED';
  end if;
  if cardinality(programme_row.accepted_materials) > 0
    and not (lower(resource_row.material) = any(select lower(value) from unnest(programme_row.accepted_materials) value)) then
    raise exception using errcode = '22023', message = 'MATERIAL_NOT_ACCEPTED';
  end if;
  if not (resource_row.condition = any(programme_row.accepted_conditions)) then
    raise exception using errcode = '22023', message = 'CONDITION_NOT_ACCEPTED';
  end if;

  target_type := programme_row.programme_type::text::public.transaction_type;
  if programme_row.coin_direction <> 'FREE' then
    unit_amount := programme_row.unit_coin_amount;
    total_amount := ceil(unit_amount * p_quantity)::bigint;
    if programme_row.coin_direction = 'OWNER_PAYS_PARTNER' then
      payer_id := actor_id; payee_id := programme_row.partner_id;
    else
      payer_id := programme_row.partner_id; payee_id := actor_id;
    end if;
  end if;

  insert into public.circular_transactions(
    programme_id, origin_event_id, resource_id, requester_id, sender_id, receiver_id,
    partner_id, transaction_type, quantity, unit, coin_payer_id, coin_payee_id,
    unit_coin_amount, total_coin_amount, request_reason
  ) values (
    programme_row.id, resource_row.origin_event_id, resource_row.id, actor_id, actor_id,
    programme_row.partner_id, programme_row.partner_id, target_type, p_quantity, resource_row.unit,
    payer_id, payee_id, unit_amount, total_amount, nullif(btrim(p_request_reason), '')
  ) returning * into transaction_row;

  return public.finish_idempotent_command(
    'request_programme_transaction', p_idempotency_key,
    jsonb_build_object('transaction', to_jsonb(transaction_row), 'replayed', false)
  );
end;
$$;

create or replace function public.approve_transaction(
  p_transaction_id uuid,
  p_idempotency_key uuid
)
returns jsonb
language plpgsql security definer set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  replay jsonb;
  transaction_row public.circular_transactions;
  resource_row public.resource_items;
  counter_row public.resource_items;
  listing_row public.marketplace_listings;
  programme_row public.circular_programmes;
  payer_wallet public.recoin_wallets;
  payee_wallet public.recoin_wallets;
  passport_id uuid;
  request_value jsonb := jsonb_build_object('transaction_id', p_transaction_id);
begin
  replay := public.begin_idempotent_command('approve_transaction', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;

  select * into strict transaction_row from public.circular_transactions where id = p_transaction_id for update;
  if transaction_row.status <> 'REQUESTED' then
    raise exception using errcode = '55000', message = 'TRANSACTION_NOT_REQUESTED';
  end if;

  if transaction_row.listing_id is not null then
    select * into strict listing_row from public.marketplace_listings where id = transaction_row.listing_id for update;
  end if;

  -- Deterministic resource lock order prevents two opposite exchange approvals deadlocking.
  perform id from public.resource_items
  where id in (transaction_row.resource_id, transaction_row.counter_resource_id)
  order by id for update;
  select * into strict resource_row from public.resource_items where id = transaction_row.resource_id;

  if transaction_row.listing_id is not null then
    if actor_id <> transaction_row.sender_id or actor_id <> listing_row.seller_id then
      raise exception using errcode = '42501', message = 'DECISION_ACTOR_REQUIRED';
    end if;
    if listing_row.status <> 'PUBLISHED' or resource_row.status <> 'ACTIVE'
      or resource_row.current_owner_id <> transaction_row.sender_id
      or not (transaction_row.transaction_type = any(listing_row.allowed_actions))
      or transaction_row.quantity > listing_row.published_quantity then
      raise exception using errcode = '55000', message = 'LISTING_CHANGED';
    end if;
  else
    select * into strict programme_row from public.circular_programmes where id = transaction_row.programme_id for update;
    if actor_id <> transaction_row.partner_id or actor_id <> programme_row.partner_id or not public.has_role('PARTNER') then
      raise exception using errcode = '42501', message = 'PARTNER_DECISION_REQUIRED';
    end if;
    if not programme_row.active
      or programme_row.remaining_capacity is not null and programme_row.remaining_capacity < transaction_row.quantity then
      raise exception using errcode = '55000', message = 'PROGRAMME_CHANGED';
    end if;
  end if;

  if transaction_row.quantity > public.available_resource_quantity(resource_row.id) then
    raise exception using errcode = '40001', message = 'QUANTITY_CONFLICT';
  end if;
  insert into public.transaction_allocations(transaction_id, resource_id, side, quantity, unit)
  values (transaction_row.id, resource_row.id, 'PRIMARY', transaction_row.quantity, transaction_row.unit);

  if transaction_row.transaction_type = 'EXCHANGE' then
    select * into strict counter_row from public.resource_items where id = transaction_row.counter_resource_id;
    if counter_row.current_owner_id <> transaction_row.requester_id
      or counter_row.status <> 'ACTIVE'
      or public.available_resource_quantity(counter_row.id) <> counter_row.quantity then
      raise exception using errcode = '40001', message = 'COUNTER_RESOURCE_CHANGED';
    end if;
    insert into public.transaction_allocations(transaction_id, resource_id, side, quantity, unit)
    values (transaction_row.id, counter_row.id, 'COUNTER', counter_row.quantity, counter_row.unit);
  end if;

  if transaction_row.total_coin_amount > 0 then
    perform id from public.recoin_wallets
    where profile_id in (transaction_row.coin_payer_id, transaction_row.coin_payee_id)
    order by id for update;
    select * into strict payer_wallet from public.recoin_wallets where profile_id = transaction_row.coin_payer_id;
    select * into strict payee_wallet from public.recoin_wallets where profile_id = transaction_row.coin_payee_id;
    if payer_wallet.available_balance < transaction_row.total_coin_amount then
      raise exception using errcode = '22003', message = 'INSUFFICIENT_RECOINS';
    end if;
    update public.recoin_wallets
    set available_balance = available_balance - transaction_row.total_coin_amount,
        held_balance = held_balance + transaction_row.total_coin_amount,
        version = version + 1
    where id = payer_wallet.id;
    insert into public.recoin_holds(transaction_id, payer_wallet_id, payee_wallet_id, amount)
    values (transaction_row.id, payer_wallet.id, payee_wallet.id, transaction_row.total_coin_amount);
  end if;

  if transaction_row.programme_id is not null and programme_row.remaining_capacity is not null then
    update public.circular_programmes
    set remaining_capacity = remaining_capacity - transaction_row.quantity
    where id = programme_row.id;
  end if;

  if transaction_row.listing_id is not null
    and public.available_resource_quantity(resource_row.id) = 0 then
    update public.marketplace_listings set status = 'RESERVED' where id = listing_row.id;
  end if;

  select id into strict passport_id from public.resource_passports where resource_id = resource_row.id;
  insert into public.passport_events(
    passport_id, transaction_id, event_type, actor_id, quantity, unit, public_summary, idempotency_key
  ) values (
    passport_id, transaction_row.id, 'RESERVED', actor_id, transaction_row.quantity,
    transaction_row.unit, 'Quantity reserved for an approved circular transaction', p_idempotency_key
  );

  update public.circular_transactions
  set status = 'APPROVED', approved_at = now(),
      due_at = case
        when transaction_type in ('BORROW', 'RENT') then now() + make_interval(days => listing_row.default_duration_days)
        else null
      end
  where id = transaction_row.id
  returning * into transaction_row;

  return public.finish_idempotent_command(
    'approve_transaction', p_idempotency_key,
    jsonb_build_object('transaction', to_jsonb(transaction_row), 'replayed', false)
  );
end;
$$;

create or replace function public.reject_transaction(
  p_transaction_id uuid,
  p_reason text,
  p_idempotency_key uuid
)
returns jsonb
language plpgsql security definer set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  replay jsonb;
  transaction_row public.circular_transactions;
  decision_actor uuid;
  request_value jsonb := jsonb_build_object('transaction_id', p_transaction_id, 'reason', p_reason);
begin
  replay := public.begin_idempotent_command('reject_transaction', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;
  if p_reason is null or btrim(p_reason) = '' then
    raise exception using errcode = '22023', message = 'TERMINAL_REASON_REQUIRED';
  end if;

  select * into strict transaction_row from public.circular_transactions where id = p_transaction_id for update;
  decision_actor := case when transaction_row.listing_id is not null then transaction_row.sender_id else transaction_row.partner_id end;
  if transaction_row.status <> 'REQUESTED' then
    raise exception using errcode = '55000', message = 'TRANSACTION_NOT_REQUESTED';
  end if;
  if actor_id <> decision_actor then
    raise exception using errcode = '42501', message = 'DECISION_ACTOR_REQUIRED';
  end if;
  update public.circular_transactions
  set status = 'REJECTED', terminal_reason = left(btrim(p_reason), 1000)
  where id = transaction_row.id
  returning * into transaction_row;

  return public.finish_idempotent_command(
    'reject_transaction', p_idempotency_key,
    jsonb_build_object('transaction', to_jsonb(transaction_row), 'replayed', false)
  );
end;
$$;

create or replace function public.cancel_transaction(
  p_transaction_id uuid,
  p_reason text,
  p_idempotency_key uuid
)
returns jsonb
language plpgsql security definer set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  replay jsonb;
  transaction_row public.circular_transactions;
  hold_row public.recoin_holds;
  decision_actor uuid;
  request_value jsonb := jsonb_build_object('transaction_id', p_transaction_id, 'reason', p_reason);
begin
  replay := public.begin_idempotent_command('cancel_transaction', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;
  if p_reason is null or btrim(p_reason) = '' then
    raise exception using errcode = '22023', message = 'TERMINAL_REASON_REQUIRED';
  end if;

  select * into strict transaction_row from public.circular_transactions where id = p_transaction_id for update;
  decision_actor := case when transaction_row.listing_id is not null then transaction_row.sender_id else transaction_row.partner_id end;
  if transaction_row.status not in ('REQUESTED', 'APPROVED') then
    raise exception using errcode = '55000', message = 'TRANSACTION_NOT_CANCELLABLE';
  end if;
  if actor_id <> transaction_row.requester_id and actor_id <> decision_actor then
    raise exception using errcode = '42501', message = 'TRANSACTION_ACTOR_REQUIRED';
  end if;

  if transaction_row.status = 'APPROVED' then
    update public.transaction_allocations set state = 'RELEASED' where transaction_id = transaction_row.id and state = 'RESERVED';
    select * into hold_row from public.recoin_holds where transaction_id = transaction_row.id and status = 'ACTIVE' for update;
    if hold_row.id is not null then
      update public.recoin_wallets
      set available_balance = available_balance + hold_row.amount,
          held_balance = held_balance - hold_row.amount,
          version = version + 1
      where id = hold_row.payer_wallet_id;
      update public.recoin_holds set status = 'RELEASED', released_at = now() where id = hold_row.id;
    end if;
    if transaction_row.programme_id is not null then
      update public.circular_programmes
      set remaining_capacity = case
        when remaining_capacity is null then null else remaining_capacity + transaction_row.quantity
      end
      where id = transaction_row.programme_id;
    else
      update public.marketplace_listings
      set status = 'PUBLISHED'
      where id = transaction_row.listing_id and status = 'RESERVED';
    end if;
  end if;

  update public.circular_transactions
  set status = 'CANCELLED', terminal_reason = left(btrim(p_reason), 1000)
  where id = transaction_row.id
  returning * into transaction_row;

  return public.finish_idempotent_command(
    'cancel_transaction', p_idempotency_key,
    jsonb_build_object('transaction', to_jsonb(transaction_row), 'replayed', false)
  );
end;
$$;

revoke all on function public.require_verified_actor() from public;
revoke all on function public.begin_idempotent_command(text, uuid, jsonb) from public;
revoke all on function public.finish_idempotent_command(text, uuid, jsonb) from public;
revoke all on function public.request_listing_transaction(uuid, public.transaction_type, numeric, uuid, text, uuid) from public;
revoke all on function public.request_programme_transaction(uuid, uuid, numeric, text, uuid) from public;
revoke all on function public.approve_transaction(uuid, uuid) from public;
revoke all on function public.reject_transaction(uuid, text, uuid) from public;
revoke all on function public.cancel_transaction(uuid, text, uuid) from public;

grant execute on function public.request_listing_transaction(uuid, public.transaction_type, numeric, uuid, text, uuid) to authenticated;
grant execute on function public.request_programme_transaction(uuid, uuid, numeric, text, uuid) to authenticated;
grant execute on function public.approve_transaction(uuid, uuid) to authenticated;
grant execute on function public.reject_transaction(uuid, text, uuid) to authenticated;
grant execute on function public.cancel_transaction(uuid, text, uuid) to authenticated;
