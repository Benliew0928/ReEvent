# WONG JIE YING - Resource Lifecycle and QR Track

**Owns:** the organiser's event and resource lifecycle: event selection/creation, inventory, add/edit resource, photo flow, digital resource passport, and QR actions.

**Current progress:** `[#########-] 90%` - core integration supplies repository-backed event/resource/passport persistence, local-first sync, typed routes, stable UUIDs, and a persisted QR payload. Checkpoint 1 UI includes event management, selected-event dashboard state, resource editing/search/filter/status controls, and validated resource entry. Checkpoint 2 photo/camera, private-photo display, local DataStore drafts, and failure-safe save behaviour are implemented and device-verified. Checkpoint 3 is device-verified. Checkpoint 4 now has an offline-capable camera scanner, QR resolution, lifecycle actions, and passport scan history; physical-device acceptance remains.

## Pages and Code Ownership

Primary pages:

- `HomeScreen` - organiser dashboard and event context
- `AddResourceScreen`
- `PassportScreen`
- `ParticipantReturnScreen`
- new event list/detail and QR scanner screens

Feature package target:

```text
feature/inventory/
feature/passport/
feature/qr/
```

Use LIEW KAIY BIN's repositories and routes. Do not add Room dependencies, edit shared models, or change navigation architecture directly; request the required contract instead.

## Delivery Order

### Checkpoint 1 - Event and inventory data `[~]`

- [ ] Confirm the `Event`, `ResourceItem`, condition, ownership, and status contracts with LIEW KAIY BIN.
- [~] Replace direct `MockData` use in owned screens with repository-backed UI state. Runtime organiser flows use ViewModel/repository state; device verification remains required.
- [~] Build event list, create-event, edit-event, and event-detail flows. Implemented with typed routes and local-first repository saves; device verification remains required.
- [~] Convert the current organiser dashboard from fixed metrics into a selected-event dashboard. The selected event is persisted in DataStore and restored when available.
- [~] Convert the add-resource form preview into a validated form. The live form now supports category, material, condition, quantity, unit, optional RM value, local photo preview/removal, cancellation-safe photo selection, and input validation; device/build verification remains required.
- [~] Support resource create, read, update, search, filter, and status changes. CRUD/read/search/status controls are implemented; archive action and device acceptance checks remain outstanding.
- [~] Add clear empty, loading, validation-error, and save-error states. Form validation plus existing loading/save-error UI are present; manual accessibility/device verification remains required.

**Completion evidence:** an organiser can create an event, add a resource, edit it, restart the app, and see the saved data again.

### Checkpoint 2 - Resource photo and drafts `[x]`

- [x] Add image picker/camera permission handling with a safe cancellation path. Android Photo Picker and camera capture preserve the current photo on cancellation; camera denial leaves the picker available.
- [x] Show a real local preview, removal action, and upload state. The selected image is previewed locally; saved private images are downloaded for runtime cards/passport. The existing action state shows save/upload progress and failures.
- [x] Save an incomplete resource as a local draft. Form metadata and selected URI are scoped to account/event in DataStore and restored for a new-resource form; the draft clears only after a successful save.
- [x] Integrate remote photo upload only through the repository supplied by LIEW KAIY BIN. Upload/read continue through `MediaRepository`; no UI screen directly calls Supabase Storage.
- [x] Tested denied permission, no image selected, offline save, and upload failure on device; confirmed cancellation, draft restoration, successful upload, and restart photo display.

**Completion evidence:** the resource form does not lose user input when photo selection, upload, or connectivity fails.

### Checkpoint 3 - Digital passport `[x]`

