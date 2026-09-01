# ReEvent Event Publication and Stakeholder Visibility Plan

**Status:** Approved — implementation completed in the repository; staging deployment and device acceptance remain pending

**Audit date:** 2026-08-30

**Audited branch/commit:** `main` at `0e9591ffa8c335b9a70844601a8b93802e9d98c5`

**Scope:** Android app, Room cache/outbox, Supabase schema/RLS/RPCs, role navigation, automated tests, and staging rollout

## 1. Approved decision

This approved change provides a controlled event-publication feature in which:

1. An organiser can keep an incomplete event as a private **Draft**.
2. The organiser explicitly selects **Publish event** after completing the required fields.
3. The server, not the phone, authoritatively changes the event to **Active**.
4. Authenticated Participants and Partners can then see a privacy-safe, read-only event listing and event details.
5. The organiser can later mark the event **Completed** or **Archived** using server-validated actions.
6. Publishing an event does **not** automatically publish its resources or expose its private resource inventory.

The recommended defaults in section 13 have been approved. The Android, Room, repository, Supabase migration, navigation, and automated-test changes are implemented in the working tree. This plan does not claim that the migration has been deployed to staging or that device/manual acceptance has passed.

The organiser form was subsequently simplified by approval: event type, timezone, and recovery target are not organiser inputs and are not displayed to stakeholders. Supabase's existing columns remain internal compatibility fields with safe defaults, so this revision adds no columns, enum values, API dependency, or additional migration.

## 2. Direct answer to the reported behaviour

The current behaviour is caused by missing product flow and data contracts, not by the organiser using the screen incorrectly:

- Creating or editing an event always saves a new event with status `DRAFT`.
- There is no Publish button or publish command in the organiser UI.
- Editing an existing event preserves its old status, so saving again cannot change Draft to Active.
- The normal event sync payload does not send `status` or all fields required by the server for an Active event.
- Supabase currently allows only the owning organiser to read an event row.
- Participant and Partner navigation has no event list or event-detail route.

Therefore, the current “blood donation” event is private to its organiser account. Other stakeholders cannot discover it through the app merely because resources were added. This is expected from the current implementation, although the UI does not explain it clearly.

The earlier partner-programme sync error is a separate staging schema issue: the installed app expected the `accepted_material_families` column, but the server schema/cache did not expose it. That error does not cause an event to remain Draft, but it proves that staging migration state must be verified before this feature is deployed.

## 3. Evidence from the current project

| Area | Current source behaviour | Consequence |
|---|---|---|
| Event editor | New events are created with `status = "DRAFT"`; edits preserve the current status | Save cannot publish an event |
| Event domain and Room entity | Status is an untyped string; event type, timezone, expected attendance, and recovery target are absent | Android cannot satisfy the complete server publication contract |
| Outbox event payload | Sends basic event details and coordinates, but not status or the publication-required fields | Ordinary sync cannot convert a Draft into Active |
| Supabase `events` table | Active/Completed rows require event type, timezone, address, latitude, and expected attendance | An incomplete draft must be completed before publication |
| Supabase RLS | Raw event rows are selectable only by the owning organiser | Participants and Partners cannot see Active events |
| Role navigation | Only organisers have event routes | No stakeholder discovery screen exists |
| Event list heading | Displays an “ACTIVE EVENTS” count based on all owned events | A Draft may misleadingly contribute to the active-event label |
| Resource creation | A new resource defaults to Active even while its parent event remains Draft | Event status and resource status are currently independent |
| Marketplace | Publication checks resource/listing state, not parent event status | Publishing an event must not silently change Marketplace behaviour |
| Remote staging | A migration/schema-cache mismatch was observed for partner programmes | Schema preflight is required before adding another migration |

Primary code areas audited:

