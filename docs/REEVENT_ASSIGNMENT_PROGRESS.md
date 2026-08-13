# ReEvent implementation and acceptance plan

This file is the single day-to-day execution plan for the ReEvent assignment. It answers four questions:

1. What is actually working now?
2. What must be fixed before more feature work?
3. What is the next task, in dependency order?
4. What evidence is required before a module can be called complete?

Do not use a checked box to mean "code exists." A module is complete only when its implementation, automated checks, required environment configuration, and manual acceptance have all passed.

## 1. Baseline and current truth

**Implementation baseline:** teammate update `origin/main` at commit `135387b` (`Complete planned ReEvent modules`, 2026-08-11), plus the local repair work on branch `codex/must-fix-six`.

**Branch state:** `codex/must-fix-six` contains the teammate baseline plus the local repair and staging-configuration work. Migrations `0009`-`0015` and the checked `delete-my-account` Function are deployed to Supabase staging. Password reset, the public Passport verifier, and the debug Android App Link are configured; physical-device acceptance remains pending.

### Verified evidence

| Check | Result | Meaning |
|---|---|---|
| Android debug and Android-test Kotlin compilation | **Passed** | Application and instrumented-test source compile. |
| Android JVM unit tests | **Passed locally** | Includes deletion routing/status, QR renderability, photo snapshot mapping, read-only resource status, Marketplace privacy, role destinations, and width breakpoints. |
| Android lint | **Passed with 15 deferred warnings** | Zero errors; only SDK/dependency-version warnings remain (10 `GradleDependency`, 4 `NewerVersionAvailable`, 1 `OldTargetApi`). |
| Android instrumented tests | **15/15 passed on Medium Phone API 35** | Includes the real Room 5-to-6 migration with retained resource and lifecycle-command rows. |
| Supabase contract tests | **20/20 passed** | Fresh PGlite applies `0001`-`0015`; protected DML is denied and anonymous public-passport resolution returns only the approved safe projection. |
| Edge Function type check | **Passed** | `deno check` passes for `delete-my-account`. |
| Edge Storage pagination tests | **2/2 passed** | More than 1,000 root and nested objects are enumerated. |
| Real-device/manual acceptance of teammate update | **Not recorded** | No module may be called accepted based only on compilation or unit tests. |

The earlier Stage 3 staging lifecycle proof remains useful historical evidence. It does not accept the new photo, QR, account-deletion, partner, Marketplace-publication, or presentation changes in `135387b`.

### Review findings and current resolution

| ID | Priority | Area | Status | Resolution or next action |
|---|---|---|---|---|
| B-01 | **P0** | Supabase tests | **RESOLVED** | Harness creates `service_role`; listing fixtures use `publish_marketplace_listing`; 20/20 tests pass through `0015`. |
| B-02 | **P0** | Account deletion | **RESOLVED / STAGING BACKEND VERIFIED** | Migrations `0011`, `0012`, and `0014` plus the Function are deployed. Live tests proved wrong-password no-mutation, active-work blocking, terminal retry, successful Auth/profile/media cleanup, and retained de-identified history. Manual Compose/provider review remains. |
| B-03 | **P0** | Resource photos | **RESOLVED / STAGING BACKEND VERIFIED** | Migration `0013`, deterministic private Storage, protected metadata RPCs, replacement, reads, cleanup, snapshot mapping, and Room updates are implemented. Live owner/non-owner backend probes pass; gallery/camera/offline UI acceptance remains. |
| B-04 | **P0** | Participant return QR | **STAGING DEPLOYED / DEVICE ACCEPTANCE PENDING** | Password-reset redirect, verifier, token resolver, and debug App Link are live. Scan from a physical device and complete one authorised action before acceptance. |
| B-05 | **P1** | Room database | **RESOLVED LOCALLY** | The 5-to-6 test retained representative resource/photo-path and lifecycle-command data on API 35. |
| B-06 | **P1** | Event detail | **RESOLVED LOCALLY** | Resource status is read-only and explains that confirmed lifecycle actions update it automatically; the no-op mutation method is removed. |
| B-07 | **P1** | Marketplace detail | **RESOLVED LOCALLY** | Marketplace exposes only authorised listing/resource fields and no longer looks up or promises event context. |
| B-08 | **P2** | Presentation | **RESOLVED LOCALLY** | Active source/docs were repaired and a zero-dependency UTF-8/mojibake scanner now protects both repositories. |

## 2. Status and priority rules

### Overall status

- **FIX REQUIRED** - a known correctness or safety defect must be resolved before acceptance.
- **CONFIG REQUIRED** - implementation exists, but a migration, secret, redirect, public URL, or staging fixture is missing.
- **READY TO VERIFY** - no known code blocker; automated or manual acceptance is still required.
- **ACCEPTED** - all required gates passed and evidence is recorded in this file.

