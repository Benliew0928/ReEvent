# WONG JIE YING - Resource Lifecycle and QR Track

**Owns:** the organiser's event and resource lifecycle: event selection/creation, inventory, add/edit resource, photo flow, digital resource passport, and QR actions.

**Current progress:** `[##--------] 20%` - the dashboard, add-resource screen, and passport screen exist as static Compose UI using `MockData`. There is no event model, Room persistence, photo upload, real item ID, QR generation, scanner, or scan history.

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

### Checkpoint 1 - Event and inventory data `[ ]`

- [ ] Confirm the `Event`, `ResourceItem`, condition, ownership, and status contracts with LIEW KAIY BIN.
- [ ] Replace direct `MockData` use in owned screens with repository-backed UI state.
- [ ] Build event list, create-event, edit-event, and event-detail flows.
- [~] Convert the current organiser dashboard from fixed metrics into a selected-event dashboard.
- [~] Convert the add-resource form preview into a validated form.
- [ ] Support resource create, read, update, search, filter, and status changes.
- [ ] Add clear empty, loading, validation-error, and save-error states.

**Completion evidence:** an organiser can create an event, add a resource, edit it, restart the app, and see the saved data again.

### Checkpoint 2 - Resource photo and drafts `[ ]`

- [ ] Add image picker/camera permission handling with a safe cancellation path.
- [ ] Show a real local preview, removal action, and upload state.
- [ ] Save an incomplete resource as a local draft.
- [ ] Integrate remote photo upload only through the repository supplied by LIEW KAIY BIN.
- [ ] Test denied permission, no image selected, offline save, and upload failure.

**Completion evidence:** the resource form does not lose user input when photo selection, upload, or connectivity fails.

### Checkpoint 3 - Digital passport `[ ]`

- [ ] Generate a stable unique item ID through the shared resource/passport contract.
- [ ] Create and persist a resource passport when a resource is saved.
- [~] Replace the visual `FakeQrPanel` with a QR code generated from the real passport/item ID.
- [~] Bind passport details, condition, owner, event history, and recommended action to actual data.
- [ ] Record history entries for resource lifecycle changes.
- [ ] Ensure a marketplace detail can open the same passport in read-only mode for MAH JUIN HONG.

**Completion evidence:** two resources have different stable IDs and display the correct passport after app restart.

### Checkpoint 4 - QR scanning and return lifecycle `[ ]`

- [ ] Ask LIEW KAIY BIN to add/approve the QR scanner dependency before integration.
- [ ] Build a QR scanner with camera permission, invalid-code, cancelled-scan, and no-network handling.
- [ ] Resolve a scanned QR value to the correct passport.
- [ ] Implement check-out, return, damaged, repair-request, and transfer actions using repository commands.
- [ ] Save scan history and show it on the passport.
- [ ] Wire the participant-return screen to a real return action.
- [ ] Test the happy path with at least two different resources and an invalid code.

**Completion evidence:** scanning a generated QR opens the corresponding passport and a return action updates its history/status.

## Inputs and Handoffs

| Needs from / gives to | Contract | Status |
|---|---|---|
| Needs from LIEW KAIY BIN | Event/resource/passport repositories, photo storage API, QR dependency, navigation routes | `[ ]` |
| Gives to MAH JUIN HONG | Read-only passport route and available-resource status | `[ ]` |
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

## Change Log

- 2026-07-14 - Tracker created from the audited full development plan.
