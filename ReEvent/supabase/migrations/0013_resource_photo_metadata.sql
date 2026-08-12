-- Keep one current resource photo while retaining replaced rows as explicit cleanup work until
-- their private Storage objects have actually been removed. Android never writes metadata rows
-- directly; these functions keep ownership, paths, and cleanup state consistent.

create or replace function public.replace_resource_photo(
  p_resource_id uuid,
  p_storage_path text,
  p_mime_type text,
  p_width integer,
  p_height integer,
  p_byte_size integer
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  resource_row public.resource_items;
  next_sort_order integer;
  existing_sort_order integer;
  current_sort_order integer;
  cleanup_paths text[];
begin
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
  if p_storage_path is null or p_storage_path not like
    actor_id::text || '/resources/' || p_resource_id::text || '/%' then
    raise exception using errcode = '22023', message = 'INVALID_RESOURCE_PHOTO_PATH';
  end if;

  -- Android uses one deterministic private object path per resource. Retrying a lost response or
  -- replacing the bytes therefore updates the same metadata row instead of accumulating orphans.
  select sort_order into existing_sort_order
  from public.resource_photos
  where resource_id = p_resource_id and storage_path = p_storage_path;
  select max(sort_order) into current_sort_order
  from public.resource_photos
  where resource_id = p_resource_id;

  if existing_sort_order is not null then
    if existing_sort_order is distinct from current_sort_order then
      raise exception using errcode = '55000', message = 'STALE_RESOURCE_PHOTO_PATH';
    end if;
    update public.resource_photos
    set mime_type = p_mime_type,
        width = p_width,
        height = p_height,
        byte_size = p_byte_size,
        created_at = now()
    where resource_id = p_resource_id and storage_path = p_storage_path;
    select coalesce(array_agg(storage_path order by sort_order), array[]::text[])
    into cleanup_paths
    from public.resource_photos
    where resource_id = p_resource_id and storage_path <> p_storage_path;
    return jsonb_build_object(
      'storage_path', p_storage_path,
      'cleanup_paths', cleanup_paths
    );
  end if;

  select coalesce(max(sort_order) + 1, 0)
  into next_sort_order
  from public.resource_photos
  where resource_id = p_resource_id;

  insert into public.resource_photos(
    resource_id, storage_path, mime_type, width, height, byte_size, sort_order
  ) values (
    p_resource_id, p_storage_path, p_mime_type, p_width, p_height, p_byte_size, next_sort_order
  );

  select coalesce(array_agg(storage_path order by sort_order), array[]::text[])
  into cleanup_paths
  from public.resource_photos
  where resource_id = p_resource_id and storage_path <> p_storage_path;

  return jsonb_build_object(
    'storage_path', p_storage_path,
    'cleanup_paths', cleanup_paths
  );
end;
$$;

create or replace function public.complete_resource_photo_cleanup(
  p_resource_id uuid,
  p_storage_path text
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  resource_row public.resource_items;
  current_sort_order integer;
  target_sort_order integer;
begin
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

  select max(sort_order) into current_sort_order
  from public.resource_photos
  where resource_id = p_resource_id;

  select sort_order into target_sort_order
  from public.resource_photos
  where resource_id = p_resource_id and storage_path = p_storage_path;

  if target_sort_order is null then
    return;
  end if;
  if target_sort_order = current_sort_order then
    raise exception using errcode = '55000', message = 'CURRENT_RESOURCE_PHOTO_CANNOT_BE_CLEANED';
  end if;

  delete from public.resource_photos
  where resource_id = p_resource_id and storage_path = p_storage_path;
end;
$$;

-- Storage SELECT policies can call this narrow helper. The object path is useful only when it is
-- the newest metadata row and the signed-in viewer is already authorised for the resource.
create or replace function public.can_read_resource_photo_object(p_storage_path text)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.resource_photos photo_row
    where photo_row.storage_path = p_storage_path
      and photo_row.sort_order = (
        select max(candidate.sort_order)
        from public.resource_photos candidate
        where candidate.resource_id = photo_row.resource_id
      )
      and public.can_read_resource(photo_row.resource_id)
  );
$$;

revoke insert, update, delete on public.resource_photos from authenticated;

revoke all on function public.replace_resource_photo(uuid, text, text, integer, integer, integer) from public;
grant execute on function public.replace_resource_photo(uuid, text, text, integer, integer, integer) to authenticated;
revoke all on function public.complete_resource_photo_cleanup(uuid, text) from public;
grant execute on function public.complete_resource_photo_cleanup(uuid, text) to authenticated;
revoke all on function public.can_read_resource_photo_object(text) from public;
grant execute on function public.can_read_resource_photo_object(text) to authenticated;

drop policy if exists reevent_storage_read on storage.objects;
create policy reevent_storage_read on storage.objects for select to authenticated using (
  bucket_id in ('resource-photos', 'event-photos', 'partner-logos', 'profile-avatars')
  and (
    (storage.foldername(name))[1] = auth.uid()::text
    or (bucket_id = 'resource-photos' and public.can_read_resource_photo_object(name))
  )
);