### Priority

- **P0** - correctness, security, data-loss, or test-trust blocker. Do this before deployment or broad manual testing.
- **P1** - required for the core assignment demonstration. Do this after all P0 gates are green.
- **P2** - important regression, clarity, accessibility, and presentation work.
- **P3** - optional polish that must not displace P0/P1 work.

### Checkbox rule

- `[ ]` means not proved, even if source code exists.
- `[x]` means the named result was run and evidence was recorded.
- When a later change invalidates evidence, change `[x]` back to `[ ]` and explain why.
- "Works on my machine" without the command/result or manual scenario is not acceptance evidence.

## 3. Required repair gate - completed locally

These six tasks were completed in dependency order on `codex/must-fix-six` and are backed by the evidence above.

1. [x] **Sync the implementation baseline.** `codex/must-fix-six` contains teammate commit `135387b`; migrations now run sequentially through `0015`.
2. [x] **Repair the Supabase test harness.** `service_role` exists, Marketplace fixtures use the protected RPC, and direct DML denial is tested.
3. [x] **Fix account-deletion recovery safety.** Deletion is terminal and retryable; role/wallet recreation is blocked; Storage pagination and OAuth-only guidance are implemented.
4. [x] **Fix resource-photo persistence.** Metadata, authorised reads, Room snapshot persistence, deterministic replacement, and retryable cleanup are implemented.
5. [x] **Fix Participant Return QR generation.** Passport and Return share canonical renderability rules and never render a raw token.
6. [x] **Add the Room 5-to-6 migration test and rerun every automated gate.** JVM, Android instrumentation, lint, Supabase, and Deno gates pass.

**Start next:** run the physical-device acceptance scenarios in Phase 4/8: install the configured debug build, complete a real password reset, scan a Passport QR, then test photo and deletion UI flows. The staging database and deletion Function are already deployed and backend-verified; do not rerun migrations blindly.

## 4. Module progress dashboard

This table is the authoritative summary. Update it only after updating the detailed phase and evidence log below.

| # | Module | Implementation reality | Environment | Acceptance | Status | Next phase |
|---:|---|---|---|---|---|---:|
| 1 | Onboarding and navigation | Core flow exists | Not required | Not run on teammate update | **READY TO VERIFY** | 9 |
| 2 | Sign-in and role setup | Core flow and reset UI exist | Reset redirect saved in staging | Real email/device flow not run | **READY TO VERIFY** | 8 |
| 3 | Organiser home | Live-data and empty states exist | Staging data required | Not run | **READY TO VERIFY** | 9 |
| 4 | Event management | Create/edit/archive exist; lifecycle-derived resource status is read-only | Staging data required | Not run | **READY TO VERIFY** | 6, 9 |
| 5 | Resource inventory | Persistent one-photo contract, metadata RPCs, snapshot/Room mapping, and cleanup exist | `0013` and owner/non-owner private-bucket rules verified on staging | Backend passed; gallery/camera/offline UI not run | **READY TO VERIFY** | 4, 9 |
| 6 | Marketplace | Discovery/request/publication source exists; SQL gate is green | `0009`/`0010` applied and staging smoke passed | Android flow not run | **READY TO VERIFY** | 5 |
| 7 | Transaction lifecycle | Server-authoritative commands and presentation exist | Earlier staging proof only | New flow not run | **READY TO VERIFY** | 6 |
| 8 | Participant return | Assigned-return flow renders only canonical scanner-compatible QR values | Staging verifier/App Link deployed | Physical scan/action not run | **READY TO VERIFY** | 3, 4 |
| 9 | Digital passports | Privacy-safe token contract, anonymous resolver, and verifier exist | Staging verifier/App Link deployed | Actual-device verification not run | **READY TO VERIFY** | 3, 4 |
| 10 | QR scanner | Camera/manual entry and App Link routing exist | Staging verifier/App Link deployed | Physical scan not run | **READY TO VERIFY** | 3, 4 |
| 11 | Partner workbench | Programme/task/actions source exists | Active programme/task data required | Not run | **READY TO VERIFY** | 7 |
| 12 | Partner map | Live list/filter/detail fallback exists | Active programme data required | Not run | **READY TO VERIFY** | 7 |
| 13 | Matching | Deterministic matching and recovery request exist | Active programme/capacity data required | Not run | **READY TO VERIFY** | 7 |
| 14 | Impact dashboard | Completed-transaction aggregation exists | Completed staging transaction required | Not run | **READY TO VERIFY** | 6, 9 |
| 15 | Offline and account switching | Queue state, retry, and isolation source exists | Failure scenario required | Not run | **READY TO VERIFY** | 6 |
| 16 | Profile, help and deletion | Terminal retry state, protected function, provider guidance, and SQL guards exist | `0011`/`0012`/`0014` and Function deployed | Disposable password-account backend acceptance passed; Compose/OAuth-only UI not run | **READY TO VERIFY** | 8 |