- [x] Generate a stable unique item ID through the shared resource/passport contract. A new resource retains its UUID, and its QR payload is `reevent://passport/<resourceId>`.
- [x] Create and persist a resource passport when a resource is saved. Creation persists an initial JSON history entry together with the existing local-first passport save.
- [x] Replace the visual `FakeQrPanel` with a real QR code generated locally from the persisted payload, using ZXing `core:3.5.4`.
- [x] Bind passport details, condition, owner, event history, and recommended action to actual data. The page reads the resource, event, and passport through `FeatureViewModel` repositories and displays no editable controls.
- [x] Record history entries for resource lifecycle changes. The organiser's **Update status** dropdown appends a timestamped action, actor, old status, new status, and note to `historyJson`.
- [x] Ensure a marketplace detail can open the same passport in read-only mode for MAH JUIN HONG. `PassportRoute(resourceId)` uses the same read-only page. Authorised remote rows are copied into the active account's cache during refresh, so Supabase RLS controls cross-account visibility.

**Completion evidence:** two resources have different stable IDs and display the correct passport after app restart.

### Checkpoint 4 - QR scanning and return lifecycle `[~]`

- [~] Implemented scanner dependencies independently: CameraX `1.5.3` plus bundled ML Kit Barcode Scanning `17.3.0`. The bundled model is intentionally used so QR recognition does not require a first-use download or network connection.
- [x] Build a QR scanner with camera permission, invalid-code, cancelled-scan, and no-network handling. **Cancel** cancels, denied permission shows a retry explanation, malformed QR values are rejected, and a valid-but-not-local passport tells the user to reconnect and refresh. Device acceptance passed.
- [x] Resolve a scanned QR value to the correct passport. Only the exact `reevent://passport/<UUID>` payload that equals the persisted passport payload resolves; the verified panel can open that resource's passport. Device acceptance passed with two resources.
- [x] Implement check-out, damaged, repair-request, and transfer actions using repository commands. Organiser owners can make all changes; each action saves resource state/condition, relevant transaction records, and passport history through repositories. Device acceptance passed.
- [x] Save scan history and show it on the passport. Every verified scan appends `QR scanned`; lifecycle changes append their action and state transition to `historyJson`. Device/restart acceptance passed.
- [~] Wire the participant-return screen to a real return action. Participant home opens the same scanner, where an authorised return can be recorded; cross-account Participant Return is intentionally not yet tested.
- [x] Tested the organiser happy path with two different resources, an invalid code, cancellation, denied permission, and no-network scan on device/emulator camera. Participant cross-account Return remains excluded from this acceptance pass.

**Completion evidence:** scanning a generated QR opens the corresponding passport and a return action updates its history/status.

## Inputs and Handoffs

| Needs from / gives to | Contract | Status |
|---|---|---|
| Needs from LIEW KAIY BIN | Event/resource/passport repositories, photo storage API, RLS-authorised refresh, QR dependency, navigation routes | `[x]` |
| Gives to MAH JUIN HONG | Read-only `PassportRoute(resourceId)` and available-resource status | `[x]` |
| Gives to WONG LOONG JIE | Resource condition/material/status events for matching and impact calculations | `[ ]` |

## AI Agent Working Rules

Before changing code:

- [ ] Pull/rebase and check for changes to LIEW KAIY BIN's shared models and repository interfaces.
- [ ] Read the Phase 4 and 5 checklists in `../REEVENT_FULL_APP_DEVELOPMENT_PLAN.md`.
- [ ] Keep resource logic in this feature package; do not bypass repositories with direct database/network calls.
- [ ] Preserve user-entered form values through rotation, permission prompts, and recoverable errors.

Before marking an item `[x]`:

- [ ] Test it with real repository data, not `MockData` or a visual-only control.
- [ ] Verify the action changes the item status/history and survives restart where applicable.
- [ ] Verify accessibility labels and text overflow on a small screen.
- [ ] Update this tracker and Phases 4/5 in the main plan with the evidence.

Status rule: `[x]` means implemented and verified; `[~]` means UI/mock/partially integrated; `[ ]` means no evidence of implementation.

## Handoff and Progress Update

After every checkpoint, update its checkbox and top progress bar, add a dated test/build note below, update the matching phase in the main plan, then push/pull and resolve conflicts before taking the next item.

