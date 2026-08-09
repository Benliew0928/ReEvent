# ReEvent Assignment Progress

**Use this file for day-to-day progress.** It tracks what can be shown in the Android assignment: modules, pages, and user-visible functions. It deliberately does **not** count release pipelines, AppGallery work, legal operations, or every production test case.

**Current assignment view:** 8 modules are **Done**, 5 are **In progress**, and 3 **Need work**. The server-authoritative transaction stage is **done** for assignment scope and recorded below.

## Status key

- **Done** — working and ready to show in a demo.
- **In progress** — screen/function exists, but needs a visible gap closed.
- **Needs work** — present as a mock, placeholder, or missing flow.

## Modules

| Module | Status | What is currently there | Next product task |
|---|---|---|---|
| Onboarding and navigation | **Done** | Welcome flow, typed role navigation, account-specific back stacks | Keep polish work small unless a flow breaks |
| Sign-in and role setup | **In progress** | Email sign-in/sign-up, session restore, and one-time role selection | Finish confirmation and password-recovery journey |
| Organiser home | **Done** | Home dashboard, quick actions, event/resource entry points | Refine real-data empty states |
| Event management | **Done** | Event list, create, edit, detail, archive entry point | Improve date/location validation UX |
| Resource inventory | **Done** | Add, edit, list and resource detail/passport entry points | Finish photo upload/replacement UX |
| Marketplace | **Done** | Browse listings and request/decision UI paths | Make all filters and detail data fully server-backed |
| Transaction lifecycle | **Done** | Server-authoritative request, approve, handover, receipt, return, settlement, impact and retry-safe completion | Demonstrate the same flow through two signed Android sessions |
| Participant return | **Needs work** | Return screen and scanner entry point exist | Replace the fake QR panel with the assigned real passport QR and return action |
| Digital passports | **In progress** | Persistent passport model, QR generation, passport screen and history foundations | Finish public-safe lookup and real assignment context |
| QR scanner | **In progress** | Camera/scanner screen and scan routing exist | Complete physical scan-to-authorised-action journey |
| Partner workbench | **In progress** | Partner workspace and programme foundations exist | Finish real recovery handover and passport navigation |
| Partner map | **Needs work** | Map page design exists | Replace static mock with real markers or an honest list fallback |
| Matching | **In progress** | Deterministic matching screen and rules exist | Improve quantity, capacity, distance explanation and alternatives |
| Impact dashboard | **Done** | Completed lifecycle creates real impact/reward data; organiser impact screen exists | Improve presentation and explain factor limitations |
| Offline and account switching | **Done** | Room cache, durable command queue, account/environment isolation and sign-out cleanup | Surface pending/failed sync state in the UI |
| Profile, help and deletion | **Needs work** | Profile page and support entry points exist | Finish password reset, account deletion, and clear support details |

## Module-by-module completion plan

Work through the modules in the order shown inside each section. A checked item should mean the behaviour is visible in the Android app and can be demonstrated; it does **not** require a production deployment checklist. A **Done** module is already core-demo-ready: its items are only small verification or polish tasks, not a reason to reopen its scope unless a real issue is found.

### 1. Onboarding and navigation — Done

**Current gap:** No known functional blocker. This needs only a short regression pass while other screens change.

1. [ ] Confirm a first-time user reaches Welcome, Sign in, then Role setup without a blank screen.
2. [ ] Confirm the chosen role opens its correct home screen and Back cannot return to onboarding.
3. [ ] Keep every new page wired through the existing typed route graph in `ReEventApp.kt`.

**Mark complete when:** the three role journeys open the correct starting page and each new page has a working Back destination.

### 2. Sign-in and role setup — In progress

**Current gap:** Basic email authentication and role selection exist, but recovery and confirmation are incomplete from the user's point of view.