- `ReEvent/app/src/main/java/com/reevent/app/core/model/CoreModels.kt`
- `ReEvent/app/src/main/java/com/reevent/app/core/database/CoreEntities.kt`
- `ReEvent/app/src/main/java/com/reevent/app/core/database/CoreDao.kt`
- `ReEvent/app/src/main/java/com/reevent/app/core/data/LocalFirstCoreRepository.kt`
- `ReEvent/app/src/main/java/com/reevent/app/core/sync/SyncScheduler.kt`
- `ReEvent/app/src/main/java/com/reevent/app/ui/screens/EventResourceScreens.kt`
- `ReEvent/app/src/main/java/com/reevent/app/feature/events/EventFormValidation.kt`
- `ReEvent/app/src/main/java/com/reevent/app/ui/ReEventApp.kt`
- `ReEvent/app/src/main/java/com/reevent/app/ui/TopLevelDestination.kt`
- `ReEvent/app/src/main/java/com/reevent/app/core/network/EventLifecycleGateway.kt`
- `ReEvent/app/src/main/java/com/reevent/app/ui/screens/EventDiscoveryScreens.kt`
- `ReEvent/supabase/migrations/0005_release_lifecycle_schema.sql`
- `ReEvent/supabase/migrations/0010_marketplace_publication_contract.sql`
- `ReEvent/supabase/migrations/0021_allow_incomplete_event_draft_archival.sql`
- `ReEvent/supabase/migrations/0022_event_publication_discovery.sql`
- `ReEvent/supabase/tests/lifecycle-schema.test.mjs`
- `docs/release/architecture.md`

## 4. Required behavioural contract

### 4.1 Event lifecycle

```text
DRAFT --publish (online/server accepted)--> ACTIVE
ACTIVE --complete (online/server accepted)--> COMPLETED
DRAFT/ACTIVE/COMPLETED --archive--> ARCHIVED
```

Rules:

- Draft creation and draft editing remain local-first and may use the outbox.
- The UI must never show Active until the server accepts the publish command.
- Publish, Active-event edits, Complete, and shared-event Archive are online-only server actions.
- Retrying an already accepted command must be safe and return the authoritative event.
- Invalid transitions are rejected by the server, regardless of client version.
- Completed and Archived events are not included in current event discovery in the first release.
- A Draft may be archived through the existing private-draft flow, provided it remains owner-only.

### 4.2 Publication readiness

The organiser must provide all server-required fields before publication:

- Event name
- Description, when required by the product wording
- Start and end date/time
- Public venue/address
- Valid location coordinates
- Expected attendance

For compatibility with the existing `events_active_fields_check`, publication normalizes hidden values to the existing `COMMUNITY`, `Asia/Kuala_Lumpur`, and `0` representations. These are implementation defaults, not organiser choices.

Recommended rule: resources are **not** a hard prerequisite for publishing an event. If none exist, show a warning and allow publication. Event discovery and resource/Marketplace publication have different purposes and should not be coupled accidentally.

### 4.3 Stakeholder visibility

Keep the raw `events` table owner-only. Do not broaden its RLS policy to all authenticated users.

Provide dedicated server functions for a safe projection, for example:

- `list_discoverable_events(...)`
- `get_discoverable_event(event_id)`

Only Active events are returned. The initial public projection should contain:

- Public event ID
- Name and description
- Start and end time
- Approved public location label
- Approved public coordinates, if map navigation is approved

It must not return:

- Organiser authentication UUID or internal account identifiers
- Expected attendance unless explicitly approved as public
- Attendee identities or participation records
- Resource inventory, quantities, QR data, transactions, or unpublished listings
- Sync metadata, internal failure data, or private audit details

The organiser’s existing event-detail screen remains the private management view. Participant and Partner event details must be separate read-only projections so private organiser controls cannot leak through navigation or caching.

## 5. Proposed implementation

### Phase 0 — Confirm product and privacy decisions