**Current accepted count under this plan:** 0 of 16. This is intentionally stricter than the previous "5 Done / 11 In progress" summary.

## 5. Ordered implementation and acceptance phases

### Phase 0 - Restore a trustworthy automated baseline

- **Priority:** P0
- **Modules affected:** 5, 6, 7, 8, 9, 10, 16
- **Depends on:** a branch containing `135387b`

#### Steps

1. [x] Confirm the working branch contains `135387b` with `git merge-base --is-ancestor 135387b HEAD`.
2. [x] Run the existing Android baseline from `ReEvent/`:
   - `./gradlew :app:testDebugUnitTest :app:compileDebugKotlin`
   - `./gradlew :app:lintDebug`
3. [x] In the PGlite bootstrap, create every role referenced by grants in the migrations, including `anon`, `authenticated`, and `service_role`, before applying migrations.
4. [x] Stop inserting `marketplace_listings` directly in lifecycle fixtures. Create an organiser/resource and call `publish_marketplace_listing`, matching migration `0010`'s security boundary.
5. [x] Apply `0001` through `0015` to a fresh PGlite database in numeric order for every contract-test run.
6. [x] Keep explicit tests proving direct listing/photo DML is rejected while the protected RPC succeeds for an authorised organiser.
7. [x] Run `npm test` from `ReEvent/supabase/tests/`; 20/20 tests pass. Run `npm ci` again from a clean checkout before commit/release handoff.
8. [x] Add an instrumented Room migration case that creates representative version-5 rows, runs `MIGRATION_5_6`, and checks both schema and retained data.
9. [x] Run the targeted migration and complete Android instrumented suite on Medium Phone API 35; 15/15 tests pass.

#### Success looks like

- Android debug compilation and JVM tests pass.
- Android lint has zero errors; warning count is recorded.
- The committed Supabase test command passes from a clean install and fresh database.
- The tests prove both the permitted RPC path and rejected direct-DML path.
- A version-5 installed database opens as version 6 without destructive fallback or lost representative data.

#### Do not proceed if

- A test passes only after a manual SQL edit that is not in source control.
- Fixtures bypass the same RPC/RLS path used by Android.
- The Room test recreates an empty database instead of exercising an upgrade.

### Phase 1 - Make account deletion terminal and retry-safe

- **Priority:** P0 security/data integrity
- **Module:** 16
- **Depends on:** Phase 0 harness repair

#### Steps

1. [x] Add `0012_account_deletion_retry_guard.sql`; do not rewrite applied migration `0011`.
2. [x] Make `deletion_started_at` terminal. `complete_profile_role` rejects a deletion-pending user.
3. [x] Ensure no retry path grants the initial 1,000 ReCoins again and no role can be restored after deletion preparation.
4. [x] Return typed results for blocked work, bad re-authentication, unsupported password re-authentication, finalisation pending, and complete.
5. [x] Route a surviving deletion-pending session to retry/sign-out only, before onboarding, password recovery, or role setup.
6. [x] Make repeated preparation and finalisation retry idempotent; finalisation failure returns a terminal pending result instead of restoring access.
7. [x] Paginate root and nested Storage listing beyond 1,000 objects; two deterministic Deno tests pass.
8. [x] Define the OAuth-only policy: password deletion is unavailable and the app gives a truthful project-support path instead of attempting preparation.
9. [x] Add contract tests for:
   - role completion rejected after `deletion_started_at`;
   - wallet/reward cannot be recreated;
   - repeated preparation is safe;
   - active work blocks deletion without partial de-identification;
   - a finalisation failure maps to the terminal retry route;
   - cleanup handles more than 1,000 objects, or the pagination helper is covered deterministically.
10. [x] Add JVM tests for deletion-pending routing and typed provider/retry outcomes. Manual Compose/provider acceptance remains in Phase 8.
11. [x] Add `0014_account_deletion_deidentification.sql` after staging exposed `IMMUTABLE_RECORD` during Auth deletion. Permit only nested FK-driven identity nulling while keeping direct immutable-history mutation rejected; prove both paths in the contract suite and on staging.

#### Success looks like

- After any failure point, reopening the app cannot create a new role, wallet, or initial reward.
- Retrying completes cleanup without duplicating or corrupting historical records.
- Wrong credentials and blocked-work cases leave the account usable and unchanged.
- A successful request removes private media and Auth access, signs out locally, and retains only the intended de-identified history.

### Phase 2 - Complete resource-photo persistence

- **Priority:** P0 data integrity
- **Module:** 5
- **Depends on:** Phase 0

#### Steps

