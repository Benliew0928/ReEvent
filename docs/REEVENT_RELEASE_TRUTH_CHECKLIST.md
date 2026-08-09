# ReEvent Release Truth Checklist

**Authority:** This is the single source of truth for ReEvent release completion from 2026-08-09 onward. Older member, deployment, solo-completion, and superpowers progress files are historical references only. Their percentages and completion claims do not count toward release readiness.

**Current decision:** **NO-GO — prototype foundation exists, but the app is not safe or complete enough to release.**

**Current strict progress:** **13 / 168 release items accepted**, with **5 P0** and **9 P1** blockers recorded below. Stage 1 froze the release contracts; Stage 2A established the account-safe Room cache; Stage 2B bound outbox execution and sign-out cleanup to the current account and typed local environment. Stage 3 is captured in reviewable commits `3721aa5efe0bfc597e4a2e02c0fee19bb561ec78`, `68e82033c45020e48d08db43259a04c86ae652a6`, and `ade8d5a3f16b04a16552e8982f7360bd373dba0e`: they implement the frozen server-authoritative lifecycle, atomic completion, durable RPC idempotency, Room 5 projections, the Supabase `pgcrypto` compatibility fix, and truthful replay responses. Official staging SQL-actor evidence plus real, separate signed staging sessions now pass, including a discarded completion response followed by an exact-key retry. The debug app is installed on an API-35 emulator but the same flow has not yet been completed through separate Android app/device sessions, so no release item is accepted yet. This is intentionally stricter than feature-presence progress: existing partial functionality is described in Section 3 and earns acceptance only after its complete release scenario is evidenced.

**Target:** A genuinely functioning Android product, companion legal/support website, Supabase backend, and Huawei AppGallery release. “Functioning” means that data is real, cross-role workflows work across separate accounts and devices, permissions are enforced on the server, offline/sync behavior is honest, every visible action has an outcome, and every release claim has reproducible evidence.

---

## 1. How this file must be used

### 1.1 The 100% rule

A release item is any checkbox whose identifier starts with `REL-`.

- `[ ]` means not accepted, even if implementation has started.
- `[x]` means the acceptance criteria passed and its evidence is recorded in Section 15.
- Source code, a pretty screen, a successful preview, or a developer saying “done” is not evidence by itself.
- Partial credit is recorded in the current-state tables, never by checking a release item early.
- A failed or skipped required test keeps its item unchecked.
- An item may be removed only through an explicit scope decision recorded in the decision log. The app, website, store copy, privacy policy, demo, and assignment report must then stop promising that feature.
- ReEvent is **release-candidate complete** only when every required `REL-` item except the production-submission/publication items is checked, all automated checks pass, all staging final-acceptance scenarios in Section 15 pass, and there are zero unresolved P0/P1 defects.
- ReEvent is **released complete (100%)** only when the release-candidate criteria pass, the production backend and public website are operational, AppGallery open testing passes, and the production submission is accepted or published.

Progress must be reported as `accepted REL items / all REL items`, followed by the number of unresolved P0/P1 defects. Do not invent a subjective percentage.

PowerShell progress command, run from `C:\MobileApp`:

```powershell
$items = Select-String -Path .\docs\REEVENT_RELEASE_TRUTH_CHECKLIST.md -Pattern '^- \[[ x]\] \*\*REL-'
$done = $items | Where-Object { $_.Line -match '^- \[x\]' }
"$($done.Count) / $($items.Count) release items accepted"
```

### 1.2 Evidence rule

Every checked item must have one evidence row containing:

1. the `REL-` identifier;
2. the exact command or manual scenario;
3. the device, API level, account role, and backend environment where relevant;
4. the result and date;
5. a durable path or URL to the report, screenshot/video, log, signed artifact, migration, or store record;
6. the commit SHA that was tested.

Secrets, passwords, signing keys, service-role keys, and personal data must never be placed in this file or evidence artifacts.

### 1.3 Environments

The app must use explicit, non-interchangeable environments:

| Environment | Purpose | Data rule |
|---|---|---|
| Local/test | Unit and instrumentation tests | Disposable fakes or isolated test database |
| Staging | Multi-account E2E, AppGallery open testing | Synthetic test accounts and removable seed data |
| Production | Public release | No demo fallback, no seed reset, least-privilege access |

---

## 2. Product target that 100% represents

### 2.1 Product promise

ReEvent helps event organisers inventory reusable event resources, issue digital resource passports, match resources to circular recovery options, transfer them through participants or recovery partners, and report traceable environmental and financial outcomes. Participants can discover and request available resources and return checked-out resources. Circular partners can publish programmes and complete approved recovery handovers.

This is a circular-resource operations product, not only a UI prototype and not an unverified “AI sustainability” demo.

### 2.2 Mandatory release journey

The same staging build must prove this entire journey with three independent accounts:

1. An organiser signs up, verifies the account, creates an event with a real date and location, and adds a photographed resource.
2. ReEvent creates one persistent digital passport and a real, decodable QR payload for that resource.
3. A participant scans the code on a second physical device, sees only authorised public data, and requests an allowed marketplace action.
4. The organiser approves the request; both accounts observe the same server-authoritative status.
5. The participant checks out and returns the resource, or the organiser selects a matching circular partner and creates a recovery handover.
6. The receiving actor confirms completion. The server atomically updates the transaction, resource, passport history, and impact record exactly once.
7. The organiser sees a quantity-aware impact dashboard whose factors, units, scope, and limitations are disclosed.
8. The app is killed, restarted, taken offline, brought online, and signed into a different account without leaking data or corrupting the workflow.

### 2.3 Required roles

| Role | Required authority |
|---|---|
| Organiser | Own events/resources/passports; publish listings; approve requests; initiate partner recovery; view owned-event impact |
| Participant | Browse authorised listings/passports; request an action; confirm only their own handover steps; return assigned resources |
| Circular partner | Manage own recovery programmes; receive approved handovers; confirm only their own processing steps |

A role displayed in the client is not a security boundary. Supabase policies, constraints, and transaction functions must enforce these authorities.

### 2.4 Explicitly outside the 1.0 release

The following are not required for 100% and must not be promised in store copy or active controls:

- real-money payment processing;
- courier dispatch or live logistics tracking;
- in-app chat;
- NFC tags;
- unconstrained generative-AI recommendations;
- a web administration console;
- social feeds, large guided tours, or enterprise analytics;
- reward redemption with monetary value.

Simple earned badges may remain only if they are derived from real completed activity and are described as recognition, not redeemable rewards.

---

## 3. Audited current state versus release target

The audit covered the Android source, resources, Gradle configuration, Room layer, Supabase migrations and seed, unit/instrumentation tests, design assets, three planning documents, the group-assignment DOCX, and the separate `ReEventWebsite` repository.

