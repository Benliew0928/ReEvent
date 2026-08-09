-- A repeat of a completed command must expose that it was served from the
-- persisted idempotency record. The original response remains immutable;
-- only the response returned to the retrying caller is marked as replayed.

create or replace function public.begin_idempotent_command(
  command_name text,
  command_key uuid,
  command_request jsonb
)
returns jsonb
language plpgsql security definer set search_path = public, extensions
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

  return jsonb_set(existing_record.response_json, '{replayed}', 'true'::jsonb, true);
end;
$$;