1. [x] Use one primary photo per resource for assignment scope; older rows exist only as tracked cleanup work.
2. [x] Flush the resource row first, upload one deterministic private object, and commit metadata through protected RPC `replace_resource_photo`.
3. [x] Query authorised `resource_photos` rows in `SupabaseCoreGateway.fetchAuthorisedSnapshot` and group them by resource.
4. [x] Map only the newest metadata row into the domain resource image list.
5. [x] Persist the server-confirmed path in Room so list, detail, Passport, and offline views share one source of truth.
6. [x] Replace bytes at a deterministic path, update metadata idempotently, and retain legacy/replaced rows until Storage deletion and `complete_resource_photo_cleanup` both succeed.
7. [x] Revoke direct metadata DML, enforce owner-only RPC writes, and allow Storage reads only for the owner or an authorised current-photo viewer.
8. [x] Add SQL contract tests for owner/non-owner replacement and cleanup plus JVM tests for primary snapshot mapping. Live upload failure remains part of manual acceptance.
9. [x] On staging, upload real PNG bytes to the deterministic private path, commit metadata through `replace_resource_photo`, download as the authorised owner, replace at the same path, and prove one current metadata row. Prove public download and direct metadata DML are denied; prove account deletion removes metadata and the object.
10. [ ] Run manual acceptance with gallery and camera input, app restart, forced refresh, offline reopen, replacement, and a failed upload retry.

#### Success looks like

- The selected photo remains visible after server refresh, app restart, and navigation across inventory/detail/Passport.
- Replacement shows only the new photo and does not leave an untracked old Storage object.
- A failed upload or metadata write produces a retryable state, not a false "saved" result.
- Another account sees only the deliberately authorised representation.

### Phase 3 - Unify Passport, Return, and scanner QR behavior

- **Priority:** P0 for invalid QR prevention; P1 for physical acceptance
- **Modules:** 8, 9, 10
- **Depends on:** Phase 0

#### Steps

1. [x] Define one shared result for QR presentation: canonical HTTPS payload or an explicit unavailable reason.
2. [x] Build only `https://<PUBLIC_BASE_URL>/p/v1/<opaque-token>` for rendered v1 codes; raw tokens are never passed to `QrCodePanel`.
3. [x] Use the shared result in both Resource Passport and Participant Return; missing configuration disables the QR and explains the problem.
4. [x] Keep legacy UUID parsing read-only and require reissue before rendering it as a return QR.
5. [x] Keep scanner/manual parsing on the same canonical contract with distinct malformed and unavailable/unauthorised outcomes.
6. [x] Add unit tests for canonical URL creation, missing configuration, raw-token canonicalisation, malformed input, legacy input, and wrong-host rejection.
7. [ ] After Phase 4 configuration, scan an on-screen/printed QR between two physical sessions and complete one authorised return action.

#### Success looks like

- Every QR the app displays is accepted by the same app's scanner.
- Missing configuration produces an honest non-QR state.
- A participant's assigned return Passport opens the correct authorised resource and advances only through the server lifecycle.
- Malformed or unauthorised scans reveal no private resource details.

### Phase 4 - Configure one controlled staging environment

- **Priority:** P1 deployment/configuration
- **Modules:** 2, 6, 8, 9, 10, 16
- **Depends on:** Phases 0-3 green

#### Steps

1. [x] Record the staging project reference and exact committed revision. Project ref `kxkdugzyjmoteguesoti` is recorded; the public verifier is versioned separately with its source commit and deployment record. Never place a service-role secret in Android properties, BuildConfig, logs, or this document.
2. [x] Apply migrations in numeric order through `0015`. Live capability queries confirm `0009`-`0015` objects, grants, policies, and server-owned fields are present.
3. [x] Run read-only capability/permission probes and the rollback-only `staging-authority-smoke.sql`; all returned `PASS`. This project was originally built with manual SQL and has no reliable CLI migration-history ledger, so adopt a tracked CLI migration workflow before production promotion.
4. [x] Deploy `delete-my-account` with JWT verification enabled. Unauthenticated and publishable-key-only calls return 401; an authenticated disposable-account flow reaches the Function. The privileged key remains server-side only.
5. [x] Add `reevent://auth/password-reset` to Supabase Authentication Redirect URLs. The allow-list now contains both callback and recovery paths.
6. [x] Configure `PUBLIC_BASE_URL`, deploy the `/p/v1/<token>` verifier, deploy `0015_public_passport_resolver.sql`, and configure Android App Link/intent handling for that host. The verifier's anonymous RPC returns only title/category/material/condition/status/latest public event; its `assetlinks.json` contains the debug certificate. Add the release signing-certificate SHA-256 before a release build.
7. [x] Verify the private resource-photo bucket and metadata-table policies with an owner and unauthenticated/direct-DML probes. Authorised upload/download/replacement succeed; public read and direct table mutation fail.
8. [ ] Prepare reusable disposable organiser, participant, and partner accounts plus one active event, resource, programme, and eligible listing for Android acceptance. The destructive backend fixtures were deleted and must not be reused.
9. [x] Record the live failure and recovery path: pre-`0014` Auth deletion returned `FINALIZATION_PENDING` because immutable-history triggers blocked FK de-identification; `0014` fixed only nested FK identity nulling, the same pending request retried to `DELETED`, and direct history mutation remains rejected.

