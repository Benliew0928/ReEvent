# ReEvent Assignment Progress

**Use this file for day-to-day progress.** It tracks what can be shown in the Android assignment: modules, pages, and user-visible functions. It deliberately does **not** count release pipelines, AppGallery work, legal operations, or every production test case.

**Current assignment view:** 5 modules are **Done** and 11 are **In progress**. The server-authoritative transaction stage is **done** for assignment scope and recorded below.

**Current local implementation note (2026-08-11):** The Participant Return, QR Scanner, Digital Passport, Partner Workbench, Partner Map, Matching, Resource Photo, Password Recovery, Profile/Support, visible Sync Status, protected account-deletion client/server source, Event Management validation, organiser Marketplace publication, Organiser-home empty states, per-transaction lifecycle feedback, and impact event-scope/latest-contribution presentation are implemented in the current working tree. They have not yet received a manual Android acceptance run with real role accounts and staging data. Matching, password rules, sign-up validation, event-form validation, Marketplace request/publication rules, account-deletion validation, transaction lifecycle presentation, impact aggregation, and passport-QR parsing have focused unit-test coverage and the debug Kotlin compilation succeeds; this is not a substitute for the pending manual checks.

## Status key

- **Done** — working and ready to show in a demo.
- **In progress** — screen/function exists, but needs a visible gap closed.
- **Needs work** — present as a mock, placeholder, or missing flow.

## Modules

| Module | Status | What is currently there | Next product task |
|---|---|---|---|
| Onboarding and navigation | **Done** | Welcome flow, typed role navigation, account-specific back stacks | Keep polish work small unless a flow breaks |
| Sign-in and role setup | **In progress** | Email sign-in/sign-up with local form validation/confirmation state, session restore, role selection, and full password-reset UI/deep-link path | Add the Supabase reset redirect URL and run the email-link acceptance pass |
| Organiser home | **Done** | Live event/resource/impact dashboard, honest no-event/no-resource states, and real quick actions | Run one organiser acceptance pass after a server save |
| Event management | **Done** | Event list, create/edit validation, detail with linked resources, and confirmed archive action | Run the organiser create/edit/archive acceptance pass |
| Resource inventory | **In progress** | Add/edit validation, safe metadata editing, photo selection/camera capture, private upload/replacement, and shared list/detail/passport thumbnails | Run the photo acceptance pass and define the archive rule |
| Marketplace | **In progress** | Published listing discovery/request flow and organiser publication form/RPC are implemented locally | Apply migrations and run a two-account acceptance pass |
| Transaction lifecycle | **In progress** | Server-authoritative request, approve, handover, receipt, return, settlement, impact and retry-safe completion; cards now show state, responsible role, permitted action and queued-command feedback | Demonstrate the full flow through two signed Android sessions, including offline retry |
| Participant return | **In progress** | Assigned return passport QR, scanner entry point, authorised return actions, and role-specific waiting/confirmed guidance are wired through repository data | Run the real two-account return journey |
| Digital passports | **In progress** | Versioned privacy-safe QR parsing/rendering, role-aware passport screen and newest-first history | Configure public verifier/App Links and run the two-account QR acceptance pass |
| QR scanner | **In progress** | Camera/scanner screen and scan routing exist | Complete physical scan-to-authorised-action journey |
| Partner workbench | **In progress** | Partner workspace and programme foundations exist | Finish real recovery handover and passport navigation |
| Partner map | **In progress** | Live partner-programme list, material filter, programme detail, and eligible-resource passport action | Populate a real active programme and confirm the visible programme-to-passport journey |
| Matching | **In progress** | Deterministic matching uses resource inputs, material/location ranking, clear no-match reasons, and confirmed recovery requests | Populate a real programme and manually verify the server-authoritative capacity outcome |
| Impact dashboard | **Done** | Completed lifecycle creates real impact/reward data; organiser impact is scoped per event and identifies its newest valid contribution | Run an organiser completion-to-dashboard acceptance pass |
| Offline and account switching | **Done** | Room cache, durable command queue, account/environment isolation, sign-out cleanup, visible queue state and user-initiated retry | Run the offline/failed/retry acceptance pass |
| Profile, help and deletion | **In progress** | Signed-in account details, data explanation, password-reset entry, support/privacy guidance, plus a re-authenticated deletion dialog, migration and Edge Function source | Deploy the protected server path, then run the disposable-account removal acceptance pass |