| Area | Current reality on 2026-08-09 | Release target | Status |
|---|---|---|---|
| Product shell | Typed role navigation, Compose screens, Hilt, Room, DataStore, WorkManager, Supabase | One coherent tested product journey; no legacy/no-op paths | Partial |
| Authentication | Email sign-in/sign-up/logout and a reset-email call exist; local demo fallback exists | Verified email and recovery deep link; update-password UI; no production fallback; account deletion | Partial |
| Events | Basic create/edit/list/detail/archive | Valid dates, type, attendance, coordinates/address, sustainability target and progress | Partial |
| Resources | CRUD, search/filter, local drafts and photo selection/upload foundations | Valid quantity/condition/status, safe media lifecycle, location, complete archive/delete behavior | Partial |
| Passport | Persistent passport model and real ZXing generation exist | Versioned payload, privacy-safe public view, immutable lifecycle history, external/app-link resolution | Partial |
| QR scan | CameraX + ML Kit scanner code exists | Physical-device round trip, server lookup on cache miss, idempotent authorised actions | Partial |
| Participant return | Displays `FakeQrPanel`, a hand-drawn non-QR grid | The actual resource/passport QR with a real assigned return workflow | Missing/blocking |
| Marketplace | Browse/search/filter and request/approve/transit/complete UI/data paths exist | Borrow/rent/buy/donate/exchange rules, server-authoritative actors and transitions, multi-account E2E | Partial/blocking |
| Partner programmes | Simple programme editor/workbench exists | Complete eligibility, capacity, reward/fee, pickup, processing, terms, coordinates and status | Partial |
| Partner map | Static `map_partner_mock.png`; accept callback is empty | Live vendor-neutral map, real markers/distance/detail/action, list fallback and attribution | Missing/blocking |
| Matching | Honest deterministic matching by a few fields | Quantity, condition, material, distance/capacity/programme rules, ranked explanation and alternatives | Partial |
| Impact | Completed-transaction aggregation and a narrow documented factor exist | Quantity-aware, idempotent, factor-provenanced results across supported actions; clear limitations | Partial |
| Offline/sync | Local-first outbox and WorkManager are account/environment-bound; stale workers no-op and sign-out purges the selected local partition | Fair retries, conflict/removal handling, visible status and recovery | Partial/blocking |
| Security | RLS exists but important policies are client-trusting or ownership-incompatible | Atomic server functions, role/actor checks, legal transitions, media access, adversarial tests | Blocking |
| Tests | 22 JVM, 14 API 35 instrumentation, and 15 embedded-PostgreSQL lifecycle contract tests pass, including Room 1→5 migrations, account isolation, durable RPC retry, adversarial actor/state/quantity checks, replay and atomic rollback; broad UI/staging E2E/accessibility coverage is still absent | Unit, repository, official Supabase policy-contract, UI, navigation, restoration, E2E, accessibility and release suites | Partial/blocking |
| Android build | Debug APK builds, local unit/device tests pass, and debug lint has zero errors with 28 warnings | Zero lint errors, reviewed warnings, signed/reproducible release AAB/APK, shrinker checked, release device matrix passed | Partial/blocking |
| Website | Landing/privacy/terms/support/delete pages and a successful direct build exist | Cross-platform scripts/tests, real contacts and deletion process, deployed public URLs | Partial/blocking |
| AppGallery | Planning/reference material only | Verified account, assets, privacy URL, open test, signed upload and accepted production release | Missing |

### 3.1 Current automated evidence

- `:app:testDebugUnitTest` passes 22 JVM tests in the Stage 3 worktree. The obsolete client-side impact-record factory tests were removed because impact creation is now exclusively server-authoritative.
- A debug APK exists at `ReEvent/app/build/outputs/apk/debug/app-debug.apk` and was approximately 69.6 MB during the audit.
- `:app:connectedDebugAndroidTest` passes 14 tests on the Android Studio API 35 `Medium_Phone` AVD, including all Room 1→5 migration chains, account/environment isolation, scoped cleanup, and durable lifecycle-command replay with the same idempotency UUID.
- `npm test` in `ReEvent/supabase/tests` passes 15 disposable embedded-PostgreSQL contract tests covering schema/grants, resource lifecycle tamper rejection, request/decision rules, programme capacity, ReCoin holds/settlement, all completion families, replay, and total rollback.
- `:app:lintDebug` passes with zero errors and 28 warnings. The prior API-27 style, CameraX opt-in, and optional-camera feature errors are fixed; warning review remains a release gate.
- The website linter passes and a platform-neutral direct build succeeds.
- The website's two rendered-HTML tests fail because they still assert deleted starter-template content.
- A real signed staging REST/RPC run now proves organiser/participant RENT plus partner role onboarding, exact-once completion, and response-loss retry; it is recorded in `docs/release/evidence/backend/stage-3-server-authoritative-lifecycle.md`. It is not Android UI/device E2E acceptance evidence.
- No authenticated three-account Android/device E2E, physical-camera QR run, live map run, offline conflict run, signed release build, or AppGallery test has been accepted.

---

## 4. P0/P1 defects that prevent a truthful release

| Priority | Defect | Evidence/location | Required outcome |
|---|---|---|---|
| P0 | Stage 3 least-privilege transaction authority has not been accepted on official Supabase staging | `0005`–`0007`; `docs/release/evidence/backend/stage-3-server-authoritative-lifecycle.md` | Apply the constrained RPC/grant/RLS boundary to staging and pass independent wrong-role/wrong-actor adversarial calls at a tested commit |
| P0 | Cross-account participant scan/return has not passed on staging devices | typed `LifecycleCommandGateway`, Stage 3 evidence | Server-authoritative lifecycle commands succeed for the authorised actor across separate sessions/devices and rejected calls change nothing |
| P0 | Atomic lifecycle rollback is proven locally but not yet against the release backend | `0007_atomic_handover_completion_rpcs.sql`; embedded-PostgreSQL rollback test | One staging database transaction updates transaction, resource, passport history, wallet and impact or updates none |
| P0 | Replay/lost-response safety passes through real signed staging REST/RPC sessions but not yet through the Android outbox on separate app/device sessions | `0008_idempotent_replay_response.sql`; Postgres idempotency records; Room `lifecycle_commands`; Stage 3 evidence | Reuse the persisted UUID across a real Android network interruption and prove no duplicate transaction, ledger, passport or impact effects |
| P0 | Account deletion is promised but has no operational app/backend flow | Android profile, website delete page, Supabase | Reauthentication, delete function, storage/data policy and confirmed user outcome |
| P1 | Participant Return shows a fake non-scannable QR | `FakeQrPanel.kt`, `ParticipantReturnScreen.kt` | Remove fake component from runtime and render/verify the real assigned passport code |
| P1 | Partner map is only a static screenshot; accept action is a no-op | `PartnerMapScreen.kt`, `RestoredVisualLiveScreens.kt` | Live markers, details, navigation/list fallback and a persisted authorised action |
| P1 | Partner workbench “Open passport” callback is empty | `ReEventApp.kt`/partner screen wiring | Visible action navigates to an authorised passport or is removed |
| P1 | Forgot-password has no complete deep-link/update-password flow | auth/navigation code | A user can recover access from email through a tested app link |
| P1 | Photo storage read policy prevents authorised non-owners from loading listing photos | storage policy in migration | Signed URLs or a privacy-safe read policy covered by cross-role tests |
| P1 | Sync errors are silent and the first failing row can starve later rows | repository/sync worker | Visible pending/failed state, retry action, fair queue and terminal failure handling |
| P1 | Public passport access disappears when resource status changes | passport/resource RLS | Current authorised transaction actors retain the minimum required read access |
| P1 | Website support/deletion contact is a placeholder and no public deployment was verified | `ReEventWebsite/app/support`, `delete-account` | Real monitored contact/process and tested deployed URLs |
| P1 | Android lint errors are cleared, but the remaining warning set has not received release review | Stage 3 `:app:lintDebug`: 0 errors, 28 warnings | Review/fix or explicitly accept every warning at the tested candidate commit; retain zero errors |

