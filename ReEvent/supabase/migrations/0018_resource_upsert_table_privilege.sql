-- PostgREST plans `INSERT ... ON CONFLICT DO UPDATE` only when the authenticated role has
-- table-level UPDATE permission. Row-level policies and the lifecycle trigger continue to
-- reject unauthorised or lifecycle-changing writes.
grant update on public.resource_items to authenticated;