## Module-by-module completion plan

Work through the modules in the order shown inside each section. A checked item should mean the behaviour is visible in the Android app and can be demonstrated; it does **not** require a production deployment checklist. A **Done** module is already core-demo-ready: its items are only small verification or polish tasks, not a reason to reopen its scope unless a real issue is found.

### 1. Onboarding and navigation — Done

**Current gap:** No known functional blocker. This needs only a short regression pass while other screens change.

1. [ ] Confirm a first-time user reaches Welcome, Sign in, then Role setup without a blank screen.
2. [ ] Confirm the chosen role opens its correct home screen and Back cannot return to onboarding.
3. [ ] Keep every new page wired through the existing typed route graph in `ReEventApp.kt`.

**Mark complete when:** the three role journeys open the correct starting page and each new page has a working Back destination.

### 2. Sign-in and role setup — In progress

**Current gap:** Sign-up now blocks malformed email, weak password, and mismatched confirmation locally; account confirmation, role persistence and credential feedback are visible in-app. Password recovery has a request screen, a dedicated deep-link recovery state, replacement-password validation, success/failure guidance, and a safe cancel/sign-out path. It still needs a real-device email pass. Before that pass, add `reevent://auth/password-reset` (or a suitably scoped `reevent://**`) to the Supabase project's Authentication Redirect URLs.

1. [x] Make Sign up validate name, email, password and password confirmation before submitting.
2. [x] Show a clear result after account creation: signed in immediately, or "check your email" when confirmation is required.
3. [x] Add Forgot password: request reset email, open the update-password state, validate the replacement password and show success/failure. (Manual Supabase email-link acceptance is still pending.)
4. [x] Persist the selected role only after its save succeeds; show an actionable error if it fails.
5. [x] Add loading, offline and invalid-credentials messages that leave the entered email visible.

**Mark complete when:** a new user can create an account, choose a role, sign out, sign back in, and recover a password without developer help.

### 3. Organiser home — Done

**Current gap:** The dashboard selects the signed-in organiser's current event, derives metrics/activity from that event, and supplies meaningful no-event/no-resource actions. The remaining work is one server-backed organiser acceptance pass.

1. [x] Drive summary cards and recent activity from the signed-in organiser's data, not sample counts.
2. [x] Give each empty card one useful next action: create event or add resource; listing discovery remains in Marketplace.
3. [x] Wire every quick action to the real editor/list screen; a post-save return is pending manual Android acceptance.

**Mark complete when:** a new organiser sees an honest empty state and a returning organiser sees their own current events/resources.

### 4. Event management — Done

**Current gap:** Create, edit and detail flows now validate title, location and ISO dates; archive requires a clear confirmation. The remaining work is a manual organiser acceptance pass with server save/failure states.

1. [x] Validate title, dates and location before save; reject an end date before the start date with an inline explanation.
2. [x] Preserve typed values while a save is pending or fails; shared sync/error feedback explains the result.
3. [x] In Event detail, show linked resources and provide a direct path to add or edit them.
4. [x] Require a confirmation before archive and explain that resource marketplace visibility remains controlled per resource.

**Mark complete when:** an organiser can create, edit, view and archive an event with understandable validation and feedback.

### 5. Resource inventory — In progress

**Current gap:** The code path now covers image selection, camera capture, authenticated private upload/replacement, visible upload/retry guidance, stored thumbnails, and an archive guard that matches the server lifecycle rule. It has not been manually accepted on Android with the configured Supabase Storage bucket or an active transaction.