No release checkbox may bypass these defects by hiding the failure with local demo data, swallowing an exception, or changing only the displayed status.

---

## 5. Release completion checklist: scope, architecture, and security

- [x] **REL-SCOPE-01 — Freeze the 1.0 product promise.** The mandatory journey, roles, and exclusions are recorded in this file.
- [x] **REL-SCOPE-02 — Supersede old progress claims.** This file explicitly defines older member/superpowers trackers as historical rather than authoritative.
- [ ] **REL-SCOPE-03 — Trace every promised feature.** App navigation, website copy, store copy, assignment report, and this checklist contain the same 1.0 scope; no mock-only feature is advertised.
- [x] **REL-ARCH-01 — Document the final architecture.** Record Android layers, dependencies, environment boundaries, data ownership, server functions, sync lifecycle and failure modes in `docs/release/architecture.md`.
- [ ] **REL-ARCH-02 — Separate staging and production configuration.** Use build variants or equivalent typed configuration; fail the build when a required value is absent; never ship a service-role key.
- [ ] **REL-ARCH-03 — Remove production demo fallback.** Demo auth/data can exist only in a clearly labelled demo build that cannot be uploaded as production.
- [x] **REL-DATA-01 — Publish the final schema and state machine.** Include entity fields, units, nullable rules, foreign keys, ownership, status transitions and deletion behavior.
- [x] **REL-DATA-02 — Make Room identity account-safe.** Prove two accounts can cache the same public record without overwrite or leakage.
- [x] **REL-DATA-03 — Scope every local mutation.** DAO update/archive/delete statements include the correct account or deliberately shared-cache boundary.
- [ ] **REL-DATA-04 — Use Room transactions for local aggregates.** Resource/passport/transaction/history/impact writes commit together or roll back together.
- [x] **REL-DATA-05 — Version and export Room schemas.** Check schema JSON into source, add migration tests, and prove upgrade from every publicly distributed database version.
- [ ] **REL-DATA-06 — Define deletion and remote-revocation reconciliation.** Rows removed or no longer visible on the server are removed/hidden locally without deleting unrelated user data.
- [x] **REL-SYNC-01 — Bind outbox rows to an account and environment.** Worker execution must match the authenticated subject and backend.
- [ ] **REL-SYNC-02 — Make sync fair and recoverable.** One poison row cannot starve the queue; retries use bounded backoff; terminal failure is retained for user action.
- [ ] **REL-SYNC-03 — Surface sync truth.** Relevant screens display saved locally, pending, synced, or failed, with a safe retry affordance.
- [ ] **REL-SYNC-04 — Define conflict behavior.** Document and test concurrent edits, approve/cancel races, offline completion, server rejection, and clock/order assumptions.
- [x] **REL-SYNC-05 — Protect sign-out/account switching.** Signing out stops work safely and a subsequent account cannot observe or execute the prior account's private queue.
- [ ] **REL-SEC-01 — Replace direct critical transaction writes.** Use Supabase/Postgres functions for request, approve, reject/cancel, handover, return and completion.
- [ ] **REL-SEC-02 — Enforce actors and roles server-side.** The server derives the caller from `auth.uid()` and validates organiser, participant, receiver and partner authority.
- [ ] **REL-SEC-03 — Enforce legal transitions server-side.** Illegal jumps, field rewrites, cross-event resources, self-dealing errors and over-quantity requests are rejected.
- [ ] **REL-SEC-04 — Make completion atomic and idempotent.** Repeated requests cannot duplicate passport events, recovered quantity, CO2e, value, or badges.
- [ ] **REL-SEC-05 — Harden every table with least-privilege RLS.** Test positive and negative cases for profiles, events, resources, passports, programmes, transactions and impact.
- [ ] **REL-SEC-06 — Secure media access.** Authorised listing/passport actors can load required images; unrelated users cannot enumerate or download them.
- [ ] **REL-SEC-07 — Validate uploaded media.** Enforce allowed MIME type, true file type, dimensions and size; compress before upload; clean replaced/orphaned objects.
- [ ] **REL-SEC-08 — Review backup and extraction rules.** Decide whether authenticated Room/DataStore/session data can be backed up, add explicit XML rules, and test restore behavior.
- [ ] **REL-SEC-09 — Add dependency and secret scanning.** Release CI fails on committed secrets, known high-risk dependencies, debug endpoints, or service-role credentials.
- [ ] **REL-SEC-10 — Threat-model the core journey.** Cover QR enumeration/tampering, replay, role escalation, IDOR, malicious uploads, account switch, offline replay and deleted-account access.

---

## 6. Release completion checklist: authentication and account lifecycle

- [ ] **REL-AUTH-01 — Validate sign-up inputs and role choice.** Email normalization, password rules, duplicate account, network failure, loading and retry states are usable and tested.
- [ ] **REL-AUTH-02 — Verify the email-confirmation journey.** A new account receives confirmation, returns through the correct app/web link and reaches the intended role graph.
- [ ] **REL-AUTH-03 — Complete password recovery.** Reset link opens a dedicated secure update-password screen; expired/used links and retry behavior are tested.
- [ ] **REL-AUTH-04 — Make session restoration deterministic.** Cold start, expired token, revoked session, offline start and background/foreground transitions show the correct state without a navigation flash or data leak.
- [ ] **REL-AUTH-05 — Enforce immutable or controlled roles.** A client cannot promote itself by updating profile data; any supported role change has an explicit verified process.
- [ ] **REL-AUTH-06 — Provide profile management.** Users can view and edit supported profile fields and see the active account/role/environment accurately.
- [ ] **REL-AUTH-07 — Provide in-app privacy, terms, support and deletion entry points.** Each opens the production URL or native flow and remains available before/after sign-in as appropriate.
- [ ] **REL-AUTH-08 — Implement account deletion end to end.** Reauthenticate; explain consequences; delete/anonymise data according to the declared policy; remove storage/session; confirm completion.
- [ ] **REL-AUTH-09 — Test deletion ownership edge cases.** Events with active transactions, completed impact history, partner programmes and shared/public passports have an explicit legal/functional outcome.
- [ ] **REL-AUTH-10 — Remove all test credentials and backdoors.** Production contains no visible or hidden universal login, seeded password, automatic role bypass or staging endpoint.

---

## 7. Release completion checklist: events, resources, media, and offline drafts

