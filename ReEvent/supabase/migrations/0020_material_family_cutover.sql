-- Strict pre-release cutover from free-text materials to the canonical Material Compass catalogue.

create type public.material_family as enum (
  'WOOD',
  'TEXTILES',
  'METAL',
  'PLASTIC',
  'PAPER_CARD',
  'GLASS',
  'CERAMIC',
  'ELECTRICAL_ELECTRONICS',
  'ORGANIC',
  'RUBBER',
  'MIXED_OTHER'
);

create or replace function public.material_family_from_legacy(p_value text)
returns public.material_family
language sql
immutable
set search_path = public
as $$
  select case lower(regexp_replace(btrim(coalesce(p_value, '')), '[-_]+', ' ', 'g'))
    when 'wood' then 'WOOD'::public.material_family
    when 'wooden' then 'WOOD'::public.material_family
    when 'timber' then 'WOOD'::public.material_family
    when 'plywood' then 'WOOD'::public.material_family
    when 'bamboo' then 'WOOD'::public.material_family
    when 'cork' then 'WOOD'::public.material_family
    when 'mdf' then 'WOOD'::public.material_family
    when 'textile' then 'TEXTILES'::public.material_family
    when 'textiles' then 'TEXTILES'::public.material_family
    when 'fabric' then 'TEXTILES'::public.material_family
    when 'canvas' then 'TEXTILES'::public.material_family
    when 'cotton' then 'TEXTILES'::public.material_family
    when 'wool' then 'TEXTILES'::public.material_family
    when 'linen' then 'TEXTILES'::public.material_family
    when 'polyester' then 'TEXTILES'::public.material_family
    when 'metal' then 'METAL'::public.material_family
    when 'steel' then 'METAL'::public.material_family
    when 'aluminium' then 'METAL'::public.material_family
    when 'aluminum' then 'METAL'::public.material_family
    when 'iron' then 'METAL'::public.material_family
    when 'copper' then 'METAL'::public.material_family
    when 'brass' then 'METAL'::public.material_family
    when 'plastic' then 'PLASTIC'::public.material_family
    when 'plastics' then 'PLASTIC'::public.material_family
    when 'acrylic' then 'PLASTIC'::public.material_family
    when 'pet' then 'PLASTIC'::public.material_family
    when 'pvc' then 'PLASTIC'::public.material_family
    when 'polypropylene' then 'PLASTIC'::public.material_family
    when 'pp' then 'PLASTIC'::public.material_family
    when 'polystyrene' then 'PLASTIC'::public.material_family
    when 'paper' then 'PAPER_CARD'::public.material_family
    when 'card' then 'PAPER_CARD'::public.material_family
    when 'cardboard' then 'PAPER_CARD'::public.material_family
    when 'paper and card' then 'PAPER_CARD'::public.material_family
    when 'paper & card' then 'PAPER_CARD'::public.material_family
    when 'glass' then 'GLASS'::public.material_family
    when 'ceramic' then 'CERAMIC'::public.material_family
    when 'ceramics' then 'CERAMIC'::public.material_family
    when 'porcelain' then 'CERAMIC'::public.material_family
    when 'stoneware' then 'CERAMIC'::public.material_family
    when 'electrical' then 'ELECTRICAL_ELECTRONICS'::public.material_family
    when 'electronics' then 'ELECTRICAL_ELECTRONICS'::public.material_family
    when 'electronic' then 'ELECTRICAL_ELECTRONICS'::public.material_family
    when 'e waste' then 'ELECTRICAL_ELECTRONICS'::public.material_family
    when 'ewaste' then 'ELECTRICAL_ELECTRONICS'::public.material_family
    when 'cable' then 'ELECTRICAL_ELECTRONICS'::public.material_family
    when 'cables' then 'ELECTRICAL_ELECTRONICS'::public.material_family
    when 'lighting' then 'ELECTRICAL_ELECTRONICS'::public.material_family
    when 'organic' then 'ORGANIC'::public.material_family
    when 'food' then 'ORGANIC'::public.material_family
    when 'compostable' then 'ORGANIC'::public.material_family
    when 'plant' then 'ORGANIC'::public.material_family
    when 'plants' then 'ORGANIC'::public.material_family
    when 'foliage' then 'ORGANIC'::public.material_family
    when 'rubber' then 'RUBBER'::public.material_family
    when 'latex' then 'RUBBER'::public.material_family
    when 'silicone' then 'RUBBER'::public.material_family
    else 'MIXED_OTHER'::public.material_family
  end