1. [x] Make name, category, quantity/unit and condition mandatory, with plain-language validation.
2. [x] Keep metadata edits separate from lifecycle actions: request/approval/return state must be refreshed from the server rather than locally guessed.
3. [x] Add an image picker, camera capture, upload/replacement progress and a visible thumbnail on the resource detail/passport screens.
4. [x] Define a simple archive rule: disable archive while a resource has an active transaction, explain why, and require confirmation before archiving an eligible resource.
5. [x] Use the same saved resource record for list, detail and passport name, condition, availability and current photo.

**Mark complete when:** an organiser can add a resource with a photo, edit safe fields, and understand why an in-use resource cannot be removed.

### 6. Marketplace — In progress

**Current gap:** Marketplace cards derive from cached published server listings; organisers can now select an owned active resource, enter validated terms, and invoke the protected publish RPC. This remains **In progress** until migrations `0009` and `0010` are applied and the two-account Android journey is accepted on real Supabase data.

#### Planned implementation: organiser listing publication

**Development steps (Android and migration files):**

1. Add one typed `MarketplaceListingDraft` and pure validation rules. A draft must select at least one marketplace action, publish a positive quantity no greater than the resource quantity, use whole quantities for ITEM/BOX, require Buy/Rent prices only when those actions are selected, require a 1–365-day duration for Borrow/Rent, and limit terms to the server maximum.
2. Add focused unit tests for accepted drafts and every invalid boundary. These tests must not require a Supabase account.
3. Add an organiser-only **Publish to marketplace** entry from their active resource context. It must never be shown for another user's resource, an archived/non-active resource, or a resource that already has an open listing.
4. Build an accessible publish dialog/form with selected action chips, quantity, optional prices, duration, terms, inline validation, loading state, and failure guidance that preserves typed values.
5. Add a server-authoritative Supabase RPC migration for publication. It must re-check authenticated organiser ownership, active resource state, available quantity, listing terms, and no existing open listing. The Android client must call this RPC rather than write listing rows directly.
6. Refresh the authorised Room snapshot after a successful publish so the organiser sees the listing card and another account can discover it. A failed/ambiguous response must tell the user to refresh/check their listings rather than blindly create a duplicate.

**Expected deliverables:**

- `MarketplaceListingDraft` validation and unit tests.
- Organiser publish dialog/form wired to an owned resource.
- Repository/gateway method that invokes the publish RPC and refreshes the shared snapshot.
- A sequential Supabase migration (after `0009`) defining the protected publication RPC.
- Updated Marketplace status and an explicit manual acceptance checklist.

**Manual Supabase and Android acceptance after code completion:**

1. In Supabase SQL Editor or CLI, apply `0009_listing_default_due_date.sql`, then the new publish-listing migration, in numeric order.
2. Confirm the organiser account has role `ORGANIZER`, owns an `ACTIVE` resource, and has no open listing for it.
3. On Android, publish a Donate-only listing; then publish/test a Borrow or Rent listing with a duration and the required price where applicable.
4. Confirm an invalid form cannot submit: zero/excess quantity, fractional ITEM/BOX quantity, no action, missing price, missing duration, or terms longer than 2,000 characters.
5. Sign in as a participant: search/filter the new listing, inspect its terms, submit one valid request, then sign in as organiser to approve or decline and verify both accounts refresh.
6. Retry after a deliberately interrupted network request. Refresh the marketplace before retrying and confirm there is never more than one open listing for the resource.

1. [x] Load only published listings from the active account snapshot; show an honest empty state when none match.
2. [x] Make search, category and action filters work from the real published-listing fields.
3. [x] On Listing detail, show published quantity, condition, material/category, event context/dates, allowed actions, price, duration and terms.
4. [x] Validate request type and quantity against published terms before sending; the server repeats all checks and now derives Borrow/Rent due dates via migration `0009`.
5. [x] Refresh the shared repository after a request or organiser decision; lifecycle cards distinguish pending, approved, declined and cancelled states.
6. [x] Let an organiser enter one valid owned active resource publication through the app; local validation and the protected server-RPC migration reject invalid, duplicate, or unauthorised publication. (Applying migration `0010` and real Supabase acceptance remain manual.)