#### Staging deployment record - 2026-08-11

- **Project:** `ReEvent-staging` (`kxkdugzyjmoteguesoti`).
- **Database source:** `0009`-`0014` from the current working tree; migration SHA-256 prefixes are `95346789D034`, `9E87713DBF37`, `C2DC3B493DCF`, `3FE2D102653A`, `801F1132DFAF`, and `AD072B4E7850` respectively.
- **Function source:** `delete-my-account/index.ts` `B6531FB387FD`; `storage-cleanup.ts` `378CF87D7076`.
- **Live deletion proof:** wrong password -> `FRESH_REAUTHENTICATION_REQUIRED`; active resource -> `BLOCKED_ACTIVE_RESOURCES`; forced pre-fix finalisation failure -> terminal `FINALIZATION_PENDING`; post-`0014` retry -> `DELETED`; old token -> 403; new sign-in -> 400.
- **Live cleanup proof:** Auth user, profile, resource-photo metadata, and private Storage object are absent; the archived resource remains with `created_by` and `current_owner_id` null.
- **Promotion warning:** dashboard SQL application proves live state but does not create a trustworthy migration ledger. Do not promote by replaying ad hoc SQL; first commit this source and establish tracked migration history.

#### Immediate manual validation handoff

Run these in order and add one evidence-log row per numbered scenario. Use disposable staging accounts and keep screenshots free of email addresses, tokens, and keys.

1. [x] **Finish configuration first.** The reset redirect, `PUBLIC_BASE_URL`, `/p/v1/<token>` verifier, anonymous resolver, and debug Android App Link are live. Expected: a clean build has no QR/reset feature that depends on an unstated local value.
2. [ ] **Clean-install authentication.** Install the staging debug build, create one disposable user, select each role with separate accounts, sign out/in, and complete password reset from the email link. Expected: the link returns to ReEvent, the session is recovered once, and no other account's cached data appears.
3. [ ] **Gallery and camera photo persistence.** As an organiser, create an active event/resource with a gallery photo, force refresh, kill/reopen the app, open list/detail/Passport, then repeat with a camera photo and replace it. Expected: the same current image appears everywhere and survives restart; replacement leaves one visible image.
4. [ ] **Photo failure/offline behavior.** Disable networking immediately before one upload/save, observe the retryable failure, reopen offline, then reconnect and retry once. Expected: the UI never claims an uncommitted photo was saved, the prior confirmed photo remains, and retry creates no duplicate metadata/object.
5. [ ] **Passport/Return QR interoperability.** Display a configured Passport QR on one physical session and scan it from another; repeat from Participant Return, then try malformed text and a wrong-host URL. Expected: both app-generated codes resolve to the authorised resource/action, while malformed/wrong-host/unauthorised input reveals no private resource details.
6. [ ] **Account-deletion UI.** With a disposable password account, keep one active resource and request deletion, then archive/finish it; retry once with a wrong password and once with the correct password. Expected: active work blocks without changing access, wrong password preserves the account, correct password signs out, and subsequent sign-in fails. Also open an OAuth-only account and confirm the app shows the truthful support/unavailable path rather than asking for a password.
7. [ ] **Supabase visual review.** In Authentication verify the deleted disposable user is gone; in Storage verify its folder is gone; in Edge Function Logs confirm the request sequence has no secret/password output; rerun `supabase/tests/staging-authority-smoke.sql` and expect one `PASS` row with all fixtures rolled back.

#### Success looks like

- A clean staging install can reset a password through the email link.
- The public Passport URL opens the app or safe verifier without exposing a raw resource/account ID.
- Android never contains the service-role key.
- Owner/non-owner policy probes produce the expected allow/deny results.
- The exact deployed migration/function versions are recorded in the evidence log.

### Phase 5 - Accept Marketplace publication and request

- **Priority:** P1 core demo
- **Module:** 6
- **Depends on:** Phases 0 and 4

#### Steps

