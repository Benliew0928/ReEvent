-- Run in Supabase SQL Editor as postgres against a disposable staging project.
-- The script uses three existing frozen-role profiles, executes as authenticated
-- actors, asserts both positive and denied paths, and rolls every fixture back.

begin;

select set_config('reevent_smoke.organizer_id', (
  select id::text from public.profiles where role = 'ORGANIZER' order by id limit 1
), true);
select set_config('reevent_smoke.participant_id', (
  select id::text from public.profiles where role = 'PARTICIPANT' order by id limit 1
), true);
select set_config('reevent_smoke.partner_id', (
  select id::text from public.profiles where role = 'PARTNER' order by id limit 1
), true);

-- Organiser creates the only directly writable root objects.
select set_config('request.jwt.claim.sub', current_setting('reevent_smoke.organizer_id'), true);
set local role authenticated;

with inserted as (
  insert into public.events(
    owner_id, name, event_type, starts_at, ends_at, timezone_id,
    address_text, latitude, longitude, expected_attendance, status
  ) values (
    auth.uid(), 'Stage 3 authority smoke', 'COMMUNITY', now() + interval '1 day',
    now() + interval '2 days', 'Asia/Kuala_Lumpur', 'Kuala Lumpur',
    3.139000, 101.686900, 50, 'ACTIVE'
  ) returning id
)
select set_config('reevent_smoke.event_id', id::text, true) from inserted;

with inserted as (
  insert into public.resource_items(
    origin_event_id, created_by, current_owner_id, title, category, material,
    condition, quantity, unit, status
  ) values (
    current_setting('reevent_smoke.event_id')::uuid, auth.uid(), auth.uid(),
    'Smoke-test reusable cups', 'SERVICEWARE', 'plastic', 'GOOD', 5, 'ITEM', 'ACTIVE'
  ) returning id
)
select set_config('reevent_smoke.resource_id', id::text, true) from inserted;

with published as (
  select public.publish_marketplace_listing(
    current_setting('reevent_smoke.resource_id')::uuid,
    array['BORROW', 'RENT', 'BUY', 'DONATE', 'EXCHANGE']::public.transaction_type[],
    5, 25, 10, 7, 'Rollback-only staging smoke fixture.'
  ) as result
)
select set_config('reevent_smoke.listing_id', result ->> 'id', true) from published;

select public.replace_resource_photo(
  current_setting('reevent_smoke.resource_id')::uuid,
  format(
    '%s/resources/%s/primary',
    current_setting('reevent_smoke.organizer_id'),
    current_setting('reevent_smoke.resource_id')
  ),
  'image/jpeg', 800, 600, 12345
);

-- A lost response may retry the same deterministic path without creating duplicate metadata.
select public.replace_resource_photo(
  current_setting('reevent_smoke.resource_id')::uuid,
  format(
    '%s/resources/%s/primary',
    current_setting('reevent_smoke.organizer_id'),
    current_setting('reevent_smoke.resource_id')
  ),
  'image/jpeg', 1024, 768, 23456
);

do $$
declare
  photo_path text := format(
    '%s/resources/%s/primary',
    current_setting('reevent_smoke.organizer_id'),
    current_setting('reevent_smoke.resource_id')
  );
begin
  if (
    select count(*)
    from public.resource_photos
    where resource_id = current_setting('reevent_smoke.resource_id')::uuid
      and storage_path = photo_path
      and width = 1024
      and byte_size = 23456
  ) <> 1 then
    raise exception 'RESOURCE_PHOTO_RETRY_NOT_IDEMPOTENT';
  end if;

  begin
    insert into public.resource_photos(resource_id, storage_path, sort_order)
    values (current_setting('reevent_smoke.resource_id')::uuid, photo_path || '-direct', 1);
    raise exception 'DIRECT_RESOURCE_PHOTO_WRITE_NOT_DENIED';
  exception when insufficient_privilege then null;
  end;

  begin
    perform public.complete_resource_photo_cleanup(
      current_setting('reevent_smoke.resource_id')::uuid,
      photo_path
    );
    raise exception 'CURRENT_RESOURCE_PHOTO_CLEANUP_NOT_DENIED';
  exception when others then
    if sqlerrm <> 'CURRENT_RESOURCE_PHOTO_CANNOT_BE_CLEANED' then raise; end if;
  end;
