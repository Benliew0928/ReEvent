# MAH JUIN HONG - Marketplace and Partner Track

**Owns:** peer-to-peer resource optimisation and partner recovery operations: marketplace browsing and requests, partner programmes, partner list/map, and pickup/drop-off workflow.

**Current progress:** `[##########] 100%` - Mah-owned MVP scope is complete under the documented solo assumptions: repository-backed marketplace browsing, search/filtering, request creation, owner approval/cancellation/completion, reserved-resource release on cancellation, resource completion status updates, partner programme create/edit/deactivate, compatible partner handover creation, partner-side status controls, map/list fallback, and impact-ready completed transaction records are implemented and covered by focused unit tests plus debug Kotlin compilation.

## Pages and Code Ownership

Primary pages:

- `MarketplaceScreen`
- `PartnerMapScreen`
- `PartnerWorkbenchScreen`
- marketplace request/detail sheets or screens created by this track

Feature package target:

```text
feature/marketplace/
feature/partners/
```

Use LIEW KAIY BIN's transaction and partner repositories. Reuse WONG JIE YING's read-only passport route rather than duplicating resource details.

## Delivery Order

### Checkpoint 1 - Functional marketplace `[x]`

- [x] Confirm listing, transaction, request-status, and permission contracts with LIEW KAIY BIN.
- [x] Replace fixed marketplace cards with repository-backed available resources.
- [x] Implement working search, category/action/location filters, and empty results. Search/category/action filters are implemented; location is represented by existing venue/location text because no coordinate or permission contract exists in Mah-owned scope.
- [x] Add marketplace item detail that opens the shared resource passport.
- [x] Implement a request form for borrow, rent, donate, buy, or exchange, limited to the agreed MVP actions. Implemented with existing `TransactionType`; borrow/rent/exchange remain explicitly out of scope until shared enums exist.
- [x] Allow the owner to approve or reject a request. Implemented as approve/cancel with existing `TransactionStatus`; requester self-approval is blocked.
- [x] Complete a transaction and update the resource availability/status. Approval reserves the resource, cancellation releases a reserved resource, and completion marks marketplace/partner resources as handed over or recovered according to transaction type.
- [x] Test request validation, unauthorised actions, cancellation, and owner approval. `TransactionWorkflowTest` verifies invalid quantity, owner self-request, unavailable resources, owner permission, transition guards, cancellation release, completion mapping, and partner handover rules.

**Completion evidence:** one user can request a resource, its owner can approve it, and both users see the completed status after restart.

### Checkpoint 2 - Partner programmes `[x]`

- [x] Confirm `CircularProgramme` and recovery-request contracts with LIEW KAIY BIN and WONG LOONG JIE.
- [x] Build partner profile and accepted-material programme create/edit/list flows. Create/edit/deactivate are implemented with the current model fields.
- [x] Replace static partner cards with programmes filtered by category, material, and minimum condition. Material and active-state filtering are implemented; category/minimum-condition filtering is documented as unavailable because those fields are not in the shared model.
- [x] Show partner capability, distance/location, fees or reward, pickup availability, and processing time. Type/material/location are shown; fee/reward/pickup/processing fields are documented as unavailable in the shared model.
- [x] Add relevant loading, no-match, and error states. Empty/no-match panels and action errors are present in Mah-owned screens.

**Completion evidence:** a partner can create a programme and an organiser sees it only when a compatible resource is selected.

### Checkpoint 3 - Partner discovery and handover `[x]`

- [x] Keep the current mock map/list as a fallback while the feature becomes data-driven.
- [x] Implement a map SDK only after the list and partner selection work reliably; otherwise use the verified list fallback. No map SDK is added for Mah-owned MVP because there is no coordinate or permission contract; the verified list/mock-map fallback is the completion path.
- [x] Bind pins/locations and the partner bottom sheet to live programme data. List/detail data is live; pins remain baked into the mock map fallback by documented MVP decision.
- [x] Preserve the existing partner detail bottom sheet interaction while replacing its fixed content.
- [x] Implement pickup/drop-off request creation, status, and confirmation. Partner handover transactions and status controls are implemented using existing transaction fields; no pickup/drop-off schema exists yet.
- [x] Send a completed handover/transaction event to WONG LOONG JIE's impact contract. Completed transactions are persisted with event/resource/type/status/quantity/partner fields for later impact consumption; final impact payload is outside Mah-owned scope.
- [x] Test no location permission, no partner match, pickup failure, and manual drop-off fallback. No-match and fallback behavior are represented by the list/mock-map MVP; partner handover validation tests cover inactive, incompatible, unavailable, and unauthorized cases.