1. [ ] Make Sign up validate email, password and password confirmation before submitting.
2. [ ] Show a clear result after account creation: signed in immediately, or "check your email" when confirmation is required.
3. [ ] Add Forgot password: request reset email, open the update-password state, validate the replacement password and show success/failure.
4. [ ] Persist the selected role only after its save succeeds; show an actionable error if it fails.
5. [ ] Add loading, offline and invalid-credentials messages that leave the entered email visible.

**Mark complete when:** a new user can create an account, choose a role, sign out, sign back in, and recover a password without developer help.

### 3. Organiser home — Done

**Current gap:** The home structure and quick actions work; the remaining issue is making its summary trustworthy when there is no data yet.

1. [ ] Drive summary cards and recent activity from the signed-in organiser's data, not sample counts.
2. [ ] Give each empty card one useful next action: create event, add resource, or publish a listing.
3. [ ] Check every quick action opens the real editor/list screen and returns to Home after saving.

**Mark complete when:** a new organiser sees an honest empty state and a returning organiser sees their own current events/resources.

### 4. Event management — Done

**Current gap:** Create, edit and detail flows exist. The remaining work is form clarity and safe archiving.

1. [ ] Validate title, dates and location before save; reject an end date before the start date with an inline explanation.
2. [ ] Preserve typed values and explain the failure when saving offline or when the server rejects the event.
3. [ ] In Event detail, show linked resources and provide a direct path to add or edit them.
4. [ ] Require a confirmation before archive and explain what archive changes for marketplace visibility.

**Mark complete when:** an organiser can create, edit, view and archive an event with understandable validation and feedback.

### 5. Resource inventory — Done

**Current gap:** Core add/edit/list/detail paths work. Photo handling and lifecycle-state presentation need finishing.

1. [ ] Make name, category, quantity/unit and condition mandatory, with plain-language validation.
2. [ ] Keep metadata edits separate from lifecycle actions: request/approval/return state must be refreshed from the server rather than locally guessed.
3. [ ] Add an image picker, upload/replacement progress and a visible thumbnail on the resource detail screen.
4. [ ] Define a simple delete/archive rule: do not allow removal while a resource has an active transaction; explain why.
5. [ ] Confirm the list, detail and passport all display the same current resource name, condition and availability.

**Mark complete when:** an organiser can add a resource with a photo, edit safe fields, and understand why an in-use resource cannot be removed.

### 6. Marketplace — Done

**Current gap:** Browse and request/decision paths exist, but the visible data and request safeguards need to be consistently server-backed.

1. [ ] Load only published, currently available listings for the active user; show an honest empty state when none match.
2. [ ] Make search/filter controls affect the real listing query or remove controls that are not yet supported.
3. [ ] On Listing detail, show quantity, condition, dates, owner/event context and the terms relevant to the selected request type.
4. [ ] Validate request type, requested quantity and required date before sending the request command.
5. [ ] Refresh the request state after an organiser decision and make pending, approved, declined and cancelled states visually distinct.

**Mark complete when:** a participant can find a real listing, submit one valid request, and both parties see the same resulting status.

### 7. Transaction lifecycle — Done for assignment scope

**Current gap:** The signed server lifecycle is implemented and verified in staging. The remaining work is making its success/failure states easy to demonstrate in the app.

1. [ ] Ensure each lifecycle button sends the existing durable, typed command with its idempotency key; do not add client-side status shortcuts.
2. [ ] Display the server-returned state after request, approval, handover, receipt, return and completion.
3. [ ] Disable or hide actions the current signed user is not allowed to perform, and explain the next permitted action.
4. [ ] After a temporary failure, show that the command is pending/retrying and refresh the server result when it completes.
5. [ ] Run one manual two-account Android demonstration using a fresh resource: participant requests/receives/returns; organiser approves/hands over/completes.

**Mark complete when:** the full lifecycle is shown through two signed Android accounts and the final settlement/reward is visible without manual database changes.

### 8. Participant return — Needs work

**Current gap:** `ParticipantReturnScreen.kt` still uses `FakeQrPanel`; this is the highest-priority visible assignment gap.

