-- ReEvent core platform schema. Run through the Supabase SQL editor or CLI migration tool.
create extension if not exists pgcrypto;

create type public.user_role as enum ('ORGANIZER', 'PARTICIPANT', 'PARTNER');
create type public.resource_status as enum ('DRAFT', 'AVAILABLE', 'RESERVED', 'HANDED_OVER', 'RECOVERED', 'ARCHIVED');
create type public.transaction_status as enum ('PENDING', 'ACCEPTED', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED');

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text not null,
  display_name text not null default '',
  role public.user_role,
  avatar_url text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.events (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null references public.profiles(id),
  name text not null,
  description text not null default '',
  venue text not null default '',
  starts_at timestamptz not null,
  ends_at timestamptz not null,
  status text not null default 'ACTIVE',
  archived boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index events_owner_starts_at_idx on public.events(owner_id, starts_at desc);

create table public.resource_items (
  id uuid primary key default gen_random_uuid(),
  event_id uuid not null references public.events(id) on delete cascade,
  owner_id uuid not null references public.profiles(id),
  title text not null,
  category text not null default '',
  material text not null default '',
  condition text not null,
  quantity integer not null check (quantity >= 0),
  unit text not null,
  status public.resource_status not null default 'DRAFT',
  value_cents bigint not null default 0 check (value_cents >= 0),
  image_urls jsonb not null default '[]'::jsonb,
  archived boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index resource_items_event_updated_idx on public.resource_items(event_id, updated_at desc) where not archived;
create index resource_items_marketplace_idx on public.resource_items(status, updated_at desc) where status = 'AVAILABLE' and not archived;

create table public.resource_passports (
  id uuid primary key default gen_random_uuid(),
  resource_id uuid not null unique references public.resource_items(id) on delete cascade,
  qr_payload text not null unique,
  history jsonb not null default '[]'::jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.circular_programmes (
  id uuid primary key default gen_random_uuid(),
  partner_id uuid not null references public.profiles(id),
  name text not null,
  programme_type text not null,
  accepted_materials jsonb not null default '[]'::jsonb,
  location text not null default '',
  active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index circular_programmes_partner_idx on public.circular_programmes(partner_id, active);

create table public.circular_transactions (
  id uuid primary key default gen_random_uuid(),
  event_id uuid not null references public.events(id),
  resource_id uuid not null references public.resource_items(id),
  sender_id uuid not null references public.profiles(id),
  receiver_id uuid not null references public.profiles(id),
  partner_id uuid references public.profiles(id),
  transaction_type text not null,
  status public.transaction_status not null default 'PENDING',
  quantity integer not null check (quantity > 0),
  archived boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index circular_transactions_actor_idx on public.circular_transactions(sender_id, receiver_id, partner_id, updated_at desc) where not archived;

create table public.impact_records (
  id uuid primary key default gen_random_uuid(),
  event_id uuid not null references public.events(id),
  resource_id uuid references public.resource_items(id),
  transaction_id uuid references public.circular_transactions(id),
  material_diverted_kg numeric not null default 0,
  emissions_avoided_kg numeric not null default 0,
  value_recovered_cents bigint not null default 0,
  calculated_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index impact_records_event_calculated_idx on public.impact_records(event_id, calculated_at desc);

create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  insert into public.profiles (id, email, display_name)
  values (new.id, coalesce(new.email, ''), coalesce(new.raw_user_meta_data ->> 'display_name', split_part(coalesce(new.email, ''), '@', 1)))
  on conflict (id) do nothing;
  return new;
end;
$$;
create trigger on_auth_user_created after insert on auth.users for each row execute procedure public.handle_new_user();

create or replace function public.complete_profile_role(p_role public.user_role)
returns public.profiles
language plpgsql security definer set search_path = public as $$
declare profile_row public.profiles;
begin
  update public.profiles
  set role = p_role, updated_at = now()
  where id = auth.uid() and role is null
  returning * into profile_row;
  if profile_row.id is null then
    raise exception 'Role is already assigned or profile does not exist';
  end if;
  return profile_row;
end;
$$;
grant execute on function public.complete_profile_role(public.user_role) to authenticated;

create or replace function public.is_event_owner(p_event_id uuid)
returns boolean language sql stable security definer set search_path = public as $$
  select exists(select 1 from public.events where id = p_event_id and owner_id = auth.uid());
$$;

create trigger profiles_set_updated_at before update on public.profiles for each row execute procedure public.set_updated_at();
create trigger events_set_updated_at before update on public.events for each row execute procedure public.set_updated_at();
create trigger resources_set_updated_at before update on public.resource_items for each row execute procedure public.set_updated_at();
create trigger passports_set_updated_at before update on public.resource_passports for each row execute procedure public.set_updated_at();
create trigger programmes_set_updated_at before update on public.circular_programmes for each row execute procedure public.set_updated_at();
create trigger transactions_set_updated_at before update on public.circular_transactions for each row execute procedure public.set_updated_at();
create trigger impact_set_updated_at before update on public.impact_records for each row execute procedure public.set_updated_at();

alter table public.profiles enable row level security;
alter table public.events enable row level security;
alter table public.resource_items enable row level security;
alter table public.resource_passports enable row level security;
alter table public.circular_programmes enable row level security;
alter table public.circular_transactions enable row level security;
alter table public.impact_records enable row level security;

revoke update on public.profiles from authenticated;
grant select, insert on public.profiles to authenticated;
grant update (display_name, avatar_url, updated_at) on public.profiles to authenticated;

create policy profiles_self_read on public.profiles for select to authenticated using (id = auth.uid());
create policy profiles_self_insert on public.profiles for insert to authenticated with check (id = auth.uid() and role is null);
create policy profiles_self_update on public.profiles for update to authenticated using (id = auth.uid()) with check (id = auth.uid());

create policy events_owner_all on public.events for all to authenticated using (owner_id = auth.uid()) with check (owner_id = auth.uid());

create policy resources_read on public.resource_items for select to authenticated using (
  (status = 'AVAILABLE' and not archived) or owner_id = auth.uid() or public.is_event_owner(event_id)
);
create policy resources_owner_write on public.resource_items for all to authenticated using (owner_id = auth.uid() and public.is_event_owner(event_id)) with check (owner_id = auth.uid() and public.is_event_owner(event_id));

create policy passports_owner_read on public.resource_passports for select to authenticated using (
  exists(select 1 from public.resource_items r where r.id = resource_id and (r.owner_id = auth.uid() or public.is_event_owner(r.event_id)))
);
create policy passports_owner_write on public.resource_passports for all to authenticated using (
  exists(select 1 from public.resource_items r where r.id = resource_id and r.owner_id = auth.uid())
) with check (
  exists(select 1 from public.resource_items r where r.id = resource_id and r.owner_id = auth.uid())
);

create policy programmes_read on public.circular_programmes for select to authenticated using (active or partner_id = auth.uid());
create policy programmes_partner_write on public.circular_programmes for all to authenticated using (partner_id = auth.uid()) with check (partner_id = auth.uid());

create policy transactions_actor_read on public.circular_transactions for select to authenticated using (sender_id = auth.uid() or receiver_id = auth.uid() or partner_id = auth.uid());
create policy transactions_actor_write on public.circular_transactions for insert to authenticated with check (sender_id = auth.uid() or receiver_id = auth.uid() or partner_id = auth.uid());
create policy transactions_actor_update on public.circular_transactions for update to authenticated using (sender_id = auth.uid() or receiver_id = auth.uid() or partner_id = auth.uid()) with check (sender_id = auth.uid() or receiver_id = auth.uid() or partner_id = auth.uid());

create policy impact_event_owner on public.impact_records for all to authenticated using (public.is_event_owner(event_id)) with check (public.is_event_owner(event_id));

insert into storage.buckets (id, name, public)
values
  ('resource-photos', 'resource-photos', false),
  ('event-photos', 'event-photos', false),
  ('partner-logos', 'partner-logos', false),
  ('profile-avatars', 'profile-avatars', false)
on conflict (id) do nothing;

create policy reevent_storage_read on storage.objects for select to authenticated using (
  bucket_id in ('resource-photos', 'event-photos', 'partner-logos', 'profile-avatars')
  and (storage.foldername(name))[1] = auth.uid()::text
);
create policy reevent_storage_insert on storage.objects for insert to authenticated with check (
  bucket_id in ('resource-photos', 'event-photos', 'partner-logos', 'profile-avatars')
  and (storage.foldername(name))[1] = auth.uid()::text
);
create policy reevent_storage_update on storage.objects for update to authenticated using (
  bucket_id in ('resource-photos', 'event-photos', 'partner-logos', 'profile-avatars')
  and (storage.foldername(name))[1] = auth.uid()::text
) with check (
  bucket_id in ('resource-photos', 'event-photos', 'partner-logos', 'profile-avatars')
  and (storage.foldername(name))[1] = auth.uid()::text
);
create policy reevent_storage_delete on storage.objects for delete to authenticated using (
  bucket_id in ('resource-photos', 'event-photos', 'partner-logos', 'profile-avatars')
  and (storage.foldername(name))[1] = auth.uid()::text
);
