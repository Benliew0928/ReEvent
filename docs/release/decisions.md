# ReEvent 1.0 Frozen Decisions and Migration Map

**Status:** Frozen for Stage 2 implementation  
**Decision set:** 1.0.0  
**Frozen on:** 2026-08-09

This record explains the choices behind `architecture.md` and `data-contract.md`, maps the current prototype to the target, and records what Stage 1 intentionally did not implement.

## 1. Decision register

| ID | Frozen decision | Consequence |
|---|---|---|
| D-001 | ReEvent 1.0 is a publicly publishable Huawei AppGallery assignment demo | Privacy, support, security and release requirements remain real even though the economic system is simulated |
| D-002 | Supabase/Postgres is authoritative for shared and critical state | Android cannot directly assert roles, completion, ownership, impact or ReCoin balances |
| D-003 | Room is an environment/account-scoped cache | Local identity becomes `(environment, account_id, record_id)` and account switching cannot reuse private rows/outbox work |
| D-004 | Private organiser event/resource drafts may sync offline; critical shared commands are online-only | No optimistic QR lifecycle, approval, ReCoin, transfer, completion, impact or deletion success |
| D-005 | 1.0 supports borrow, rent, buy, donate, exchange, repair, recycle and buy-back | All eight require server state-machine coverage and final E2E evidence |
| D-006 | Return is a lifecycle phase, not a transaction type | Borrow/rent/repair use `RETURN_IN_PROGRESS`; legacy `RETURN` requires migration handling |
| D-007 | Organisers originate event resources; every verified role may own/relist an acquired resource | Ownership is separate from event origin and may change atomically at completion |
| D-008 | Permanent partial transfers split a resource lot/passport; exchange uses whole lots | Quantity and provenance stay traceable without giving two owners one passport |
| D-009 | Marketplace listing availability is separate from resource lifecycle | Reservations/loans use allocations instead of misleading whole-resource statuses |
| D-010 | ReCoins are fictional, whole, non-purchasable and non-convertible | No card/payment SDK, withdrawal, cash price or RM savings claim is permitted |
| D-011 | Each verified account receives one 1,000 ReCoin grant | Wallet creation and grant are one idempotent server operation |
| D-012 | Priced actions hold on approval, settle on completion and release on reject/cancel | Concurrent spending cannot make balances negative and failed handovers do not transfer coins |
| D-013 | Circular reward v1 mints return 5, donation 10, repair 15 and recycle 10 | One versioned reward at most per eligible completed transaction |
| D-014 | Exchange is resource-for-resource with no ReCoins | Both whole lots reserve and ownership swaps only after both receipts |
| D-015 | QR v1 is an HTTPS public-domain link with a random opaque token | Normal scanners can read it; Android App Links and a privacy-safe web fallback can resolve it |
| D-016 | QR token is identification, not authorisation, and normally persists for passport life | Every write still requires authentication, role/state checks and idempotency |
| D-017 | Account deletion anonymises completed shared history | Active work/holds are resolved, PII/media/session are removed, balance is burned and public history says `Deleted user` |
| D-018 | Matching is deterministic and named Smart Match/Circular Match | No AI/model claim; inputs, hard exclusions, score and tie-breaks are testable |
| D-019 | Environmental estimates remain deliberately narrow | Only documented completed plastic/acrylic recycling in kilograms gets CO2e; other values are unavailable |
| D-020 | Economic reporting uses actual ReCoins, not cents or RM | `valueRecoveredCents` is removed from the target and is never converted into ReCoins |
| D-021 | Local/test, staging and production are isolated | Production cannot contain demo auth fallback, staging endpoints or administrator reset controls |
| D-022 | Stage 1 changes contracts and tracker evidence only | Runtime Kotlin, Room, SQL, UI, website deployment and AppGallery state remain unchanged until later stages |

## 2. Frozen transaction catalogue

| Action | Entry point | Price | Terminal physical result |
|---|---|---|---|
| Borrow | P2P listing | Free | Resource returns to owner; borrower receives 5-ReCoin return reward |
| Rent | P2P listing | Receiver pays per-unit ReCoin fee after return | Resource returns to owner; renter receives 5-ReCoin return reward |
| Buy | P2P listing | Receiver pays per-unit ReCoin price | Full/child lot permanently belongs to receiver |
| Donate | P2P listing | Free | Full/child lot permanently belongs to receiver; donor receives 10 ReCoins |
| Exchange | P2P listing plus counter-resource | No ReCoins | Two whole lots swap owners after both receipts |
| Repair | Partner programme | Free or owner pays partner | Repaired resource returns to owner; owner receives 15 ReCoins |
| Recycle | Partner programme | Free or partner pays owner | Quantity becomes recovered; sender receives 10 ReCoins |
| Buy-back | Partner programme | Partner pays owner | Full/child lot permanently belongs to partner |

