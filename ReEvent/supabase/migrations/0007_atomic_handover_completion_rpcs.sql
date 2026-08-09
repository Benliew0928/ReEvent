-- Physical custody commands and the single atomic completion boundary.

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
        category, material, condition, quantity, unit, status, address_text, latitude, longitude
      ) values (
        resource_row.origin_event_id, resource_row.id, resource_row.created_by, transaction_row.receiver_id,
        resource_row.title, resource_row.description, resource_row.category, resource_row.material,
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
        category, material, condition, quantity, unit, status, address_text, latitude, longitude
      ) values (
        resource_row.origin_event_id, resource_row.id, resource_row.created_by, resource_row.current_owner_id,
        resource_row.title, resource_row.description, resource_row.category, resource_row.material,
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
    and lower(resource_row.material) in ('plastic', 'acrylic') then
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

create or replace function public.begin_transaction_handover(
  p_transaction_id uuid,
  p_resource_side public.allocation_side,
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
  passport_id uuid;
  expected_actor uuid;
  side_resource_id uuid;
  request_value jsonb := jsonb_build_object('transaction_id', p_transaction_id, 'resource_side', p_resource_side);
begin
  replay := public.begin_idempotent_command('begin_transaction_handover', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;
  select * into strict transaction_row from public.circular_transactions where id = p_transaction_id for update;
  if transaction_row.status <> 'APPROVED'
    and not (transaction_row.transaction_type = 'EXCHANGE' and transaction_row.status = 'IN_TRANSIT') then
    raise exception using errcode = '55000', message = 'TRANSACTION_NOT_APPROVED';
  end if;
  if p_resource_side = 'COUNTER' and transaction_row.transaction_type <> 'EXCHANGE' then
    raise exception using errcode = '22023', message = 'COUNTER_SIDE_NOT_ALLOWED';
  end if;
  expected_actor := case when p_resource_side = 'PRIMARY' then transaction_row.sender_id else transaction_row.requester_id end;
  side_resource_id := case when p_resource_side = 'PRIMARY' then transaction_row.resource_id else transaction_row.counter_resource_id end;
  if actor_id <> expected_actor then
    raise exception using errcode = '42501', message = 'HANDOVER_ACTOR_REQUIRED';
  end if;
  select * into strict resource_row from public.resource_items where id = side_resource_id for update;
  insert into public.transaction_confirmations(
    transaction_id, actor_id, resource_side, confirmation_type, idempotency_key
  ) values (transaction_row.id, actor_id, p_resource_side, 'HANDOVER', p_idempotency_key);

  if p_resource_side = 'PRIMARY'
    and transaction_row.programme_id is not null
    and transaction_row.quantity = resource_row.quantity then
    update public.resource_items set status = 'RECOVERY_IN_PROGRESS' where id = resource_row.id;
  end if;
  select id into strict passport_id from public.resource_passports where resource_id = resource_row.id;
  insert into public.passport_events(
    passport_id, transaction_id, event_type, actor_id, quantity, unit, public_summary, idempotency_key
  ) values (
    passport_id, transaction_row.id,
    case when transaction_row.transaction_type = 'REPAIR' then 'REPAIR_STARTED'::public.passport_event_type else 'CHECKED_OUT'::public.passport_event_type end,
    actor_id,
    case when p_resource_side = 'PRIMARY' then transaction_row.quantity else resource_row.quantity end,
    resource_row.unit, 'Physical handover started', p_idempotency_key
  );
  update public.circular_transactions
  set status = 'IN_TRANSIT', in_transit_at = coalesce(in_transit_at, now())
  where id = transaction_row.id
  returning * into transaction_row;
  return public.finish_idempotent_command(
    'begin_transaction_handover', p_idempotency_key,
    jsonb_build_object('transaction', to_jsonb(transaction_row), 'replayed', false)
  );
end;
$$;

create or replace function public.confirm_transaction_receipt(
  p_transaction_id uuid,
  p_resource_side public.allocation_side,
  p_idempotency_key uuid
)
returns jsonb
language plpgsql security definer set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  replay jsonb;
  transaction_row public.circular_transactions;
  expected_actor uuid;
  receipt_count integer;
  completed_row public.circular_transactions;
  request_value jsonb := jsonb_build_object('transaction_id', p_transaction_id, 'resource_side', p_resource_side);
begin
  replay := public.begin_idempotent_command('confirm_transaction_receipt', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;
  select * into strict transaction_row from public.circular_transactions where id = p_transaction_id for update;
  if transaction_row.status <> 'IN_TRANSIT' then
    raise exception using errcode = '55000', message = 'TRANSACTION_NOT_IN_TRANSIT';
  end if;
  if p_resource_side = 'COUNTER' and transaction_row.transaction_type <> 'EXCHANGE' then
    raise exception using errcode = '22023', message = 'COUNTER_SIDE_NOT_ALLOWED';
  end if;
  expected_actor := case when p_resource_side = 'PRIMARY' then transaction_row.receiver_id else transaction_row.sender_id end;
  if actor_id <> expected_actor then
    raise exception using errcode = '42501', message = 'RECEIPT_ACTOR_REQUIRED';
  end if;
  if not exists(
    select 1 from public.transaction_confirmations
    where transaction_id = transaction_row.id and resource_side = p_resource_side and confirmation_type = 'HANDOVER'
  ) then
    raise exception using errcode = '55000', message = 'HANDOVER_CONFIRMATION_REQUIRED';
  end if;
  insert into public.transaction_confirmations(
    transaction_id, actor_id, resource_side, confirmation_type, idempotency_key
  ) values (transaction_row.id, actor_id, p_resource_side, 'RECEIPT', p_idempotency_key);

  if transaction_row.transaction_type in ('BORROW', 'RENT', 'REPAIR') then
    update public.transaction_allocations
    set state = 'IN_CUSTODY'
    where transaction_id = transaction_row.id and side = 'PRIMARY' and state = 'RESERVED';
    if not found then
      raise exception using errcode = '55000', message = 'RESERVED_ALLOCATION_MISSING';
    end if;
    update public.circular_transactions
    set status = 'ACTIVE', active_at = now()
    where id = transaction_row.id
    returning * into transaction_row;
  elsif transaction_row.transaction_type = 'EXCHANGE' then
    select count(*) into receipt_count from public.transaction_confirmations
    where transaction_id = transaction_row.id and confirmation_type = 'RECEIPT';
    if receipt_count = 2 then
      completed_row := public.complete_transaction_effects(transaction_row.id, actor_id, p_idempotency_key);
      transaction_row := completed_row;
    end if;
  else
    completed_row := public.complete_transaction_effects(transaction_row.id, actor_id, p_idempotency_key);
    transaction_row := completed_row;
  end if;

  return public.finish_idempotent_command(
    'confirm_transaction_receipt', p_idempotency_key,
    jsonb_build_object('transaction', to_jsonb(transaction_row), 'replayed', false)
  );
end;
$$;

create or replace function public.begin_transaction_return(
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
  passport_id uuid;
  request_value jsonb := jsonb_build_object('transaction_id', p_transaction_id);
begin
  replay := public.begin_idempotent_command('begin_transaction_return', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;
  select * into strict transaction_row from public.circular_transactions where id = p_transaction_id for update;
  if transaction_row.status <> 'ACTIVE' or transaction_row.transaction_type not in ('BORROW', 'RENT', 'REPAIR') then
    raise exception using errcode = '55000', message = 'TRANSACTION_NOT_RETURNABLE';
  end if;
  if actor_id <> transaction_row.receiver_id then
    raise exception using errcode = '42501', message = 'CUSTODIAN_REQUIRED';
  end if;
  insert into public.transaction_confirmations(
    transaction_id, actor_id, resource_side, confirmation_type, idempotency_key
  ) values (transaction_row.id, actor_id, 'PRIMARY', 'RETURN', p_idempotency_key);
  select id into strict passport_id from public.resource_passports where resource_id = transaction_row.resource_id;
  insert into public.passport_events(
    passport_id, transaction_id, event_type, actor_id, quantity, unit, public_summary, idempotency_key
  ) values (
    passport_id, transaction_row.id, 'RETURN_STARTED', actor_id, transaction_row.quantity,
    transaction_row.unit, 'Return handover started', p_idempotency_key
  );
  update public.circular_transactions
  set status = 'RETURN_IN_PROGRESS', return_started_at = now()
  where id = transaction_row.id
  returning * into transaction_row;
  return public.finish_idempotent_command(
    'begin_transaction_return', p_idempotency_key,
    jsonb_build_object('transaction', to_jsonb(transaction_row), 'replayed', false)
  );
end;
$$;

create or replace function public.confirm_transaction_return(
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
  request_value jsonb := jsonb_build_object('transaction_id', p_transaction_id);
begin
  replay := public.begin_idempotent_command('confirm_transaction_return', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;
  select * into strict transaction_row from public.circular_transactions where id = p_transaction_id for update;
  if transaction_row.status <> 'RETURN_IN_PROGRESS'
    or transaction_row.transaction_type not in ('BORROW', 'RENT', 'REPAIR') then
    raise exception using errcode = '55000', message = 'RETURN_NOT_IN_PROGRESS';
  end if;
  if actor_id <> transaction_row.sender_id then
    raise exception using errcode = '42501', message = 'ORIGINAL_OWNER_REQUIRED';
  end if;
  if not exists(
    select 1 from public.transaction_confirmations confirmation
    where confirmation.transaction_id = transaction_row.id
      and confirmation.actor_id = transaction_row.receiver_id
      and confirmation.confirmation_type = 'RETURN'
  ) then
    raise exception using errcode = '55000', message = 'CUSTODIAN_RETURN_REQUIRED';
  end if;
  insert into public.transaction_confirmations(
    transaction_id, actor_id, resource_side, confirmation_type, idempotency_key
  ) values (transaction_row.id, actor_id, 'PRIMARY', 'RETURN', p_idempotency_key);
  transaction_row := public.complete_transaction_effects(transaction_row.id, actor_id, p_idempotency_key);
  return public.finish_idempotent_command(
    'confirm_transaction_return', p_idempotency_key,
    jsonb_build_object('transaction', to_jsonb(transaction_row), 'replayed', false)
  );
end;
$$;

revoke all on function public.complete_transaction_effects(uuid, uuid, uuid) from public;
revoke all on function public.begin_transaction_handover(uuid, public.allocation_side, uuid) from public;
revoke all on function public.confirm_transaction_receipt(uuid, public.allocation_side, uuid) from public;
revoke all on function public.begin_transaction_return(uuid, uuid) from public;
revoke all on function public.confirm_transaction_return(uuid, uuid) from public;

grant execute on function public.begin_transaction_handover(uuid, public.allocation_side, uuid) to authenticated;
grant execute on function public.confirm_transaction_receipt(uuid, public.allocation_side, uuid) to authenticated;
grant execute on function public.begin_transaction_return(uuid, uuid) to authenticated;
grant execute on function public.confirm_transaction_return(uuid, uuid) to authenticated;
