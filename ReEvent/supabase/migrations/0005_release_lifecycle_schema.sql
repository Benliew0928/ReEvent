-- ReEvent 1.0 release schema.
--
-- The application has not had a public production release. The legacy rows created by
-- 0001-0004 are synthetic staging data whose narrow shape cannot be mapped to the frozen
-- release contract without inventing ownership, listing, quantity, or lifecycle facts.
-- Reset those rows here while preserving authenticated profiles and their frozen roles.

drop table if exists public.impact_records cascade;
drop table if exists public.circular_transactions cascade;
drop table if exists public.resource_passports cascade;
drop table if exists public.circular_programmes cascade;
drop table if exists public.resource_items cascade;
drop table if exists public.events cascade;

alter type public.resource_status rename to resource_status_legacy;
alter type public.transaction_status rename to transaction_status_legacy;

create type public.event_status as enum ('DRAFT', 'ACTIVE', 'COMPLETED', 'ARCHIVED');
create type public.event_type as enum ('CONFERENCE', 'EXHIBITION', 'FESTIVAL', 'WORKSHOP', 'COMMUNITY', 'OTHER');
create type public.resource_condition as enum ('NEW', 'GOOD', 'FAIR', 'NEEDS_REPAIR', 'END_OF_LIFE');
create type public.resource_status as enum ('DRAFT', 'ACTIVE', 'RECOVERY_IN_PROGRESS', 'RECOVERED', 'ARCHIVED');
create type public.quantity_unit as enum ('ITEM', 'BOX', 'KG');
create type public.passport_token_status as enum ('ACTIVE', 'REVOKED', 'RETIRED');
create type public.passport_event_type as enum (
  'CREATED', 'LISTED', 'RESERVED', 'CHECKED_OUT', 'RETURN_STARTED', 'RETURNED',
  'OWNERSHIP_TRANSFERRED', 'SPLIT_FROM', 'SPLIT_TO', 'CONDITION_CHANGED',
  'REPAIR_STARTED', 'REPAIRED', 'RECYCLED', 'ARCHIVED'
);
create type public.listing_status as enum ('DRAFT', 'PUBLISHED', 'RESERVED', 'CLOSED', 'CANCELLED');
create type public.programme_type as enum ('REPAIR', 'RECYCLE', 'BUY_BACK');
create type public.coin_direction as enum ('FREE', 'OWNER_PAYS_PARTNER', 'PARTNER_PAYS_OWNER');
create type public.transaction_type as enum ('BORROW', 'RENT', 'BUY', 'DONATE', 'EXCHANGE', 'REPAIR', 'RECYCLE', 'BUY_BACK');
create type public.transaction_status as enum (
  'REQUESTED', 'APPROVED', 'IN_TRANSIT', 'ACTIVE', 'RETURN_IN_PROGRESS',
  'COMPLETED', 'REJECTED', 'CANCELLED'
);
create type public.allocation_side as enum ('PRIMARY', 'COUNTER');
create type public.allocation_state as enum ('RESERVED', 'IN_CUSTODY', 'RELEASED', 'TRANSFERRED', 'CONSUMED');
create type public.confirmation_type as enum ('HANDOVER', 'RECEIPT', 'RETURN');
create type public.hold_status as enum ('ACTIVE', 'SETTLED', 'RELEASED');
create type public.ledger_entry_type as enum (
  'INITIAL_GRANT', 'CIRCULAR_REWARD', 'SETTLEMENT_IN', 'SETTLEMENT_OUT',
  'ACCOUNT_CLOSE_BURN', 'ADMIN_CORRECTION'
);

drop type public.resource_status_legacy;
drop type public.transaction_status_legacy;

drop policy if exists profiles_self_read on public.profiles;
drop policy if exists profiles_self_insert on public.profiles;
drop policy if exists profiles_self_update on public.profiles;
drop trigger if exists profiles_set_updated_at on public.profiles;

drop function if exists public.ensure_current_profile();
drop function if exists public.complete_profile_role(public.user_role);
drop function if exists public.is_event_owner(uuid);

alter table public.profiles drop constraint if exists profiles_id_fkey;
alter table public.profiles drop column if exists email;
alter table public.profiles rename column avatar_url to avatar_path;
alter table public.profiles add column role_frozen_at timestamptz;
update public.profiles set display_name = 'ReEvent user' where btrim(display_name) = '';
update public.profiles set role_frozen_at = coalesce(role_frozen_at, updated_at, now()) where role is not null;
alter table public.profiles
  add constraint profiles_id_fkey foreign key (id) references auth.users(id) on delete restrict,
  add constraint profiles_display_name_check check (char_length(btrim(display_name)) between 1 and 80),
  add constraint profiles_role_frozen_check check (
    (role is null and role_frozen_at is null) or (role is not null and role_frozen_at is not null)
  );