1. Confirm every choice in section 13.
2. Record the accepted public event fields and role access as a shared contract.
3. Confirm ownership of the cross-cutting model, Room, repository, navigation, and Supabase changes before editing them.

**Exit gate:** Written approval of this plan and its selected options.

### Phase 1 — Repair and record staging schema state

1. Inspect the staging project’s actual columns, functions, policies, and migration history.
2. Reconcile the missing partner-programme migration/schema-cache issue through migrations `0020` and `0021` without blindly replaying SQL.
3. Record the applied migration/version evidence and reload the PostgREST schema cache where required.
4. Re-run the existing Supabase contract tests against an isolated test database.

**Exit gate:** Staging and repository schema are proven consistent before migration `0022` is introduced.

### Phase 2 — Add server-authoritative lifecycle and discovery

Create a reviewed migration such as `0022_event_publication_and_discovery.sql` that:

1. Adds owner- and role-validated functions for Publish, Complete, and Archive.
2. Validates allowed state transitions and all publication-required fields.
3. Makes retries idempotent and returns the authoritative event row.
4. Prevents broad direct updates from bypassing lifecycle validation; direct draft writes must remain appropriately constrained.
5. Adds paginated, stable-order discovery functions returning only the approved public fields.
6. Includes explicit grants and revokes; raw owner-only event access remains intact.
7. Does not alter Marketplace or resource-publication rules.

**Exit gate:** SQL contract tests prove lifecycle correctness, role isolation, and projection privacy.

### Phase 3 — Align Android models, Room, gateway, and repositories

1. Replace free-form lifecycle values at application boundaries with typed `EventStatus` and `EventType` representations.
2. Extend the owned-event model, DTO, Room entity, and draft payload with timezone ID, expected attendance, and recovery target.
3. Add a Room `9 -> 10` migration that preserves existing drafts and uses safe nullable/default values until the organiser completes them.
4. Add dedicated repository operations such as `saveDraft`, `publish`, `complete`, and `archive`.
5. Keep shared lifecycle commands out of the offline outbox; only update local status after a successful server response.
6. Add a separate `DiscoverableEventSummary`/detail model and account/environment-scoped cache. Do not reuse private owned-event rows as the public projection.
7. Reconcile discovery refreshes authoritatively so an event removed from server results does not remain visible indefinitely.

**Exit gate:** Unit and migration tests pass, including account isolation and failed-publication behaviour.

### Phase 4 — Complete the organiser lifecycle UI

1. Ask organisers only for expected attendance in addition to the existing event details; do not expose event type, timezone, or recovery target controls.
2. Preserve Supabase compatibility through hidden defaults without creating new schema types or values.
3. Keep **Save draft** and **Publish event** as distinct actions.
4. Before publication, display a checklist and confirmation showing exactly which event information becomes visible.
5. Show Active only after the server responds successfully; a failure leaves the event Draft with a clear retry message.
6. Add status-appropriate actions on event detail: Publish, Complete, and Archive.
7. Fix the event-list heading to use actual status counts or neutral wording such as “Events”.
8. Explain that adding a resource does not publish the event and publishing an event does not list its resources on Marketplace.

**Exit gate:** Compose/UI tests cover draft saving, validation, publication confirmation, server failure, and state-specific controls.

### Phase 5 — Add Participant and Partner discovery

1. Add an Events top-level destination for Participant and Partner roles.
2. Create read-only list and detail screens backed only by the public discovery repository.
3. Support loading, empty, offline/stale, refresh, and unavailable-event states.
4. Ensure no organiser edit, archive, resource, QR, or transaction controls appear.
5. Use a maximum of five top-level destinations per affected role and verify labels at supported font scales.

**Exit gate:** Both roles can view an Active event but cannot retrieve its private fields or invoke organiser actions.

### Phase 6 — Verification and staging acceptance

Run the repository-defined verification commands and add the feature-specific tests in sections 7 and 8.

Perform a three-account staging acceptance test using disposable Organiser, Participant, and Partner accounts:

