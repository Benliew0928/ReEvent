-- A prepared account is terminal even when private Storage or Auth deletion must be retried.
-- This follows 0011 instead of rewriting it so environments that already ran 0011 receive the
-- same guard. In particular, role completion must never create a second wallet/initial grant.

create or replace function public.prepare_account_deletion(p_user_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
  profile_row public.profiles;
  wallet_to_close public.recoin_wallets;
begin
  if p_user_id is null then
    raise exception using errcode = '22023', message = 'ACCOUNT_ID_REQUIRED';
  end if;

  perform pg_advisory_xact_lock(hashtext(p_user_id::text));
  select * into profile_row from public.profiles where id = p_user_id for update;
  if not found then
    raise exception using errcode = 'P0002', message = 'PROFILE_NOT_FOUND';
  end if;
  if profile_row.deletion_started_at is not null then
    return jsonb_build_object('status', 'READY_FOR_AUTH_DELETION');
  end if;

  if exists (
    select 1 from public.circular_transactions transaction_row
    where p_user_id in (
      transaction_row.requester_id,
      transaction_row.sender_id,
      transaction_row.receiver_id,
      transaction_row.partner_id
    )
    and transaction_row.status in ('REQUESTED', 'APPROVED', 'IN_TRANSIT', 'ACTIVE', 'RETURN_IN_PROGRESS')
  ) then
    return jsonb_build_object('status', 'BLOCKED_ACTIVE_TRANSACTIONS');
  end if;

  if exists (
    select 1 from public.resource_items resource_row
    where resource_row.current_owner_id = p_user_id
      and resource_row.status not in ('RECOVERED', 'ARCHIVED')
  ) then
    return jsonb_build_object('status', 'BLOCKED_ACTIVE_RESOURCES');
  end if;

  if exists (
    select 1 from public.events event_row
    where event_row.owner_id = p_user_id and event_row.status <> 'ARCHIVED'
  ) then
    return jsonb_build_object('status', 'BLOCKED_ACTIVE_EVENTS');
  end if;

  if exists (
    select 1 from public.marketplace_listings listing_row
    where listing_row.seller_id = p_user_id
      and listing_row.status in ('DRAFT', 'PUBLISHED', 'RESERVED')
  ) then
    return jsonb_build_object('status', 'BLOCKED_OPEN_LISTINGS');
  end if;

  if exists (
    select 1 from public.circular_programmes programme_row
    where programme_row.partner_id = p_user_id and programme_row.active
  ) then
    return jsonb_build_object('status', 'BLOCKED_ACTIVE_PROGRAMMES');
  end if;

  -- Use an alias distinct from the PL/pgSQL record variable. Migration 0011 used wallet_row for
  -- both, which made this expression ambiguous and prevented eligible deletion preparation.
  if exists (
    select 1
    from public.recoin_wallets candidate_wallet
    left join public.recoin_holds hold_row
      on hold_row.payer_wallet_id = candidate_wallet.id
      or hold_row.payee_wallet_id = candidate_wallet.id
    where candidate_wallet.profile_id = p_user_id
      and (candidate_wallet.held_balance <> 0 or hold_row.status = 'ACTIVE')
  ) then
    return jsonb_build_object('status', 'BLOCKED_UNSETTLED_COINS');
  end if;

  delete from public.resource_photos photo_row
  using public.resource_items resource_row
  where photo_row.resource_id = resource_row.id
    and p_user_id in (resource_row.created_by, resource_row.current_owner_id);

  delete from public.transaction_confirmations where actor_id = p_user_id;
  delete from public.idempotency_records where actor_id = p_user_id;

  for wallet_to_close in
    select * from public.recoin_wallets where profile_id = p_user_id for update
  loop
    if wallet_to_close.available_balance <> 0 then
      insert into public.recoin_ledger_entries(
        entry_group_id, wallet_id, entry_type, amount, created_at
      ) values (
        gen_random_uuid(), wallet_to_close.id, 'ACCOUNT_CLOSE_BURN', -wallet_to_close.available_balance, now()
      );
    end if;
    update public.recoin_wallets
    set available_balance = 0,
        held_balance = 0,
        profile_id = null,
        closed_at = now(),
        updated_at = now()
    where id = wallet_to_close.id;
  end loop;

  update public.profiles
  set display_name = 'Deleted ReEvent account',
      role = null,
      role_frozen_at = null,
      avatar_path = null,
      deletion_started_at = now(),
      updated_at = now()
  where id = p_user_id;

  return jsonb_build_object('status', 'READY_FOR_AUTH_DELETION');
end;
$$;

revoke all on function public.prepare_account_deletion(uuid) from public, anon, authenticated;
grant execute on function public.prepare_account_deletion(uuid) to service_role;

create or replace function public.complete_profile_role(p_role public.user_role)
returns public.profiles
language plpgsql
security definer
set search_path = public
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
  select * into strict profile_row
  from public.profiles
  where id = auth.uid()
  for update;

  if profile_row.deletion_started_at is not null then
    raise exception using errcode = '55000', message = 'ACCOUNT_DELETION_PENDING';
  end if;

  update public.profiles
  set role = p_role, role_frozen_at = now(), updated_at = now()
  where id = auth.uid() and role is null and deletion_started_at is null
  returning * into profile_row;

  if not found then
    select * into profile_row
    from public.profiles
    where id = auth.uid() and role = p_role and deletion_started_at is null;
    if not found then
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

revoke all on function public.complete_profile_role(public.user_role) from public;
grant execute on function public.complete_profile_role(public.user_role) to authenticated;