end;
$$;

-- Even the owner cannot directly mutate lifecycle status.
do $$
begin
  begin
    update public.resource_items
    set status = 'RECOVERED'
    where id = current_setting('reevent_smoke.resource_id')::uuid;
    raise exception 'LIFECYCLE_DENIAL_MISSING';
  exception when others then
    if sqlerrm <> 'RESOURCE_LIFECYCLE_SERVER_ONLY' then raise; end if;
  end;
end;
$$;

reset role;

-- Participant requests a rent, cannot write a protected workflow table, and
-- receives the same transaction for the same persisted idempotency key.
select set_config('request.jwt.claim.sub', current_setting('reevent_smoke.participant_id'), true);
set local role authenticated;

do $$
begin
  begin
    perform public.replace_resource_photo(
      current_setting('reevent_smoke.resource_id')::uuid,
      format(
        '%s/resources/%s/not-owned',
        current_setting('reevent_smoke.participant_id'),
        current_setting('reevent_smoke.resource_id')
      ),
      'image/jpeg', 200, 200, 1000
    );
    raise exception 'NON_OWNER_RESOURCE_PHOTO_WRITE_NOT_DENIED';
  exception when others then
    if sqlerrm <> 'RESOURCE_OWNER_REQUIRED' then raise; end if;
  end;
end;
$$;

with requested as (
  select public.request_listing_transaction(
    current_setting('reevent_smoke.listing_id')::uuid, 'RENT', 2, null,
    'Stage 3 rollback smoke', '70000000-0000-4000-8000-000000000001'::uuid
  ) as result
)
select set_config('reevent_smoke.transaction_id', result -> 'transaction' ->> 'id', true)
from requested;

do $$
declare replay_id uuid;
begin
  begin
    insert into public.circular_transactions default values;
    raise exception 'DIRECT_TRANSACTION_WRITE_NOT_DENIED';
  exception when insufficient_privilege then null;
  end;

  select (public.request_listing_transaction(
    current_setting('reevent_smoke.listing_id')::uuid, 'RENT', 2, null,
    'Stage 3 rollback smoke', '70000000-0000-4000-8000-000000000001'::uuid
  ) -> 'transaction' ->> 'id')::uuid into replay_id;
  if replay_id <> current_setting('reevent_smoke.transaction_id')::uuid then
    raise exception 'IDEMPOTENT_REPLAY_MISMATCH';
  end if;

  begin
    perform public.request_listing_transaction(
      current_setting('reevent_smoke.listing_id')::uuid, 'RENT', 3, null,
      'Changed request', '70000000-0000-4000-8000-000000000001'::uuid
    );
    raise exception 'IDEMPOTENCY_REUSE_NOT_DENIED';
  exception when others then
    if sqlerrm <> 'IDEMPOTENCY_KEY_REUSED' then raise; end if;
  end;
end;
$$;

reset role;

-- An unrelated partner cannot approve a marketplace transaction.
select set_config('request.jwt.claim.sub', current_setting('reevent_smoke.partner_id'), true);
set local role authenticated;
do $$
begin
  begin
    perform public.approve_transaction(
      current_setting('reevent_smoke.transaction_id')::uuid,
      '70000000-0000-4000-8000-000000000002'::uuid
    );
    raise exception 'WRONG_APPROVER_NOT_DENIED';
  exception when others then
    if sqlerrm <> 'DECISION_ACTOR_REQUIRED' then raise; end if;
  end;
end;
$$;

reset role;