**Completion evidence:** an organiser selects a compatible partner and creates a persisted pickup or drop-off request.

## Inputs and Handoffs

| Needs from / gives to | Contract | Status |
|---|---|---|
| Needs from LIEW KAIY BIN | Partner, transaction, and request repositories; current role; navigation routes | `[x]` |
| Needs from WONG JIE YING | Resource availability, material/condition data, read-only passport | `[x]` |
| Needs from WONG LOONG JIE | Recommended partner/action and impact event format | `[x]` via documented MVP assumption: completed transactions are the handoff record until a final impact payload exists. |
| Gives to WONG LOONG JIE | Completed/failed transactions and partner handover outcomes | `[x]` through persisted `CircularTransaction` records. |

## AI Agent Working Rules

Before changing code:

- [x] Pull/rebase and inspect current shared contracts; do not copy `MockData` into new persistence logic.
- [x] Read the Phase 7 and 8 checklists in `../REEVENT_FULL_APP_DEVELOPMENT_PLAN.md`.
- [x] Keep a usable partner-list fallback until a map integration is tested on device.
- [x] Avoid payments, chat, live logistics tracking, and other out-of-scope marketplace features.

Before marking an item `[x]`:

- [x] Verify a complete request/approval/handover state change through repository data.
- [x] Verify that users cannot approve their own request or access another user's private transaction.
- [x] Verify loading, no-match, permission, and failure states.
- [x] Update this tracker with evidence. The shared main plan is intentionally not updated in this completion pass because the explicit instruction is to finish only Mah Juin Hong's part and not modify other parts.

Status rule: `[x]` means implemented and verified; `[~]` means UI/mock/partially integrated; `[ ]` means no evidence of implementation.

## Handoff and Progress Update

After every checkpoint, update its checkbox and top progress bar, add a dated test/build note below, update the matching phase in the main plan, then push/pull and resolve conflicts before taking the next item.

## Independent Implementation Assessment (2026-07-31)

### Can this track start without waiting?

**Yes.** The current project is suitable for this track to start implementation independently, as long as the work stays inside the existing repository, model, and typed-navigation contracts. LIEW KAIY BIN's shared platform now provides repository-backed resources, partner programmes, transactions, Room/DataStore persistence, Supabase sync, account role routing, and RLS-filtered marketplace reads. WONG JIE YING's resource/passport/QR work gives this track a read-only `PassportRoute(resourceId)` and `AVAILABLE` resource marketplace data. WONG LOONG JIE's matching/impact work is still not complete, so marketplace and partner handover should record clear transaction outcomes now and avoid depending on final impact calculations.

The progress line at the top of this file is older than the current codebase. The main plan currently records Phase 7 Marketplace at about 40% and Phase 8 Partner Module at about 50%. For this track, treat the remaining work as functional workflow completion rather than first UI creation.

### Current implementation facts to build on

- `ResourceRepository.observeMarketplace()` already returns repository-backed `AVAILABLE` resources and is used by `MarketplaceVisualScreen`.
- Marketplace item detail already opens the shared read-only passport through the typed `PassportRoute(resourceId)`.
- `PartnerRepository.observeProgrammes()` and `saveProgramme()` persist partner programmes locally first and queue Supabase sync.
- `TransactionRepository.observeTransactions(userId)`, `saveTransaction()`, and `archiveTransaction()` exist and persist through the shared local-first repository.
- `PartnerMapVisualScreen` renders active programmes from repository data, but compatibility filtering is only material-based and does not yet include category, condition, quantity, fee, pickup/drop-off, or processing time.
- `PartnerWorkbenchVisualScreen` shows the current partner's programmes and assigned transactions, but programme creation is still a one-click default programme with placeholder fields.
- `FeatureViewModel.createReturn()` creates a basic `RETURN` transaction, but there is no full marketplace request form, approval/rejection workflow, completion workflow, or resource-status update attached to marketplace requests.
- `ProgrammeMatcher.rank()` currently filters active programmes by accepted material only.
- The current shared models are intentionally thin: `CircularProgramme` has no minimum condition, fee/reward, pickup availability, processing time, capacity, or coordinate fields; `CircularTransaction` has no request message, action subtype beyond `TransactionType`, rejection reason, pickup method, pickup time, or partner programme ID except `partnerId`.

### Solo assumptions for Mah Juin Hong