No action invokes real money. Rent is a fixed ReCoin fee for the selected quantity/due date, not daily billing. Overdue items are shown as overdue but 1.0 has no late charge, dispute engine, courier service or forced ownership transfer.

## 3. Current enum to target mapping

### Roles and condition

| Current Kotlin/SQL | Target | Migration rule |
|---|---|---|
| `ORGANIZER`, `PARTICIPANT`, `PARTNER` | Same | Preserve only server-authorised profile role |
| `NEW`, `GOOD`, `FAIR`, `NEEDS_REPAIR` | Same | Direct mapping |
| `RECYCLE_ONLY` | `END_OF_LIFE` | Rename; append migration passport note only when public data already exists |

### Resource state

| Current | Target representation | Migration rule |
|---|---|---|
| `DRAFT` | Resource `DRAFT` | Direct |
| `AVAILABLE` | Resource `ACTIVE` plus `PUBLISHED` listing when marketplace-visible | Create listing only when existing row was genuinely published |
| `RESERVED` | Resource `ACTIVE` plus active `RESERVED` transaction allocation | Derive from an accepted non-terminal transaction; otherwise return to active/unlisted |
| `HANDED_OVER` | Resource `ACTIVE`, `RECOVERY_IN_PROGRESS`, or transferred ownership plus passport event | Derive from transaction type/status; ambiguous staging rows are reset rather than guessed |
| `RECOVERED` | Resource `RECOVERED` | Direct, with recovered passport event if absent |
| `ARCHIVED` or `archived=true` | Resource `ARCHIVED` | Direct; close listing |

### Transaction type

| Current | Target | Migration rule |
|---|---|---|
| `RESALE` | `BUY` | Rename; current `valueCents` does not become a ReCoin price |
| `DONATION` | `DONATE` | Rename |
| `REPAIR` | `REPAIR` | Direct |
| `RECYCLE` | `RECYCLE` | Direct |
| `BUY_BACK` | `BUY_BACK` | Direct |
| `RETURN` | Borrow/rent/repair return lifecycle | Link to its originating temporary transaction; disposable staging rows without one are removed |
| None | `BORROW`, `RENT`, `EXCHANGE` | New types; no historical inference |

### Transaction status

| Current | Target | Migration rule |
|---|---|---|
| `PENDING` | `REQUESTED` | Direct semantic rename |
| `ACCEPTED` | `APPROVED` | Direct only after allocation/hold validation |
| `IN_TRANSIT` | `IN_TRANSIT` | Direct only when required confirmation exists |
| `COMPLETED` | `COMPLETED` | Preserve only if completion invariants can be reconstructed; staging may reset |
| `CANCELLED` | `CANCELLED` | Preserve terminal status |
| None | `ACTIVE`, `RETURN_IN_PROGRESS`, `REJECTED` | New states |

### Programme type

| Current | Target | Migration rule |
|---|---|---|
| `REPAIR`, `RECYCLE`, `BUY_BACK` | Same | Add required eligibility/location/coin terms before activation |
| `COLLECTION` | `RECYCLE` | Migrate as free recycle programme and require partner review before activation |
| `REUSE` | `BUY_BACK` with `FREE` coin direction | Represents partner acquisition for reuse; require partner review before activation |

### Sync state

| Current | Target | Migration rule |
|---|---|---|
| `SYNCED` | `SYNCED` | Direct |
| `PENDING` | `PENDING` | Direct only for permitted offline draft operations |
| `FAILED` | `FAILED` | Preserve error metadata and user recovery action |
| None | `LOCAL_ONLY` | New state for drafts that have never entered the remote outbox |

## 4. Current table/model to target mapping