create table public.events (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid references public.profiles(id) on delete set null,
  name text not null check (char_length(btrim(name)) between 1 and 120),
  description text not null default '' check (char_length(description) <= 2000),
  event_type public.event_type,
  starts_at timestamptz not null,
  ends_at timestamptz not null,
  timezone_id text,
  address_text text not null default '',
  latitude numeric(9,6),
  longitude numeric(9,6),
  expected_attendance integer check (expected_attendance > 0),
  recovery_target_percent numeric(5,2) not null default 0 check (recovery_target_percent between 0 and 100),
  status public.event_status not null default 'DRAFT',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  archived_at timestamptz,
  constraint events_time_check check (ends_at > starts_at),
  constraint events_coordinates_check check ((latitude is null) = (longitude is null)),
  constraint events_latitude_check check (latitude is null or latitude between -90 and 90),
  constraint events_longitude_check check (longitude is null or longitude between -180 and 180),
  constraint events_active_fields_check check (
    status = 'DRAFT' or (
      event_type is not null and timezone_id is not null and btrim(timezone_id) <> '' and
      btrim(address_text) <> '' and latitude is not null and expected_attendance is not null
    )
  ),
  constraint events_archive_check check ((status = 'ARCHIVED') = (archived_at is not null))
);
create index events_owner_starts_at_idx on public.events(owner_id, starts_at desc);