-- The authorised organiser and participant complete the full RENT lifecycle.
select set_config('request.jwt.claim.sub', current_setting('reevent_smoke.organizer_id'), true);
set local role authenticated;
select public.approve_transaction(
  current_setting('reevent_smoke.transaction_id')::uuid,
  '70000000-0000-4000-8000-000000000003'::uuid
);
select public.begin_transaction_handover(
  current_setting('reevent_smoke.transaction_id')::uuid, 'PRIMARY',
  '70000000-0000-4000-8000-000000000004'::uuid
);

reset role;
select set_config('request.jwt.claim.sub', current_setting('reevent_smoke.participant_id'), true);
set local role authenticated;
select public.confirm_transaction_receipt(
  current_setting('reevent_smoke.transaction_id')::uuid, 'PRIMARY',
  '70000000-0000-4000-8000-000000000005'::uuid
);
select public.begin_transaction_return(
  current_setting('reevent_smoke.transaction_id')::uuid,
  '70000000-0000-4000-8000-000000000006'::uuid
);

reset role;
select set_config('request.jwt.claim.sub', current_setting('reevent_smoke.partner_id'), true);
set local role authenticated;
do $$
begin
  begin
    perform public.confirm_transaction_return(
      current_setting('reevent_smoke.transaction_id')::uuid,
      '70000000-0000-4000-8000-000000000007'::uuid
    );
    raise exception 'WRONG_RETURN_CONFIRMATION_NOT_DENIED';
  exception when others then
    if sqlerrm <> 'ORIGINAL_OWNER_REQUIRED' then raise; end if;
  end;
end;
$$;

reset role;
select set_config('request.jwt.claim.sub', current_setting('reevent_smoke.organizer_id'), true);
set local role authenticated;
select public.confirm_transaction_return(
  current_setting('reevent_smoke.transaction_id')::uuid,
  '70000000-0000-4000-8000-000000000008'::uuid
);

do $$
declare replay_id uuid;
begin
  select (public.confirm_transaction_return(
    current_setting('reevent_smoke.transaction_id')::uuid,
    '70000000-0000-4000-8000-000000000008'::uuid
  ) -> 'transaction' ->> 'id')::uuid into replay_id;
  if replay_id <> current_setting('reevent_smoke.transaction_id')::uuid then
    raise exception 'COMPLETION_REPLAY_MISMATCH';
  end if;
end;
$$;

reset role;
do $$
declare current_status public.transaction_status;
begin
  select status into current_status
  from public.circular_transactions
  where id = current_setting('reevent_smoke.transaction_id')::uuid;
  if current_status <> 'COMPLETED' then raise exception 'COMPLETION_STATUS_MISMATCH'; end if;
  if (select count(*) from public.transaction_allocations
      where transaction_id = current_setting('reevent_smoke.transaction_id')::uuid and state = 'RELEASED') <> 1 then
    raise exception 'ALLOCATION_EFFECT_MISMATCH';
  end if;
  if (select count(*) from public.recoin_holds
      where transaction_id = current_setting('reevent_smoke.transaction_id')::uuid and status = 'SETTLED') <> 1 then
    raise exception 'HOLD_EFFECT_MISMATCH';
  end if;
  if (select count(*) from public.impact_records
      where transaction_id = current_setting('reevent_smoke.transaction_id')::uuid) <> 1 then
    raise exception 'IMPACT_EFFECT_MISMATCH';
  end if;
  if (select count(*) from public.passport_events
      where transaction_id = current_setting('reevent_smoke.transaction_id')::uuid and event_type = 'RETURNED') <> 1 then
    raise exception 'PASSPORT_EFFECT_MISMATCH';
  end if;
end;
$$;

rollback;

select 'PASS' as staging_authority_smoke,
       'three roles; listing/photo RPC and direct-DML denial; photo retry/cleanup authorization; request/decision, handover/return, completion replay; fixtures rolled back' as coverage;