| Current source | Preserved target data | Required target change |
|---|---|---|
| `profiles` / `User` | ID, display name, role, avatar, timestamps | Stop duplicating email in shared profile; freeze role; wallet created after verification; deletion behavior |
| `events` / `Event` | ID, owner, name, description, venue/address, dates, status, timestamps | Add type, timezone, coordinates, attendance, recovery target; replace free-text status enum |
| `resource_items` / `ResourceItem` | ID, event origin, owner, title, category, material, condition, quantity, unit, images, timestamps | Split `created_by` from current owner; add parent, description/location/reuse; separate photos and listings; replace cents/status meaning |
| `resource_passports` / `ResourcePassport` | One passport per resource, creation timestamps | Replace full stored URI/UUID with version/token; move JSON history to append-only events; add revocation/replacement |
| `circular_programmes` / `CircularProgramme` | ID, partner, name, material rules, location, active | Restrict types; add category/condition/quantity/capacity/unit, real coordinates, pickup, processing, terms and ReCoin direction/amount |
| `circular_transactions` / `CircularTransaction` | IDs, event/resource, actors, type/status, quantity, timestamps | Add source listing/programme, counter resource, unit, price snapshot, due date, confirmations, allocations, idempotency and terminal reason |
| `impact_records` / `ImpactRecord` | Event/resource/transaction linkage, supported mass/CO2, timestamp | Make transaction unique/immutable; add action/quantity/unit/factor provenance and ReCoins; remove cents |
| `historyJson` | Valid parseable history content | Convert to individual passport events; malformed staging history is discarded with migration report |
| `valueCents` / `valueRecoveredCents` | None | Do not convert real/cents fields into fictional coins; listing/programme ReCoin values start unset/zero until configured |
| `sync_outbox` | Draft operation concept, attempt/error metadata | Include environment/account/payload version/next attempt; exclude critical shared commands; make uniqueness account/environment-safe |
| None | Listings, photos, allocations, confirmations, wallets, holds, ledger, impact factors, idempotency records | New authoritative entities |

Current production has not been publicly released. Therefore Stage 2 may reset synthetic staging rows that cannot be migrated without inventing ownership, transaction origin, price or completion evidence. A production migration must never guess these values.

## 5. QR decision

```text
Canonical v1 payload = ${PUBLIC_BASE_URL}/p/v1/<128-bit-opaque-token>
```

- `PUBLIC_BASE_URL` is typed per environment and must be HTTPS outside local tests.
- Tokens contain no UUID or PII, have at least 128 bits of cryptographic entropy, and use a URL-safe encoding.
- The public resolver returns only title, category, material, condition, resource lifecycle status, and privacy-safe history summaries/timestamps.
- Token scanning never grants a state-changing action. Authenticated actor/state checks determine available commands.
- Tokens do not expire naturally; they are revoked/replaced or retired with an explicit status.
- Legacy `reevent://passport/<UUID>` is read only during an authenticated migration window and is never generated after Stage 2 QR migration.

## 6. ReCoin decision

ReCoin exists only to exercise transaction logic in the assignment demo.

- One verified account receives exactly one 1,000-ReCoin grant.
- Users cannot buy, cash out, convert, manually gift or self-mint ReCoins.
- Buy and rent listing prices are whole ReCoins per unit; free actions use zero.
- Partner programmes declare free, owner-pays-partner, or partner-pays-owner terms.
- Approval revalidates balance and creates a hold; completion settles; permitted cancellation/rejection releases.
- Wallet balances, holds and append-only ledger entries change in the same server transaction as workflow effects.
- Circular reward policy `reevent-demo-reward-v1` is fixed at return 5, donation 10, repair 15 and recycle 10.
- A reward is keyed to transaction plus policy version and cannot be replayed.
- Account deletion burns remaining demo balance with an auditable non-cash ledger entry.

## 7. Matching decision

The release term is **Smart Match** or **Circular Match**. The algorithm is a deterministic rule engine, not AI.

- Hard eligibility covers role/self-dealing, active status, action, material/category/condition, quantity/capacity, unit and exchange ownership.
- Eligible candidates score material 30/15, category 20/10, distance 25/18/8/0, capacity 15/8 and pickup 10 as detailed in `data-contract.md`.
- Unknown location yields no distance points and sorts after equivalent known-distance candidates; it never invents a distance.
- Stable tie-breaks are score, known/shorter distance, name and UUID.
- Every result includes reasons and rejected constraints.

## 8. Impact decision

- Every completed transaction contributes its actual channel, completed quantity/unit, ReCoins transferred and ReCoins rewarded.
- CO2e is available only for completed plastic/acrylic recycling recorded in kilograms.
- The frozen factor is `1.59710826 kgCO2e/kg`, sourced and limited exactly as documented in `docs/impact/IMPACT_ESTIMATE_FACTORS.md`.
- Stored results keep full numeric precision; display uses two-decimal half-up rounding.
- Unsupported mass/CO2 values are null/unavailable, never invented zero.
- No RM, cents, carbon-credit, certification, Malaysia-specific lifecycle, transport or item-to-mass claim is made.
- Recovery calculations remain separated by unit and count borrow/rent only after return; buy is reported as transfer rather than environmental recovery.

## 9. Account deletion decision

