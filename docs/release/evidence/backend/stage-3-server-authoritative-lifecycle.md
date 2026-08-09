# Stage 3 — Server-authoritative lifecycle

- **Date:** 2026-08-09
- **Base commit:** `0edc3ba2591202d8e9eb4a917768b0c7cb38f921`
- **Implementation commits:** `3721aa5efe0bfc597e4a2e02c0fee19bb561ec78` (`feat: enforce server-authoritative lifecycle`) and `68e82033c45020e48d08db43259a04c86ae652a6` (`fix: resolve pgcrypto lookup in Supabase`)
- **Evidence state:** Local verification and the rollback-only official staging SQL smoke now pass. This is not release-checklist acceptance evidence until real signed multi-account/device and interrupted-response flows are recorded.

## Outcome

Four reviewed implementation stages now form one server-authoritative resource lifecycle:

1. **3A — Frozen lifecycle schema:** typed release entities, ownership/quantity constraints, immutable passport/ledger/impact records, least-privilege grants and RLS, automatic passports, lifecycle-field protection, and exactly-once archive history.
2. **3B — Request and decision commands:** marketplace/programme request, approve, reject, and cancel RPCs derive the actor from `auth.uid()`, validate roles/eligibility/quantity, lock competing rows, and reserve or release allocations, capacity, and ReCoins atomically.
3. **3C — Handover and completion commands:** handover, receipt, return, and completion implement all frozen transaction types, partial-lot lineage, exchange, settlement, impact, passport history, terminal immutability, rollback, and replay.
4. **3D — Android command boundary:** runtime lifecycle actions use typed RPCs only. A dedicated account/environment-scoped Room command queue persists the exact payload and UUID before invocation; transport/process retries reuse that UUID. Generic row sync rejects server-owned tables. Server snapshots and command acknowledgement are applied with Room transactions.

## Artifacts

| Artifact | SHA-256 |
|---|---|
| `ReEvent/supabase/migrations/0005_release_lifecycle_schema.sql` | `15A09675F26592A9EF9F51946C5C6960D69EB5387C53378F5D842BDF8998E9EE` |
| `ReEvent/supabase/migrations/0006_transaction_request_decision_rpcs.sql` | `1F2538220F2563E97AFEA86DAE511DABBC432E7DB277C3384FE000665A6CCA33` |
| `ReEvent/supabase/migrations/0007_atomic_handover_completion_rpcs.sql` | `27337DA92013F846EFAB80191B52E7E0F45850A18C3691ECE31082C516E46544` |
| Room schema 5 | `52E3710FC54EEBF579D790063AE7CA68D529E9030453EFB4B438DFDCFB37A69A` |
| `ReEvent/supabase/tests/staging-authority-smoke.sql` | `E585A2F784DD81C452AB5B30923CAB7CB5BF7AF9C225BB2767772AC8F3E87B96` |

Primary Android boundaries are `LifecycleCommandGateway.kt`, `LocalFirstCoreRepository.kt`, `SyncScheduler.kt`, `CoreDao.kt`, and `ReEventDatabase.kt`.

## Verification

| Gate | Environment | Result |
|---|---|---|
| `npm test` from `ReEvent/supabase/tests` | Embedded disposable PostgreSQL via PGlite | **15 tests, 0 failures** |
| `:app:testDebugUnitTest` | Local JVM | **22 tests, 0 failures/errors/skips** |
| `:app:connectedDebugAndroidTest` | Android Studio `Medium_Phone` AVD, API 35 | **14 tests, 0 failures/errors/skips** |
| `:app:lintDebug` | Android debug variant | **Passed; 0 errors, 28 warnings** |
| `staging-authority-smoke.sql` | Official Supabase `ReEvent-staging` project `kxkdugzyjmoteguesoti`; SQL Editor; three frozen role profiles through `auth.uid()` claims | **PASS** — direct lifecycle/transaction writes and wrong actors rejected; RENT request, decision, handover, return, completion and completion replay passed; fixtures rolled back |
| `git diff --check` | Working tree | **Passed** |
| Forbidden-write audit | Android main source | No removed lifecycle mutators, legacy lifecycle enums, or generic critical-table outbox targets found |

The database suite proves schema application, grants, RLS-facing authority, frozen enums, one-time wallet/passport creation, constraints, direct lifecycle tamper rejection, archive-history idempotency, request/decision validation, programme capacity, ReCoin holds, RENT return, partial RECYCLE, EXCHANGE, partial BUY, full REPAIR, replay, terminal immutability, and total rollback after a forced mid-completion failure.

## Official staging execution

- Target: Supabase project `ReEvent-staging` (`kxkdugzyjmoteguesoti`), primary database, executed 2026-08-09 through the logged-in SQL Editor.
- Preflight found the legacy 0001-shaped schema with 5 frozen profiles and only synthetic staging fixtures (10 events, 11 resources, 5 transactions, and 1 impact row). No migration history was recorded in `supabase_migrations.schema_migrations`.
- The committed 0005-0007 transaction replaced that legacy schema. The first smoke attempt found an actual Supabase incompatibility: `pgcrypto.digest` lives in `extensions`, while the idempotency helper had a `public`-only search path. Commit `68e82033c45020e48d08db43259a04c86ae652a6` adds `extensions` to that fixed search path; its replacement function was then applied to staging.
- The final smoke script passed and ends with `ROLLBACK`; its generated event, resource, listing, transaction, allocation, hold, ledger, passport, and impact rows were not retained.

The device suite includes Room 1→5/2→5/3→5/4→5 validation, account isolation, sign-out cleanup, environment/account worker identity, cancellation behavior, and a failed-then-successful lifecycle retry that proves the same durable idempotency UUID is used twice before the server projection is cached and the command is acknowledged.

## Review findings closed during the stage

- Removed recursive cross-table RLS evaluation by routing resource/passport visibility through security-definer read helpers.
- Corrected lock order, temporary-transaction due-date constraints, and an ambiguous completion actor reference.
- Added an active-request uniqueness constraint and explicit terminal mutation protection.
- Removed client creation of passport history and impact rows.
- Replaced in-memory-only RPC keys with a durable typed command queue after review identified lost-response duplication risk.
- Protected resource ownership, quantity, lineage, and lifecycle status from direct clients while retaining a safe, ledger-backed archive transition.
- Aligned Room/Supabase transaction, quantity, impact, passport-history, event-address, and programme-address projections.
- Cleared all three recorded Android lint errors.

## Remaining release acceptance gate

This report must not be used to check `REL-SEC-01` through `REL-SEC-05`, `REL-DATA-04`, or the final E2E items yet. The remaining gates are:

The reviewable commits, official staging deployment, and rollback-only three-role SQL authority smoke are complete. The remaining evidence must exercise authenticated client sessions rather than SQL-injected claim settings.

1. [x] create a reviewable commit containing the Stage 3 artifacts: `3721aa5efe0bfc597e4a2e02c0fee19bb561ec78`;
2. apply migrations 0005–0007 to a disposable official Supabase local stack and then staging;
3. rerun positive and adversarial calls with independent organiser, participant, and partner JWTs;
4. run the interrupted/lost-response flow against staging on separate app sessions/devices;
5. record the tested commit SHA, backend project/environment, logs, and reviewer in the checklist evidence registry.

`PUBLIC_BASE_URL` must also be configured to a real HTTPS passport verifier before QR/public-passport acceptance; the Android cache deliberately retains the opaque token when that release configuration is absent, but that fallback is not public-QR evidence.