- [ ] **REL-EVENT-01 — Complete the event model.** Store and validate name, type, start/end time with timezone, address, coordinates, expected attendance, sustainability target, owner and lifecycle status.
- [ ] **REL-EVENT-02 — Build usable date/time and location input.** No hard-coded “now + one day”; errors identify invalid range, missing field, past policy and unavailable map/geocoder.
- [ ] **REL-EVENT-03 — Finish event CRUD.** Create, edit, view, archive/restore or delete behave consistently locally and remotely with confirmation and error recovery.
- [ ] **REL-EVENT-04 — Calculate event progress from owned data.** Counts and targets update after resource and transaction changes and never use a silently selected “first event.”
- [ ] **REL-EVENT-05 — Test event isolation.** Organisers cannot read or mutate private events/resources outside authorised visibility.
- [ ] **REL-RESOURCE-01 — Complete the resource model.** Store event, owner, category/type, material, quantity/unit, condition, status, current location, description, photo, reuse count and timestamps.
- [ ] **REL-RESOURCE-02 — Validate resource creation/editing.** Reject invalid quantity/unit, unsupported condition/status, blank required data and cross-account/event references.
- [ ] **REL-RESOURCE-03 — Finish resource lifecycle controls.** Draft, publish/available, reserve, hand over, return/recover and archive are state-machine actions, not arbitrary status edits.
- [ ] **REL-RESOURCE-04 — Finish archive/delete UX.** Destructive actions are reachable, confirmed, reversible where promised, synced and blocked when an active transaction requires it.
- [ ] **REL-RESOURCE-05 — Make photos production-safe.** Correct EXIF orientation, compression, MIME/extension, upload progress, retry, replacement, removal, authorised display and offline placeholder.
- [ ] **REL-RESOURCE-06 — Preserve offline drafts.** Kill/restart and network-loss tests prove draft text/photo references survive safely and later sync without duplicates.
- [ ] **REL-RESOURCE-07 — Finish search and filters.** Results match real event/status/category/material/location fields, empty state is clear, and filtering is tested with realistic data volume.
- [ ] **REL-RESOURCE-08 — Test quantities, not only rows.** Partial quantities and multiple transactions cannot recover or transfer more than the available quantity.

---

## 8. Release completion checklist: digital passports and real QR scanning

- [x] **REL-QR-01 — Define a versioned QR contract.** Document payload format, identifier/token entropy, supported versions, privacy exposure, validation, expiry/revocation policy and migration behavior.
- [ ] **REL-QR-02 — Generate only real QR symbols in runtime.** Remove `FakeQrPanel` from production paths; every displayed code is produced by the QR library from a stored passport payload.
- [ ] **REL-QR-03 — Make the participant return pass real.** It represents a currently assigned resource/transaction, is decodable, and cannot be used by another participant.
- [ ] **REL-QR-04 — Resolve cache misses from the server.** A valid authorised code scanned on a clean second device loads the minimum passport/resource data instead of requiring prior cache population.
- [ ] **REL-QR-05 — Add verified app/deep-link handling.** Supported QR links open the correct screen after cold start, authentication and role routing; malformed/unknown/unauthorised links are safe.
- [ ] **REL-QR-06 — Preserve passport history.** Creation, condition changes, checkout, return, transfer, repair and recovery events are immutable, ordered, actor-attributed and privacy-filtered.
- [ ] **REL-QR-07 — Make scan actions contextual.** The scanner offers only transitions authorised for that actor and current transaction; it never universally offers RETURN.
- [ ] **REL-QR-08 — Make scan submission idempotent.** Rapid double scan, same-code replay, process death and network retry produce one domain event.
- [ ] **REL-QR-09 — Handle camera capability honestly.** Declare camera as optional where appropriate, provide permission rationale/denial/settings states and a manual code/link fallback.
- [ ] **REL-QR-10 — Pass the QR device matrix.** Test printed screen-to-camera and device-to-device scanning in bright/dim light, portrait/landscape, supported API levels, Huawei and non-Huawei hardware.
- [ ] **REL-QR-11 — Verify barcode readability automatically.** Unit/instrumentation tests decode generated bitmaps and assert payload round trips and malformed payload rejection.
- [ ] **REL-QR-12 — Limit public passport data.** A scanner without transaction authority sees no owner email, private event detail, storage path, internal notes or write affordance.

---

## 9. Release completion checklist: marketplace and cross-role transactions

- [x] **REL-MKT-01 — Freeze the supported transaction types.** Define whether 1.0 supports borrow, rent, buy, donate and exchange; remove any type without a real end-to-end contract. If no payment exists, “rent” and “buy” must clearly describe offline settlement or be excluded.
- [ ] **REL-MKT-02 — Complete discovery.** Browse, search, action, category, material and location/distance filters operate on server-backed authorised listings with pagination and error states.
- [ ] **REL-MKT-03 — Complete listing detail.** Show accurate owner-safe event/resource data, quantity available, terms, location precision, photo, passport summary and allowed action.
- [ ] **REL-MKT-04 — Validate requests server-side.** Quantity, type, requester, resource availability and duplicate active request constraints are enforced atomically.
- [ ] **REL-MKT-05 — Complete organiser decision flow.** Approve/reject updates both actors, reserves the correct quantity and handles competing requests without over-allocation.
- [ ] **REL-MKT-06 — Complete handover flow.** Only the assigned actors can confirm checkout/in-transit/receipt/return steps, with unambiguous pending responsibility.
- [ ] **REL-MKT-07 — Complete cancellation/dispute-safe behavior.** Allowed cancellation windows and resulting inventory state are defined; 1.0 does not falsely imply payment dispute support.
- [ ] **REL-MKT-08 — Complete transaction history.** Each actor sees accurate current and completed transactions, timestamps, quantity, resource and next action across restart/devices.
- [ ] **REL-MKT-09 — Keep passport/photo access valid for assigned actors.** Access lasts only as long as the workflow requires and is revoked afterward according to policy.
- [ ] **REL-MKT-10 — Pass adversarial concurrency tests.** Two users racing for the last quantity, cancel/approve races, repeated completion and unauthorised status edits remain consistent.

---

## 10. Release completion checklist: circular partners, real map, and matching

- [ ] **REL-PARTNER-01 — Complete the programme model.** Include owner, accepted categories/materials, minimum condition, quantity/capacity, reward/fee, pickup availability, address/coordinates, processing method, terms and active status.
- [ ] **REL-PARTNER-02 — Validate programme ownership.** Partners can manage only their programmes; organiser/participant accounts cannot impersonate a partner.
- [ ] **REL-PARTNER-03 — Finish programme CRUD and workbench.** Create/edit/activate/deactivate, incoming requests, detail, passport action and empty/error states all work.
- [ ] **REL-MAP-01 — Replace the static map with a live map.** Use a Huawei-compatible, vendor-neutral Android renderer such as MapLibre Native with an approved production tile provider.
- [ ] **REL-MAP-02 — Store real coordinates.** Events/programmes/resources use validated latitude/longitude plus display address; migrations and geocoding behavior are documented.
- [ ] **REL-MAP-03 — Render live eligible partner markers.** Marker identity, selected state, programme detail, distance and filtering come from current backend records.
- [ ] **REL-MAP-04 — Implement location permission states.** The map works without location permission, explains coarse/fine use, handles denial/settings, and never fabricates user position.
- [ ] **REL-MAP-05 — Provide an accessible list fallback.** All map results/actions are available without gestures, location permission, or map-tile connectivity.
- [ ] **REL-MAP-06 — Respect map data terms.** Provider key stays out of source, attribution is visible, caching/rate limits are followed and production billing/quota alerts are owned.
- [ ] **REL-MAP-07 — Make partner selection persist.** The current empty accept callback becomes an authorised handover request with visible success/failure and transaction linkage.
- [x] **REL-MATCH-01 — Define the deterministic matching contract.** Inputs, hard exclusions, weights/tie-breaks, supported actions and missing-data behavior are documented.
- [ ] **REL-MATCH-02 — Use complete eligibility.** Match material/category, condition, quantity/capacity, active status, supported action, distance and pickup constraints.
- [ ] **REL-MATCH-03 — Explain every recommendation.** Show why the best option fits, rejected constraints, distance, expected outcome and viable alternatives.
- [ ] **REL-MATCH-04 — Keep language honest.** Rename “AI” to smart/rule-based matching unless a real model/service, consent, evaluation and fallback are implemented.
- [ ] **REL-MATCH-05 — Complete recommendation-to-action.** Selecting a recommendation opens the real programme and creates an authorised request; no dead-end visual result.
- [ ] **REL-MATCH-06 — Test ranking and edge cases.** Cover ties, missing coordinates, insufficient capacity, inactive programme, unknown material, no match and deterministic repeatability.