1. Organiser creates a Draft — only the organiser can see it.
2. Organiser adds a resource — the event remains Draft and undiscoverable.
3. Organiser publishes — the server accepts it and both stakeholder roles can see the approved projection.
4. Stakeholders cannot see inventory, QR data, transactions, or organiser-only controls.
5. Organiser completes or archives it — it disappears from current discovery.
6. Offline publication fails safely and never shows a false Active state.
7. Sign-out/sign-in and account switching do not leak cached event data.

**Exit gate:** Automated checks and the signed staging evidence matrix pass.

### Phase 7 — Release and documentation

1. Deploy the reviewed database migration before releasing an app build that depends on it.
2. Record migration evidence and perform a schema-cache refresh/check.
3. Release behind a controlled rollout or feature gate until staging acceptance is complete.
4. Use forward-fix migration procedures if a production schema issue appears; do not mutate history silently.
5. Update `docs/REEVENT_ASSIGNMENT_PROGRESS.md` and member handoff evidence only after verified implementation and testing.

## 6. Ownership and review boundaries

| Workstream | Required ownership/review |
|---|---|
| Organiser event editor and lifecycle screens | Organiser event/resource lifecycle owner |
| Shared domain models, Room migration, repositories, sync contract | Shared platform/data owner review |
| Supabase migration, lifecycle functions, grants, and RLS | Backend/shared-contract review |
| Participant and Partner routes/navigation | Role-navigation/integration owner review |
| Matching, impact, Marketplace, and resource behaviour | Existing owners verify no regression; no scope transfer implied |

This respects the existing ownership record: organiser event work may lead the UI, but shared models, persistence, repositories, routes, and server contracts must be changed through shared review rather than silently edited as a local screen-only task.

## 7. Required automated tests

### Supabase contract tests

- Owner can create and edit an incomplete Draft.
- Incomplete Draft cannot be published.
- Only an authenticated organiser who owns the event can publish, complete, or archive it.
- Allowed transitions succeed; invalid transitions fail.
- Repeated accepted commands are idempotent.
- Raw event reads remain owner-only.
- Participant and Partner discovery returns Active events only.
- Draft, Completed, and Archived events are excluded.
- The discovery result contains exactly the approved public columns.
- Discovery does not expose resource inventory through joins or related functions.
- Pagination and ordering are deterministic.

### Android unit and Room tests

- Publication-readiness validation for every required field.
- Date conversion remains stable when the device timezone differs from the event timezone.
- Domain/entity/DTO mapping preserves the lifecycle fields.
- Draft payload contains the newly supported draft data.
- Publish/complete/archive errors do not apply false local success.
- Room migration `9 -> 10` preserves existing events.
- Discovery cache is isolated by signed-in account and environment.
- Authoritative refresh removes no-longer-discoverable events.
- Event list counts Draft and Active statuses correctly.

### Compose/navigation tests

- Organiser sees separate Save draft and Publish actions.
- Publication confirmation lists visible fields.
- Participant and Partner see Events navigation and read-only detail.
- Non-organisers never see organiser controls.
- Deep-link/direct-route attempts cannot open the private organiser detail.
- Empty, offline, stale, and removed-event states are usable and accessible.

## 8. Full verification gate

