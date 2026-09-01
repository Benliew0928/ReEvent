-- Server-authoritative event publication and privacy-safe stakeholder discovery.
-- Raw events remain organiser-owned. Stakeholder reads use the narrow RPC below.

create or replace function public.enforce_event_lifecycle_mutation()
returns trigger
language plpgsql security definer set search_path = public
as $$
begin
  -- Protected lifecycle functions set this transaction-local marker after they have
  -- authenticated and validated the actor. Direct table writes never receive it.
  if current_setting('reevent.event_lifecycle_rpc', true) = 'on' then
    return new;
  end if;

  if tg_op = 'INSERT' then
    if new.owner_id is distinct from auth.uid() or new.status <> 'DRAFT' then
      raise exception using errcode = '42501', message = 'EVENT_LIFECYCLE_SERVER_ONLY';
    end if;
    return new;
  end if;

  -- A private Draft may be edited or discarded by its organiser through the
  -- existing local-first path. Every shared transition is protected by an RPC.
  if old.status = 'DRAFT' and new.status in ('DRAFT', 'ARCHIVED') then
    return new;
  end if;

  raise exception using errcode = '42501', message = 'EVENT_LIFECYCLE_SERVER_ONLY';
end;
$$;

drop trigger if exists events_enforce_lifecycle on public.events;
create trigger events_enforce_lifecycle
before insert or update on public.events
for each row execute function public.enforce_event_lifecycle_mutation();

drop policy if exists events_owner_access on public.events;
drop policy if exists events_owner_read on public.events;
drop policy if exists events_draft_insert on public.events;
drop policy if exists events_draft_update on public.events;

create policy events_owner_read on public.events for select to authenticated
using (owner_id = auth.uid());

create policy events_draft_insert on public.events for insert to authenticated
with check (
  owner_id = auth.uid()
  and public.has_role('ORGANIZER')
  and status = 'DRAFT'
);

create policy events_draft_update on public.events for update to authenticated
using (owner_id = auth.uid())
with check (
  owner_id = auth.uid()
  and public.has_role('ORGANIZER')
  and status in ('DRAFT', 'ARCHIVED')
);

revoke delete on public.events from authenticated;
grant insert, update on public.events to authenticated;

create or replace function public.validate_event_publication(
  p_name text,
  p_description text,
  p_event_type public.event_type,
  p_starts_at timestamptz,
  p_ends_at timestamptz,
  p_timezone_id text,
  p_address_text text,
  p_latitude numeric,
  p_longitude numeric,
  p_expected_attendance integer,
  p_recovery_target_percent numeric
)
returns void
language plpgsql security definer set search_path = public
as $$
begin
  if p_name is null or char_length(btrim(p_name)) not between 1 and 120 then
    raise exception using errcode = '22023', message = 'EVENT_NAME_REQUIRED';
  end if;
  if char_length(coalesce(p_description, '')) > 2000 then
    raise exception using errcode = '22023', message = 'EVENT_DESCRIPTION_TOO_LONG';
  end if;
  if p_event_type is null then
    raise exception using errcode = '22023', message = 'EVENT_TYPE_REQUIRED';
  end if;
  if p_starts_at is null or p_ends_at is null or p_ends_at <= p_starts_at then
    raise exception using errcode = '22023', message = 'EVENT_TIME_INVALID';
  end if;
  if p_timezone_id is null or btrim(p_timezone_id) = '' then
    raise exception using errcode = '22023', message = 'EVENT_TIMEZONE_REQUIRED';
  end if;
  if not exists (
    select 1 from pg_timezone_names where name = btrim(p_timezone_id)
  ) then
    raise exception using errcode = '22023', message = 'EVENT_TIMEZONE_INVALID';
  end if;
  if p_address_text is null or btrim(p_address_text) = '' then
    raise exception using errcode = '22023', message = 'EVENT_ADDRESS_REQUIRED';
  end if;
  if p_latitude is null or p_longitude is null
    or p_latitude not between -90 and 90
    or p_longitude not between -180 and 180 then
    raise exception using errcode = '22023', message = 'EVENT_LOCATION_REQUIRED';
  end if;
  if p_expected_attendance is null or p_expected_attendance <= 0 then
    raise exception using errcode = '22023', message = 'EVENT_ATTENDANCE_REQUIRED';
  end if;
  if p_recovery_target_percent is null or p_recovery_target_percent not between 0 and 100 then
    raise exception using errcode = '22023', message = 'EVENT_RECOVERY_TARGET_INVALID';
  end if;