## Independent Implementation Assessment (2026-07-30)

### Can this track start without waiting?

**Yes, with a narrow boundary.** The current codebase provides `EventRepository`, `ResourceRepository`, `PassportRepository`, `MediaRepository`, Room-first persistence/outbox, typed role routes, a working organiser resource form, and photo-picker upload. Work that only composes these existing contracts can start immediately. Do not change Gradle, core models, Room migrations, Supabase schema/RLS, shared repository interfaces, or typed navigation route definitions while working independently.

The most valuable independent work is the organiser-facing UI/workflow and QR rendering. Scanner and lifecycle persistence need small shared-contract extensions; isolate those behind a feature-local interface/state first, then make one reviewed core-contract change when ready.

### Verified project facts to build on

- `FeatureViewModel.saveResource` persists the resource and passport locally first, queues sync, and can upload an optional photo through `MediaRepository`.
- The live add-resource form creates a UUID resource ID and `reevent://passport/<resourceId>` payload. It currently validates only non-empty title/material and defaults category, condition, unit, value, status, and event selection.
- `PassportVisualScreen` reads resource data through the repository, but the established visual passport still renders `FakeQrPanel`; no encoded QR bitmap/library exists.
- `ParticipantReturnScreen` is a visual shell. A minimal `createReturn` transaction helper exists, but it does not update resource status or passport history.
- Manifest has only `INTERNET`; no camera permission or scanner dependency is present. Android Photo Picker avoids broad media permission for the current photo flow.
- Current runtime routes contain organiser/participant/partner graphs and a read-only `PassportRoute(resourceId)` suitable for marketplace use.

### Assumptions for solo completion

1. Use the existing model enums as the MVP source of truth: `ResourceCondition` and `ResourceStatus`; do not introduce a competing condition/status representation in UI code.
2. Treat the first owned event as the selected event until event-selection UI is added. Do not silently create multiple default events.
3. Use `reevent://passport/<resourceId>` as the QR payload format and validate both scheme and UUID before resolving a scan.
4. Generate QR images locally from the payload. No remote QR service or secret is required.
5. A lifecycle entry must include timestamp, action, actor ID/role, previous status, new status, and optional note. Store it in `ResourcePassport.historyJson` only after the shared serialization format is agreed and tested.
6. For the MVP, reject invalid/unknown scans locally and display a retry/cancel state; scanner operation must not depend on network availability.
7. Use the system photo picker as the supported photo acquisition path. Camera capture is explicitly deferred unless a separate camera/file-provider design is added.

### Missing information / decisions that must be made explicit

| Missing item | Solo default / action |
|---|---|
| Exact event fields, event edit/archive UX, and selected-event persistence policy | Keep the current first-event fallback; implement no destructive event operation until a product decision exists. |
| Resource form taxonomy (categories, materials, allowed units, required fields, quantity/value rules) | Use a small documented local list and validation; avoid hard-coding rules into shared models. |
| QR library approval/version and scanner implementation choice | Add neither without a scoped dependency decision; use a feature wrapper so rendering/scanning can be swapped. |
| Scan-history JSON schema and status-transition rules | Define and unit-test the schema/transition table before persisting history. |
| Who is allowed to check out, return, damage, repair, or transfer a resource | Default to owner for organiser actions and transaction sender for return; block other actions until role rules are agreed. |
| Return/repair/transfer transaction payload and how it changes `ResourceStatus` | Do not claim completion or send impact events until this shared behaviour is defined. |
| Draft storage policy, retention, and whether drafts sync remotely | Store drafts locally and account/event scoped only after a repository-backed design is approved. |
| Photo type/size limits, deletion policy, and failure/retry UX | Accept the system picker URI, show local preview/removal, and retain form data on upload failure; do not invent cloud deletion semantics. |
| Target Android devices/OS and physical camera test device | Emulator alone cannot validate a production scanner; record one real-device test before sign-off. |

### Solo safety notes