---

## 11. Release completion checklist: impact, circularity, and badges

- [x] **REL-IMPACT-01 — Define calculation boundaries.** Document supported materials/actions, units, mass/quantity conversion, baseline, lifecycle boundary, rounding, factor source/version and unsupported cases.
- [ ] **REL-IMPACT-02 — Store factor provenance.** Every CO2e/value result can identify its factor, unit, source, version/date and assumptions.
- [ ] **REL-IMPACT-03 — Make aggregation quantity-aware.** Reused/repaired/donated/recycled/recovered totals use completed quantities or mass, not merely transaction row count.
- [ ] **REL-IMPACT-04 — Calculate from server-authoritative completion.** A client cannot award impact by directly writing a completed transaction or impact row.
- [ ] **REL-IMPACT-05 — Prevent double counting.** Replay, edit, sync retry and status reversal tests preserve one contribution per accepted completion event.
- [ ] **REL-IMPACT-06 — Scope dashboards correctly.** Selected event and account filters are explicit; organiser totals never silently use the first event or another account.
- [ ] **REL-IMPACT-07 — Disclose estimates and unavailable values.** UI shows units, factor limitations and “not available” rather than zero or invented precision.
- [ ] **REL-IMPACT-08 — Reconcile recovery rate.** Numerator/denominator and treatment of drafts, archived resources, quantity and partial recovery are specified and tested.
- [ ] **REL-IMPACT-09 — Make badges deterministic and non-monetary.** Thresholds use real accepted impact, cannot be client-forged and do not promise redeemable rewards.
- [ ] **REL-IMPACT-10 — Validate calculations with fixtures.** Domain fixtures cover each supported channel/material, zero/unknown values, partial quantity, rounding and invariant totals.

---

## 12. Release completion checklist: testing and quality engineering

The target test pyramid is approximately 70% fast JVM/domain tests, 25% repository/integration/UI-component tests, and 5% full E2E. Percentages describe suite balance, not release progress.

- [ ] **REL-TEST-01 — Install the missing test infrastructure.** Add Compose UI test, Hilt test, Room migration, coroutine/Flow test and suitable fake/mock dependencies with separate test DI.
- [ ] **REL-TEST-02 — Add coverage reporting.** Generate deterministic unit/instrumentation coverage; set justified package gates for domain/data/auth/sync code and publish reports in CI.
- [ ] **REL-TEST-03 — Test every business-logic unit.** Validators, payload parser, state machine, matching, impact, quantity allocation and role routing have positive, boundary and failure cases.
- [ ] **REL-TEST-04 — Test repositories and outbox.** Cover offline writes, retry, poison row, account switch, remote deletion, conflict, rollback and error presentation using controlled gateways.
- [ ] **REL-TEST-05 — Test Supabase contracts.** Against isolated staging/test Postgres, assert RLS and server functions for every role plus anonymous/forged/replay cases.
- [ ] **REL-TEST-06 — Expand Room device tests.** Cover all account-scoped entities, composite identity, transactions, migration, process restart and database upgrade.
- [ ] **REL-TEST-07 — Add plain Compose screen tests.** Loading/content/empty/error/offline/permission states and important enabled/disabled actions use semantics assertions and controlled image loading.
- [ ] **REL-TEST-08 — Add navigation and deep-link tests.** Test all role graphs, back behavior, cold/warm links, auth redirects, password recovery and unsupported routes.
- [ ] **REL-TEST-09 — Add state-restoration tests.** Forms, selected event/filter, camera permission, rotation, process recreation and background/foreground preserve or deliberately reset documented state.
- [ ] **REL-TEST-10 — Add screenshot regression coverage.** Capture representative roles and state variants at compact phone, large phone, tablet and large-font configurations.
- [ ] **REL-TEST-11 — Add accessibility checks.** TalkBack labels/order/actions, contrast, touch targets, dynamic type, keyboard/focus and non-map alternatives pass.
- [ ] **REL-TEST-12 — Add full three-account E2E.** Automate or reproducibly script the mandatory release journey against staging with independent organiser, participant and partner sessions.
- [ ] **REL-TEST-13 — Test offline/recovery E2E.** Interrupt network, kill process, expire session and retry during each critical lifecycle step; prove no false success or duplicate effect.
- [ ] **REL-TEST-14 — Add release smoke tests.** Run against the minified signed candidate, not only debug, including auth, image loading, QR scan, map and links.
- [ ] **REL-TEST-15 — Keep test data disposable.** Seed script uses environment-provided test identities, no real user UUIDs/passwords, and a documented cleanup/reset procedure.
- [ ] **REL-TEST-16 — Make CI a release gate.** Clean build, unit, lint/static analysis, database contract, website lint/test/build, instrumentation subset and artifact checks must pass before tagging.

---

## 13. Release completion checklist: Android UX, accessibility, performance, and build

