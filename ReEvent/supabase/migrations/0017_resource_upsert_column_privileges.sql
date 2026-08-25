-- Generic Android sync uses PostgREST upsert so an interrupted create can be replayed safely.
-- PostgreSQL requires UPDATE privilege for every supplied conflict-update column, including
-- immutable fields. The lifecycle trigger and RLS policies remain the enforcement boundary.
grant update (
  origin_event_id,
  created_by,
  current_owner_id,
  unit
) on public.resource_items to authenticated;