end;
$$;

create or replace function public.publish_event(
  p_event_id uuid,
  p_name text,
  p_description text,
  p_event_type public.event_type,
  p_starts_at timestamptz,
  p_ends_at timestamptz,
  p_timezone_id text,
  p_address_text text,
  p_latitude numeric,
  p_longitude numeric,
  p_expected_attendance integer,
  p_recovery_target_percent numeric,
  p_idempotency_key uuid
)
returns jsonb
language plpgsql security definer set search_path = public, extensions
as $$
declare
  actor_id uuid := public.require_verified_actor();
  event_row public.events;
  replay jsonb;
  request_value jsonb := jsonb_build_object(
    'event_id', p_event_id,
    'name', p_name,
    'description', p_description,
    'event_type', p_event_type,
    'starts_at', p_starts_at,
    'ends_at', p_ends_at,
    'timezone_id', p_timezone_id,
    'address_text', p_address_text,
    'latitude', p_latitude,
    'longitude', p_longitude,
    'expected_attendance', p_expected_attendance,
    'recovery_target_percent', p_recovery_target_percent
  );
  response jsonb;
begin
  replay := public.begin_idempotent_command('publish_event', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;

  if not public.has_role('ORGANIZER') then
    raise exception using errcode = '42501', message = 'ORGANIZER_ROLE_REQUIRED';
  end if;
  perform public.validate_event_publication(
    p_name, p_description, p_event_type, p_starts_at, p_ends_at, p_timezone_id,
    p_address_text, p_latitude, p_longitude, p_expected_attendance, p_recovery_target_percent
  );

  select * into event_row from public.events where id = p_event_id for update;
  -- Do not use FOUND after set_config: PERFORM changes PL/pgSQL's FOUND flag.
  if event_row.id is null then
    perform set_config('reevent.event_lifecycle_rpc', 'on', true);
    insert into public.events(
      id, owner_id, name, description, event_type, starts_at, ends_at, timezone_id,
      address_text, latitude, longitude, expected_attendance, recovery_target_percent,
      status, archived_at
    ) values (
      p_event_id, actor_id, btrim(p_name), coalesce(p_description, ''), p_event_type,
      p_starts_at, p_ends_at, btrim(p_timezone_id), btrim(p_address_text), p_latitude,
      p_longitude, p_expected_attendance, p_recovery_target_percent, 'ACTIVE', null
    ) returning * into event_row;
  else
    perform set_config('reevent.event_lifecycle_rpc', 'on', true);
    if event_row.owner_id is distinct from actor_id then
      raise exception using errcode = '42501', message = 'EVENT_OWNER_REQUIRED';
    end if;
    if event_row.status = 'ACTIVE' then
      response := jsonb_build_object('event', to_jsonb(event_row), 'replayed', true);
      return public.finish_idempotent_command('publish_event', p_idempotency_key, response);
    end if;
    if event_row.status <> 'DRAFT' then
      raise exception using errcode = '55000', message = 'EVENT_NOT_DRAFT';
    end if;
    update public.events
    set name = btrim(p_name), description = coalesce(p_description, ''), event_type = p_event_type,
      starts_at = p_starts_at, ends_at = p_ends_at, timezone_id = btrim(p_timezone_id),
      address_text = btrim(p_address_text), latitude = p_latitude, longitude = p_longitude,
      expected_attendance = p_expected_attendance, recovery_target_percent = p_recovery_target_percent,
      status = 'ACTIVE', archived_at = null, updated_at = now()
    where id = p_event_id
    returning * into event_row;
  end if;

  response := jsonb_build_object('event', to_jsonb(event_row), 'replayed', false);
  return public.finish_idempotent_command('publish_event', p_idempotency_key, response);
end;
$$;

create or replace function public.update_active_event(
  p_event_id uuid,
  p_name text,
  p_description text,
  p_event_type public.event_type,
  p_starts_at timestamptz,
  p_ends_at timestamptz,
  p_timezone_id text,
  p_address_text text,
  p_latitude numeric,
  p_longitude numeric,
  p_expected_attendance integer,
  p_recovery_target_percent numeric,
  p_idempotency_key uuid
)
returns jsonb
language plpgsql security definer set search_path = public, extensions
as $$
declare
  actor_id uuid := public.require_verified_actor();
  event_row public.events;
  replay jsonb;
  request_value jsonb := jsonb_build_object(
    'event_id', p_event_id, 'name', p_name, 'description', p_description,
    'event_type', p_event_type, 'starts_at', p_starts_at, 'ends_at', p_ends_at,
    'timezone_id', p_timezone_id, 'address_text', p_address_text,
    'latitude', p_latitude, 'longitude', p_longitude,
    'expected_attendance', p_expected_attendance,
    'recovery_target_percent', p_recovery_target_percent
  );
  response jsonb;
begin
  replay := public.begin_idempotent_command('update_active_event', p_idempotency_key, request_value);
  if replay is not null then return replay; end if;
  if not public.has_role('ORGANIZER') then
    raise exception using errcode = '42501', message = 'ORGANIZER_ROLE_REQUIRED';
  end if;
  perform public.validate_event_publication(
    p_name, p_description, p_event_type, p_starts_at, p_ends_at, p_timezone_id,
    p_address_text, p_latitude, p_longitude, p_expected_attendance, p_recovery_target_percent
  );
  select * into strict event_row from public.events where id = p_event_id for update;
  if event_row.owner_id is distinct from actor_id then
    raise exception using errcode = '42501', message = 'EVENT_OWNER_REQUIRED';
  end if;
  if event_row.status <> 'ACTIVE' then
    raise exception using errcode = '55000', message = 'EVENT_NOT_ACTIVE';
  end if;
  perform set_config('reevent.event_lifecycle_rpc', 'on', true);
  update public.events
  set name = btrim(p_name), description = coalesce(p_description, ''), event_type = p_event_type,
    starts_at = p_starts_at, ends_at = p_ends_at, timezone_id = btrim(p_timezone_id),
    address_text = btrim(p_address_text), latitude = p_latitude, longitude = p_longitude,
    expected_attendance = p_expected_attendance, recovery_target_percent = p_recovery_target_percent,
    updated_at = now()
  where id = p_event_id
  returning * into event_row;
  response := jsonb_build_object('event', to_jsonb(event_row), 'replayed', false);
  return public.finish_idempotent_command('update_active_event', p_idempotency_key, response);
end;
$$;

create or replace function public.complete_event(
  p_event_id uuid,
  p_idempotency_key uuid
)
returns jsonb
language plpgsql security definer set search_path = public, extensions
as $$
declare
  actor_id uuid := public.require_verified_actor();
  event_row public.events;
  replay jsonb;
  response jsonb;
begin
  replay := public.begin_idempotent_command(
    'complete_event', p_idempotency_key, jsonb_build_object('event_id', p_event_id)
  );
  if replay is not null then return replay; end if;
  if not public.has_role('ORGANIZER') then
    raise exception using errcode = '42501', message = 'ORGANIZER_ROLE_REQUIRED';
  end if;
  select * into strict event_row from public.events where id = p_event_id for update;
  if event_row.owner_id is distinct from actor_id then
    raise exception using errcode = '42501', message = 'EVENT_OWNER_REQUIRED';
  end if;
  if event_row.status = 'COMPLETED' then
    response := jsonb_build_object('event', to_jsonb(event_row), 'replayed', true);
    return public.finish_idempotent_command('complete_event', p_idempotency_key, response);
  end if;
  if event_row.status <> 'ACTIVE' then
    raise exception using errcode = '55000', message = 'EVENT_NOT_ACTIVE';
  end if;
  if exists (
    select 1 from public.circular_transactions
    where origin_event_id = p_event_id
      and status not in ('COMPLETED', 'REJECTED', 'CANCELLED')
  ) then
    raise exception using errcode = '55000', message = 'EVENT_HAS_OPEN_TRANSACTIONS';
  end if;
  perform set_config('reevent.event_lifecycle_rpc', 'on', true);
  update public.events set status = 'COMPLETED', updated_at = now()
  where id = p_event_id returning * into event_row;
  response := jsonb_build_object('event', to_jsonb(event_row), 'replayed', false);
  return public.finish_idempotent_command('complete_event', p_idempotency_key, response);
end;
$$;

create or replace function public.archive_event(
  p_event_id uuid,
  p_idempotency_key uuid
)
returns jsonb
language plpgsql security definer set search_path = public, extensions
as $$
declare
  actor_id uuid := public.require_verified_actor();
  event_row public.events;
  replay jsonb;
  response jsonb;
begin
  replay := public.begin_idempotent_command(
    'archive_event', p_idempotency_key, jsonb_build_object('event_id', p_event_id)
  );
  if replay is not null then return replay; end if;
  if not public.has_role('ORGANIZER') then
    raise exception using errcode = '42501', message = 'ORGANIZER_ROLE_REQUIRED';
  end if;
  select * into strict event_row from public.events where id = p_event_id for update;
  if event_row.owner_id is distinct from actor_id then
    raise exception using errcode = '42501', message = 'EVENT_OWNER_REQUIRED';
  end if;
  if event_row.status = 'ARCHIVED' then
    response := jsonb_build_object('event', to_jsonb(event_row), 'replayed', true);
    return public.finish_idempotent_command('archive_event', p_idempotency_key, response);
  end if;
  if exists (
    select 1 from public.circular_transactions
    where origin_event_id = p_event_id
      and status not in ('COMPLETED', 'REJECTED', 'CANCELLED')
  ) then
    raise exception using errcode = '55000', message = 'EVENT_HAS_OPEN_TRANSACTIONS';
  end if;
  perform set_config('reevent.event_lifecycle_rpc', 'on', true);
  update public.events set status = 'ARCHIVED', archived_at = now(), updated_at = now()
  where id = p_event_id returning * into event_row;
  response := jsonb_build_object('event', to_jsonb(event_row), 'replayed', false);
  return public.finish_idempotent_command('archive_event', p_idempotency_key, response);
end;
$$;

create or replace function public.list_discoverable_events()
returns table (
  id uuid,
  name text,
  description text,
  event_type public.event_type,
  starts_at timestamptz,
  ends_at timestamptz,
  timezone_id text,
  address_text text,
  recovery_target_percent numeric
)
language sql stable security definer set search_path = public
as $$
  select event.id, event.name, event.description, event.event_type,
    event.starts_at, event.ends_at, event.timezone_id, event.address_text,
    event.recovery_target_percent
  from public.events event
  where auth.uid() is not null
    and public.current_profile_role() is not null
    and event.status = 'ACTIVE'
    and event.archived_at is null
  order by event.starts_at asc, event.id asc;
$$;

revoke all on function public.enforce_event_lifecycle_mutation() from public;
revoke all on function public.validate_event_publication(
  text, text, public.event_type, timestamptz, timestamptz, text, text,
  numeric, numeric, integer, numeric
) from public;
revoke all on function public.publish_event(
  uuid, text, text, public.event_type, timestamptz, timestamptz, text, text,
  numeric, numeric, integer, numeric, uuid
) from public;
revoke all on function public.update_active_event(
  uuid, text, text, public.event_type, timestamptz, timestamptz, text, text,
  numeric, numeric, integer, numeric, uuid
) from public;
revoke all on function public.complete_event(uuid, uuid) from public;
revoke all on function public.archive_event(uuid, uuid) from public;
revoke all on function public.list_discoverable_events() from public;

grant execute on function public.publish_event(
  uuid, text, text, public.event_type, timestamptz, timestamptz, text, text,
  numeric, numeric, integer, numeric, uuid
) to authenticated;
grant execute on function public.update_active_event(
  uuid, text, text, public.event_type, timestamptz, timestamptz, text, text,
  numeric, numeric, integer, numeric, uuid
) to authenticated;
grant execute on function public.complete_event(uuid, uuid) to authenticated;
grant execute on function public.archive_event(uuid, uuid) to authenticated;
grant execute on function public.list_discoverable_events() to authenticated;