1. [x] Resolve review item B-07 locally: Marketplace shows authorised resource/listing fields only and makes no event-context promise.
2. [ ] As organiser, publish a Donate-only listing from one owned, active, unlisted resource.
3. [ ] Publish a Borrow or Rent listing with valid duration and price rules.
4. [ ] Prove local and server rejection for: no action, zero/excess quantity, fractional ITEM/BOX quantity, missing price/duration, excessive terms, non-owner, archived resource, and duplicate open listing.
5. [ ] As participant, search/filter the listing, inspect all actually available terms/context, and submit a valid request.
6. [ ] As organiser, approve one request and decline/cancel another; refresh both accounts after each result.
7. [ ] Interrupt one publication response, refresh before retrying, and prove there is never more than one open listing for the resource.

#### Success looks like

- Publication is possible only through the protected RPC and only for an eligible owned resource.
- Both accounts see the same server-returned listing/request status after refresh.
- An ambiguous/lost response does not create a duplicate.
- The UI does not promise event data that RLS does not provide.

### Phase 6 - Accept lifecycle, event status, offline queue, and impact

- **Priority:** P1 core demo
- **Modules:** 4, 7, 14, 15
- **Depends on:** Phases 0, 4, and 5

#### Steps

1. [x] Remove the Event Detail "Update status" selector and show read-only server state; a local semantic test protects the lifecycle-derived explanation.
2. [ ] With fresh organiser/participant accounts, run Request -> Approve -> Handover -> Receipt -> Return started -> Return confirmed -> Completed.
3. [ ] At every step, verify both accounts see the server-returned state, exactly one responsible role, and only the permitted action.
4. [ ] Verify rejected and cancelled requests expose no later completion action.
5. [ ] Disconnect before one permitted action. Confirm the card says pending/failed and does not claim the server state changed.
6. [ ] Restore network, use Profile -> Retry sync, refresh, and confirm the server result replaces the queued guidance without duplicate settlement.
7. [ ] Sign out and into another account while work is queued; prove cached data and commands never cross account/environment boundaries.
8. [ ] Complete the transaction and verify event-scoped impact, reward, and newest contribution update exactly once.

#### Success looks like

- No visible control performs a no-op or guesses a lifecycle transition locally.
- The two accounts agree at every stage after refresh.
- Retry is idempotent and the final reward/impact is created once.
- Account switching reveals no previous account's cached records or queued commands.

### Phase 7 - Accept the partner journey

- **Priority:** P1 assignment journey
- **Modules:** 11, 12, 13
- **Depends on:** Phases 4 and 6

#### Steps

1. [ ] Seed one active programme with supported material/service-area data and capacity for the chosen resource.
2. [ ] As organiser, open Partner Map, filter by material, open programme detail, and navigate to the eligible resource Passport.
3. [ ] Open Matching and verify the visible inputs, deterministic reason, and honest no-match states for incompatible/inactive programmes.
4. [ ] Submit a confirmed recovery request and verify the server, not the client, enforces current capacity.
5. [ ] As the assigned partner, open Workbench, inspect the task/Passport, accept or decline as appropriate, and confirm receipt after handover.
6. [ ] Verify an unrelated partner cannot see or act on the task.
7. [ ] Refresh Workbench and Impact after the action and verify the visible result.

#### Success looks like

- Map/list -> programme -> Passport -> matching -> recovery request -> Workbench is one unbroken, real-data journey.
- Capacity/authorization failures are truthful and do not advance local state.
- Only the assigned partner receives the task and permitted actions.

### Phase 8 - Accept authentication, role, profile, and deletion

- **Priority:** P1 trust journey
- **Modules:** 2 and 16
- **Depends on:** Phases 1 and 4

#### Steps

1. [ ] Create a new email account; verify malformed email, weak password, and mismatch are blocked without clearing input.
2. [ ] Complete confirmation if enabled, choose a role, sign out, and sign back in. Confirm the persisted role opens the correct home.
3. [ ] Request a password reset, open the real email link on device, set a valid replacement, and sign in with it.
4. [ ] Verify the selected OAuth-only deletion policy with a Google-only account.
5. [ ] With a disposable eligible account, try wrong phrase/password and confirm no change; then complete deletion and verify Auth, private Storage, local session, and de-identified history outcomes.
6. [ ] For each blocked condition - active transaction/custody, open listing, active programme, unsettled hold - verify deletion is rejected before destructive preparation and gives a safe resolution.
7. [ ] Simulate a finalisation failure after preparation, restart the app, verify the deletion-pending route, then retry to completion without new role/wallet/reward creation.

#### Success looks like

- Sign-up, role restore, sign-in, and email password recovery work without developer intervention.
- Provider-specific deletion UI asks only for credentials the account actually has.
- Every failed deletion path is either unchanged and usable or explicitly deletion-pending and safely retryable.
- Successful deletion makes future sign-in impossible and leaves no private media.

### Phase 9 - Accept organiser basics and assignment presentation

- **Priority:** P2 regression/completeness
- **Modules:** 1, 3, 4, 5, 9, 14
- **Depends on:** all P0/P1 phases relevant to those modules