From the Android project (`ReEvent/`):

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug :app:assembleRelease
```

From `ReEvent/supabase/tests/`:

```powershell
npm ci
npm test
```

From the repository root:

```powershell
node scripts/check-encoding.mjs
git diff --check
```

Implementation verification evidence on 2026-08-31:

- `:app:testDebugUnitTest` — passed.
- `:app:compileDebugAndroidTestKotlin` — passed.
- `:app:lintDebug` — passed.
- `:app:assembleDebug :app:assembleRelease` — passed.
- `node --test lifecycle-schema.test.mjs` — 28/28 Supabase contract tests passed.
- `node scripts/check-encoding.mjs` and `git diff --check` — passed.

Device/manual acceptance, staging migration deployment, schema-cache evidence, and the three-account staging matrix remain pending.

## 9. Acceptance criteria

The work is complete only when all of the following are true:

1. A new event stays Draft after ordinary save.
2. Adding resources does not silently change event status.
3. Publish is explicit and requires all approved public fields.
4. The UI shows Active only after server acceptance.
5. Participant and Partner accounts can discover an Active event.
6. They cannot discover Draft, Completed, or Archived events.
7. They cannot see private inventory, QR, transaction, attendee, or internal organiser information.
8. Event publication does not automatically publish resources or Marketplace listings.
9. Offline and server-error cases never report false publication success.
10. Room migration and account switching do not lose or leak data.
11. All automated and manual verification gates pass.
12. Staging migration state and schema-cache evidence are recorded.

## 10. Risks and controls

| Risk | Control |
|---|---|
| Broad RLS change leaks event/private data | Keep raw table owner-only; expose a narrow RPC projection |
| Client bypasses lifecycle rules | Validate transitions and required fields on the server |
| False Active state while offline | Shared lifecycle commands are online-only; update after server response |
| Device timezone changes event time | Store and use explicit IANA timezone IDs |
| Publishing exposes resources unexpectedly | Keep event discovery and Marketplace/resource contracts separate |
| Old Drafts lack new fields | Nullable/default Room migration; require completion only at Publish |
| Remote schema is already behind | Complete staging migration preflight before new migration work |
| Cached event remains visible after archive | Authoritative discovery reconciliation and removed-event handling |
| Scope expands into registration/attendance | Keep first release read-only and enforce non-goals |

## 11. Explicit non-goals for this change

- RSVP, registration, ticketing, attendance, or waitlists
- Blood-donation medical data or eligibility workflows
- Anonymous/public-web event discovery
- Stakeholder editing of organiser events
- Automatic Marketplace listing or resource sharing
- Event chat or notifications
- Exposure of organiser identity beyond separately approved public profile data
- Rewriting unrelated partner-programme, matching, impact, wallet, or transaction features

## 12. Recommended defaults

Unless changed during approval, implementation should use these defaults:

- Audience: authenticated Participants and Partners; organisers retain their private manager view.
- Discovery status: Active only.
- Interaction: read-only; no RSVP or join action.
- Location: public venue/address; coordinates included only when map navigation is approved.
- Expected attendance: private.
- Event type, timezone, and recovery target: hidden compatibility values only; do not display them or ask organisers to manage them.
- Resource requirement: warning only, not a publication blocker.
- Navigation: Events top-level destination for both Participant and Partner.
- Supabase scope: use the schema already defined by `0005` plus the approved publication/discovery migration `0022`; add no further migration for the simplified form.
- Active-event edits and lifecycle transitions: online-only.

## 13. Approval checklist

The approved defaults are recorded below:

- [x] **Audience:** Participants and Partners may discover Active events.
- [x] **Location:** Public venue/address is visible; exact coordinates are not public in this release.
- [x] **Expected attendance:** Keep private.
- [x] **Simplified form:** Do not show event type, timezone, or recovery target; normalize existing Supabase-compatible defaults internally.
- [x] **Resources:** Warn when no resources exist, but do not block publication.
- [x] **Navigation:** Add an Events destination for Participant and Partner roles.
- [x] **Interaction:** Read-only discovery; no RSVP/join in this scope.
- [x] **Completed events:** Hide from current discovery in the first release.
- [x] **Online rule:** Publish, Active edits, Complete, and shared Archive require server confirmation.
- [x] **Migration order:** Repair/verify staging through migration `0021` before creating/deploying `0022`.

Approval recorded: the recommended defaults are approved, with exact coordinates not public in this release. If any checkbox is changed later, update this plan and obtain review of the revised contract before deployment.
