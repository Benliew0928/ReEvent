-- Backfills a profile for authenticated accounts created before the auth trigger was installed.
-- The function never assigns or changes a role; role assignment remains one-time through complete_profile_role.
create or replace function public.ensure_current_profile()
returns public.profiles
language plpgsql
security definer
set search_path = public
as $$
declare
  profile_row public.profiles;
  current_email text;
  current_display_name text;
begin
  if auth.uid() is null then
    raise exception 'An authenticated user is required';
  end if;

  current_email := coalesce(auth.jwt() ->> 'email', '');
  current_display_name := coalesce(
    nullif(auth.jwt() -> 'user_metadata' ->> 'display_name', ''),
    split_part(current_email, '@', 1)
  );

  insert into public.profiles (id, email, display_name)
  values (auth.uid(), current_email, current_display_name)
  on conflict (id) do nothing;

  select * into profile_row
  from public.profiles
  where id = auth.uid();

  if profile_row.id is null then
    raise exception 'The authenticated profile could not be created';
  end if;

  return profile_row;
end;
$$;

revoke all on function public.ensure_current_profile() from public;
grant execute on function public.ensure_current_profile() to authenticated;