- User must have a recently reauthenticated session.
- Requested/approved work is cancelled and allocations/holds are released.
- Deletion waits in a visible pending state when physical custody is already in transit/active/returning.
- Listings close, partner programmes deactivate and remaining owned resources archive.
- Profile, auth account, avatar/private media, sessions and private notes are removed.
- Remaining ReCoins are burned because they have no monetary value.
- Completed transaction/passport/impact/ledger integrity remains with actor IDs nulled and the public label `Deleted user`.
- Retained records expose no stable public pseudonym or contact information.

## 10. Promise trace and `REL-SCOPE-03`

The canonical 1.0 promise is the release-truth checklist plus the Stage 1 contracts. Current active surfaces still differ:

| Surface | Current mismatch | Required later action |
|---|---|---|
| Android participant return | Fake non-QR panel | Replace with QR v1 after server/token implementation |
| Android partner map | Static image and no-op acceptance | Replace with live map/action in later stage |
| Android “AI Match” wording | Rule-based behavior presented as AI in places | Rename to Smart/Circular Match |
| Android marketplace | Narrow legacy enum and client-controlled transitions | Implement target listing/state-machine/RPC contracts |
| Website support/deletion | Pages exist but operational contact/backend are absent | Implement before release |
| AppGallery copy | Not authored | Create from final verified feature set |
| Assignment report | Not final | Generate evidence from signed candidate only |
| Older plans/member trackers | Contain broader/historical promises | Remain historical; do not use as completion authority |

Therefore `REL-SCOPE-03` remains unchecked after Stage 1. This section records its progress without falsely claiming final copy alignment.

## 11. Contract walkthrough results

| Scenario | Frozen result |
|---|---|
| Concurrent approval for last quantity | Row locks/allocation sum allow one approval; loser receives conflict with no hold |
| Insufficient balance | Approval fails before allocation/hold/state change |
| Duplicate request | Same idempotency key returns original; active duplicate constraint rejects a new logical duplicate |
| Repeated completion | Returns original terminal result; unique impact/reward/ledger/passport effects prevent duplication |
| Partial buy/donate/buy-back/recycle | Server reduces parent and creates linked child lot/passport atomically |
| Whole-lot exchange | Both lots reserve; both receipts required; ownership swaps atomically |
| Offline critical action | No local critical mutation; UI reports connection required |
| Deleted account with requested/approved work | Cancel, release allocation/hold, then delete/anonymise |
| Deleted account with physical custody active | Deletion remains pending until a safe terminal handover state |
| Expired session | No RPC runs; sign-in and server refresh required |
| Unauthorised actor/forged IDs | Server derives caller and rejects with no partial effect |
| Poison offline draft | Terminal failed row is visible and does not starve later account-scoped rows |
| Unknown matching coordinates | Candidate remains eligible after known-distance peers and shows distance unavailable |
| Unsupported impact input | Channel/quantity recorded; mass/CO2 null and displayed unavailable |

## 12. Deliberately deferred implementation

Stage 1 does not modify or claim completion for:

- Kotlin enums/models/use cases/repositories;
- Room entities, database version, migration JSON or outbox;
- Supabase tables, RPCs, triggers, RLS, storage policy or seed;
- ReCoin UI or wallet persistence;
- QR generation/scanning/deep links or public passport website route;
- live map, matching UI, impact UI or fake/no-op controls;
- auth recovery/deletion runtime;
- test infrastructure, release build, website deployment or AppGallery submission.

These are implementation stages that must conform to the frozen contracts rather than reinterpret them.

## 13. Stage 1 acceptance record

The three contracts were reviewed against:

- current domain enums/models in `ReEvent/app/src/main/java/com/reevent/app/core/model/CoreModels.kt`;
- current workflow rules in `ReEvent/app/src/main/java/com/reevent/app/core/data/TransactionWorkflow.kt`;
- current Room entities/DAO/repository interfaces;
- Supabase migrations `0001` through `0004` and staging seed;
- impact factor documentation;
- current Android routes/runtime surfaces and website legal/support pages;
- `REEVENT_RELEASE_TRUTH_CHECKLIST.md`.

Acceptance checks:

- all target enums have a current mapping or are explicitly new;
- every authoritative entity has identity, ownership, nullability and lifecycle rules;
- every transaction type has actors, state path, quantity, ReCoin, ownership/custody, passport and impact outcomes;
- failure and deletion behavior are defined;
- QR, matching, impact, environment and reward values agree across all three contracts;
- Stage 1 contains no unresolved placeholder or implementation claim.

Stage 1 acceptance changes tracker evidence only. It does not reduce the eight P0 or nine P1 runtime blockers.