- [ ] **REL-UX-01 — Remove every runtime mock/no-op.** Search and manually traverse all routes; no fake QR, static functional map, empty click callback, preview data, misleading enabled button or hard-coded success remains.
- [ ] **REL-UX-02 — Consolidate legacy screens.** Delete or isolate unused prototype screens/components and split oversized live-screen files into maintainable feature/state components.
- [ ] **REL-UX-03 — Fix text encoding and centralise user text.** Remove mojibake and move runtime strings to resources; include content descriptions, errors and plurals.
- [ ] **REL-UX-04 — Finish loading/empty/error/offline states.** Every data screen has actionable and visually distinct states; asynchronous buttons prevent duplicate submission.
- [ ] **REL-UX-05 — Make destructive and irreversible actions explicit.** Archive, cancel, complete, recover and delete use correct confirmation and explain consequences.
- [ ] **REL-UX-06 — Support adaptive layouts.** Compact phone is polished; tablet/foldable/landscape do not crop, stretch, lose actions or rely on raw screen-width checks.
- [ ] **REL-UX-07 — Support system UI and IME safely.** Edge-to-edge, status/navigation bars, camera cutouts, keyboard insets and minSdk behavior pass across the device matrix.
- [ ] **REL-UX-08 — Pass accessibility acceptance.** TalkBack can complete the core journey; semantics do not expose decorative images or omit status/action context.
- [ ] **REL-UX-09 — Validate visual assets.** Optimise large PNGs/WebP/vector assets, remove duplicate/unused images, supply adaptive/round/monochrome icon and verify all densities.
- [ ] **REL-UX-10 — Measure performance.** Record cold/warm startup, critical screen render, scrolling, image memory, QR analysis and map behavior on a representative low/mid device; fix release-blocking jank/ANR/leaks.
- [ ] **REL-UX-11 — Test realistic data volume.** Lists, search, sync and dashboard remain responsive with documented maximum events/resources/transactions/programmes.
- [ ] **REL-UX-12 — Add safe observability.** Production crash/ANR and backend error monitoring has owner, retention/consent review, environment tags and no secret/PII logging.
- [ ] **REL-BUILD-01 — Fix all Android lint errors.** Resolve minSdk style, ML Kit opt-in and camera feature declaration; review rather than blindly suppress warnings.
- [ ] **REL-BUILD-02 — Define release build type.** Release has minification/resource shrink decisions, maintained rules, disabled debugging, production configuration and no debug libraries/logs.
- [ ] **REL-BUILD-03 — Establish signing custody.** Create/choose upload key, store it outside the repository, document backup/rotation/owners and configure CI/local signing without exposing passwords.
- [ ] **REL-BUILD-04 — Automate versioning.** Unique versionCode and human-readable versionName map to a release tag and backend schema compatibility.
- [ ] **REL-BUILD-05 — Produce reproducible APK/AAB artifacts.** Clean checkout builds signed candidate; record SHA-256, size, application ID, version and commit.
- [ ] **REL-BUILD-06 — Pass package inspection.** Verify manifest permissions/features/exported components, deep links, native libraries, resources, secrets, debuggability and target/min SDK.
- [ ] **REL-BUILD-07 — Keep package size justified.** Optimise the current approximately 69.6 MB debug baseline and record final download/install size without breaking visuals or ML/QR behavior.
- [ ] **REL-BUILD-08 — Pass the supported Android matrix.** At minimum minSdk 24, representative mid version, target/latest version, Huawei device without Google dependencies, non-Huawei device, phone and tablet/foldable window.

---

## 14. Release completion checklist: website, privacy, operations, and AppGallery

- [ ] **REL-WEB-01 — Replace starter metadata.** Repository/package/README/test descriptions identify ReEvent rather than `vinext-starter` or deleted preview content.
- [ ] **REL-WEB-02 — Make scripts cross-platform.** `npm ci`, lint, test and build pass on Windows and CI without POSIX-only inline environment syntax.
- [ ] **REL-WEB-03 — Rewrite website tests.** Assert actual landing, privacy, terms, support and deletion content/links; remove stale starter-template expectations.
- [ ] **REL-WEB-04 — Assign a real support channel.** Publish a monitored project-owned email or form with named owners and response expectations; verify delivery and reply.
- [ ] **REL-WEB-05 — Implement the stated deletion request path.** Requests are authenticated/verified, tracked, completed within the stated policy and safe from impersonation.
- [ ] **REL-WEB-06 — Reconcile privacy disclosures with behavior.** List actual account/profile/event/resource/photo/location/device/log/service processing, purpose, retention, sharing, security, user rights and contact.
- [ ] **REL-WEB-07 — Reconcile terms with product behavior.** Avoid claiming payments, guaranteed environmental results, partner certification, logistics or support that 1.0 does not provide.
- [ ] **REL-WEB-08 — Deploy the production website.** HTTPS public domain serves `/`, `/privacy`, `/terms`, `/support`, and `/delete-account`; mobile/desktop visual QA and link checking pass.
- [ ] **REL-WEB-09 — Connect app and store to canonical URLs.** URLs use production domain, remain stable without login, and match the submitted AppGallery metadata.
- [ ] **REL-OPS-01 — Assign operational ownership.** Name owners for backend, store, signing, domain, support, privacy/deletion, map quota, incident response and release rollback.
- [ ] **REL-OPS-02 — Protect and back up production.** Document Supabase backup/restore, migration deployment/rollback, storage retention, secret rotation and least-privilege team access.
- [ ] **REL-OPS-03 — Create a release/rollback runbook.** Include freeze, migration order, smoke test, staged rollout, monitoring, rollback/forward-fix and user communication.
- [ ] **REL-OPS-04 — Validate quotas and failure behavior.** Supabase, email, storage, map tiles/geocoding, website and monitoring limits/alerts are owned; app degrades honestly when unavailable.
- [ ] **REL-STORE-01 — Verify the Huawei developer organisation/account.** Legal/contact information is authentic, complete and controlled by the project owner.
- [ ] **REL-STORE-02 — Create the AppGallery record.** Correct application ID, category, free/paid model, countries/regions, languages, age rating and content declarations are recorded.
- [ ] **REL-STORE-03 — Prepare truthful store assets.** Name, short/full description, icon, feature graphics and device screenshots show the final candidate and only functioning features.
- [ ] **REL-STORE-04 — Complete privacy and permission declarations.** Production privacy URL and CAMERA/location/data disclosures match manifest, runtime prompts, SDKs and actual backend behavior.
- [ ] **REL-STORE-05 — Upload the signed candidate.** AppGallery accepts the APK/AAB/signing configuration and automated checks; uploaded hash/version matches Section 15 evidence.
- [ ] **REL-STORE-06 — Pass AppGallery open testing.** Independent organiser, participant and partner testers execute the release journey and recorded edge cases; blocking feedback is closed.
- [ ] **REL-STORE-07 — Submit the formal release.** Complete metadata, regions, pricing, release notes and review submission; record submission ID/date.
- [ ] **REL-STORE-08 — Resolve review findings and publish.** No rejected/privacy/security/functionality item remains; public listing installs and runs the approved version.
- [ ] **REL-STORE-09 — Run post-publication smoke tests.** Install from AppGallery on clean Huawei hardware and verify auth, links, QR, map, cross-role workflow, impact, support and deletion entry.
- [ ] **REL-ASSIGN-01 — Produce the assignment evidence from the release candidate.** Report/screenshots demonstrate the working data-driven tool, mobile storage, external services, design, civil/commercial value, source tidiness, documentation and real contribution evidence.
- [ ] **REL-ASSIGN-02 — Rehearse the live demo and failure fallback.** Demonstrate the mandatory journey with staged accounts/devices and a truthful contingency for network/camera outage, without fake success.

---

## 15. Final acceptance scenarios and evidence registry

### 15.1 Mandatory final scenarios

