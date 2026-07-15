# LIEW KAIY BIN - Core Platform and Account Track

**Owns:** project foundation, shared data contracts, offline-first local storage, Supabase/auth integration, role routing, and final integration of cross-cutting changes.

**Delivery baseline:** ReEvent is a real, deployable Android app. Use Supabase for cloud authentication and the shared MVP data; use Room and DataStore for secure offline-first behaviour; retain demo data only behind repository interfaces. All changes must remain compatible with Huawei AppGallery release requirements.

**Current progress:** `[#########-] 90%` - Typed runtime role graphs, account-scoped Room v2 cache/outbox, remote Supabase reads/writes, offline sync state, deterministic matching, media upload support, repository-backed live screens, and focused unit tests are implemented. Applying the latest staging SQL, seed data, and real-provider/device acceptance evidence remain.

## Pages and Code Ownership

Primary pages:

- `OnboardingScreen`
- `SignInScreen`
- `ProfileScreen` data and account actions

Primary shared code:

- `MainActivity`
- `ReEventApp`
- app Gradle configuration, manifest permissions, dependency versions
- new `core/model`, `core/data`, `core/database`, `core/network`, and `core/navigation` packages

Do not let another track directly change Gradle, shared models, database migrations, or navigation architecture without coordinating through this track.

## Delivery Order

### Checkpoint 1 - Stable shared contracts `[~]`

- [ ] Pull the latest branch and inspect the current UI before changing shared code.
- [x] Add Navigation Compose and replace runtime manual routes with typed destinations. The live graph passes only record IDs; the retained legacy enum is compile-only support for archived mock/previews.
- [x] Define the shared domain models from the main plan: `User`, `Event`, `ResourceItem`, `ResourcePassport`, `CircularProgramme`, `CircularTransaction`, and `ImpactRecord`.
- [x] Define repository interfaces that feature tracks can use without knowing whether data comes from Room, a fake repository, or the backend.
- [~] Provide repeatable staging seed data in `ReEvent/supabase/seeds/staging_seed.sql`; it needs the three real profile IDs before use.
- [x] Publish repository methods and typed live-route ownership in the coordination contract.

**Completion evidence:** the app compiles, all existing screens still navigate, and each feature track can obtain its data through an interface rather than importing `MockData` directly.

### Checkpoint 2 - Local-first persistence `[~]`

- [x] Add Hilt dependency injection.
- [~] Add Room and create the database, DAOs, entities, and migrations for the shared models. Version 2 scopes every feature cache/outbox row to its authenticated account; a device migration test remains required.
- [x] Add DataStore for selected role, onboarding completion, session cache, and last opened event.
- [x] Implement repositories that use Room first and fall back to fake data only in a deliberate demo mode.
- [~] Add focused unit tests for role routing and matching. DAO/repository integration tests require the current staging test-account credentials and device migration test setup.

**Completion evidence:** create/read/update flows survive an app restart in a local test or emulator; no feature screen writes directly to a DAO.

### Checkpoint 3 - Account and role routing `[~]`

- [~] Keep the existing onboarding and sign-in UI, but connect its controls to real state.
- [~] Implement sign-up, sign-in, sign-out, Google OAuth, password reset, and session restore using Supabase Auth. Code is implemented; real-provider verification needs Supabase/Google configuration.
- [x] Persist the selected role locally and protect remote role assignment through the Supabase `complete_profile_role` RPC.
- [x] Route organiser, participant, and partner users to separate intended starting destinations.
- [x] Add loading, empty, retry/error, sync notice, and signed-out states to the live account/feature graph.
- [~] Test role routing and session restoration in code; real provider/device evidence remains manual staging work.

**Completion evidence:** three test accounts can sign in, close/reopen the app, and enter the correct role experience.

### Checkpoint 4 - Remote integration and release integration `[~]`

- [x] Add the Supabase client and network-constrained local outbox behind repository interfaces.
- [x] Implement the agreed MVP remote reads, local-first writes, outbox delivery, retry state, and owner-scoped media upload path.
- [x] Store secrets in ignored local configuration; never commit API keys or credentials.
- [x] Publish migrations and shared contracts before feature integration.
- [x] Run compile/unit-test verification after the integration change.

**Completion evidence:** the agreed MVP data synchronises safely with Supabase, failure states are visible, offline behaviour is usable, and the release configuration remains suitable for AppGallery submission.

## Coordination Contract

Update this table whenever a shared contract changes. Feature teammates should pull after this section changes.

| Contract | Consumer | Status / notes |
|---|---|---|
| `AuthRepository` and current user/role state | WONG JIE YING, MAH JUIN HONG, WONG LOONG JIE | `[x]` Email/password, Google PKCE, fixed role and session flow. |
| `EventRepository` and `ResourceRepository` | WONG JIE YING, WONG LOONG JIE | `[x]` Room-first reads/writes, Supabase refresh/outbox; routes use event/resource IDs only. |
| `PartnerRepository` and `TransactionRepository` | MAH JUIN HONG, WONG LOONG JIE | `[x]` Programmes, deterministic matching and assigned transaction flows. |
| `ImpactRepository` | WONG LOONG JIE | `[x]` Event-ID impact observation and local-first persistence. |
| Navigation routes and argument rules | WONG JIE YING, MAH JUIN HONG, WONG LOONG JIE | `[x]` Role-specific typed destinations; no cross-role graph is registered. |

## AI Agent Working Rules

Before changing code:

- [ ] Read this tracker, the relevant phase in `../REEVENT_FULL_APP_DEVELOPMENT_PLAN.md`, and the latest files that will be edited.
- [ ] Pull/rebase on the latest shared branch and inspect `git status`; preserve unrelated teammate changes.
- [ ] Announce any change to a public model, repository interface, Gradle dependency, schema, or route before implementing it.

Before marking an item `[x]`:

- [ ] Build and run the affected app path or test it at the appropriate layer.
- [ ] Confirm the work is not only an in-memory state or hard-coded UI value.
- [ ] Confirm loading and failure behaviour is reasonable for an event app.
- [ ] Update this tracker and the matching checklist in the main development plan with evidence.

Status rule: `[x]` means implemented and verified; `[~]` means UI/mock/partially integrated; `[ ]` means no evidence of implementation. Do not mark a task complete merely because its screen exists.

## Handoff and Progress Update

After every finished checkpoint:

1. Update its checkbox and the progress line at the top of this file.
2. Add a dated one-line entry below describing the verified result and test/build command used.
3. Update the corresponding Phase 2 or 3 status in the main plan.
4. Push the branch, notify the team of any contract change, and help resolve integration conflicts before starting the next shared change.

## Change Log

- 2026-07-14 - Tracker created from the audited full development plan.
- 2026-07-14 - Implemented the core architecture, role-isolated navigation, Room/DataStore local state, Supabase/Google auth wiring, SQL schema/RLS migration, and sync outbox. `:app:compileDebugKotlin --no-daemon` passed; cloud/device verification is recorded in `../LIEW KAIY BIN Guide.md`.
- 2026-07-15 - Completed live typed-route migration, account-scoped Room v2 cache/outbox, Supabase RLS snapshot refresh, sync state reconciliation, media upload boundary, deterministic programme matching and repository-backed live screens. `:app:testDebugUnitTest :app:compileDebugKotlin` passed. Apply `0003_public_passport_read.sql`, run the staging seed with real profile IDs, then complete the manual staging matrix in the guide.