1. [ ] Remove the runtime fake QR panel from the participant return journey.
2. [ ] Load the participant's active received/returnable transaction and its assigned resource passport from the signed account.
3. [ ] Render the real, canonical passport QR payload using the shared QR component.
4. [ ] Provide a clear return action: scan the organiser/resource code or open the assigned passport, then submit the authorised return command.
5. [ ] Show waiting, confirmed and failure states, including who must take the next step.

**Mark complete when:** a participant can open their assigned passport, use a real QR/return action, and see the transaction move to the next lifecycle state.

### 9. Digital passports — In progress

**Current gap:** Passport storage, QR generation and history foundations exist. Assignment context and safe lookup need to be joined up.

1. [ ] Define one versioned passport payload containing only the passport/resource identifier and no private account data.
2. [ ] Generate and display that exact payload as a real QR on the passport screen.
3. [ ] Load the passport's resource details and lifecycle history from the server, ordered newest first.
4. [ ] Show whether the viewer is owner, current holder, organiser or partner and only expose actions allowed to that viewer.
5. [ ] Add a safe lookup result for a scanned passport: recognised, unavailable, unauthorised or malformed.

**Mark complete when:** the same real QR opens the correct resource passport and history for an authorised user, without exposing private data to an unauthorised viewer.

### 10. QR scanner — In progress

**Current gap:** The scanner page and routing exist, but the physical scan must lead to an authorised real action.

1. [ ] Request camera permission with an understandable denied-state and a manual code-entry fallback.
2. [ ] Accept only the defined ReEvent passport payload; reject arbitrary QR values with a useful message.
3. [ ] Resolve the scanned identifier against the signed user's authorised data, not a mock record.
4. [ ] Route a recognised code to the correct passport, handover, receipt or return action for that user's role.
5. [ ] Test one physical camera scan between two Android sessions or a printed/on-screen QR.

**Mark complete when:** scanning a real ReEvent passport QR opens the correct authorised resource/action and malformed codes fail safely.

### 11. Partner workbench — In progress

**Current gap:** The workspace foundation exists, but it does not yet complete a partner recovery task and some callbacks remain unconnected.

1. [ ] Show the signed partner's active programmes, eligible materials and current assigned/claimed recovery tasks.
2. [ ] Replace no-op actions in `RestoredVisualLiveScreens.kt` with navigation to the relevant passport or programme detail.
3. [ ] Let the partner open a resource passport and see its condition, material category and recovery eligibility.
4. [ ] Provide one real partner action appropriate to the data model: accept/decline a recovery task or record recovery receipt.
5. [ ] Refresh the workbench and impact outcome after that action succeeds.

**Mark complete when:** a partner can open a real task, inspect its passport and complete one authorised recovery action with a visible result.

### 12. Partner map — Needs work

**Current gap:** `PartnerMapScreen.kt` still displays the static `map_partner_mock.png`; it must become live data or an honest usable fallback.

1. [ ] Remove the static mock image from the runtime page.
2. [ ] Start with a real list/card view of partner programmes showing name, supported materials, service area and availability.
3. [ ] Add a category/material filter that changes the displayed programmes.
4. [ ] Open a programme detail screen with a useful next action: view eligible resource, contact/request recovery, or return to workbench.
5. [ ] Add an interactive map only if real coordinates and map setup are ready; otherwise keep the well-labelled list view for the assignment.

**Mark complete when:** the page presents real partner programme data and every visible action works, without a decorative mock map.

### 13. Matching — In progress

**Current gap:** Deterministic matching/rules exist, but users need an explanation that connects a resource to a practical next action.

1. [ ] Use resource material/category, condition, available quantity and relevant event location as the matching inputs.
2. [ ] Filter out inactive programmes and programmes that cannot accept the resource.
3. [ ] Rank the remaining options deterministically and display a short reason for each result (for example, material accepted and capacity available).
4. [ ] State why there are no matches when the result is empty.
5. [ ] Make a match open the actual partner programme or passport/workbench action rather than a dead end.

