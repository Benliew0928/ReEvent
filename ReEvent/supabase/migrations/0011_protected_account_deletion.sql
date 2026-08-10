-- Protected account-deletion preparation for the release lifecycle schema (0005+).
-- The mobile client never calls this RPC. Only the authenticated Edge Function uses the
-- service-role client after it has verified the authenticated caller's current password.

alter table public.profiles
  add column if not exists deletion_started_at timestamptz;

-- A successful Auth deletion cascades the profile. Every workflow reference from 0005 uses
-- ON DELETE SET NULL (or is explicitly removed below), so retained terminal history becomes
-- de-identified rather than preventing Auth deletion.
alter table public.profiles drop constraint if exists profiles_id_fkey;
alter table public.profiles
  add constraint profiles_id_fkey foreign key (id) references auth.users(id) on delete cascade;

create or replace function public.prepare_account_deletion(p_user_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
  profile_row public.profiles;
  wallet_row public.recoin_wallets;
begin
  if p_user_id is null then
    raise exception using errcode = '22023', message = 'ACCOUNT_ID_REQUIRED';
  end if;

  -- Serialise repeated deletion requests for this account. A retry after a storage/Auth failure
  -- returns READY again and is safe; a second client cannot race the clean-up transaction.
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

  if exists (
    select 1
    from public.recoin_wallets wallet_row
    left join public.recoin_holds hold_row on hold_row.payer_wallet_id = wallet_row.id
      or hold_row.payee_wallet_id = wallet_row.id
    where wallet_row.profile_id = p_user_id
      and (wallet_row.held_balance <> 0 or hold_row.status = 'ACTIVE')
  ) then
    return jsonb_build_object('status', 'BLOCKED_UNSETTLED_COINS');
  end if;

  -- Remove metadata that links a transferred/archived resource to private photo storage before
  -- the Edge Function removes every object in this user's private Storage prefix.
  delete from public.resource_photos photo_row
  using public.resource_items resource_row
  where photo_row.resource_id = resource_row.id
    and p_user_id in (resource_row.created_by, resource_row.current_owner_id);

  -- Transaction confirmation rows have a non-null actor in their primary key. Removing the
  -- actor's confirmation is the only safe de-identification path; the terminal transaction and
  -- passport event remain, with actor references set to null by the profile delete cascade.
  delete from public.transaction_confirmations where actor_id = p_user_id;
  delete from public.idempotency_records where actor_id = p_user_id;

  -- Close each eligible wallet and record the available-balance burn in the immutable ledger.
  -- Active holds were rejected above, therefore a completed account cannot strand escrow.
  for wallet_row in
    select * from public.recoin_wallets where profile_id = p_user_id for update
  loop
    if wallet_row.available_balance <> 0 then
      insert into public.recoin_ledger_entries(
        entry_group_id, wallet_id, entry_type, amount, created_at
      ) values (
        gen_random_uuid(), wallet_row.id, 'ACCOUNT_CLOSE_BURN', -wallet_row.available_balance, now()
      );
    end if;
    update public.recoin_wallets
    set available_balance = 0,
        held_balance = 0,
        profile_id = null,
        closed_at = now(),
        updated_at = now()
    where id = wallet_row.id;
  end loop;

  -- This immediately prevents further user lifecycle RPCs: require_verified_actor() requires a
  -- frozen role. The Edge Function may now remove Storage and the Auth user in a separate call.
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