**Mark complete when:** an organiser can publish one eligible resource in-app, a participant can find and request it, and both parties see the same resulting status after the server refresh.

### 7. Transaction lifecycle — In progress

**Current gap:** The Android cards now derive their state wording, next responsible person, permitted action, and pending/failed-command guidance from one shared model. The transaction command remains server-authoritative: a queued command never advances the visible server state locally. Supabase setup/migrations and the manual two-account/offline acceptance are still required before this module can be marked Done.

#### Planned implementation: per-transaction lifecycle feedback

**Development steps (Android only):**

1. Define one pure, tested presentation model that maps a server `CircularTransaction`, signed user, and any matching queued-command state to: visible status label, next-step explanation, current responsible role, permitted action label (if any), and sync-feedback state.
2. Cover Request, Approve, Handover, Receipt, Return started, Return confirmed, Rejected, Cancelled and Completed outcomes. Unknown/unauthorised roles must receive explanation only, never an action shortcut.
3. Match pending/failed lifecycle commands to the correct transaction without exposing idempotency keys or treating a queued command as a server success. A failed command must direct the user to the existing Retry sync action.
4. Update Marketplace and Partner transaction cards to render the shared model consistently: state, next person, permitted action, and a short pending/failed message. Reuse the existing `FeatureViewModel` lifecycle commands; do not add direct database mutations.
5. Add focused unit tests for role/status mapping and command feedback. Tests must run without a Supabase account.

**Expected deliverables:**

- Pure `TransactionLifecyclePresentation` model/rules with unit tests.
- Transaction cards showing a truthful status, next step and responsible role.
- Per-transaction pending/failed command guidance linked to the existing Profile Retry action.
- No new client-side lifecycle state transitions or server API changes.
- Updated tracker status and explicit manual two-account/offline acceptance steps.

**Manual Android acceptance after code completion:**

1. Use a fresh resource and two signed accounts to request, approve, begin handover, confirm receipt, begin return and confirm return. At every stage, check that both accounts see the server-returned state and exactly one next responsible person.
2. Test a rejected and a cancelled request. Confirm neither account sees a completion action afterwards.
3. Disconnect network before one permitted action. Confirm the relevant card says pending or failed, does not claim server success, and directs the user to Profile → Retry sync.
4. Restore network, choose Retry sync, refresh the affected card, and confirm the real server state replaces the pending/failed guidance.
5. Include one Partner recovery handover and confirm Partner-only receipt guidance never appears for an unrelated user.

1. [x] Keep each lifecycle button on the existing durable, typed command path; do not add client-side status shortcuts.
2. [x] Display the server-returned state after request, approval, handover, receipt, return and completion through one shared card model.
3. [x] Hide actions the current signed user is not allowed to perform and explain the next permitted action/responsible role.
4. [x] Match a temporary failure or pending command to its transaction, show truthful guidance, and direct the user to Profile → Retry sync rather than claiming completion.
5. [ ] Run one manual two-account Android demonstration using a fresh resource: participant requests/receives/returns; organiser approves/hands over/completes.

**Mark complete when:** the full lifecycle is shown through two signed Android accounts and the final settlement/reward is visible without manual database changes.

### 8. Participant return — In progress

**Current gap:** The runtime screen displays the assigned resource's real passport QR rather than a fake panel, and explains the waiting, organiser-confirmation, and completed states. The implementation has not yet received a manual Android check with a Participant's active transaction, and still needs a two-account confirmation that scanning works through the live lifecycle.

1. [x] Remove the runtime fake QR panel from the participant return journey.
2. [x] Load the participant's active received/returnable transaction and its assigned resource passport from the signed account.
3. [x] Render the real, canonical passport QR payload using the shared QR component.
4. [x] Provide a clear return action: scan the organiser/resource code or open the assigned passport, then submit the authorised return command.
5. [x] Show waiting, confirmed and failure guidance, including that the organiser must confirm a started return.

**Mark complete when:** a participant can open their assigned passport, use a real QR/return action, and see the transaction move to the next lifecycle state.

### 9. Digital passports — In progress