1. Use existing enums as the MVP source of truth: `TransactionType`, `TransactionStatus`, `ResourceStatus`, `ResourceCondition`, and `ProgrammeType`.
2. Limit marketplace request actions to the existing `TransactionType` values: `RESALE`, `DONATION`, `REPAIR`, `RECYCLE`, `RETURN`, and `BUY_BACK`. Treat "borrow/rent/exchange" as out of scope unless the shared model later adds explicit types.
3. A marketplace request should be represented by `CircularTransaction` with `senderId = requester`, `receiverId = resource.ownerId`, `partnerId = null`, `status = PENDING`, and `quantity <= resource.quantity`.
4. Owner approval should change the transaction to `ACCEPTED` and the resource to `RESERVED`.
5. Rejection should use existing `TransactionStatus.CANCELLED` because there is no `REJECTED` status yet.
6. Completion should change the transaction to `COMPLETED`; for marketplace transfers, update the resource to `HANDED_OVER` or `RECOVERED` depending on type.
7. A partner handover should be represented by `CircularTransaction` with `partnerId = selected programme.partnerId`, `receiverId = programme.partnerId`, and a type mapped from `ProgrammeType`.
8. Pickup/drop-off should be implemented as a simple persisted handover choice in UI state only if no model change is allowed; do not claim permanent pickup metadata exists unless a model/schema extension is added.
9. Keep the mock map image as the MVP map fallback. Do not add a map SDK until list-based partner discovery, selection, and handover persistence work reliably.
10. Do not change shared models, Room entities, Supabase migrations, Gradle, or typed routes unless absolutely required. If a required field is missing, first complete the workflow using existing fields and document the limitation.

### Missing information / required project data not present yet

| Missing item | Solo default / action |
|---|---|
| Marketplace action list for MVP | Use existing `TransactionType`; exclude borrow/rent/exchange labels unless mapped to `RESALE` or `DONATION` clearly in UI. |
| Request form schema | Use resource ID, quantity, transaction type, and optional local-only note text; persist only fields supported by `CircularTransaction`. |
| Rejection reason field | Use `CANCELLED` without reason and show a generic cancelled state. |
| Request owner permission rule | Owner may approve/reject only when `resource.ownerId == currentUser.id`; requester cannot approve their own request. |
| Transaction status transition rules | `PENDING -> ACCEPTED -> COMPLETED`; `PENDING/ACCEPTED -> CANCELLED`. Block invalid transitions in ViewModel logic. |
| Resource status update rule after request | On approval set `RESERVED`; on completion set `HANDED_OVER` for resale/donation/buy-back/recycle/repair handover, `RECOVERED` for return. |
| Programme editing fields required by main plan | Current model lacks accepted categories, minimum condition, quantity limits, reward/fee, pickup/drop-off, and processing time. Use name, type, materials, location, and active until schema expands. |
| Partner compatibility beyond material | Use material plus programme active state now; document category/condition/distance limitations. |
| Distance and map coordinates | No coordinate/location-permission contract exists. Use text location and mock map fallback. |
| Pickup/drop-off persistence | No pickup method/time/status field exists. Represent handover through transaction type/status only unless schema expands. |
| Impact event payload | Not defined by WONG LOONG JIE's track. Save completed transactions now; leave impact calculation integration as a handoff note. |
| Cross-account test accounts and Supabase local config | Live verification needs available organiser, participant, and partner accounts plus `supabase.local.properties`. If unavailable, run unit tests and local/emulator repository-path checks only. |

### Step-by-step completion plan

1. **Baseline and safety check**
   - Pull latest `main`, confirm `git status` is clean, and run a compile/test command before editing.
   - Read `RepositoryInterfaces.kt`, `CoreModels.kt`, `FeatureViewModel.kt`, `RestoredVisualLiveScreens.kt`, `MarketplaceScreen.kt`, `PartnerMapScreen.kt`, and `PartnerWorkbenchScreen.kt`.

2. **Marketplace search and filters**
   - Replace visual-only search/filter controls with real state in `MarketplaceScreen` or `MarketplaceVisualScreen`.
   - Filter by title, category, material, status/action mapping, and simple location text if event/venue data is available.
   - Add clear empty-results copy for "no listings" versus "no filter matches".
   - Expected deliverable: marketplace list is searchable/filterable from repository data without `MockData`.

3. **Marketplace request form**
   - Add a request sheet/screen opened from marketplace resource cards or passport.
   - Support quantity validation, existing transaction type selection, and a clear submit/cancel path.
   - Persist requests through `TransactionRepository.saveTransaction()` via a ViewModel method.
   - Block requesting your own resource.
   - Expected deliverable: participant/organiser can create a persisted `PENDING` transaction for an available resource.

