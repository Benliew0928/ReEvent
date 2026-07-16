-- STAGING ONLY. Replace all three UUID placeholders with ids from public.profiles, then run once.
-- The INSERT statements are idempotent through their stable test ids and never create auth.users.
do $$
declare
  organiser_id uuid := '44c85b75-3c99-4220-a7b3-c4f4736e2b6a'; -- replace
  participant_id uuid := '9c5ffe1d-6001-4ccb-938d-e59040d94dcb'; -- replace
  partner_id uuid := '0459b23e-57d2-481f-9808-90a5dd59f9cb'; -- replace
  v_event_id uuid := '10000000-0000-0000-0000-000000000001';
  v_resource_id uuid := '20000000-0000-0000-0000-000000000001';
begin
  insert into public.events (id, owner_id, name, description, venue, starts_at, ends_at, status)
  values (v_event_id, organiser_id, 'ReEvent staging showcase', 'Repeatable integration-test event', 'Staging venue', now(), now() + interval '1 day', 'ACTIVE')
  on conflict (id) do update set name = excluded.name, updated_at = now();

  insert into public.resource_items (id, event_id, owner_id, title, category, material, condition, quantity, unit, status, value_cents)
  values (v_resource_id, v_event_id, organiser_id, 'Staging reusable signage', 'Signage', 'Acrylic', 'GOOD', 10, 'items', 'AVAILABLE', 15000)
  on conflict (id) do update set status = 'AVAILABLE', archived = false, updated_at = now();

  insert into public.resource_passports (id, resource_id, qr_payload, history)
  values ('30000000-0000-0000-0000-000000000001', v_resource_id, 'reevent://passport/20000000-0000-0000-0000-000000000001', '[]'::jsonb)
  on conflict (resource_id) do update set updated_at = now();

  insert into public.circular_programmes (id, partner_id, name, programme_type, accepted_materials, location, active)
  values ('40000000-0000-0000-0000-000000000001', partner_id, 'Staging reuse programme', 'REUSE', '["Acrylic","Fabric"]'::jsonb, 'Staging partner hub', true)
  on conflict (id) do update set active = true, updated_at = now();

  insert into public.circular_transactions (id, event_id, resource_id, sender_id, receiver_id, partner_id, transaction_type, status, quantity)
  values ('50000000-0000-0000-0000-000000000001', v_event_id, v_resource_id, participant_id, organiser_id, partner_id, 'RETURN', 'PENDING', 1)
  on conflict (id) do update set status = 'PENDING', updated_at = now();

  insert into public.impact_records (id, event_id, resource_id, transaction_id, material_diverted_kg, emissions_avoided_kg, value_recovered_cents)
  values ('60000000-0000-0000-0000-000000000001', v_event_id, v_resource_id, '50000000-0000-0000-0000-000000000001', 12.5, 21.4, 15000)
  on conflict (id) do update set updated_at = now();
end $$;