**Current gap:** Passport QR output now follows the configured HTTPS `/p/v1/<opaque-token>` contract, with no resource/account identifier in new codes. The Passport view shows authorised viewer context and newest-first server history, with privacy-safe owner wording and unit-aware quantity labels. A real verifier host, App Link configuration, two-account Android scan, and the Android presentation checks below are still needed for final acceptance.

#### Planned refinement: readable ownership and unit-aware quantities

**Development steps (Android only):**

1. Add one pure, tested resource-presentation helper. It must render whole-count units (`item`/`items`, `box`/`boxes`, including the existing plural unit values) without a trailing `.0`, render `kg` using only meaningful decimal places, and preserve the stored numeric value without rounding it for business logic.
2. Use that helper for every read-only resource quantity shown in the current organiser inventory, Marketplace, listing detail, recovery guidance, and passport paths. Numeric form fields may remain numeric inputs, but their “up to”/summary labels must use the same helper.
3. Replace the Passport's raw `ownerId`-derived visual text with a privacy-safe viewer-relative label. It may say **You** or **Your organisation** only when the signed-in user owns the resource; otherwise it must use a neutral label such as **Owner identity protected**, never a database ID or guessed personal name.
4. Keep this a presentation-only change: do not alter Supabase schema, ownership checks, quantities, transactions, passport payloads, or Room data.
5. Add unit tests for count units, `kg` decimal trimming, singular/plural wording, and each allowed owner-label outcome. The tests must run without Supabase.

**Expected deliverables:**

- A reusable, deterministic resource display helper with focused unit tests.
- Passport metadata that never exposes a raw owner UUID.
- Consistent quantity labels across the live resource and Marketplace views, including `11 items` rather than `11.0 items` and `2.5 kg` rather than padded decimals.
- No database migration or behavioural change to inventory/lifecycle rules.

**Manual Android acceptance after code completion:**

1. Open an owned organiser resource Passport and confirm the owner wording is **Your organisation** rather than an ID.
2. Open the same resource from a non-owner account (or a cached non-owner listing) and confirm no raw UUID or invented name appears.
3. Check an item/box resource with quantities `1` and `11`: labels must read `1 item`/`1 box` and `11 items`/`11 boxes`, with no `.0`.
4. Check `kg` resources with `2`, `2.5`, and `0.125`: labels must read `2 kg`, `2.5 kg`, and `0.125 kg` wherever the quantity is presented.
5. Confirm editing, publishing, requesting, and lifecycle actions retain their original stored quantities and validation behaviour.

1. [x] Define one versioned passport payload: the configured HTTPS `/p/v1/<opaque-token>` URL contains no resource/account identifier or private data. Legacy UUID codes are accepted only for read-only migration compatibility.
2. [x] Generate and display that exact payload as a real QR on the passport screen when `PUBLIC_BASE_URL` is configured; otherwise show a truthful configuration message instead of rendering an opaque token.
3. [x] Load the passport's resource details and lifecycle history from the authorised server snapshot, ordered newest first.
4. [x] Show whether the viewer is organiser/owner, current holder, assigned partner, requester, or marketplace viewer; only an active organiser-owner sees the partner-match action.
5. [x] Add a safe scan result: recognised opens the cached authorised passport; a valid but unavailable/unauthorised code and a malformed code have distinct guidance without leaking resource details.
6. [x] Apply the readable owner label and shared unit-aware quantity formatting described above. `ResourcePresentationRulesTest` and debug Kotlin compilation pass; Android presentation verification on owned/non-owner Passport plus inventory/Marketplace surfaces remains manual.

**Mark complete when:** the same real QR opens the correct resource passport and history for an authorised user, without exposing private data to an unauthorised viewer.

### 10. QR scanner — In progress

**Current gap:** The scanner page, permission/fallback UI, and routing are implemented, but none of the new scanner behaviour has yet been manually checked with a physical camera scan or real passport payload.