$$;

create or replace function public.material_family_display(
  p_family public.material_family,
  p_detail text
)
returns text
language sql
immutable
set search_path = public
as $$
  select coalesce(
    nullif(btrim(p_detail), ''),
    case p_family
      when 'WOOD' then 'Wood'
      when 'TEXTILES' then 'Textiles'
      when 'METAL' then 'Metal'
      when 'PLASTIC' then 'Plastic'
      when 'PAPER_CARD' then 'Paper & Card'
      when 'GLASS' then 'Glass'
      when 'CERAMIC' then 'Ceramic'
      when 'ELECTRICAL_ELECTRONICS' then 'Electrical & Electronics'
      when 'ORGANIC' then 'Organic'
      when 'RUBBER' then 'Rubber'
      when 'MIXED_OTHER' then 'Mixed / Other'
    end
  )
$$;

alter table public.resource_items
  add column material_family public.material_family,
  add column material_detail text;

update public.resource_items
set
  material_family = public.material_family_from_legacy(material),
  material_detail = case
    when public.material_family_from_legacy(material) = 'MIXED_OTHER' then
      left(coalesce(nullif(btrim(material), ''), 'Unspecified material'), 120)
    when lower(btrim(material)) in (
      'wood','wooden','timber','textile','textiles','fabric','metal','plastic','plastics',
      'paper and card','paper & card','glass','ceramic','ceramics','electrical','electronics',
      'electronic','organic','rubber'
    ) then null
    else left(nullif(btrim(material), ''), 120)
  end;

alter table public.resource_items
  alter column material_family set not null,
  add constraint resource_material_detail_length check (
    material_detail is null or char_length(material_detail) <= 120
  ),
  add constraint resource_mixed_material_detail_required check (
    material_family <> 'MIXED_OTHER' or nullif(btrim(material_detail), '') is not null
  );

alter table public.circular_programmes
  add column accepted_material_families public.material_family[] not null default '{}';

update public.circular_programmes programme
set accepted_material_families = coalesce(
  (
    select array_agg(distinct public.material_family_from_legacy(value) order by public.material_family_from_legacy(value))
    from unnest(programme.accepted_materials) as value
  ),
  '{}'::public.material_family[]
);


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
  if cardinality(programme_row.accepted_material_families) > 0
    and not (resource_row.material_family = any(programme_row.accepted_material_families)) then
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

create or replace function public.complete_transaction_effects(
  p_transaction_id uuid,
  p_effect_actor uuid,
  p_effect_key uuid
)
returns public.circular_transactions
language plpgsql security definer set search_path = public
as $$
declare
  transaction_row public.circular_transactions;
  resource_row public.resource_items;
  counter_row public.resource_items;
  child_row public.resource_items;
  allocation_row public.transaction_allocations;
  hold_row public.recoin_holds;
  payer_wallet public.recoin_wallets;
  payee_wallet public.recoin_wallets;
  reward_wallet public.recoin_wallets;
  primary_passport uuid;
  counter_passport uuid;
  child_passport uuid;
  settlement_group uuid;
  reward_group uuid;
  reward_amount bigint := 0;
  reward_actor uuid;
  factor_row public.impact_factors;
  diverted numeric(12,3);
  avoided numeric(16,8);