create table public.resource_items (
  id uuid primary key default gen_random_uuid(),
  origin_event_id uuid not null references public.events(id),
  parent_resource_id uuid references public.resource_items(id),
  created_by uuid references public.profiles(id) on delete set null,
  current_owner_id uuid references public.profiles(id) on delete set null,
  title text not null check (char_length(btrim(title)) between 1 and 120),
  description text not null default '' check (char_length(description) <= 2000),
  category text not null check (btrim(category) <> ''),
  material text not null check (btrim(material) <> ''),
  condition public.resource_condition not null,
  quantity numeric(12,3) not null check (quantity > 0),
  unit public.quantity_unit not null,
  status public.resource_status not null default 'DRAFT',
  address_text text,
  latitude numeric(9,6),
  longitude numeric(9,6),
  reuse_count integer not null default 0 check (reuse_count >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  archived_at timestamptz,
  constraint resources_not_own_parent check (parent_resource_id is null or parent_resource_id <> id),
  constraint resources_quantity_unit_check check (unit = 'KG' or quantity = trunc(quantity)),
  constraint resources_coordinates_check check ((latitude is null) = (longitude is null)),
  constraint resources_latitude_check check (latitude is null or latitude between -90 and 90),
  constraint resources_longitude_check check (longitude is null or longitude between -180 and 180),
  constraint resources_archive_check check ((status = 'ARCHIVED') = (archived_at is not null)),
  constraint resources_owner_check check (status in ('RECOVERED', 'ARCHIVED') or current_owner_id is not null)
);
create index resources_event_updated_idx on public.resource_items(origin_event_id, updated_at desc);
create index resources_owner_updated_idx on public.resource_items(current_owner_id, updated_at desc);

create table public.resource_passports (
  id uuid primary key default gen_random_uuid(),
  resource_id uuid not null unique references public.resource_items(id),
  qr_version smallint not null default 1 check (qr_version = 1),
  public_token text not null unique default rtrim(translate(encode(gen_random_bytes(16), 'base64'), '+/', '-_'), '='),
  token_status public.passport_token_status not null default 'ACTIVE',
  replacement_passport_id uuid references public.resource_passports(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  retired_at timestamptz,
  constraint passports_token_format_check check (public_token ~ '^[A-Za-z0-9_-]{22}$'),
  constraint passports_retired_check check ((token_status = 'RETIRED') = (retired_at is not null)),
  constraint passports_replacement_check check (replacement_passport_id is null or replacement_passport_id <> id)
);

create table public.resource_photos (
  id uuid primary key default gen_random_uuid(),
  resource_id uuid not null references public.resource_items(id),
  storage_path text not null unique check (btrim(storage_path) <> ''),
  mime_type text not null check (mime_type in ('image/jpeg', 'image/png', 'image/webp')),
  width integer not null check (width between 1 and 8192),
  height integer not null check (height between 1 and 8192),
  byte_size integer not null check (byte_size between 1 and 10485760),
  sort_order integer not null check (sort_order >= 0),
  created_at timestamptz not null default now(),
  unique (resource_id, sort_order)
);

create table public.marketplace_listings (
  id uuid primary key default gen_random_uuid(),
  resource_id uuid not null references public.resource_items(id),
  seller_id uuid references public.profiles(id) on delete set null,
  allowed_actions public.transaction_type[] not null,
  published_quantity numeric(12,3) not null check (published_quantity > 0),
  unit_coin_price_buy bigint check (unit_coin_price_buy > 0),
  unit_coin_price_rent bigint check (unit_coin_price_rent > 0),
  default_duration_days integer check (default_duration_days between 1 and 365),
  terms text not null default '' check (char_length(terms) <= 2000),
  status public.listing_status not null default 'DRAFT',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  published_at timestamptz,
  closed_at timestamptz,
  constraint listings_actions_check check (
    cardinality(allowed_actions) > 0 and
    allowed_actions <@ array['BORROW', 'RENT', 'BUY', 'DONATE', 'EXCHANGE']::public.transaction_type[]
  ),
  constraint listings_buy_price_check check (not ('BUY' = any(allowed_actions)) or unit_coin_price_buy is not null),
  constraint listings_rent_price_check check (not ('RENT' = any(allowed_actions)) or unit_coin_price_rent is not null),
  constraint listings_duration_check check (
    not (allowed_actions && array['BORROW', 'RENT']::public.transaction_type[]) or default_duration_days is not null
  ),
  constraint listings_status_time_check check (
    (status = 'PUBLISHED') = (published_at is not null and closed_at is null) or
    (status in ('CLOSED', 'CANCELLED') and closed_at is not null) or
    (status in ('DRAFT', 'RESERVED') and closed_at is null)
  )
);
create unique index listings_one_open_per_resource_idx on public.marketplace_listings(resource_id)
where status in ('DRAFT', 'PUBLISHED', 'RESERVED');
create index listings_marketplace_idx on public.marketplace_listings(status, updated_at desc)
where status = 'PUBLISHED';

create table public.circular_programmes (
  id uuid primary key default gen_random_uuid(),
  partner_id uuid references public.profiles(id) on delete set null,
  name text not null check (char_length(btrim(name)) between 1 and 120),
  programme_type public.programme_type not null,
  accepted_categories text[] not null default '{}',
  accepted_materials text[] not null default '{}',
  accepted_conditions public.resource_condition[] not null,
  minimum_quantity numeric(12,3) check (minimum_quantity > 0),
  maximum_quantity numeric(12,3) check (maximum_quantity > 0),
  unit public.quantity_unit,
  remaining_capacity numeric(12,3) check (remaining_capacity >= 0),
  coin_direction public.coin_direction not null default 'FREE',
  unit_coin_amount bigint check (unit_coin_amount > 0),
  pickup_available boolean not null default false,
  address_text text not null default '',
  latitude numeric(9,6),
  longitude numeric(9,6),
  processing_method text not null default '',
  terms text not null default '' check (char_length(terms) <= 2000),
  active boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint programmes_conditions_check check (cardinality(accepted_conditions) > 0),
  constraint programmes_quantity_range_check check (
    minimum_quantity is null or maximum_quantity is null or maximum_quantity >= minimum_quantity
  ),
  constraint programmes_unit_check check (
    unit is not null or (minimum_quantity is null and maximum_quantity is null and remaining_capacity is null and unit_coin_amount is null)
  ),
  constraint programmes_coin_check check (
    (coin_direction = 'FREE' and unit_coin_amount is null) or
    (coin_direction <> 'FREE' and unit_coin_amount is not null)
  ),
  constraint programmes_direction_check check (
    (programme_type = 'REPAIR' and coin_direction in ('FREE', 'OWNER_PAYS_PARTNER')) or
    (programme_type in ('RECYCLE', 'BUY_BACK') and coin_direction in ('FREE', 'PARTNER_PAYS_OWNER'))
  ),
  constraint programmes_coordinates_check check ((latitude is null) = (longitude is null)),
  constraint programmes_active_fields_check check (
    not active or (
      partner_id is not null and btrim(address_text) <> '' and latitude is not null and
      btrim(processing_method) <> '' and btrim(terms) <> ''
    )
  )
);
create index programmes_active_idx on public.circular_programmes(programme_type, updated_at desc) where active;
create index programmes_partner_idx on public.circular_programmes(partner_id, active);

create table public.circular_transactions (
  id uuid primary key default gen_random_uuid(),
  listing_id uuid references public.marketplace_listings(id),
  programme_id uuid references public.circular_programmes(id),
  origin_event_id uuid not null references public.events(id),
  resource_id uuid not null references public.resource_items(id),
  counter_resource_id uuid references public.resource_items(id),
  requester_id uuid references public.profiles(id) on delete set null,
  sender_id uuid references public.profiles(id) on delete set null,
  receiver_id uuid references public.profiles(id) on delete set null,
  partner_id uuid references public.profiles(id) on delete set null,
  transaction_type public.transaction_type not null,
  status public.transaction_status not null default 'REQUESTED',
  quantity numeric(12,3) not null check (quantity > 0),
  unit public.quantity_unit not null,
  coin_payer_id uuid references public.profiles(id) on delete set null,
  coin_payee_id uuid references public.profiles(id) on delete set null,
  unit_coin_amount bigint not null default 0 check (unit_coin_amount >= 0),
  total_coin_amount bigint not null default 0 check (total_coin_amount >= 0),
  due_at timestamptz,
  reward_policy_version text not null default 'reevent-demo-reward-v1',
  request_reason text check (char_length(request_reason) <= 1000),
  terminal_reason text check (char_length(btrim(terminal_reason)) between 1 and 1000),
  created_at timestamptz not null default now(),
  approved_at timestamptz,
  in_transit_at timestamptz,
  active_at timestamptz,
  return_started_at timestamptz,
  completed_at timestamptz,
  updated_at timestamptz not null default now(),
  constraint transactions_source_check check ((listing_id is null) <> (programme_id is null)),
  constraint transactions_counter_check check (
    (transaction_type = 'EXCHANGE' and counter_resource_id is not null and counter_resource_id <> resource_id) or
    (transaction_type <> 'EXCHANGE' and counter_resource_id is null)
  ),
  constraint transactions_partner_check check (
    (programme_id is not null and partner_id is not null and transaction_type in ('REPAIR', 'RECYCLE', 'BUY_BACK')) or
    (listing_id is not null and partner_id is null and transaction_type in ('BORROW', 'RENT', 'BUY', 'DONATE', 'EXCHANGE'))
  ),
  constraint transactions_due_check check (
    (transaction_type in ('BORROW', 'RENT') and due_at is not null) or
    (transaction_type not in ('BORROW', 'RENT') and due_at is null)
  ),
  constraint transactions_coin_actors_check check (
    (coin_payer_id is null and coin_payee_id is null and unit_coin_amount = 0 and total_coin_amount = 0) or
    (coin_payer_id is not null and coin_payee_id is not null and coin_payer_id <> coin_payee_id and unit_coin_amount > 0 and total_coin_amount > 0)
  ),
  constraint transactions_status_time_check check (
    (status = 'REQUESTED' and approved_at is null and in_transit_at is null and active_at is null and return_started_at is null and completed_at is null and terminal_reason is null) or
    (status = 'APPROVED' and approved_at is not null and in_transit_at is null and active_at is null and return_started_at is null and completed_at is null and terminal_reason is null) or
    (status = 'IN_TRANSIT' and approved_at is not null and in_transit_at is not null and active_at is null and return_started_at is null and completed_at is null and terminal_reason is null) or
    (status = 'ACTIVE' and approved_at is not null and in_transit_at is not null and active_at is not null and return_started_at is null and completed_at is null and terminal_reason is null) or
    (status = 'RETURN_IN_PROGRESS' and approved_at is not null and in_transit_at is not null and active_at is not null and return_started_at is not null and completed_at is null and terminal_reason is null) or
    (status = 'COMPLETED' and approved_at is not null and in_transit_at is not null and completed_at is not null and terminal_reason is null) or
    (status in ('REJECTED', 'CANCELLED') and terminal_reason is not null and completed_at is null)
  )
);
create index transactions_actor_idx on public.circular_transactions(requester_id, sender_id, receiver_id, partner_id, updated_at desc);
create index transactions_resource_idx on public.circular_transactions(resource_id, status);
create unique index transactions_one_active_request_idx
on public.circular_transactions(resource_id, requester_id, transaction_type)
where status in ('REQUESTED', 'APPROVED', 'IN_TRANSIT', 'ACTIVE', 'RETURN_IN_PROGRESS');

create table public.passport_events (
  id uuid primary key default gen_random_uuid(),
  passport_id uuid not null references public.resource_passports(id),
  transaction_id uuid references public.circular_transactions(id),
  event_type public.passport_event_type not null,
  actor_id uuid references public.profiles(id) on delete set null,
  quantity numeric(12,3),
  unit public.quantity_unit,
  previous_condition public.resource_condition,
  new_condition public.resource_condition,
  public_summary text not null check (char_length(btrim(public_summary)) between 1 and 240),
  private_note text check (char_length(private_note) <= 1000),
  occurred_at timestamptz not null default now(),
  idempotency_key uuid not null,
  unique (passport_id, idempotency_key),
  constraint passport_events_quantity_check check (
    (quantity is null and unit is null) or
    (quantity > 0 and unit is not null and (unit = 'KG' or quantity = trunc(quantity)))
  ),
  constraint passport_events_condition_check check (
    (event_type = 'CONDITION_CHANGED' and previous_condition is not null and new_condition is not null) or
    (event_type <> 'CONDITION_CHANGED' and previous_condition is null and new_condition is null)
  )
);
create index passport_events_passport_time_idx on public.passport_events(passport_id, occurred_at, id);

create table public.transaction_allocations (
  id uuid primary key default gen_random_uuid(),
  transaction_id uuid not null references public.circular_transactions(id),
  resource_id uuid not null references public.resource_items(id),
  side public.allocation_side not null,
  quantity numeric(12,3) not null check (quantity > 0),
  unit public.quantity_unit not null,
  state public.allocation_state not null default 'RESERVED',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (transaction_id, side),
  constraint allocations_quantity_unit_check check (unit = 'KG' or quantity = trunc(quantity))
);
create index allocations_resource_active_idx on public.transaction_allocations(resource_id, state)
where state in ('RESERVED', 'IN_CUSTODY');

create table public.transaction_confirmations (
  transaction_id uuid not null references public.circular_transactions(id),
  actor_id uuid not null references public.profiles(id),
  resource_side public.allocation_side not null,
  confirmation_type public.confirmation_type not null,
  confirmed_at timestamptz not null default now(),
  idempotency_key uuid not null unique,
  primary key (transaction_id, actor_id, resource_side, confirmation_type)
);

create table public.recoin_wallets (
  id uuid primary key default gen_random_uuid(),
  profile_id uuid unique references public.profiles(id) on delete set null,
  available_balance bigint not null default 0 check (available_balance >= 0),
  held_balance bigint not null default 0 check (held_balance >= 0),
  version bigint not null default 0 check (version >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  closed_at timestamptz,
  constraint wallets_close_check check ((profile_id is null) = (closed_at is not null))
);

create table public.recoin_holds (
  id uuid primary key default gen_random_uuid(),
  transaction_id uuid not null unique references public.circular_transactions(id),
  payer_wallet_id uuid not null references public.recoin_wallets(id),
  payee_wallet_id uuid not null references public.recoin_wallets(id),
  amount bigint not null check (amount > 0),
  status public.hold_status not null default 'ACTIVE',
  created_at timestamptz not null default now(),
  settled_at timestamptz,
  released_at timestamptz,
  constraint holds_wallet_check check (payer_wallet_id <> payee_wallet_id),
  constraint holds_status_time_check check (
    (status = 'ACTIVE' and settled_at is null and released_at is null) or
    (status = 'SETTLED' and settled_at is not null and released_at is null) or
    (status = 'RELEASED' and settled_at is null and released_at is not null)
  )
);

create table public.recoin_ledger_entries (
  id uuid primary key default gen_random_uuid(),
  entry_group_id uuid not null,
  wallet_id uuid not null references public.recoin_wallets(id),
  transaction_id uuid references public.circular_transactions(id),
  hold_id uuid references public.recoin_holds(id),
  entry_type public.ledger_entry_type not null,
  amount bigint not null check (amount <> 0),
  reward_policy_version text,
  created_at timestamptz not null default now(),
  constraint ledger_hold_check check (
    (entry_type in ('SETTLEMENT_IN', 'SETTLEMENT_OUT') and hold_id is not null and transaction_id is not null) or
    (entry_type not in ('SETTLEMENT_IN', 'SETTLEMENT_OUT') and hold_id is null)
  ),
  constraint ledger_reward_check check (
    (entry_type = 'CIRCULAR_REWARD' and transaction_id is not null and reward_policy_version is not null) or
    entry_type <> 'CIRCULAR_REWARD'
  )
);
create unique index ledger_initial_grant_idx on public.recoin_ledger_entries(wallet_id)
where entry_type = 'INITIAL_GRANT';
create unique index ledger_transaction_reward_idx on public.recoin_ledger_entries(transaction_id, reward_policy_version)
where entry_type = 'CIRCULAR_REWARD';

create table public.impact_factors (
  id uuid primary key default gen_random_uuid(),
  factor_version text not null unique,
  transaction_type public.transaction_type not null,
  material_key text not null,
  input_unit public.quantity_unit not null,
  kg_co2e_per_unit numeric(16,8) not null check (kg_co2e_per_unit > 0),
  source_name text not null,
  source_url text not null,
  published_on date not null,
  accessed_on date not null,
  mapping_note text not null,
  scope_note text not null,
  active boolean not null default true
);

create table public.impact_records (
  id uuid primary key default gen_random_uuid(),
  transaction_id uuid not null unique references public.circular_transactions(id),
  event_id uuid not null references public.events(id),
  resource_id uuid not null references public.resource_items(id),
  transaction_type public.transaction_type not null,
  completed_quantity numeric(12,3) not null check (completed_quantity > 0),
  unit public.quantity_unit not null,
  material_diverted_kg numeric(12,3),
  emissions_avoided_kg numeric(16,8),
  factor_id uuid references public.impact_factors(id),
  recoins_transferred bigint not null default 0 check (recoins_transferred >= 0),
  recoins_rewarded bigint not null default 0 check (recoins_rewarded >= 0),
  calculated_at timestamptz not null default now(),
  constraint impact_quantity_unit_check check (unit = 'KG' or completed_quantity = trunc(completed_quantity)),
  constraint impact_factor_check check (
    (emissions_avoided_kg is null and factor_id is null) or
    (emissions_avoided_kg is not null and factor_id is not null and material_diverted_kg is not null)
  )
);

create table public.idempotency_records (
  actor_id uuid not null references public.profiles(id) on delete cascade,
  function_name text not null,
  idempotency_key uuid not null,
  request_hash text not null,
  response_json jsonb,
  created_at timestamptz not null default now(),
  expires_at timestamptz not null default (now() + interval '400 days'),
  primary key (actor_id, function_name, idempotency_key),
  constraint idempotency_expiry_check check (expires_at > created_at)
);

create or replace function public.current_profile_role()
returns public.user_role
language sql stable security definer set search_path = public
as $$
  select role from public.profiles where id = auth.uid() and role_frozen_at is not null
$$;

create or replace function public.has_role(expected_role public.user_role)
returns boolean
language sql stable security definer set search_path = public
as $$
  select auth.uid() is not null and public.current_profile_role() = expected_role
$$;

create or replace function public.is_event_owner(event_id uuid)
returns boolean
language sql stable security definer set search_path = public
as $$
  select exists(select 1 from public.events e where e.id = event_id and e.owner_id = auth.uid())
$$;

create or replace function public.is_transaction_actor(transaction_id uuid)
returns boolean
language sql stable security definer set search_path = public
as $$
  select exists(
    select 1 from public.circular_transactions t
    where t.id = transaction_id
      and auth.uid() in (t.requester_id, t.sender_id, t.receiver_id, t.partner_id)
  )
$$;

create or replace function public.owns_resource(target_resource_id uuid)
returns boolean
language sql stable security definer set search_path = public
as $$
  select exists(
    select 1 from public.resource_items resource
    where resource.id = target_resource_id and resource.current_owner_id = auth.uid()
  )
$$;

create or replace function public.can_read_resource(target_resource_id uuid)
returns boolean
language sql stable security definer set search_path = public
as $$
  select exists(
    select 1 from public.resource_items resource
    where resource.id = target_resource_id and (
      resource.current_owner_id = auth.uid()
      or public.is_event_owner(resource.origin_event_id)
      or exists (
        select 1 from public.marketplace_listings listing
        where listing.resource_id = resource.id and listing.status = 'PUBLISHED'
      )
      or exists (
        select 1 from public.circular_transactions transaction
        where (transaction.resource_id = resource.id or transaction.counter_resource_id = resource.id)
          and auth.uid() in (transaction.requester_id, transaction.sender_id, transaction.receiver_id, transaction.partner_id)
      )
    )
  )
$$;

create or replace function public.can_read_passport(target_passport_id uuid)
returns boolean
language sql stable security definer set search_path = public
as $$
  select exists(
    select 1 from public.resource_passports passport
    where passport.id = target_passport_id and public.can_read_resource(passport.resource_id)
  )
$$;

create or replace function public.available_resource_quantity(target_resource_id uuid)
returns numeric
language sql stable security definer set search_path = public
as $$
  select greatest(
    r.quantity - coalesce(sum(a.quantity) filter (where a.state in ('RESERVED', 'IN_CUSTODY')), 0),
    0
  )
  from public.resource_items r
  left join public.transaction_allocations a on a.resource_id = r.id
  where r.id = target_resource_id
  group by r.quantity
$$;

create or replace function public.handle_new_user()
returns trigger
language plpgsql security definer set search_path = public
as $$
begin
  insert into public.profiles (id, display_name)
  values (
    new.id,
    left(coalesce(nullif(btrim(new.raw_user_meta_data ->> 'display_name'), ''), split_part(coalesce(new.email, ''), '@', 1), 'ReEvent user'), 80)
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

create or replace function public.ensure_current_profile()
returns public.profiles
language plpgsql security definer set search_path = public
as $$
declare
  profile_row public.profiles;
  display_value text;
begin
  if auth.uid() is null then
    raise exception using errcode = '28000', message = 'AUTH_REQUIRED';
  end if;
  display_value := left(coalesce(
    nullif(btrim(auth.jwt() -> 'user_metadata' ->> 'display_name'), ''),
    nullif(split_part(coalesce(auth.jwt() ->> 'email', ''), '@', 1), ''),
    'ReEvent user'
  ), 80);
  insert into public.profiles (id, display_name)
  values (auth.uid(), display_value)
  on conflict (id) do nothing;
  select * into strict profile_row from public.profiles where id = auth.uid();
  return profile_row;
end;
$$;

create or replace function public.complete_profile_role(p_role public.user_role)
returns public.profiles
language plpgsql security definer set search_path = public
as $$
declare
  profile_row public.profiles;
  wallet_row public.recoin_wallets;
  grant_group uuid := gen_random_uuid();
begin
  if auth.uid() is null then
    raise exception using errcode = '28000', message = 'AUTH_REQUIRED';
  end if;

  perform public.ensure_current_profile();
  update public.profiles
  set role = p_role, role_frozen_at = now(), updated_at = now()
  where id = auth.uid() and role is null
  returning * into profile_row;

  if profile_row.id is null then
    select * into profile_row from public.profiles where id = auth.uid() and role = p_role;
    if profile_row.id is null then
      raise exception using errcode = '23514', message = 'ROLE_ALREADY_FROZEN';
    end if;
  end if;

  insert into public.recoin_wallets(profile_id, available_balance, version)
  values (auth.uid(), 1000, 1)
  on conflict (profile_id) do nothing
  returning * into wallet_row;

  if wallet_row.id is not null then
    insert into public.recoin_ledger_entries(entry_group_id, wallet_id, entry_type, amount)
    values (grant_group, wallet_row.id, 'INITIAL_GRANT', 1000);
  end if;
  return profile_row;
end;
$$;

-- Existing staged profiles with a frozen role receive the same one-time wallet grant.
with created_wallets as (
  insert into public.recoin_wallets(profile_id, available_balance, version)
  select id, 1000, 1 from public.profiles where role is not null
  on conflict (profile_id) do nothing
  returning id
)
insert into public.recoin_ledger_entries(entry_group_id, wallet_id, entry_type, amount)
select gen_random_uuid(), id, 'INITIAL_GRANT', 1000 from created_wallets;

create or replace function public.create_resource_passport()
returns trigger
language plpgsql security definer set search_path = public
as $$
declare
  passport_id uuid;
  command_key uuid := gen_random_uuid();
begin
  insert into public.resource_passports(resource_id)
  values (new.id)
  returning id into passport_id;
  insert into public.passport_events(
    passport_id, event_type, actor_id, quantity, unit, public_summary, occurred_at, idempotency_key
  ) values (
    passport_id, 'CREATED', new.created_by, new.quantity, new.unit,
    'Resource passport created', now(), command_key
  );
  return new;
end;
$$;

create or replace function public.reject_immutable_mutation()
returns trigger language plpgsql set search_path = public
as $$
begin
  raise exception using errcode = '55000', message = 'IMMUTABLE_RECORD';
end;
$$;

create or replace function public.reject_terminal_transaction_mutation()
returns trigger language plpgsql set search_path = public
as $$
begin
  if tg_op = 'DELETE' or old.status in ('COMPLETED', 'REJECTED', 'CANCELLED') then
    raise exception using errcode = '55000', message = 'TERMINAL_TRANSACTION_IMMUTABLE';
  end if;
  return new;
end;
$$;

-- Inventory owners may edit descriptive metadata, but custody, quantity, lineage and lifecycle
-- state are server-owned once the resource exists. Archival is the sole direct transition and is
-- accepted only when no open listing or transaction can be orphaned.
create or replace function public.enforce_resource_lifecycle_mutation()
returns trigger language plpgsql set search_path = public
as $$
declare
  lifecycle_changed boolean :=
    new.origin_event_id is distinct from old.origin_event_id or
    new.parent_resource_id is distinct from old.parent_resource_id or
    new.created_by is distinct from old.created_by or
    new.current_owner_id is distinct from old.current_owner_id or
    new.quantity is distinct from old.quantity or
    new.unit is distinct from old.unit or
    new.status is distinct from old.status or
    new.reuse_count is distinct from old.reuse_count or
    new.archived_at is distinct from old.archived_at;
begin
  if not lifecycle_changed or current_user in ('postgres', 'service_role', 'supabase_admin') then
    return new;
  end if;

  -- A replayed generic archive is harmless and keeps the original server timestamp.
  if old.status = 'ARCHIVED' and new.status = 'ARCHIVED'
    and new.origin_event_id is not distinct from old.origin_event_id
    and new.parent_resource_id is not distinct from old.parent_resource_id
    and new.created_by is not distinct from old.created_by
    and new.current_owner_id is not distinct from old.current_owner_id
    and new.quantity is not distinct from old.quantity
    and new.unit is not distinct from old.unit
    and new.reuse_count is not distinct from old.reuse_count then
    new.archived_at := old.archived_at;
    return new;
  end if;

  if new.status = 'ARCHIVED' and old.status in ('DRAFT', 'ACTIVE')
    and new.archived_at is not null
    and new.origin_event_id is not distinct from old.origin_event_id
    and new.parent_resource_id is not distinct from old.parent_resource_id
    and new.created_by is not distinct from old.created_by
    and new.current_owner_id is not distinct from old.current_owner_id
    and new.quantity is not distinct from old.quantity
    and new.unit is not distinct from old.unit
    and new.reuse_count is not distinct from old.reuse_count
    and not exists (
      select 1 from public.marketplace_listings listing
      where listing.resource_id = old.id and listing.status in ('DRAFT', 'PUBLISHED', 'RESERVED')
    )
    and not exists (
      select 1 from public.circular_transactions txn
      where (txn.resource_id = old.id or txn.counter_resource_id = old.id)
        and txn.status in ('REQUESTED', 'APPROVED', 'IN_TRANSIT', 'ACTIVE', 'RETURN_IN_PROGRESS')
    ) then
    return new;
  end if;

  raise exception using errcode = '42501', message = 'RESOURCE_LIFECYCLE_SERVER_ONLY';
end;
$$;

create or replace function public.append_resource_lifecycle_event()
returns trigger language plpgsql security definer set search_path = public
as $$
begin
  if old.condition is distinct from new.condition then
    insert into public.passport_events(
      passport_id, event_type, actor_id, previous_condition, new_condition,
      public_summary, occurred_at, idempotency_key
    )
    select passport.id, 'CONDITION_CHANGED', auth.uid(), old.condition, new.condition,
      'Resource condition updated', now(), gen_random_uuid()
    from public.resource_passports passport where passport.resource_id = new.id;
  end if;
  if old.status <> 'ARCHIVED' and new.status = 'ARCHIVED' then
    insert into public.passport_events(
      passport_id, event_type, actor_id, public_summary, occurred_at, idempotency_key
    )
    select passport.id, 'ARCHIVED', auth.uid(), 'Resource archived', now(), gen_random_uuid()
    from public.resource_passports passport where passport.resource_id = new.id;
  end if;
  return new;
end;
$$;

create trigger profiles_set_updated_at before update on public.profiles for each row execute function public.set_updated_at();
create trigger events_set_updated_at before update on public.events for each row execute function public.set_updated_at();
create trigger resources_set_updated_at before update on public.resource_items for each row execute function public.set_updated_at();
create trigger resources_enforce_lifecycle before update on public.resource_items for each row execute function public.enforce_resource_lifecycle_mutation();
create trigger passports_set_updated_at before update on public.resource_passports for each row execute function public.set_updated_at();
create trigger listings_set_updated_at before update on public.marketplace_listings for each row execute function public.set_updated_at();
create trigger programmes_set_updated_at before update on public.circular_programmes for each row execute function public.set_updated_at();
create trigger transactions_set_updated_at before update on public.circular_transactions for each row execute function public.set_updated_at();
create trigger allocations_set_updated_at before update on public.transaction_allocations for each row execute function public.set_updated_at();
create trigger wallets_set_updated_at before update on public.recoin_wallets for each row execute function public.set_updated_at();
create trigger resource_passport_after_insert after insert on public.resource_items for each row execute function public.create_resource_passport();
create trigger resource_lifecycle_event_after_update after update on public.resource_items for each row execute function public.append_resource_lifecycle_event();
create trigger passport_events_immutable before update or delete on public.passport_events for each row execute function public.reject_immutable_mutation();
create trigger ledger_entries_immutable before update or delete on public.recoin_ledger_entries for each row execute function public.reject_immutable_mutation();
create trigger impact_records_immutable before update or delete on public.impact_records for each row execute function public.reject_immutable_mutation();
create trigger terminal_transactions_immutable before update or delete on public.circular_transactions for each row execute function public.reject_terminal_transaction_mutation();

alter table public.profiles enable row level security;
alter table public.events enable row level security;
alter table public.resource_items enable row level security;
alter table public.resource_passports enable row level security;
alter table public.passport_events enable row level security;
alter table public.resource_photos enable row level security;
alter table public.marketplace_listings enable row level security;
alter table public.circular_programmes enable row level security;
alter table public.circular_transactions enable row level security;
alter table public.transaction_allocations enable row level security;
alter table public.transaction_confirmations enable row level security;
alter table public.recoin_wallets enable row level security;
alter table public.recoin_holds enable row level security;
alter table public.recoin_ledger_entries enable row level security;
alter table public.impact_factors enable row level security;
alter table public.impact_records enable row level security;
alter table public.idempotency_records enable row level security;

revoke all on all tables in schema public from anon, authenticated;
grant select on public.profiles, public.events, public.resource_items, public.resource_passports,
  public.passport_events, public.resource_photos, public.marketplace_listings, public.circular_programmes,
  public.circular_transactions, public.transaction_allocations, public.transaction_confirmations,
  public.recoin_wallets, public.recoin_holds, public.recoin_ledger_entries,
  public.impact_factors, public.impact_records to authenticated;
grant insert, update, delete on public.events to authenticated;
grant insert, delete on public.resource_items to authenticated;
grant update (
  title, description, category, material, condition, quantity, address_text, latitude, longitude,
  status, archived_at, updated_at
) on public.resource_items to authenticated;
grant insert, update, delete on public.resource_photos to authenticated;
grant insert, update, delete on public.marketplace_listings to authenticated;
grant insert, update, delete on public.circular_programmes to authenticated;
grant update (display_name, avatar_path, updated_at) on public.profiles to authenticated;

create policy profiles_self_read on public.profiles for select to authenticated using (id = auth.uid());
create policy profiles_self_update on public.profiles for update to authenticated using (id = auth.uid()) with check (id = auth.uid());

create policy events_owner_access on public.events for all to authenticated
using (owner_id = auth.uid())
with check (owner_id = auth.uid() and public.has_role('ORGANIZER'));

create policy resources_authorized_read on public.resource_items for select to authenticated using (
  current_owner_id = auth.uid() or public.is_event_owner(origin_event_id) or public.can_read_resource(id)
);
create policy resources_root_insert on public.resource_items for insert to authenticated with check (
  parent_resource_id is null and created_by = auth.uid() and current_owner_id = auth.uid()
  and public.has_role('ORGANIZER') and public.is_event_owner(origin_event_id)
);
create policy resources_owner_update on public.resource_items for update to authenticated
using (current_owner_id = auth.uid())
with check (current_owner_id = auth.uid());
create policy resources_owner_delete on public.resource_items for delete to authenticated
using (current_owner_id = auth.uid() and status = 'DRAFT');

create policy passports_authorized_read on public.resource_passports for select to authenticated using (
  public.can_read_resource(resource_id)
);

create policy passport_events_authorized_read on public.passport_events for select to authenticated using (
  public.can_read_passport(passport_id)
);

create policy resource_photos_authorized_read on public.resource_photos for select to authenticated using (
  public.can_read_resource(resource_id)
);
create policy resource_photos_owner_write on public.resource_photos for all to authenticated
using (public.owns_resource(resource_id))
with check (public.owns_resource(resource_id));

create policy listings_marketplace_read on public.marketplace_listings for select to authenticated using (
  status = 'PUBLISHED' or seller_id = auth.uid()
);
create policy listings_owner_write on public.marketplace_listings for all to authenticated
using (seller_id = auth.uid())
with check (
  seller_id = auth.uid()
  and public.owns_resource(resource_id)
);

create policy programmes_read on public.circular_programmes for select to authenticated using (active or partner_id = auth.uid());
create policy programmes_partner_write on public.circular_programmes for all to authenticated
using (partner_id = auth.uid())
with check (partner_id = auth.uid() and public.has_role('PARTNER'));

create policy transactions_actor_read on public.circular_transactions for select to authenticated using (
  auth.uid() in (requester_id, sender_id, receiver_id, partner_id)
);
create policy allocations_actor_read on public.transaction_allocations for select to authenticated using (
  public.is_transaction_actor(transaction_id)
);
create policy confirmations_actor_read on public.transaction_confirmations for select to authenticated using (
  public.is_transaction_actor(transaction_id)
);
create policy wallets_self_read on public.recoin_wallets for select to authenticated using (profile_id = auth.uid());
create policy holds_actor_read on public.recoin_holds for select to authenticated using (public.is_transaction_actor(transaction_id));
create policy ledger_self_read on public.recoin_ledger_entries for select to authenticated using (
  exists(select 1 from public.recoin_wallets wallet where wallet.id = wallet_id and wallet.profile_id = auth.uid())
);
create policy impact_factors_read on public.impact_factors for select to authenticated using (active);
create policy impact_actor_read on public.impact_records for select to authenticated using (
  public.is_event_owner(event_id) or public.is_transaction_actor(transaction_id)
);

revoke all on function public.current_profile_role() from public;
revoke all on function public.has_role(public.user_role) from public;
revoke all on function public.is_event_owner(uuid) from public;
revoke all on function public.is_transaction_actor(uuid) from public;
revoke all on function public.owns_resource(uuid) from public;
revoke all on function public.can_read_resource(uuid) from public;
revoke all on function public.can_read_passport(uuid) from public;
revoke all on function public.available_resource_quantity(uuid) from public;
revoke all on function public.ensure_current_profile() from public;
revoke all on function public.complete_profile_role(public.user_role) from public;
grant execute on function public.current_profile_role() to authenticated;
grant execute on function public.has_role(public.user_role) to authenticated;
grant execute on function public.is_event_owner(uuid) to authenticated;
grant execute on function public.is_transaction_actor(uuid) to authenticated;
grant execute on function public.owns_resource(uuid) to authenticated;
grant execute on function public.can_read_resource(uuid) to authenticated;
grant execute on function public.can_read_passport(uuid) to authenticated;
grant execute on function public.available_resource_quantity(uuid) to authenticated;
grant execute on function public.ensure_current_profile() to authenticated;
grant execute on function public.complete_profile_role(public.user_role) to authenticated;

insert into public.impact_factors(
  factor_version, transaction_type, material_key, input_unit, kg_co2e_per_unit,
  source_name, source_url, published_on, accessed_on, mapping_note, scope_note
) values (
  'desnz-2025-average-plastics-v1', 'RECYCLE', 'average_plastics', 'KG', 1.59710826,
  'UK Government GHG Conversion Factors for Company Reporting 2025',
  'https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2025',
  date '2025-06-10', date '2026-08-09',
  'Plastic and acrylic are approximated using the disclosed average-plastics factor.',
  'Estimate covers the documented material-diversion boundary only; transport and processing are excluded.'
);
