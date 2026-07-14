# LIEW KAIY BIN - Core Platform and Account Track

**Owns:** project foundation, shared data contracts, local storage, backend/auth integration, role routing, and final integration of cross-cutting changes.

**Current progress:** `[####------] 40%` - a Compose app, Material 3 theme, launcher icon, and manual screen routing exist. The project has no Navigation Compose, Hilt, Room, DataStore, Retrofit/Ktor, authentication backend, or role persistence.

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

### Checkpoint 1 - Stable shared contracts `[ ]`

- [ ] Pull the latest branch and inspect the current UI before changing shared code.
- [ ] Add Navigation Compose and replace the manual `rememberSaveable` route with typed destinations.
- [ ] Define the shared domain models from the main plan: `User`, `Event`, `ResourceItem`, `ResourcePassport`, `CircularProgramme`, `CircularTransaction`, and `ImpactRecord`.
- [ ] Define repository interfaces that feature tracks can use without knowing whether data comes from Room, a fake repository, or the backend.
- [ ] Provide realistic seeded fake data so other tracks can build in parallel before remote sync is ready.
- [ ] Publish the exact repository methods and navigation routes in the `Coordination Contract` section below.

**Completion evidence:** the app compiles, all existing screens still navigate, and each feature track can obtain its data through an interface rather than importing `MockData` directly.

### Checkpoint 2 - Local-first persistence `[ ]`

- [ ] Add Hilt dependency injection.
- [ ] Add Room and create the database, DAOs, entities, and migrations for the shared models.
- [ ] Add DataStore for selected role, onboarding completion, session cache, and last opened event.
- [ ] Implement repositories that use Room first and fall back to fake data only in a deliberate demo mode.
- [ ] Add basic repository/DAO tests.

**Completion evidence:** create/read/update flows survive an app restart in a local test or emulator; no feature screen writes directly to a DAO.

### Checkpoint 3 - Account and role routing `[ ]`

- [~] Keep the existing onboarding and sign-in UI, but connect its controls to real state.
- [ ] Implement sign-up, sign-in, sign-out, and session restore using the selected backend.
- [ ] Persist the selected role locally.
- [ ] Route organiser, participant, and partner users to their intended starting destination.
- [ ] Add loading, empty, error, and signed-out states.
- [ ] Test role routing and session restoration.

**Completion evidence:** three test accounts can sign in, close/reopen the app, and enter the correct role experience.

### Checkpoint 4 - Remote integration and release integration `[ ]`

- [ ] Add Retrofit/Ktor or the selected Supabase/Firebase client behind repository interfaces.
- [ ] Implement only the API calls required by the agreed MVP flow before adding optional endpoints.
- [ ] Store secrets in local configuration; never commit API keys or credentials.
- [ ] Coordinate migrations and dependency additions before merging feature branches.
- [ ] Run a clean debug build and the available automated tests after each cross-track merge.

**Completion evidence:** the agreed MVP data can sync safely, failure states are visible, and the app still has a usable offline/demo fallback.

## Coordination Contract

Update this table whenever a shared contract changes. Feature teammates should pull after this section changes.

| Contract | Consumer | Status / notes |
|---|---|---|
| `AuthRepository` and current user/role state | WONG JIE YING, MAH JUIN HONG, WONG LOONG JIE | `[ ]` |
| `EventRepository` and `ResourceRepository` | WONG JIE YING, WONG LOONG JIE | `[ ]` |
| `PartnerRepository` and `TransactionRepository` | MAH JUIN HONG, WONG LOONG JIE | `[ ]` |
| `ImpactRepository` | WONG LOONG JIE | `[ ]` |
| Navigation routes and argument rules | WONG JIE YING, MAH JUIN HONG, WONG LOONG JIE | `[ ]` |

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