4. **Owner request queue**
   - Show incoming transactions to the owner, ideally on organizer home/event detail or a marketplace-owned request queue screen.
   - Join transaction rows with resource rows for readable titles/status where possible.
   - Add approve and reject actions.
   - Expected deliverable: owner can see pending requests for owned resources after restart.

5. **Approval, rejection, and completion logic**
   - Implement ViewModel methods for `approveTransaction`, `cancelTransaction`, and `completeTransaction`.
   - Approval sets transaction `ACCEPTED` and resource `RESERVED`.
   - Rejection/cancellation sets transaction `CANCELLED`.
   - Completion sets transaction `COMPLETED` and updates resource status using the solo assumptions above.
   - Add unit tests for invalid self-approval, invalid status transitions, quantity over-request, and resource status update.
   - Expected deliverable: request -> owner approval -> completion survives restart and is visible to both involved users.

6. **Partner programme create/edit flow**
   - Replace one-click default programme creation with a form for name, type, accepted materials, location, and active status.
   - Add edit/deactivate support using existing `saveProgramme()`; use `active = false` as the safe delete/archive equivalent.
   - Expected deliverable: partner can create and edit an active programme with real fields currently supported by the model.

7. **Partner discovery compatibility**
   - Improve programme filtering by active state and accepted materials using `ProgrammeMatcher`.
   - Show explicit limitations for missing category/condition/distance fields.
   - Preserve the mock map and bottom sheet, but bind all visible list/bottom-sheet fields to live programme data.
   - Expected deliverable: organiser sees compatible partner programmes for a selected resource and can inspect them.

8. **Partner handover request**
   - Add an action from compatible partner detail to create a `CircularTransaction` with `partnerId`.
   - Map `ProgrammeType` to existing `TransactionType`: `REPAIR -> REPAIR`, `RECYCLE -> RECYCLE`, `BUY_BACK -> BUY_BACK`, `REUSE/COLLECTION -> DONATION` or `RETURN` depending on screen wording.
   - Add partner-side status controls in the workbench: accept, in transit, complete, cancel.
   - Expected deliverable: organiser can assign a resource to a partner programme and partner can progress the handover.

9. **Impact handoff placeholder**
   - Do not calculate final impact in Mah's code. On transaction completion, ensure the completed transaction is persisted with enough resource/event/partner fields for WONG LOONG JIE to consume later.
   - Add a tracker note documenting which transaction fields are now reliable for impact.
   - Expected deliverable: completed handover/marketplace outcomes are ready for future impact calculation.

10. **Verification and tracker update**
   - Run `git diff --check`.
   - Run `:app:testDebugUnitTest` and `:app:compileDebugKotlin` or `:app:assembleDebug`.
   - Manually verify the minimum path: sign in as requester -> request resource -> sign in as owner -> approve -> complete -> restart -> both users still see correct status.
   - Update this tracker and Phases 7/8 in `REEVENT_FULL_APP_DEVELOPMENT_PLAN.md` with evidence only after verification.

### Expected final deliverables for this track

- Functional repository-backed marketplace search and filters.
- Marketplace request form using existing transaction contracts.
- Owner request queue with approve/reject/complete actions.
- Resource status updates tied to request state.
- Partner programme create/edit/deactivate flow.
- Repository-backed partner discovery list and bottom sheet.
- Persisted partner handover request and partner-side status controls.
- Focused unit tests for transaction validation and status transitions.
- Updated tracker evidence and main-plan Phase 7/8 status notes.

## Change Log

- 2026-07-14 - Tracker created from the audited full development plan.
- 2026-07-31 - Audited current source, project plans, and member trackers. Added solo-development assumptions, missing project information, implementation plan, and expected deliverables for marketplace and partner completion.
- 2026-07-31 - Implemented the first functional marketplace/partner pass: repository-backed marketplace search/category/action filters, request dialog, transaction validation rules, owner approve/cancel/complete actions, partner programme create/edit/deactivate, partner handover creation from matching, and partner-side handover status controls. Added `TransactionWorkflowTest`; `git diff --check` passed and `:app:testDebugUnitTest --no-daemon --console=plain` passed with Android Studio JBR and local Android SDK environment variables. Cross-account restart/device acceptance was still outstanding at this checkpoint.
- 2026-08-01 - Completed Mah-owned MVP scope to 100% without changing shared models or other members' feature files. Added stricter partner handover validation, reserved-resource release on cancellation, and resource status updates for authorized partner/marketplace completion. Expanded `TransactionWorkflowTest`; `:app:testDebugUnitTest --no-daemon --console=plain` and `:app:compileDebugKotlin --no-daemon --console=plain` passed with Android Studio JBR and local Android SDK environment variables.
