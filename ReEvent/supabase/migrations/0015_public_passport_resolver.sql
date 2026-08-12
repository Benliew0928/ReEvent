-- Anonymous QR verification is intentionally narrower than the authenticated passport view.
-- It accepts only an opaque v1 public token and returns no identifiers, ownership, location,
-- transaction, actor, or private-note data.
create or replace function public.resolve_public_passport(p_token text)
returns table (
  title text,
  category text,
  material text,
  condition public.resource_condition,
  resource_status public.resource_status,
  latest_summary text,
  latest_occurred_at timestamptz
)
language sql
stable
security definer
set search_path = public
as $$
  select
    resource.title,
    resource.category,
    resource.material,
    resource.condition,
    resource.status as resource_status,
    latest_event.public_summary as latest_summary,
    latest_event.occurred_at as latest_occurred_at
  from public.resource_passports as passport
  join public.resource_items as resource on resource.id = passport.resource_id
  left join lateral (
    select event.public_summary, event.occurred_at
    from public.passport_events as event
    where event.passport_id = passport.id
    order by event.occurred_at desc, event.id desc
    limit 1
  ) as latest_event on true
  where p_token ~ '^[A-Za-z0-9_-]{22}$'
    and passport.public_token = p_token
    and passport.token_status = 'ACTIVE'
    and resource.status in ('ACTIVE', 'RECOVERY_IN_PROGRESS', 'RECOVERED');
$$;

revoke all on function public.resolve_public_passport(text) from public;
grant execute on function public.resolve_public_passport(text) to anon, authenticated;