- The repository is owned by another Windows identity in this execution environment; use `git -c safe.directory='C:/Group Assignment Mobile app/ReEvent' ...` for read-only Git inspection if needed. Do not alter global Git trust settings.
- The shell does not set `JAVA_HOME`, but Android Studio's bundled JDK at `C:\Program Files\Android\Android Studio\jbr` successfully built `:app:assembleDebug --no-daemon --console=plain` on 2026-07-30. `supabase.local.properties` is still absent, so a live authentication test remains blocked until the public project URL and anon key are supplied.
- Preserve the established Compose visual wrappers in `RestoredVisualLiveScreens.kt`; runtime data must continue to pass through repositories/ViewModels rather than `MockData` or direct DAOs.

## Change Log

- 2026-07-14 - Tracker created from the audited full development plan.
- 2026-07-30 - Audited current source, project plans, and member trackers. Updated this track to reflect the repository-backed baseline and recorded solo-development assumptions, missing decisions, and environment verification blocker.
- 2026-07-30 - Completed the first resource-form implementation pass in `LiveFeatureScreens.kt`: extended resource metadata inputs, safe optional-photo preview/removal, and local validation while preserving the existing repository-backed save path. Static diff check passed.
- 2026-07-30 - Implemented the Checkpoint 1 event/inventory UI pass: event list/create/edit/archive/detail routes, selected-event DataStore state, repository-backed event dashboard, resource search/status filter/edit/status update, and empty/error-aware forms. `git diff --check` and `:app:assembleDebug --no-daemon --console=plain` passed. Device/restart/live-auth acceptance verification awaits the missing local Supabase configuration.
- 2026-07-31 - Implemented the Checkpoint 2 photo/draft pass: Android Photo Picker and FileProvider camera capture with permission/denial/cancellation feedback; private authenticated image reads for cards and passport; account/event-scoped DataStore resource drafts restored before save and cleared after success. Static diff check passed; Gradle compilation exceeded this environment's single-command time limit, so physical-device permission/camera/upload/restart validation remains required.
- 2026-07-31 - Device acceptance confirmed: camera denial, cancellation, no-image save, offline/upload-failure draft recovery, and successful upload followed by restart all behave correctly. Checkpoint 2 marked complete.
- 2026-07-31 - Implemented the Checkpoint 3 integration pass: `QrCodePanel` now encodes each persisted `reevent://passport/<resourceId>` value; resource creation records the first typed history entry; organiser status updates append a typed lifecycle entry; the passport page reads event/resource/passport data for details, owner, recommendation, and history. `git diff --check` and offline `:app:compileDebugKotlin --no-daemon --console=plain` passed (`BUILD SUCCESSFUL in 42s`).
- 2026-07-31 - Device/restart acceptance confirmed for Checkpoint 3: two resources retain different passport IDs and QR values after restart; the real passport displays the correct data; status updates add history; and the Passport Back control returns to its previous page. Checkpoint 3 marked complete. Rechecked cross-account behaviour: the local account cache receives all rows authorised by the signed-in user's Supabase RLS snapshot, so the shared read-only `PassportRoute` is suitable for marketplace use.
- 2026-07-31 - Implemented the Checkpoint 4 scanner pass under the documented solo assumption: CameraX preview plus ML Kit's bundled QR model, strict ReEvent-payload validation, scan cancellation/permission/invalid/offline states, scan-to-passport navigation, repository-backed lifecycle commands, and passport scan history. `:app:compileDebugKotlin --no-daemon --console=plain --offline` passed after adding the dependencies. Physical-device camera, two-resource, invalid-code, no-network, and return lifecycle acceptance remain required before marking complete.
- 2026-07-31 - Checkpoint 4 organiser acceptance confirmed: camera permission/cancellation, resources A and B, invalid QR, offline scan of cached passport, organiser lifecycle actions, history, and restart persistence passed. The Participant Return cross-account scenario was explicitly deferred, so Checkpoint 4 remains partial.