begin
  select * into strict transaction_row
  from public.circular_transactions where id = p_transaction_id for update;

  if transaction_row.transaction_type in ('BORROW', 'RENT', 'REPAIR') then
    if transaction_row.status <> 'RETURN_IN_PROGRESS' then
      raise exception using errcode = '55000', message = 'RETURN_NOT_IN_PROGRESS';
    end if;
  elsif transaction_row.status <> 'IN_TRANSIT' then
    raise exception using errcode = '55000', message = 'HANDOVER_NOT_IN_TRANSIT';
  end if;

  perform id from public.resource_items
  where id in (transaction_row.resource_id, transaction_row.counter_resource_id)
  order by id for update;
  select * into strict resource_row from public.resource_items where id = transaction_row.resource_id;
  if transaction_row.counter_resource_id is not null then
    select * into strict counter_row from public.resource_items where id = transaction_row.counter_resource_id;
  end if;
  select id into strict primary_passport from public.resource_passports where resource_id = resource_row.id;

  if transaction_row.transaction_type in ('BORROW', 'RENT', 'REPAIR') then
    update public.transaction_allocations
    set state = 'RELEASED'
    where transaction_id = transaction_row.id and side = 'PRIMARY' and state = 'IN_CUSTODY';
    if not found then
      raise exception using errcode = '55000', message = 'CUSTODY_ALLOCATION_MISSING';
    end if;
    update public.resource_items
    set status = 'ACTIVE',
        reuse_count = reuse_count + case when transaction_row.transaction_type in ('BORROW', 'RENT') then 1 else 0 end
    where id = resource_row.id;
    insert into public.passport_events(
      passport_id, transaction_id, event_type, actor_id, quantity, unit, public_summary, idempotency_key
    ) values (
      primary_passport, transaction_row.id,
      case when transaction_row.transaction_type = 'REPAIR' then 'REPAIRED'::public.passport_event_type else 'RETURNED'::public.passport_event_type end,
      p_effect_actor, transaction_row.quantity, transaction_row.unit,
      case when transaction_row.transaction_type = 'REPAIR' then 'Repair completed and custody returned' else 'Resource quantity returned to its owner' end,
      p_effect_key
    );
    if transaction_row.listing_id is not null then
      update public.marketplace_listings
      set status = 'PUBLISHED'
      where id = transaction_row.listing_id and status = 'RESERVED';
    end if;

  elsif transaction_row.transaction_type in ('BUY', 'DONATE', 'BUY_BACK') then
    select * into strict allocation_row from public.transaction_allocations
    where transaction_id = transaction_row.id and side = 'PRIMARY' for update;
    if transaction_row.quantity = resource_row.quantity then
      update public.resource_items
      set current_owner_id = transaction_row.receiver_id, status = 'ACTIVE'
      where id = resource_row.id;
      update public.transaction_allocations set state = 'TRANSFERRED' where id = allocation_row.id;
      insert into public.passport_events(
        passport_id, transaction_id, event_type, actor_id, quantity, unit, public_summary, idempotency_key
      ) values (
        primary_passport, transaction_row.id, 'OWNERSHIP_TRANSFERRED', p_effect_actor,
        transaction_row.quantity, transaction_row.unit, 'Resource ownership transferred', p_effect_key
      );
    else
      update public.resource_items set quantity = quantity - transaction_row.quantity where id = resource_row.id;
      insert into public.resource_items(
        origin_event_id, parent_resource_id, created_by, current_owner_id, title, description,
        category, material_family, material_detail, condition, quantity, unit, status, address_text, latitude, longitude
      ) values (
        resource_row.origin_event_id, resource_row.id, resource_row.created_by, transaction_row.receiver_id,
        resource_row.title, resource_row.description, resource_row.category, resource_row.material_family, resource_row.material_detail,
        resource_row.condition, transaction_row.quantity, resource_row.unit, 'ACTIVE',
        resource_row.address_text, resource_row.latitude, resource_row.longitude
      ) returning * into child_row;
      select id into strict child_passport from public.resource_passports where resource_id = child_row.id;
      update public.transaction_allocations set state = 'TRANSFERRED' where id = allocation_row.id;
      insert into public.passport_events(
        passport_id, transaction_id, event_type, actor_id, quantity, unit, public_summary, idempotency_key
      ) values
        (primary_passport, transaction_row.id, 'SPLIT_TO', p_effect_actor, transaction_row.quantity,
         transaction_row.unit, 'Quantity split into a transferred child passport', gen_random_uuid()),
        (child_passport, transaction_row.id, 'SPLIT_FROM', p_effect_actor, transaction_row.quantity,
         transaction_row.unit, 'Resource lot split from its origin passport', gen_random_uuid()),
        (child_passport, transaction_row.id, 'OWNERSHIP_TRANSFERRED', p_effect_actor, transaction_row.quantity,
         transaction_row.unit, 'Split resource ownership transferred', gen_random_uuid());
    end if;
    update public.marketplace_listings
    set status = 'CLOSED', closed_at = now()
    where id = transaction_row.listing_id;

  elsif transaction_row.transaction_type = 'RECYCLE' then
    select * into strict allocation_row from public.transaction_allocations
    where transaction_id = transaction_row.id and side = 'PRIMARY' for update;
    if transaction_row.quantity = resource_row.quantity then
      update public.resource_items set status = 'RECOVERED' where id = resource_row.id;
      update public.transaction_allocations set state = 'CONSUMED' where id = allocation_row.id;
      insert into public.passport_events(
        passport_id, transaction_id, event_type, actor_id, quantity, unit, public_summary, idempotency_key
      ) values (
        primary_passport, transaction_row.id, 'RECYCLED', p_effect_actor,
        transaction_row.quantity, transaction_row.unit, 'Resource quantity confirmed recycled', p_effect_key
      );
    else
      update public.resource_items set quantity = quantity - transaction_row.quantity where id = resource_row.id;
      insert into public.resource_items(
        origin_event_id, parent_resource_id, created_by, current_owner_id, title, description,
        category, material_family, material_detail, condition, quantity, unit, status, address_text, latitude, longitude
      ) values (
        resource_row.origin_event_id, resource_row.id, resource_row.created_by, resource_row.current_owner_id,
        resource_row.title, resource_row.description, resource_row.category, resource_row.material_family, resource_row.material_detail,
        resource_row.condition, transaction_row.quantity, resource_row.unit, 'RECOVERED',
        resource_row.address_text, resource_row.latitude, resource_row.longitude
      ) returning * into child_row;
      select id into strict child_passport from public.resource_passports where resource_id = child_row.id;
      update public.transaction_allocations set state = 'CONSUMED' where id = allocation_row.id;
      insert into public.passport_events(
        passport_id, transaction_id, event_type, actor_id, quantity, unit, public_summary, idempotency_key
      ) values
        (primary_passport, transaction_row.id, 'SPLIT_TO', p_effect_actor, transaction_row.quantity,
         transaction_row.unit, 'Quantity split into a recovered child passport', gen_random_uuid()),
        (child_passport, transaction_row.id, 'SPLIT_FROM', p_effect_actor, transaction_row.quantity,
         transaction_row.unit, 'Recovered lot split from its origin passport', gen_random_uuid()),
        (child_passport, transaction_row.id, 'RECYCLED', p_effect_actor, transaction_row.quantity,
         transaction_row.unit, 'Split resource quantity confirmed recycled', gen_random_uuid());
    end if;

  elsif transaction_row.transaction_type = 'EXCHANGE' then
    if resource_row.quantity <> transaction_row.quantity or counter_row.id is null then
      raise exception using errcode = '55000', message = 'EXCHANGE_LOTS_CHANGED';
    end if;
    select id into strict counter_passport from public.resource_passports where resource_id = counter_row.id;
    update public.resource_items
    set current_owner_id = case
      when id = resource_row.id then transaction_row.receiver_id
      when id = counter_row.id then transaction_row.sender_id
    end
    where id in (resource_row.id, counter_row.id);
    update public.transaction_allocations set state = 'TRANSFERRED' where transaction_id = transaction_row.id;
    insert into public.passport_events(
      passport_id, transaction_id, event_type, actor_id, quantity, unit, public_summary, idempotency_key
    ) values
      (primary_passport, transaction_row.id, 'OWNERSHIP_TRANSFERRED', p_effect_actor,
       resource_row.quantity, resource_row.unit, 'Resource ownership exchanged', gen_random_uuid()),
      (counter_passport, transaction_row.id, 'OWNERSHIP_TRANSFERRED', p_effect_actor,
       counter_row.quantity, counter_row.unit, 'Resource ownership exchanged', gen_random_uuid());
    update public.marketplace_listings
    set status = 'CLOSED', closed_at = now()
    where id = transaction_row.listing_id;
  else
    raise exception using errcode = '22023', message = 'UNSUPPORTED_COMPLETION_TYPE';
  end if;

  select * into hold_row from public.recoin_holds
  where transaction_id = transaction_row.id and status = 'ACTIVE' for update;
  if hold_row.id is not null then
    perform id from public.recoin_wallets
    where id in (hold_row.payer_wallet_id, hold_row.payee_wallet_id)
    order by id for update;
    select * into strict payer_wallet from public.recoin_wallets where id = hold_row.payer_wallet_id;
    select * into strict payee_wallet from public.recoin_wallets where id = hold_row.payee_wallet_id;
    if payer_wallet.held_balance < hold_row.amount then
      raise exception using errcode = '22003', message = 'HELD_BALANCE_CORRUPT';
    end if;
    update public.recoin_wallets
    set held_balance = held_balance - hold_row.amount, version = version + 1
    where id = payer_wallet.id;
    update public.recoin_wallets
    set available_balance = available_balance + hold_row.amount, version = version + 1
    where id = payee_wallet.id;
    update public.recoin_holds set status = 'SETTLED', settled_at = now() where id = hold_row.id;
    settlement_group := gen_random_uuid();
    insert into public.recoin_ledger_entries(
      entry_group_id, wallet_id, transaction_id, hold_id, entry_type, amount
    ) values
      (settlement_group, payer_wallet.id, transaction_row.id, hold_row.id, 'SETTLEMENT_OUT', -hold_row.amount),
      (settlement_group, payee_wallet.id, transaction_row.id, hold_row.id, 'SETTLEMENT_IN', hold_row.amount);
  end if;

  case transaction_row.transaction_type
    when 'BORROW' then reward_amount := 5; reward_actor := transaction_row.receiver_id;
    when 'RENT' then reward_amount := 5; reward_actor := transaction_row.receiver_id;
    when 'DONATE' then reward_amount := 10; reward_actor := transaction_row.sender_id;
    when 'REPAIR' then reward_amount := 15; reward_actor := transaction_row.sender_id;
    when 'RECYCLE' then reward_amount := 10; reward_actor := transaction_row.sender_id;
    else reward_amount := 0;
  end case;
  if reward_amount > 0 then
    select * into strict reward_wallet from public.recoin_wallets where profile_id = reward_actor for update;
    update public.recoin_wallets
    set available_balance = available_balance + reward_amount, version = version + 1
    where id = reward_wallet.id;
    reward_group := gen_random_uuid();
    insert into public.recoin_ledger_entries(
      entry_group_id, wallet_id, transaction_id, entry_type, amount, reward_policy_version
    ) values (
      reward_group, reward_wallet.id, transaction_row.id, 'CIRCULAR_REWARD', reward_amount,
      transaction_row.reward_policy_version
    );
  end if;

  if transaction_row.transaction_type = 'RECYCLE'
    and transaction_row.unit = 'KG'
    and resource_row.material_family = 'PLASTIC' then
    select * into strict factor_row from public.impact_factors
    where factor_version = 'desnz-2025-average-plastics-v1';
    diverted := transaction_row.quantity;
    avoided := round(transaction_row.quantity * factor_row.kg_co2e_per_unit, 8);
  end if;
  insert into public.impact_records(
    transaction_id, event_id, resource_id, transaction_type, completed_quantity, unit,
    material_diverted_kg, emissions_avoided_kg, factor_id, recoins_transferred, recoins_rewarded
  ) values (
    transaction_row.id, transaction_row.origin_event_id, transaction_row.resource_id,
    transaction_row.transaction_type, transaction_row.quantity, transaction_row.unit,
    diverted, avoided, factor_row.id, transaction_row.total_coin_amount, reward_amount
  );

  update public.circular_transactions
  set status = 'COMPLETED', completed_at = now()
  where id = transaction_row.id
  returning * into transaction_row;
  return transaction_row;
