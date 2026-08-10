-- A single server-authoritative path for organiser marketplace publication.
-- It follows 0009_listing_default_due_date.sql and removes client table writes
-- so ownership, available quantity, and the one-open-listing invariant are
-- always checked in the same locked transaction.

create or replace function public.publish_marketplace_listing(
  p_resource_id uuid,
  p_allowed_actions public.transaction_type[],
  p_published_quantity numeric,
  p_unit_coin_price_buy bigint,
  p_unit_coin_price_rent bigint,
  p_default_duration_days integer,
  p_terms text
)
returns jsonb
language plpgsql security definer set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  resource_row public.resource_items;
  listing_row public.marketplace_listings;
  requested_actions public.transaction_type[] := coalesce(
    p_allowed_actions,
    array[]::public.transaction_type[]
  );
begin
  if not public.has_role('ORGANIZER') then
    raise exception using errcode = '42501', message = 'ORGANIZER_ROLE_REQUIRED';
  end if;

  select * into resource_row
  from public.resource_items
  where id = p_resource_id
  for update;

  if not found then
    raise exception using errcode = 'P0002', message = 'RESOURCE_NOT_FOUND';
  end if;
  if resource_row.current_owner_id is distinct from actor_id then
    raise exception using errcode = '42501', message = 'RESOURCE_OWNER_REQUIRED';
  end if;
  if resource_row.status <> 'ACTIVE' or resource_row.archived_at is not null then
    raise exception using errcode = '55000', message = 'RESOURCE_NOT_PUBLISHABLE';
  end if;

  if cardinality(requested_actions) = 0 then
    raise exception using errcode = '22023', message = 'LISTING_ACTION_REQUIRED';
  end if;
  if exists (
    select 1
    from unnest(requested_actions) as requested(action)
    where action not in ('BORROW', 'RENT', 'BUY', 'DONATE', 'EXCHANGE')
  ) then
    raise exception using errcode = '22023', message = 'LISTING_ACTION_NOT_SUPPORTED';
  end if;
  if array_position(requested_actions, null) is not null
    or cardinality(requested_actions) <> (
      select count(distinct action)
      from unnest(requested_actions) as requested(action)
    ) then
    raise exception using errcode = '22023', message = 'LISTING_ACTIONS_MUST_BE_UNIQUE';
  end if;
  if p_published_quantity is null or p_published_quantity <= 0
    or p_published_quantity > public.available_resource_quantity(resource_row.id) then
    raise exception using errcode = '22023', message = 'INVALID_LISTING_QUANTITY';
  end if;
  if resource_row.unit <> 'KG' and p_published_quantity <> trunc(p_published_quantity) then
    raise exception using errcode = '22023', message = 'FRACTIONAL_DISCRETE_QUANTITY';
  end if;
  if 'BUY' = any(requested_actions)
    and (p_unit_coin_price_buy is null or p_unit_coin_price_buy <= 0) then
    raise exception using errcode = '22023', message = 'BUY_PRICE_REQUIRED';
  end if;
  if not ('BUY' = any(requested_actions)) and p_unit_coin_price_buy is not null then
    raise exception using errcode = '22023', message = 'UNEXPECTED_BUY_PRICE';
  end if;
  if 'RENT' = any(requested_actions)
    and (p_unit_coin_price_rent is null or p_unit_coin_price_rent <= 0) then
    raise exception using errcode = '22023', message = 'RENT_PRICE_REQUIRED';
  end if;
  if not ('RENT' = any(requested_actions)) and p_unit_coin_price_rent is not null then
    raise exception using errcode = '22023', message = 'UNEXPECTED_RENT_PRICE';
  end if;
  if requested_actions && array['BORROW', 'RENT']::public.transaction_type[]
    and (p_default_duration_days is null or p_default_duration_days not between 1 and 365) then
    raise exception using errcode = '22023', message = 'LISTING_DURATION_REQUIRED';
  end if;
  if not (requested_actions && array['BORROW', 'RENT']::public.transaction_type[])
    and p_default_duration_days is not null then
    raise exception using errcode = '22023', message = 'UNEXPECTED_LISTING_DURATION';
  end if;
  if char_length(coalesce(p_terms, '')) > 2000 then
    raise exception using errcode = '22023', message = 'LISTING_TERMS_TOO_LONG';
  end if;
  if exists (
    select 1
    from public.marketplace_listings
    where resource_id = resource_row.id
      and status in ('DRAFT', 'PUBLISHED', 'RESERVED')
  ) then
    raise exception using errcode = '55000', message = 'LISTING_ALREADY_OPEN';
  end if;

  insert into public.marketplace_listings (
    resource_id,
    seller_id,
    allowed_actions,
    published_quantity,
    unit_coin_price_buy,
    unit_coin_price_rent,
    default_duration_days,
    terms,
    status,
    published_at
  ) values (
    resource_row.id,
    actor_id,
    requested_actions,
    p_published_quantity,
    p_unit_coin_price_buy,
    p_unit_coin_price_rent,
    p_default_duration_days,
    coalesce(p_terms, ''),
    'PUBLISHED',
    now()
  ) returning * into listing_row;

  return jsonb_build_object('id', listing_row.id);
end;
$$;

revoke insert, update, delete on public.marketplace_listings from authenticated;
revoke all on function public.publish_marketplace_listing(
  uuid,
  public.transaction_type[],
  numeric,
  bigint,
  bigint,
  integer,
  text
) from public;
grant execute on function public.publish_marketplace_listing(
  uuid,
  public.transaction_type[],
  numeric,
  bigint,
  bigint,
  integer,
  text
) to authenticated;