#### Steps

1. [ ] First-time navigation: Welcome -> Sign in -> Role setup -> correct home, with no blank screen or Back path into onboarding.
2. [ ] Organiser empty state: no event/resource counts are invented, and each action opens the correct editor.
3. [ ] Create, edit, view, and archive an event; verify date/location validation, input preservation on failure, linked resources, and archive confirmation.
4. [ ] Create/edit/archive an eligible resource and verify the server blocks archive while an active transaction exists.
5. [ ] Check Passport owner labels and unit formatting for `1 item`, `11 items`, `1 box`, `11 boxes`, `2 kg`, `2.5 kg`, and `0.125 kg`; no raw owner UUID may be displayed.
6. [ ] Verify organiser Impact switches event scope correctly and labels estimates/missing factors honestly.

#### Success looks like

- A marker can follow the organiser journey without sample data, dead controls, raw IDs, or unexplained validation.
- List, detail, Passport, Marketplace, and Impact agree after refresh.

### Phase 10 - Polish only after functional acceptance

- **Priority:** P2/P3
- **Depends on:** P0 and P1 complete

#### Steps

1. [x] Repair corrupted active Kotlin/Markdown/TSX/XML/CSS text and add `scripts/check-encoding.mjs` for both repositories.
2. [x] Reduce lint to 15 policy-deferred SDK/dependency-version warnings; source, adaptive, modifier, QR, icon, and resource findings are resolved.
3. [ ] Verify loading, empty, offline, error, and success states at compact and expanded widths for the demo-critical screens.
4. [x] Remove unused runtime mock assets after the zero-caller/resource audit; retained previews use debug-only fixtures and all automated gates pass.
5. [ ] Capture final screenshots and a short demo script only after all relevant module rows are ACCEPTED.

#### Success looks like

- No visible encoding corruption or placeholder control remains.
- Demo-critical screens are readable and actionable on the target device sizes.
- Documentation and screenshots describe the behavior that was actually accepted.

## 6. Decisions that need explicit review

Record each decision before implementing around it; otherwise different contributors can build incompatible assumptions.

| Decision | Recommended choice | Why it needs review |
|---|---|---|
| D-01: account deletion after preparation failure | Treat `deletion_started_at` as terminal; allow finalisation retry/sign-out only. | Restoring role/home can recreate privileges and rewards on a partially deleted account. |
| D-02: OAuth-only account deletion | Implement provider re-auth if feasible; otherwise show a truthful support/unavailable path and do not ask for a password. | Google-only users may have no current password. |
| D-03: resource photo cardinality | Use one primary photo for assignment scope unless the UI truly supports a gallery. | Metadata, replacement, ordering, and cleanup differ significantly for one versus many photos. |
| D-04: Marketplace event context | **Chosen:** remove event context and expose only authorised resource/listing fields. | Current participant RLS does not supply the event object used by owner views. |
| D-05: Event Detail status | **Chosen:** read-only derived status until a specific server-authorised transition is required. | A visible no-op/manual status selector undermines the server-authoritative lifecycle. |
| D-06: public Passport behavior | The URL may identify only an opaque token and must reveal nothing until authorization is established. | Public QR convenience must not leak resource/account identifiers. |

## 7. Module completion checklist

For each module, copy this checklist into the evidence log or a linked issue. A module moves to **ACCEPTED** only when every applicable item is checked.

- [ ] Implementation steps for the module are complete.
- [ ] Focused unit/contract/migration tests pass.
- [ ] Full Android and Supabase gates still pass.
- [ ] Required migration, redirect, secret, bucket, public URL, and staging data are configured.
- [ ] Happy path passes on the target Android device/session.
- [ ] At least one relevant validation/authorization/error path passes.
- [ ] Refresh/restart shows the same server-authoritative result.
- [ ] Offline/retry/account-switch behavior is checked where applicable.
- [ ] No private ID, secret, or unauthorised data is shown.
- [ ] Evidence is dated and recorded below.

## 8. Evidence log

Add rows; do not replace failed evidence with an unsupported claim. Link an issue, screenshot, test report, or commit when available.

