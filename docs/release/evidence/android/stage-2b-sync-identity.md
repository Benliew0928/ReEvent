# Stage 2B account/environment sync acceptance evidence

- **Date:** 2026-08-09
- **Base commit:** `fedebae1` plus the current Stage 1/Stage 2A/Stage 2B working tree
- **Runtime environment:** typed `AppEnvironment.LOCAL` (`local`)
- **Device:** Android Studio `Medium_Tablet` AVD, Android API 35
- **Backend:** fake `SyncGateway`; no Supabase migration or live backend mutation belongs to Stage 2B
- **Account fixtures:** `account-a` and `account-b`

## Accepted gates

### REL-SYNC-01 — Account/environment-bound outbox execution

- Room database version 4 adds a non-null `environment` field to every outbox row.
- Outbox uniqueness is `(environment, accountId, tableName, recordId)`. Pending reads, failure updates, successful deletion and sign-out cleanup all require both the environment and account partition.
- `MIGRATION_3_4` rebuilds the outbox, preserves all existing fields and assigns every attributable version-3 row to `local`.
- `SyncWorkIdentity` carries a typed environment and nonblank account ID in both WorkManager input and the unique work name.
- `SyncCoordinator` accepts work only when the requested environment, running APK environment, gateway environment, active `AccountScope` and authenticated Supabase subject all agree.
- The coordinator revalidates that identity before every remote operation and again before deleting the queue row or changing local sync state.
- Wrong environment, wrong active account, missing/expired authenticated subject, wrong authenticated subject and wrong gateway environment return a terminal stale-identity no-op with zero remote calls and zero queue mutation.
- Correct work executes only its own environment/account rows. Equivalent local rows for another account and equivalent staging rows for the same account remain untouched.

### REL-SYNC-05 — Safe sign-out/account replacement

- Scheduling is `requestSync(accountId)` and cancellation is the awaited `cancelSync(accountId)` operation.
- Current work uses `reevent-core-sync-v2:<environment>:<accountId>`. The legacy global `reevent-core-sync` job is cancelled when scheduling or cancelling and a legacy/malformed worker invocation without identity input exits successfully without accessing the queue.
- Coroutine cancellation is rethrown. The interrupted row remains pending with no attempt increment, failure text or sync-state change.
- If account/session identity changes after a remote operation, the coordinator does not delete or mark the local row; it remains available to the correct account.
- Sign-out and account replacement clear `AccountScope` before awaiting cancellation, then remove only the selected account's user projection, six Room cache projections, current-environment outbox partition and private resource drafts. Another account's rows and drafts remain intact.

## Migration and schema proof

- Exported Room schema 4 SHA-256: `DA0CA945B83DAF65022F0144AD5C6160300918132F7BA9B2F5FD046250A73C51`.
- Direct migration 3→4 preserves environment-independent row fields (`id`, account, table, record, operation, payload, attempts, last error and timestamp) and sets `environment = local`.
- Full paths 1→2→3→4 and 2→3→4 pass Room schema validation. Attributable version-2 data remains preserved; ambiguous blank-account data is still removed at 2→3 as required by Stage 2A.
- The previous Stage 2A report remains the historical version-3 acceptance record and links back to this version-4 continuation.

## Commands and results

| Command/scenario | Result |
|---|---|
| `.\gradlew.bat :app:compileDebugKotlin :app:compileDebugUnitTestKotlin :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest` | PASS — 24 JVM tests, 0 failures, 0 errors; production, unit-test and Android-test Kotlin compiled |
| `.\gradlew.bat :app:connectedDebugAndroidTest` | PASS — 12 instrumentation tests, 0 failures, 0 errors, API 35 AVD |
| Room migration scenarios | PASS — 1→2→3→4, 2→3→4 and 3→4 |
| Fake-gateway stale identity scenarios | PASS — wrong runtime/gateway environment, active account and authenticated subject made zero remote calls and zero queue mutations |
| Cancellation/session-change scenarios | PASS — interrupted/stale rows remained pending and unmodified |
| Account cleanup scenario | PASS — selected account cancelled and purged; other-account projections, outbox and drafts preserved |
| `.\gradlew.bat :app:assembleDebug` | PASS — debug APK assembled |
| `git diff --check` | PASS — no whitespace errors |

## Source evidence

- `ReEvent/app/schemas/com.reevent.app.core.database.ReEventDatabase/4.json`
- `ReEvent/app/src/main/java/com/reevent/app/core/config/AppEnvironment.kt`
- `ReEvent/app/src/main/java/com/reevent/app/core/sync/SyncWorkIdentity.kt`
- `ReEvent/app/src/main/java/com/reevent/app/core/sync/SyncGateway.kt`
- `ReEvent/app/src/main/java/com/reevent/app/core/sync/SyncScheduler.kt`
- `ReEvent/app/src/main/java/com/reevent/app/core/auth/AccountSessionCleaner.kt`
- `ReEvent/app/src/androidTest/java/com/reevent/app/core/database/ReEventDatabaseMigrationTest.kt`
- `ReEvent/app/src/androidTest/java/com/reevent/app/core/database/CoreDaoAccountIsolationTest.kt`
- `ReEvent/app/src/androidTest/java/com/reevent/app/core/sync/SyncCoordinatorIdentityTest.kt`
- `ReEvent/app/src/androidTest/java/com/reevent/app/core/auth/AccountSessionCleanerTest.kt`
- `ReEvent/app/src/test/java/com/reevent/app/core/sync/SyncWorkIdentityTest.kt`

## Deliberately deferred

This evidence does not accept staging/release variants, production configuration validation, demo-auth removal, environment keys for shared projections, atomic Room aggregates, retry fairness, terminal-failure handling, sync-status UI, Supabase RLS/RPC authority, remote deletion reconciliation, or removal of critical commands from the generic outbox.
