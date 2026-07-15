-- Public marketplace resources may expose their passport read-only to authenticated participants.
-- Writes remain restricted to the owning organiser through the original policy.
drop policy if exists passports_owner_read on public.resource_passports;
create policy passports_authorized_read on public.resource_passports
for select to authenticated using (
  exists (
    select 1 from public.resource_items r
    where r.id = resource_id
      and (
        (r.status = 'AVAILABLE' and not r.archived)
        or r.owner_id = auth.uid()
        or public.is_event_owner(r.event_id)
      )
  )
);