- [ ] **REL-E2E-01 — Organiser creation path.** Clean install → confirmed sign-up → event → photographed resource → passport → real QR, then restart and verify persistence/sync.
- [ ] **REL-E2E-02 — Participant marketplace path.** Separate account/device discovers listing → requests allowed quantity → organiser approves → both see matching status → checkout/return completes once.
- [ ] **REL-E2E-03 — Partner recovery path.** Partner publishes eligible programme with coordinates → organiser receives ranked match → sees real map → creates handover → partner opens passport and completes recovery.
- [ ] **REL-E2E-04 — Impact integrity path.** Each accepted completion changes only the correct event/account totals by the expected quantity/factor; replay changes nothing.
- [ ] **REL-E2E-05 — Offline interruption path.** Interrupt every critical mutation before request, after local commit and after server response; UI reports truth and recovery creates no duplicate.
- [ ] **REL-E2E-06 — Account isolation path.** Switch among organiser/participant/partner accounts on one device; private rows, drafts, photos and outbox operations never cross accounts.
- [ ] **REL-E2E-07 — Adversarial API path.** Anonymous/wrong-role/wrong-actor calls, forged IDs/statuses/quantities, QR replay and unauthorised media reads are rejected without partial changes.
- [ ] **REL-E2E-08 — Lifecycle/device path.** Rotate, resize/fold, background, lock, kill, update and restore during core screens; state and navigation satisfy documented behavior.
- [ ] **REL-E2E-09 — Accessibility path.** A TalkBack/large-text/keyboard user can complete the non-camera alternative and all non-map core actions.
- [ ] **REL-E2E-10 — Production smoke path.** AppGallery-installed build against production and public website completes a controlled non-destructive smoke test with operational monitoring visible.

### 15.2 Evidence registry template

Add one row for every checked release item. A combined report may support multiple IDs only when each scenario/result is explicit.

| REL ID | Commit/tag | Environment/device | Command or scenario | Result/date | Evidence path or URL | Reviewer |
|---|---|---|---|---|---|---|
| REL-SCOPE-01 | Working tree at audit | Documentation | Product target and exclusions review | Accepted 2026-08-09 | This file, Sections 2.1–2.4 | Workspace audit |
| REL-SCOPE-02 | Working tree at audit | Documentation | Tracker authority review | Accepted 2026-08-09 | This file, title/Section 1 | Workspace audit |
| REL-ARCH-01 | Base HEAD `fedebae1`; doc SHA-256 `52AD0B1E9706AB43BD31EB67D3D92530306FBB3245647DACC54810A867150D74` | Documentation | Reviewed authority, Android layers, environment isolation, cache/sync and RPC/failure boundaries | Accepted 2026-08-09 | `docs/release/architecture.md`; `decisions.md` Section 13 | Workspace audit |
| REL-DATA-01 | Base HEAD `fedebae1`; data SHA-256 `838DC242B4302016F09D67C6A3FAA59054D46A7EBB9E46124DD45D7D9444081A` | Documentation | Reviewed entities, nullability/ownership, quantities, wallet/passport/impact integrity and all state transitions | Accepted 2026-08-09 | `docs/release/data-contract.md`; `decisions.md` Sections 3–4, 11, 13 | Workspace audit |
| REL-DATA-02 | Base HEAD `fedebae1`; Stage 2A working tree | Android Studio `Medium_Tablet` AVD, API 35; `account-a`/`account-b` fixtures | `.\gradlew.bat :app:connectedDebugAndroidTest`; same IDs inserted/upserted across all six Room projections | Accepted 2026-08-09 — 6 instrumentation tests, 0 failures/errors | `docs/release/evidence/android/stage-2a-room-cache.md`; `CoreDaoAccountIsolationTest.kt` | Workspace verification |
| REL-DATA-03 | Base HEAD `fedebae1`; Stage 2A working tree | Android Studio `Medium_Tablet` AVD, API 35; `account-a`/`account-b` fixtures | Account-scoped archive/outbox mutation audit and `.\gradlew.bat :app:connectedDebugAndroidTest` | Accepted 2026-08-09 — cross-account mutation attempts changed no other-account rows | `docs/release/evidence/android/stage-2a-room-cache.md`; `CoreDao.kt` | Workspace verification |
| REL-DATA-05 | Base HEAD `fedebae1`; schema 2 SHA-256 `151B86CB30A77B9A231C31FEC118586605AA39D956FF9F714BEF0C4DB9A26F4B`; schema 3 SHA-256 `CF7BC9F1DD0AC0238094A46C7C5B0C70F42EBDC4D9F29059B5D8399C5E74ABAE` | Android Studio `Medium_Tablet` AVD, API 35; local Room database | Migration 1→2→3 and 2→3 validation via `.\gradlew.bat :app:connectedDebugAndroidTest` | Accepted 2026-08-09 — attributable data preserved, blank-account rows removed, Room schema validated | `docs/release/evidence/android/stage-2a-room-cache.md`; exported schemas 2/3; `ReEventDatabaseMigrationTest.kt` | Workspace verification |
| REL-SYNC-01 | Base HEAD `fedebae1`; Stage 2B working tree; schema 4 SHA-256 `DA0CA945B83DAF65022F0144AD5C6160300918132F7BA9B2F5FD046250A73C51` | Android Studio `Medium_Tablet` AVD, API 35; typed `local`; fake gateway; `account-a`/`account-b` | Room 1→2→3→4, 2→3→4 and 3→4; environment/account partition and stale-identity scenarios via `.\gradlew.bat :app:connectedDebugAndroidTest` | Accepted 2026-08-09 — 12 instrumentation tests; wrong identity made zero remote calls/queue mutations; correct identity touched only its partition | `docs/release/evidence/android/stage-2b-sync-identity.md`; exported schema 4; `SyncCoordinatorIdentityTest.kt` | Workspace verification |
| REL-SYNC-05 | Base HEAD `fedebae1`; Stage 2B working tree | Android Studio `Medium_Tablet` AVD, API 35; typed `local`; fake scheduler/gateway; `account-a`/`account-b` | Cancellation, mid-operation session change, sign-out purge and unique identity tests; JVM + connected test suites | Accepted 2026-08-09 — cancellation left queue pending; selected account purged; other-account rows/drafts remained | `docs/release/evidence/android/stage-2b-sync-identity.md`; `AccountSessionCleanerTest.kt`; `SyncWorkIdentityTest.kt` | Workspace verification |
| REL-QR-01 | Base HEAD `fedebae1`; decision SHA-256 `DC9D8AE29ED2DC96BAB6FB9E7DCAB105B3C39CCD5FEB01D1F139E21A1524FC76` | Documentation | Validated HTTPS v1 payload, 128-bit opaque token, parsing/privacy, persistence/revocation and legacy migration | Accepted 2026-08-09 | `data-contract.md` Section 10; `decisions.md` Section 5 | Workspace audit |
| REL-MKT-01 | Base HEAD `fedebae1`; data/decision hashes in adjacent evidence rows | Documentation | Walked borrow, rent, buy, donate, exchange, repair, recycle and buy-back actors/states/ReCoin/ownership outcomes | Accepted 2026-08-09 | `data-contract.md` Sections 5–8; `decisions.md` Sections 2, 6, 11 | Workspace audit |
| REL-MATCH-01 | Base HEAD `fedebae1`; data/decision hashes in adjacent evidence rows | Documentation | Validated hard eligibility, numeric weights, missing-coordinate behavior, stable tie-breaks and honest naming | Accepted 2026-08-09 | `data-contract.md` Section 9; `decisions.md` Section 7 | Workspace audit |
| REL-IMPACT-01 | Base HEAD `fedebae1`; data/decision hashes in adjacent evidence rows | Documentation | Validated channel/quantity boundary, lineage cap, ReCoins, factor provenance, formula/rounding and unavailable cases | Accepted 2026-08-09 | `data-contract.md` Section 11; `decisions.md` Section 8; `docs/impact/IMPACT_ESTIMATE_FACTORS.md` | Workspace audit |