**Mark complete when:** an organiser can choose a resource, understand why a partner matched (or did not), and continue to a real follow-up action.

### 14. Impact dashboard — Done

**Current gap:** Lifecycle completion creates real impact/reward data. The screen needs presentation rules that make the figures credible.

1. [ ] Aggregate only completed server transactions for the current organiser/event scope.
2. [ ] Show a clear breakdown for reuse/recovery outcome, CO2e estimate and ReCoins/reward where available.
3. [ ] Label estimates and missing factors honestly rather than showing invented precision.
4. [ ] Refresh the dashboard after a transaction is completed and make the latest contribution identifiable.

**Mark complete when:** completing a real transaction produces a visible, traceable impact update in the dashboard.

### 15. Offline and account switching — Done

**Current gap:** The Room cache, durable command queue and sign-out cleanup exist. Users cannot yet see enough of the sync state to trust it.

1. [ ] Add visible labels for local/draft, pending sync, synced and failed command states.
2. [ ] Give a failed queued command an explicit Retry action and preserve its error until it succeeds or is discarded.
3. [ ] Show command order when several commands are pending for one resource, so lifecycle actions cannot appear to overtake each other.
4. [ ] Confirm signing out clears the outgoing account's cached/private state before another account is shown.

**Mark complete when:** a user can tell whether a change is local, syncing, completed or needs a retry, and account switching never exposes the prior user's data.

### 16. Profile, help and deletion — Needs work

**Current gap:** Entry points exist but the account-support promises are incomplete.

1. [ ] Show the signed-in email, selected role and a short explanation of what account data is stored.
2. [ ] Add working Help/Support content with a contact method and links to the assignment's privacy/terms text if those are included.
3. [ ] Finish the password-recovery journey described in Module 2 and link to it from Profile.
4. [ ] Implement a deliberate account-deletion flow: warning, re-authentication/confirmation where required, submission result and signed-out state.
5. [ ] If full deletion cannot be delivered for the assignment, remove any misleading Delete account control and label the remaining support route honestly instead.

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
| Resource Passport | Show QR/passport identity and history | **In progress** |
| Marketplace | Discover a listing and request an allowed action | **Done** |
| Participant Return | Scan and return an assigned resource | **Needs work** — real QR still required |
| Partner Workbench | Manage partner programmes and recovery work | **In progress** |
| Partner Map | Find a recovery option | **Needs work** — mock map must be replaced |
| Matching | Explain suggested circular options | **In progress** |
| Impact | Show reuse, ReCoins and impact after completion | **Done** |
| QR Scanner | Scan a real passport and open the right context | **In progress** |
| Profile | Show active account and supported settings | **Needs work** |

## Recently completed

- [x] **Server-authoritative transaction lifecycle (Stage 3)** — staging proof completed for organiser/participant RENT, partner wrong-actor denial, exact-once settlement/impact, and lost-response retry. The retry response bug was fixed in `ReEvent/supabase/migrations/0008_idempotent_replay_response.sql`.
- [x] **Staging demo setup** — three disposable staging accounts and a staging-bound debug APK are ready for the Android demo.

## Focus next — product work only

1. Make Participant Return use a real assigned QR/passport instead of the fake QR panel.
2. Finish the partner map with real data or a clean list fallback, then connect its action to a real programme request.
3. Finish photo upload/display so a resource can be demonstrated with an actual image.
4. Complete password recovery and account deletion so the profile area is not a dead end.
5. Run the organiser-to-participant transaction once through two signed app sessions and capture a short demo video or screenshots.

## Old tracker

`docs/REEVENT_RELEASE_TRUTH_CHECKLIST.md` is an archived production-release audit with 168 granular acceptance items. It no longer controls assignment progress. Keep it only if you want a future production checklist; otherwise it can be deleted after you have reviewed this file.
