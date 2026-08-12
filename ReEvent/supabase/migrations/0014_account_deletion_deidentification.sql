-- Auth deletion cascades through profiles and de-identifies retained history with ON DELETE
-- SET NULL. Those database-managed updates run inside nested referential-action triggers and
-- must not be mistaken for direct client mutation of immutable records.

create or replace function public.reject_immutable_mutation()
returns trigger
language plpgsql
set search_path = public
as $$
declare
  old_row jsonb;
  new_row jsonb;
begin
  if tg_op = 'UPDATE'
    and tg_table_name = 'passport_events'
    and pg_trigger_depth() > 1 then
    old_row := to_jsonb(old);
    new_row := to_jsonb(new);
    if old_row -> 'actor_id' <> 'null'::jsonb
      and new_row -> 'actor_id' = 'null'::jsonb
      and old_row - 'actor_id' = new_row - 'actor_id' then
      return new;
    end if;
  end if;

  raise exception using errcode = '55000', message = 'IMMUTABLE_RECORD';
end;
$$;

create or replace function public.reject_terminal_transaction_mutation()
returns trigger
language plpgsql
set search_path = public
as $$
declare
  old_row jsonb;
  new_row jsonb;
  actor_column text;
  actor_columns text[] := array[
    'requester_id',
    'sender_id',
    'receiver_id',
    'partner_id',
    'coin_payer_id',
    'coin_payee_id'
  ];
  deidentified boolean := false;
  valid_deidentification boolean := true;
begin
  if tg_op = 'UPDATE' and pg_trigger_depth() > 1 then
    old_row := to_jsonb(old);
    new_row := to_jsonb(new);

    if old_row - actor_columns = new_row - actor_columns then
      foreach actor_column in array actor_columns loop
        if old_row -> actor_column is distinct from new_row -> actor_column then
          if old_row -> actor_column = 'null'::jsonb
            or new_row -> actor_column <> 'null'::jsonb then
            valid_deidentification := false;
            exit;
          end if;
          deidentified := true;
        end if;
      end loop;
      if valid_deidentification and deidentified then
        return new;
      end if;
    end if;
  end if;

  if tg_op = 'DELETE' or old.status in ('COMPLETED', 'REJECTED', 'CANCELLED') then
    raise exception using errcode = '55000', message = 'TERMINAL_TRANSACTION_IMMUTABLE';
  end if;
  return new;
end;
$$;

create or replace function public.enforce_resource_lifecycle_mutation()
returns trigger
language plpgsql
set search_path = public
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
  old_row jsonb;
  new_row jsonb;
  profile_column text;
  profile_columns text[] := array['created_by', 'current_owner_id'];
  deidentified boolean := false;
  valid_deidentification boolean := true;
begin
  if not lifecycle_changed or current_user in ('postgres', 'service_role', 'supabase_admin') then
    return new;
  end if;

  -- A profiles foreign-key action may clear created_by and current_owner_id in separate nested
  -- updates. It may not alter custody, quantity, status, lineage, or any descriptive field.
  if pg_trigger_depth() > 1 then
    old_row := to_jsonb(old);
    new_row := to_jsonb(new);
    if old_row - profile_columns = new_row - profile_columns then
      foreach profile_column in array profile_columns loop
        if old_row -> profile_column is distinct from new_row -> profile_column then
          if old_row -> profile_column = 'null'::jsonb
            or new_row -> profile_column <> 'null'::jsonb then
            valid_deidentification := false;
            exit;
          end if;
          deidentified := true;
        end if;
      end loop;
      if valid_deidentification and deidentified then
        return new;
      end if;
    end if;
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