Recommended durable structure:

```text
docs/release/
  architecture.md
  data-contract.md
  threat-model.md
  test-plan.md
  release-runbook.md
  decisions.md
  evidence/
    android/
    backend/
    qr/
    map/
    website/
    appgallery/
```

### 15.3 Required final commands

Exact task names may evolve, but the final evidence must cover these outcomes from clean checkouts:

```powershell
# Android
Set-Location C:\MobileApp\ReEvent
.\gradlew.bat clean test lint connectedCheck bundleRelease

# Website
Set-Location C:\MobileApp\ReEventWebsite
npm ci
npm run lint
npm test
npm run build
```

Database contract tests, E2E tests, package inspection, accessibility/device checks and AppGallery steps must also be recorded; a green Gradle/npm command alone cannot accept them.

---

## 16. External inputs that must be real, not guessed

These are release work, not excuses to substitute placeholders:

| Required input | Needed for | Acceptance evidence |
|---|---|---|
| Legal developer/publisher identity | AppGallery and legal pages | Verified AppGallery account/organisation record |
| Monitored support email/form | App, website, AppGallery | Send/receive/reply test |
| Public HTTPS domain | Privacy, terms, support, deletion and app links | Deployed URL and ownership |
| Production Supabase project/owners | Auth/data/storage/functions | Environment record, RLS tests, backup/restore proof |
| Signing/upload key custody | Release package | Key ownership/backup runbook; no key in repo |
| Map tile/geocoding provider and key | Real partner map | Terms/quota/attribution review and production device run |
| Three staging test identities | Cross-role E2E | Synthetic organiser/participant/partner run |
| Huawei and non-Huawei test hardware | QR/map/AppGallery compatibility | Device evidence |
| Target regions/languages/age rating | Store/legal scope | AppGallery metadata decision |

---

## 17. Recommended implementation order

Do not start with visual polish or store screenshots. Complete work in this dependency order:

1. **Freeze contracts:** schema, roles, state machine, QR contract, impact boundary, environments and legal scope.
2. **Fix security and atomicity:** server functions/RLS, account-safe Room/outbox, idempotency and media access.
3. **Build the test harness:** repository fakes, policy tests, Compose tests, migration tests, coverage and CI.
4. **Finish the mandatory vertical slice:** auth/recovery → event/resource → real QR → marketplace/partner workflow → impact.
5. **Replace fake/dead surfaces:** participant QR, live map, partner actions, selected-event routing, sync/error states.
6. **Complete privacy/account operations:** deletion backend, profile links, public website, support ownership.
7. **Harden UX and devices:** accessibility, adaptive/edge-to-edge, assets, performance, camera/map/device matrix.
8. **Build and test the signed candidate:** clean release artifacts, full E2E, open testing and defect closure.
9. **Submit and operate:** AppGallery production review, public install smoke test, monitoring and rollback readiness.

This order keeps later UI, tests, screenshots, legal text and store assets aligned with the real server behavior instead of repeatedly certifying a changing prototype.

---

## 18. Sources used to define the target

### Local project sources

- `Group_Assignment.docx` — functional/data-driven tool, local data, external service, publishability, AppGallery and assessment expectations.
- `archive/historical-plans/REEVENT_CIRCULAR_EVENT_ECONOMY_PROJECT_PLAN.md` — historical product concept and circular-economy roles.
- `archive/historical-plans/REEVENT_FULL_APP_DEVELOPMENT_PLAN.md` — historical planned modules, data and feature scope.
- `archive/historical-plans/REEVENT_UI_DEVELOPMENT_PLAN.md` plus `archive/design-source/figma_screenshots/` — historical visual intent.
- Android implementation under `ReEvent/app/src/`, Supabase under `ReEvent/supabase/`, and website implementation under `ReEventWebsite/` — audited current behavior.

### Current official release/quality references

- [Huawei AppGallery release overview](https://developer.huawei.com/consumer/en/appgallery) — verified developer identity, app/package creation, localised listing information, regions/business model and privacy-policy URL.
- [Huawei AppGallery Review Guidelines: Developers' Behavior](https://developer.huawei.com/consumer/de/doc/app/50104-11) — authentic developer information, valid user support contact, and a current app that runs properly.
- [Huawei AppGallery Policy Center](https://developer.huawei.com/consumer/en/policy-center/) — review, age-rating, naming, copyright and rejection policy entry point.
- [Huawei AppGallery open testing](https://developer.huawei.com/consumer/en/agconnect/open-test/) — tester distribution and feedback before formal release.
- [Huawei formal version release guidance](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agcapi-release_app) — configured app information/assets/package and review submission.
- [Android core app quality guidelines](https://developer.android.com/docs/quality-guidelines/archive/core/core-app-quality-2026-03-20) — full-flow, interruption, orientation/form-factor, visual and current-version device testing.
- [MapLibre Native Android documentation](https://maplibre.org/maplibre-native/docs/book/platforms/android/android-documentation.html) — vendor-neutral native Android map implementation reference; a production tile/geocoding provider and its terms are still required.

Store policies and SDK requirements can change. Re-check the official sources at candidate freeze and submission time, and record that review in the evidence registry.

---

## 19. Decision log

| Date | Decision | Reason | Consequence |
|---|---|---|---|
| 2026-08-09 | Make this file the only release-completion authority | Old member trackers mixed assigned work, implementation and unverified completion | Old files remain historical; release progress comes from accepted `REL-` evidence only |
| 2026-08-09 | Define rule-based matching as the 1.0 target | It can be explainable and testable with current product data | Do not call it AI unless a real evaluated model/service is later approved |
| 2026-08-09 | Require a live vendor-neutral partner map | Static imagery cannot perform location discovery and Google-only assumptions are risky for a Huawei-targeted release | Use MapLibre-compatible production tiles/geocoding, real coordinates, attribution and list fallback |
| 2026-08-09 | Treat server authority and atomic completion as P0 | Current cross-role client writes are either rejected by RLS or too easily forged | Secure database contracts precede UI completion claims |
| 2026-08-09 | Accept Stage 2A account-safe Room cache gates | API 35 instrumentation proved composite account identity, mutation isolation, and both legacy migration paths | Accept `REL-DATA-02`, `REL-DATA-03`, and `REL-DATA-05`; leave environment, sync-worker, aggregate, backend, and UI gates unchecked |
| 2026-08-09 | Accept Stage 2B account/environment-bound sync gates | API 35 and fake-boundary tests proved partitioned Room v4 outbox execution, stale-worker rejection, cancellation preservation, and scoped account cleanup | Accept `REL-SYNC-01` and `REL-SYNC-05`; leave variants, retry/UI/conflict, atomicity, backend security, deletion reconciliation, and feature gates unchecked |