1. [x] Request camera permission with an understandable denied-state and a manual code-entry fallback.
2. [x] Accept only the defined ReEvent passport payload; reject arbitrary QR values with a useful message.
3. [x] Resolve the scanned identifier against the signed user's authorised data, not a mock record.
4. [x] Route a recognised code to the correct passport, handover, receipt or return action for that user's role.
5. [ ] Test one physical camera scan between two Android sessions or a printed/on-screen QR.

**Mark complete when:** scanning a real ReEvent passport QR opens the correct authorised resource/action and malformed codes fail safely.

### 11. Partner workbench — In progress

**Current gap:** The workspace now shows its active programmes and non-terminal assigned recovery tasks. Passport navigation and the existing server-authorised accept/decline/receipt actions are wired, but the new workbench path has not yet had a real Partner-account walkthrough.

1. [x] Show the signed partner's active programmes, eligible materials and current non-terminal recovery tasks.
2. [x] Replace the Workbench passport no-op callback with navigation to the relevant passport.
3. [x] Let the partner open a resource passport and see its condition, material category and recovery eligibility.
4. [x] Provide server-authorised Partner actions to accept/decline a recovery task and confirm recovery receipt when it is handed over.
5. [x] Refresh the workbench and impact outcome after a lifecycle action succeeds through the shared repository refresh.

**Mark complete when:** a partner can open a real task, inspect its passport and complete one authorised recovery action with a visible result.

### 12. Partner map — In progress

**Current gap:** `PartnerMapScreen.kt` now uses an honest live list fallback instead of `map_partner_mock.png`. It has not yet been manually checked with an active staging programme, material filter, eligible resource, and programme-to-passport action.

1. [x] Remove the static mock image from the runtime page.
2. [x] Start with a real list/card view of partner programmes showing name, supported materials, service area and availability.
3. [x] Add a category/material filter that changes the displayed programmes.
4. [x] Open a programme detail screen with a useful next action: view eligible resource and its passport.
5. [x] Keep the well-labelled list view because real coordinates and map setup are not yet available.

**Mark complete when:** the page presents real partner programme data and every visible action works, without a decorative mock map.

### 13. Matching — In progress

**Current gap:** Matching now shows the resource inputs, deterministic material/location ranking, clear no-match reasons, and a confirmed recovery-request action. Matching unit tests pass, but the new UI and recovery request have not yet been manually checked against an active staging programme. Live capacity and distance are not exposed by the current Android programme contract, so capacity remains server-authoritative at request time and location ranking uses the recorded service-area text.

1. [x] Use resource material/category, condition, available quantity and relevant event location as the matching inputs.
2. [x] Filter inactive and material-incompatible programmes; the server verifies remaining capacity on a recovery request.
3. [x] Rank remaining options deterministically by material match, then service-area text, name, and ID; show the reason and its data limits.
4. [x] State whether the resource is unavailable, has no quantity, lacks material, has no active programme, or has no compatible programme.
5. [x] Let an organiser confirm and submit a real partner recovery request from a recommended programme.

**Mark complete when:** an organiser can choose a resource, understand why a partner matched (or did not), and continue to a real follow-up action.

### 14. Impact dashboard — Done

**Current gap:** The dashboard only aggregates records tied to completed transactions, lets an organiser choose the event scope, labels estimates/missing factors, and shows the newest valid contribution. The remaining work is a manual completion-to-dashboard acceptance pass.

1. [x] Aggregate only completed server transactions for the current organiser/event scope.
2. [x] Show a clear breakdown for reuse/recovery outcome, CO2e estimate and ReCoins/reward where available.
3. [x] Label estimates and missing factors honestly rather than showing invented precision.
4. [x] Refresh the dashboard after a transaction is completed and make the latest contribution identifiable.

**Mark complete when:** completing a real transaction produces a visible, traceable impact update in the dashboard.

### 15. Offline and account switching — Done

**Current gap:** The profile now shows actual account-scoped queued commands in scheduler order, record-level event/resource sync chips, failed reasons, and a Retry action that replaces delayed WorkManager backoff. The remaining work is a manual offline/failure/retry acceptance pass; no mock status is used.