| Date | Commit/build | Environment/device | Module/scenario | Result | Evidence/notes |
|---|---|---|---|---|---|
| 2026-08-11 | `codex/must-fix-six` working tree | Local JVM | Debug compile + JVM tests | Pass, 65/65 | Includes deletion route/outcome, QR renderability, and photo snapshot tests. |
| 2026-08-11 | `codex/must-fix-six` working tree | Medium Phone AVD API 35 | Full Android instrumented suite | Pass, 15/15 | Includes `migrate5To6_preservesResourcesAndLifecycleCommandsWhileAddingMarketplaceProjection`. |
| 2026-08-11 | `codex/must-fix-six` working tree | Local lint | Android lint | Pass, 0 errors / 29 warnings | Warnings remain for Phase 10 review. |
| 2026-08-11 | `codex/must-fix-six` working tree | Fresh PGlite | Migrations `0001`-`0014` and contract tests | Pass, 19/19 | Includes protected Marketplace/photo DML, deletion terminality/idempotency, active-work no-mutation, Auth-deletion de-identification, and continued direct-history-mutation denial. |
| 2026-08-11 | `codex/must-fix-six` working tree | Deno 2.9.5 | Edge Function check + Storage pagination tests | Pass, check + 2/2 | Pagination covers 1,001 root and nested objects. |
| 2026-08-11 | staging-bound debug APK | Medium Phone AVD API 35 | Clean install and cold launch to Welcome -> Sign in | Pass | `assembleDebug`, streamed install, cold `MainActivity` launch, Welcome navigation, email/password form, and no fatal/configuration-incomplete log were verified. This is a launch smoke, not physical-device acceptance. |
| 2026-08-11 | working tree on `135387b` | `ReEvent-staging` | Migrations `0009`-`0014`, capabilities, permissions, rollback-only authority smoke | Pass | Final smoke returned `PASS` after `0014`; listing/photo direct-DML denial, protected RPCs, three roles, lifecycle replay, and rollback were exercised. |
| 2026-08-11 | Function hashes recorded in Phase 4 | `ReEvent-staging` | Private resource-photo upload/read/replace/delete | Pass | Real PNG bytes and metadata matched; public read/direct DML failed; replacement stayed singular; deletion removed the row and object. |
| 2026-08-11 | Function hashes recorded in Phase 4 | `ReEvent-staging` | Disposable password-account deletion and retry | Pass | Wrong password and active work caused no mutation; pre-fix finalisation stayed terminal; `0014` retry returned `DELETED`; Auth/profile/media cleanup and retained de-identification all verified. |
| 2026-08-12 | public verifier version 2 | `ReEvent-staging` + public site | Reset redirect, public Passport resolver/verifier, Android App Link configuration | Pass | Supabase allow-list has both `reevent://auth/callback` and `reevent://auth/password-reset`; `0015` succeeded; anonymous unknown-token RPC returned `200 []`; a staged valid passport rendered its public title without internal IDs; hosted `assetlinks.json` returned JSON and matched the debug APK's certificate. |
| 2026-08-13 | local working tree | Local JVM/lint/web | B-06/B-07/B-08 cleanup and regression coverage | Pass | Android unit/Compose tests passed 73/73, debug Android-test sources compiled, debug/release APKs assembled, lint passed with 0 errors and only 15 policy-deferred version warnings, all 20 Supabase contracts passed, and website clean install/lint/typecheck/build plus 3/3 rendered-route tests passed. Manual/device acceptance remains unchanged. |
| 2026-08-13 | local working tree | Local build artifacts | Cleanup performance/size comparison | Improved | Debug APK: 75,043,775 -> 75,043,131 bytes; drawable source: 19,017,415 bytes/12 files -> 4,249,413 bytes/5 files; website server bundle: 1,325,129 -> 1,317,937 bytes; website direct dependencies: 20 -> 18. |
| TBD | TBD | Staging + target Android device | First P0/P1 acceptance | Not run | Record account roles, seed IDs (non-secret), exact steps, and screenshot/test report link. |

## 9. Final assignment acceptance sequence

After Phases 0-10 are complete, run one clean end-to-end demonstration in this order:

1. New user signs up, confirms, selects a role, signs out/in, and proves password recovery.
2. Organiser creates an event and resource with a persistent photo.
3. Organiser publishes the resource; participant discovers and requests it.
4. Organiser approves/hands over; participant confirms receipt.
5. Participant opens/scans the canonical Passport QR and starts return; organiser confirms return.
6. Completion creates exactly one reward/impact contribution visible in the correct event scope.
7. Organiser matches another eligible resource to a real partner; assigned partner completes its authorised Workbench action.
8. One offline command visibly queues, retries, and reconciles without duplicate effects or account-data leakage.
9. A disposable account proves blocked, failed-finalisation/retry, and successful deletion behavior.

**The assignment is complete when:** all 16 module rows are **ACCEPTED**, the full automated baseline is green from a clean checkout, the staging configuration is recorded, and the final sequence can be demonstrated without SQL edits, fake data substitutions, dead controls, or developer intervention.

## 10. Historical trackers

The granular production-release audit is archived at [`archive/historical-plans/REEVENT_RELEASE_TRUTH_CHECKLIST.md`](../archive/historical-plans/REEVENT_RELEASE_TRUTH_CHECKLIST.md). It does not control assignment progress. Keep historical scope there; keep current priorities, proof, and next actions in this file.
