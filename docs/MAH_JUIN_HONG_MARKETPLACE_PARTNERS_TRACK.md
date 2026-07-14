# MAH JUIN HONG - Marketplace and Partner Track

**Owns:** peer-to-peer resource optimisation and partner recovery operations: marketplace browsing and requests, partner programmes, partner list/map, and pickup/drop-off workflow.

**Current progress:** `[##--------] 20%` - marketplace resource cards, partner cards, a mock map image, and a partner detail bottom sheet exist. Search and filters are visual only, and no transaction, programme, map SDK, or pickup data is persisted.

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

### Checkpoint 1 - Functional marketplace `[ ]`

- [ ] Confirm listing, transaction, request-status, and permission contracts with LIEW KAIY BIN.
- [~] Replace fixed marketplace cards with repository-backed available resources.
- [ ] Implement working search, category/action/location filters, and empty results.
- [ ] Add marketplace item detail that opens the shared resource passport.
- [ ] Implement a request form for borrow, rent, donate, buy, or exchange, limited to the agreed MVP actions.
- [ ] Allow the owner to approve or reject a request.
- [ ] Complete a transaction and update the resource availability/status.
- [ ] Test request validation, unauthorised actions, cancellation, and owner approval.

**Completion evidence:** one user can request a resource, its owner can approve it, and both users see the completed status after restart.

### Checkpoint 2 - Partner programmes `[ ]`

- [ ] Confirm `CircularProgramme` and recovery-request contracts with LIEW KAIY BIN and WONG LOONG JIE.
- [ ] Build partner profile and accepted-material programme create/edit/list flows.
- [~] Replace static partner cards with programmes filtered by category, material, and minimum condition.
- [ ] Show partner capability, distance/location, fees or reward, pickup availability, and processing time.
- [ ] Add relevant loading, no-match, and error states.

**Completion evidence:** a partner can create a programme and an organiser sees it only when a compatible resource is selected.

### Checkpoint 3 - Partner discovery and handover `[ ]`

- [~] Keep the current mock map/list as a fallback while the feature becomes data-driven.
- [ ] Implement a map SDK only after the list and partner selection work reliably; otherwise use the verified list fallback.
- [ ] Bind pins/locations and the partner bottom sheet to live programme data.
- [x] Preserve the existing partner detail bottom sheet interaction while replacing its fixed content.
- [ ] Implement pickup/drop-off request creation, status, and confirmation.
- [ ] Send a completed handover/transaction event to WONG LOONG JIE's impact contract.
- [ ] Test no location permission, no partner match, pickup failure, and manual drop-off fallback.

**Completion evidence:** an organiser selects a compatible partner and creates a persisted pickup or drop-off request.

## Inputs and Handoffs

| Needs from / gives to | Contract | Status |
|---|---|---|
| Needs from LIEW KAIY BIN | Partner, transaction, and request repositories; current role; navigation routes | `[ ]` |
| Needs from WONG JIE YING | Resource availability, material/condition data, read-only passport | `[ ]` |
| Needs from WONG LOONG JIE | Recommended partner/action and impact event format | `[ ]` |
| Gives to WONG LOONG JIE | Completed/failed transactions and partner handover outcomes | `[ ]` |

## AI Agent Working Rules

Before changing code:

- [ ] Pull/rebase and inspect current shared contracts; do not copy `MockData` into new persistence logic.
- [ ] Read the Phase 7 and 8 checklists in `../REEVENT_FULL_APP_DEVELOPMENT_PLAN.md`.
- [ ] Keep a usable partner-list fallback until a map integration is tested on device.
- [ ] Avoid payments, chat, live logistics tracking, and other out-of-scope marketplace features.

Before marking an item `[x]`:

- [ ] Verify a complete request/approval/handover state change through repository data.
- [ ] Verify that users cannot approve their own request or access another user's private transaction.
- [ ] Verify loading, no-match, permission, and failure states.
- [ ] Update this tracker and Phases 7/8 in the main plan with evidence.

Status rule: `[x]` means implemented and verified; `[~]` means UI/mock/partially integrated; `[ ]` means no evidence of implementation.

## Handoff and Progress Update

After every checkpoint, update its checkbox and top progress bar, add a dated test/build note below, update the matching phase in the main plan, then push/pull and resolve conflicts before taking the next item.

## Change Log

- 2026-07-14 - Tracker created from the audited full development plan.
