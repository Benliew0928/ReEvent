# Partner map operations

## Configuration and deployment order

1. Apply `supabase/migrations/0019_partner_map_discovery.sql`.
2. Set the `MAPTILER_API_KEY` Edge Function secret and deploy `maptiler-geocode` with JWT verification enabled.
3. Copy `supabase.local.properties.example` to the ignored `supabase.local.properties` file and set the same MapTiler project credential for the Android build.
4. Seed at least one active programme that passes every activation rule and has an exact Malaysian business point before acceptance testing.

The Android key can be extracted from an installed application, so it must be a restricted MapTiler project credential rather than a general secret. Restrict its allowed User-Agent to the case-sensitive substring `ReEvent/1.0`; both MapLibre tile requests and the Edge Function send this identifier. Do not also configure an HTTP-origin restriction for this shared mobile/server key because MapTiler combines restrictions with strict `AND` semantics. Monitor tile and geocoding quota and alert on unusual use.

## Rotation

The credential is intentionally shared across tile rendering and the geocoding proxy. Rotation affects every environment: create and restrict the replacement in MapTiler, update the Supabase secret, redeploy the Edge Function, update each environment's ignored Android properties, rebuild/release Android, verify tiles and forward/reverse geocoding, then revoke the old credential.

## Privacy and attribution

The Edge Function accepts only authenticated requests, restricts results to Malaysia, rate-limits per user, and does not log query text or coordinates. Device coordinates are sent only with the current discovery request and are not persisted. Release privacy disclosures must cover MapTiler location processing and optional approximate device location.

The map and pin editor keep `© MapTiler © OpenStreetMap contributors` visible. A tile/configuration outage must not remove the programme list, published point text, eligibility details, filters, or recovery request path. ReEvent does not calculate directions or routes.