end;
$$;

create or replace function public.resolve_public_passport(p_token text)
returns table (
  title text,
  category text,
  material text,
  condition public.resource_condition,
  resource_status public.resource_status,
  latest_summary text,
  latest_occurred_at timestamptz
)
language sql
stable
security definer
set search_path = public
as $$
  select
    resource.title,
    resource.category,
    public.material_family_display(resource.material_family, resource.material_detail),
    resource.condition,
    resource.status as resource_status,
    latest_event.public_summary as latest_summary,
    latest_event.occurred_at as latest_occurred_at
  from public.resource_passports as passport
  join public.resource_items as resource on resource.id = passport.resource_id
  left join lateral (
    select event.public_summary, event.occurred_at
    from public.passport_events as event
    where event.passport_id = passport.id
    order by event.occurred_at desc, event.id desc
    limit 1
  ) as latest_event on true
  where p_token ~ '^[A-Za-z0-9_-]{22}$'
    and passport.public_token = p_token
    and passport.token_status = 'ACTIVE'
    and resource.status in ('ACTIVE', 'RECOVERY_IN_PROGRESS', 'RECOVERED');
$$;


drop function public.find_partner_programmes(
  uuid, double precision, double precision, text, public.programme_type[], double precision, boolean, integer, integer
);

create or replace function public.find_partner_programmes(
  p_resource_id uuid default null,
  p_origin_latitude double precision default null,
  p_origin_longitude double precision default null,
  p_material_family public.material_family default null,
  p_programme_types public.programme_type[] default null,
  p_max_distance_km double precision default null,
  p_pickup_only boolean default false,
  p_limit integer default 100,
  p_offset integer default 0
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  resource_row public.resource_items;
  event_row public.events;
  origin_lat double precision;
  origin_lon double precision;
  origin_address text;
  origin_source text := 'NONE';
  effective_material_family public.material_family := p_material_family;
  candidate_rows jsonb;
  exclusion_rows jsonb;
  eligible_count integer;
begin
  if (p_origin_latitude is null) <> (p_origin_longitude is null) then
    raise exception using errcode = '22023', message = 'ORIGIN_COORDINATES_INCOMPLETE';
  end if;
  if p_origin_latitude is not null and (
    p_origin_latitude not between -90 and 90 or p_origin_longitude not between -180 and 180
  ) then
    raise exception using errcode = '22023', message = 'ORIGIN_COORDINATES_INVALID';
  end if;
  if p_max_distance_km is not null and (p_max_distance_km <= 0 or p_max_distance_km > 500) then
    raise exception using errcode = '22023', message = 'DISTANCE_FILTER_INVALID';
  end if;
  if p_limit not between 1 and 100 or p_offset < 0 then
    raise exception using errcode = '22023', message = 'PAGINATION_INVALID';
  end if;

  if p_resource_id is not null then
    select * into strict resource_row from public.resource_items where id = p_resource_id;
    if resource_row.current_owner_id <> actor_id then
      raise exception using errcode = '42501', message = 'RESOURCE_OWNER_REQUIRED';
    end if;
    effective_material_family := resource_row.material_family;
  end if;

  if p_origin_latitude is not null then
    origin_lat := p_origin_latitude;
    origin_lon := p_origin_longitude;
    origin_source := 'DEVICE';
  elsif p_resource_id is not null and resource_row.latitude is not null then
    origin_lat := resource_row.latitude;
    origin_lon := resource_row.longitude;
    origin_address := resource_row.address_text;
    origin_source := 'RESOURCE';
  elsif p_resource_id is not null then
    select * into strict event_row from public.events where id = resource_row.origin_event_id;
    if event_row.latitude is not null then
      origin_lat := event_row.latitude;
      origin_lon := event_row.longitude;
      origin_address := event_row.address_text;
      origin_source := 'EVENT';
    end if;
  end if;

  with distanced as (
    select
      programme.*,
      case when origin_lat is null then null else
        6371.0088 * 2 * asin(sqrt(least(1.0,
          power(sin(radians(programme.latitude::double precision - origin_lat) / 2), 2) +
          cos(radians(origin_lat)) * cos(radians(programme.latitude::double precision)) *
          power(sin(radians(programme.longitude::double precision - origin_lon) / 2), 2)
        )))
      end as distance_km
    from public.circular_programmes programme
    where programme.active
  ), evaluated as (
    select
      distanced.*,
      case
        when distanced.latitude is null or distanced.longitude is null then 'PROGRAMME_LOCATION_MISSING'
        when p_programme_types is not null and cardinality(p_programme_types) > 0 and
          not distanced.programme_type = any(p_programme_types) then 'TYPE_FILTERED'
        when p_pickup_only and not distanced.pickup_available then 'PICKUP_UNAVAILABLE'
        when effective_material_family is not null and cardinality(distanced.accepted_material_families) > 0 and not (
          effective_material_family = any(distanced.accepted_material_families)
        ) then 'MATERIAL_NOT_ACCEPTED'
        when p_max_distance_km is not null and (distance_km is null or distance_km > p_max_distance_km) then 'OUTSIDE_DISTANCE'
        when p_resource_id is null then null
        when resource_row.status <> 'ACTIVE' then 'RESOURCE_NOT_ACTIVE'
        when distanced.partner_id = actor_id then 'SELF_DEALING_FORBIDDEN'
        when distanced.unit is not null and distanced.unit <> resource_row.unit then 'UNIT_NOT_ACCEPTED'
        when distanced.minimum_quantity is not null and resource_row.quantity < distanced.minimum_quantity then 'BELOW_MINIMUM_QUANTITY'
        when distanced.maximum_quantity is not null and resource_row.quantity > distanced.maximum_quantity then 'ABOVE_MAXIMUM_QUANTITY'
        when distanced.remaining_capacity is not null and resource_row.quantity > distanced.remaining_capacity then 'CAPACITY_UNAVAILABLE'
        when cardinality(distanced.accepted_categories) > 0 and not (
          lower(resource_row.category) = any(select lower(value) from unnest(distanced.accepted_categories) value)
        ) then 'CATEGORY_NOT_ACCEPTED'
        when cardinality(distanced.accepted_material_families) > 0 and not (
          resource_row.material_family = any(distanced.accepted_material_families)
        ) then 'MATERIAL_NOT_ACCEPTED'
        when not resource_row.condition = any(distanced.accepted_conditions) then 'CONDITION_NOT_ACCEPTED'
        else null
      end as exclusion_code,
      case when p_resource_id is null then null else
        (case when cardinality(distanced.accepted_material_families) = 0 then 15 else 30 end) +
        (case when cardinality(distanced.accepted_categories) = 0 then 10 else 20 end) +
        (case when distance_km is null or distance_km > 50 then 0 when distance_km <= 5 then 25 when distance_km <= 15 then 18 else 8 end) +
        (case when distanced.remaining_capacity is null or distanced.remaining_capacity >= resource_row.quantity * 2 then 15 else 8 end) +
        (case when p_pickup_only and distanced.pickup_available then 10 else 0 end)
      end as match_score
    from distanced
  )
  select
    coalesce((
      select jsonb_agg(jsonb_build_object(
        'id', page.id,
        'partner_id', page.partner_id,
        'name', page.name,
        'programme_type', page.programme_type,
        'accepted_categories', page.accepted_categories,
        'accepted_material_families', page.accepted_material_families,
        'accepted_conditions', page.accepted_conditions,
        'minimum_quantity', page.minimum_quantity,
        'maximum_quantity', page.maximum_quantity,
        'unit', page.unit,
        'remaining_capacity', page.remaining_capacity,
        'coin_direction', page.coin_direction,
        'unit_coin_amount', page.unit_coin_amount,
        'pickup_available', page.pickup_available,
        'address_text', page.address_text,
        'latitude', page.latitude,
        'longitude', page.longitude,
        'processing_method', page.processing_method,
        'terms', page.terms,
        'distance_km', page.distance_km,
        'score', page.match_score,
        'reasons', case when p_resource_id is null then jsonb_build_array('Active partner programme') else jsonb_build_array(
          case when cardinality(page.accepted_material_families) = 0 then 'Accepts all materials' else 'Accepts ' || public.material_family_display(resource_row.material_family, resource_row.material_detail) end,
          case when cardinality(page.accepted_categories) = 0 then 'Accepts all categories' else 'Accepts ' || resource_row.category end,
          case when page.distance_km is null then 'Distance unavailable' else round(page.distance_km::numeric, 1)::text || ' km away' end,
          case when page.pickup_available then 'Pickup available' else 'Drop-off required' end
        ) end,
        'created_at_ms', floor(extract(epoch from page.created_at) * 1000)::bigint,
        'updated_at_ms', floor(extract(epoch from page.updated_at) * 1000)::bigint
      ) order by page.match_score desc nulls last, page.distance_km nulls last, lower(page.name), page.id)
      from (
        select * from evaluated where exclusion_code is null
        order by match_score desc nulls last, distance_km nulls last, lower(name), id
        limit p_limit offset p_offset
      ) page
    ), '[]'::jsonb),
    coalesce((
      select jsonb_object_agg(exclusion_code, excluded_count)
      from (
        select exclusion_code, count(*)::integer as excluded_count
        from evaluated where exclusion_code is not null group by exclusion_code
      ) excluded
    ), '{}'::jsonb),
    (select count(*)::integer from evaluated where exclusion_code is null)
  into candidate_rows, exclusion_rows, eligible_count;

  return jsonb_build_object(
    'origin_source', origin_source,
    'origin_address', origin_address,
    'origin_latitude', origin_lat,
    'origin_longitude', origin_lon,
    'candidates', candidate_rows,
    'exclusion_counts', exclusion_rows,
    'next_offset', case when p_offset + p_limit < eligible_count then p_offset + p_limit else null end
  );
end;
$$;

revoke all on function public.find_partner_programmes(
  uuid, double precision, double precision, public.material_family, public.programme_type[], double precision, boolean, integer, integer
) from public;
grant execute on function public.find_partner_programmes(
  uuid, double precision, double precision, public.material_family, public.programme_type[], double precision, boolean, integer, integer
) to authenticated;

alter table public.resource_items drop column material;
alter table public.circular_programmes drop column accepted_materials;

drop function public.material_family_from_legacy(text);

grant execute on function public.material_family_display(public.material_family, text) to anon, authenticated;

