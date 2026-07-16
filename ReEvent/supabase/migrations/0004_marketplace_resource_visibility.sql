-- Every signed-in user may browse an organiser's published resource without gaining access
-- to its private event, profile, drafts, or write operations. This is intentionally role-agnostic:
-- participant, partner, and organiser accounts all use the same marketplace.
grant select on public.resource_items to authenticated;
grant select on public.resource_passports to authenticated;

drop policy if exists resources_read on public.resource_items;
drop policy if exists resources_marketplace_read on public.resource_items;
create policy resources_marketplace_read on public.resource_items
for select to authenticated
using (
  (status = 'AVAILABLE'::public.resource_status and not archived)
  or owner_id = auth.uid()
  or public.is_event_owner(event_id)
);

-- A published resource's passport is read-only and is needed by the marketplace detail screen.
drop policy if exists passports_authorized_read on public.resource_passports;
create policy passports_authorized_read on public.resource_passports
for select to authenticated using (
  exists (
    select 1
    from public.resource_items r
    where r.id = resource_id
      and (
        (r.status = 'AVAILABLE'::public.resource_status and not r.archived)
        or r.owner_id = auth.uid()
        or public.is_event_owner(r.event_id)
      )
  )
);
