# LIEW KAIY BIN Guide - Current Implementation and Staging Handoff

## Purpose

This guide records the real-app foundation owned by LIEW KAIY BIN: Supabase authentication, fixed server-authorised roles, Room/DataStore offline support, repository contracts, typed navigation, and shared Supabase synchronisation. It is not a local-only prototype guide.

## Implemented in the Android app

- Email/password authentication, password-reset request, browser-based Google OAuth, session restoration, sign-out, and one-time role completion.
- Separate organiser, participant, and partner navigation graphs. An account role is stored in Supabase and cannot be changed in the app.
- Room v2 cache and persistent outbox scoped to the active account, plus DataStore preferences and WorkManager sync retry.
- Repository-backed Supabase/Room data paths for events, resources, passports, programmes, transactions, impact records, and owner-scoped media uploads.
- Original Compose visual layouts are kept through `RestoredVisualLiveScreens`; dashboards, marketplace, passports, impact, programme and transaction views use repository data. `MockData` is now deprecated preview-only fixture data and no longer a runtime fallback.
- Deterministic MVP partner matching based on material, resource state, programme type, and availability.

## Applied staging SQL (30 July 2026)

The ReEvent staging project is `kxkdugzyjmoteguesoti`. It was restored and healthy before the database changes below were applied.

1. [0003_public_passport_read.sql](C:/MobileApp/ReEvent/supabase/migrations/0003_public_passport_read.sql) was applied. It recreates the read-only passport rule and is now safe to run repeatedly.
2. [0004_marketplace_resource_visibility.sql](C:/MobileApp/ReEvent/supabase/migrations/0004_marketplace_resource_visibility.sql) was applied. It grants authenticated users read access only to published marketplace resources and their read-only passports; it does not grant cross-user write access.
3. The SQL Editor returned success for both migrations. The production deployment process should apply the same versioned files through its controlled migration workflow; do not copy dashboard query history between environments.

## Repeatable staging seed data

The seed script creates one stable showcase event, published resource, passport, programme, transaction, and impact record. It uses fixed record IDs, so running it again updates the same records rather than duplicating them. It does not create Auth users and it never contains credentials. It was applied to staging on 30 July 2026.

The current script already targets verified staging accounts: organiser `44c85b75-3c99-4220-a7b3-c4f4736e2b6a`, participant `9c5ffe1d-6001-4ccb-938d-e59040d94dcb`, and partner `0459b23e-57d2-481f-9808-90a5dd59f9cb`.

For a new staging project only:

1. In Supabase Dashboard, open **Table Editor** -> `profiles`.
2. Find one deliberate staging test account for each role: `ORGANIZER`, `PARTICIPANT`, and `PARTNER`.
3. Replace the three UUIDs in [staging_seed.sql](C:/MobileApp/ReEvent/supabase/seeds/staging_seed.sql) with those profile IDs, then run it once in **SQL Editor**.

The three UUIDs belong only to the fixed staging demo accounts. Regular users do not need to be added to this script: when they register, Supabase automatically creates their own profile, and normal app data is created through the Android app and protected by RLS.

## Known limitations outside this track

- A real encoded QR bitmap and QR scanner are not implemented; the passport currently has a persistent payload with a visual QR placeholder.
- Marketplace search/filter controls, approval/completion workflow, full event/programme CRUD, real map coordinates/provider, and automatic per-channel impact calculations remain feature-track work.
- The impact visual has real totals and recovery rate but not a complete recovery-channel breakdown.
- No AppGallery release, production SMTP, public domain, or production privacy-policy release work is included yet by the agreed scope.

## Current handoff locations

- Core ownership/status: [LIEW_KAIY_BIN_CORE_ACCOUNT_TRACK.md](C:/MobileApp/docs/LIEW_KAIY_BIN_CORE_ACCOUNT_TRACK.md)
- Shared full-app status: [REEVENT_FULL_APP_DEVELOPMENT_PLAN.md](C:/MobileApp/REEVENT_FULL_APP_DEVELOPMENT_PLAN.md)
- Runtime visual data adapters: [RestoredVisualLiveScreens.kt](C:/MobileApp/ReEvent/app/src/main/java/com/reevent/app/ui/screens/RestoredVisualLiveScreens.kt)
- Staging migrations and seed: [supabase](C:/MobileApp/ReEvent/supabase)

## Verification status

The staging seed verification query returned exactly one row for each expected entity: event, resource, passport, programme, transaction, and impact record. Automated/manual test expansion was intentionally not performed in this implementation pass. `:app:compileDebugKotlin --no-daemon --console=plain` passed after the visual/mock-isolation changes; the build emitted only existing deprecation warnings for the legacy visual adapter enum.