1. [x] Add visible labels for pending sync, synced and failed command states; local unsynced writes are labelled Pending sync.
2. [x] Give a failed queued command an explicit Retry action and preserve its error until it succeeds or is discarded.
3. [x] Show queue order using the same lifecycle-first, stable ordering as the scheduler, so actions cannot appear to overtake each other.
4. [x] Keep account-scoped cache cleanup and sync cancellation on sign out; run a final visible account-switching acceptance check.

**Mark complete when:** a user can tell whether a change is local, syncing, completed or needs a retry, and account switching never exposes the prior user's data.

### 16. Profile, help and deletion — In progress

**Current gap:** Profile now has a protected self-service deletion dialog, Android-side validation, and local source for its migration and Edge Function. The server flow re-authenticates the current user with the submitted password, blocks unsafe active work, removes private media, preserves only de-identified historical workflow records, deletes the Auth account, and clears the local session after the server acknowledgement. It cannot be marked Done until migration `0011` and the Function are deployed and accepted against real Supabase data.

#### Planned implementation: protected account deletion

**Development steps (Android, migration and Edge Function):**

1. Add a typed deletion request/result contract and pure confirmation/password validation. The user must type an exact destructive confirmation phrase and their current password; neither value is persisted, logged, placed in Room, or included in error text.
2. Extend the Auth gateway/repository with a dedicated deletion call that sends the current password only in the authenticated TLS request. The Edge Function must prove it belongs to the same user identified by the caller JWT; a wrong password must not clear local account data.
3. Add a protected `delete-my-account` Edge Function. It must require a valid caller JWT, identify the target solely from that JWT, re-authenticate that caller with the submitted current password in the same request, call privileged database/storage APIs only on the server, and never expose a service-role key to Android.
4. Add a sequential migration that safely prepares deletion: block active transactions, resources/custody, open listings, active programmes and unsettled coin holds; remove private media metadata; close/burn an eligible wallet; de-identify retained historical workflow records; and keep historical foreign-key references valid after Auth deletion. The operation must be safely retryable after a Storage/Auth finalisation failure.
5. In Profile, replace the guide-only card with a warning dialog, password + phrase confirmation, non-dismissable loading state, success message, and a safe signed-out state. Server rejection must show the reason and leave the account signed in.
6. Add local unit tests for confirmation validation and result/error mapping. These tests must run without a Supabase account.

**Expected deliverables:**

- `AccountDeletionRules` tests and Android confirmation dialog/state.
- A repository/gateway call that sends the typed current password only to the authenticated Edge Function; the Function re-authenticates that same JWT caller before any privileged work.
- Migration `0011` with a server-only deletion-preparation RPC and active-work guards.
- `supabase/functions/delete-my-account` source plus deployment/environment instructions; the service-role secret remains server-only.
- Profile success safely clears local data and returns to Sign in; failure keeps the signed-in session and says what must be resolved.
- Updated tracker status and explicit Supabase/manual acceptance steps.

**Manual Supabase and Android acceptance after code completion:**

1. Apply migrations `0001` through `0011` in order. Deploy `delete-my-account` with JWT verification enabled and configure its service-role secret only in Supabase Function Secrets; never add that secret to `supabase.local.properties` or Android BuildConfig.
2. Create a disposable verified account with no active resources, listings, programmes, transactions or coin holds. Sign in on Android, enter a wrong phrase/password, then verify the account remains signed in with no data change.
3. Repeat with the correct phrase/password. Confirm the app signs out, the Auth user cannot sign in again, private Storage objects under that user folder are gone, and retained historical records show de-identified actor data only.
4. With a separate account, create each blocked condition (requested/active transaction, in-custody resource, open listing, active programme, unsettled hold). Attempt deletion and confirm it is rejected with the specific safe-resolution message; the account must remain usable.
5. Call the deployed Function with a valid user JWT but no/wrong password and confirm it returns an authentication failure without changing data. After a successful deletion, refresh/reopen the app and verify the server session is gone; keep the Auth JWT lifetime short for production-like revocation behaviour.

