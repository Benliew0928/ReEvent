-- An organizer may discard an incomplete local-first event draft. Only events that
-- remain actionable need the complete operational fields required for ACTIVE and
-- COMPLETED states.
alter table public.events
  drop constraint events_active_fields_check,
  add constraint events_active_fields_check check (
    status in ('DRAFT', 'ARCHIVED') or (
      event_type is not null and timezone_id is not null and btrim(timezone_id) <> '' and
      btrim(address_text) <> '' and latitude is not null and expected_attendance is not null
    )
  );