1. [x] Show the signed-in email, selected role and a short explanation of what account data is stored.
2. [x] Add working Help/Support content with a safe assignment support route and clear privacy/data guidance.
3. [x] Finish the password-recovery journey described in Module 2 and link to it from Profile.
4. [x] Implement the re-authenticated deletion dialog, protected server operation source, success sign-out and safe failure guidance. (Supabase deployment/acceptance remains manual.)
5. [x] Avoid a misleading immediate Delete control until the secure server path is available; after deployment, expose it only through the explicit confirmation flow above.

**Mark complete when:** every visible account-support action works end-to-end, or is clearly scoped out rather than pretending to work.

## Recommended build order

1. **Core demo journey:** Modules 8, 9 and 10 (real participant return and QR) followed by the two-account app demonstration in Module 7.
2. **Partner journey:** Modules 11, 12 and 13 (workbench, real partner list, and matching follow-through).
3. **Trust and polish:** Modules 5, 2, 15 and 16 (photos, recovery, visible sync state and account support).
4. **Presentation pass:** Modules 3, 4, 6 and 14 (empty states, validation, marketplace state and impact clarity).

## Pages to show in the assignment

| Page | Main function | Status |
|---|---|---|
| Welcome / sign-in / role choice | Enter the correct role experience | **In progress** |
| Organiser Home | View event/resource actions and impact shortcut | **Done** |
| Events List / Editor / Detail | Create and manage event information | **Done** |
| Add / Edit Resource | Capture resource details and inventory | **Done** |
| Resource Passport | Show privacy-safe QR identity, role-aware authorised view and newest-first history | **In progress** — real verifier/App Link and two-account scan remain |
| Marketplace | Publish an eligible organiser resource, then discover and request it | **In progress** — apply `0009`/`0010` and complete two-account acceptance |
| Participant Return | Scan and return an assigned resource | **In progress** — real QR is wired; two-account lifecycle verification remains |
| Partner Workbench | Manage partner programmes and recovery work | **In progress** |
| Partner Map | Find a recovery option | **In progress** — real programme-list fallback is implemented; active-data verification remains |
| Matching | Explain suggested circular options | **In progress** |
| Impact | Show reuse, ReCoins and impact after completion | **Done** |
| QR Scanner | Scan a real passport and open the right context | **In progress** |
| Profile | Show active account and supported settings | **In progress** — password recovery, support guidance and the protected deletion flow are implemented locally; Supabase deployment/acceptance remains |

## Recently completed

- [x] **Server-authoritative transaction lifecycle (Stage 3)** — staging proof completed for organiser/participant RENT, partner wrong-actor denial, exact-once settlement/impact, and lost-response retry. The retry response bug was fixed in `ReEvent/supabase/migrations/0008_idempotent_replay_response.sql`.
- [x] **Staging demo setup** — three disposable staging accounts and a staging-bound debug APK are ready for the Android demo.

## Focus next — product work only

1. Run the first manual Android acceptance pass: active staging programme → Partner Map filter/detail/passport → Matching recovery request → Partner Workbench action.
2. Run the second manual Android acceptance pass: Participant assigned passport → camera/manual QR scan → authorised return state.
3. Run the photo acceptance pass: gallery/camera selection → private Storage upload → resource-detail and Passport thumbnail, including a failed-upload retry.
4. Add the reset redirect URL and run its Android email-link acceptance pass; deploy the protected account-deletion Function/migration and run its disposable-account acceptance pass.
5. Run the offline/failure/retry acceptance pass: create or edit a record offline â†’ inspect Pending sync â†’ restore network â†’ inspect Synced; then force one safe failure and use Retry.

6. Apply `0009_listing_default_due_date.sql`, then `0010_publish_marketplace_listing_rpc.sql`, and run the Marketplace two-account acceptance pass: publish a listing -> filter/details -> request -> approve or decline -> verify the refreshed lifecycle state.

## Old tracker

`docs/REEVENT_RELEASE_TRUTH_CHECKLIST.md` is an archived production-release audit with 168 granular acceptance items. It no longer controls assignment progress. Keep it only if you want a future production checklist; otherwise it can be deleted after you have reviewed this file.
