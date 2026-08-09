# Stage 2A Room cache acceptance evidence

**Date:** 2026-08-09  
**Base commit:** `fedebae1` plus the current Stage 1/Stage 2A working tree  
**Device:** Android Studio `Medium_Tablet` AVD, Android API 35  
**Backend:** Not exercised; Stage 2A is limited to the local Room boundary  
**Account fixtures:** `account-a` and `account-b`

## Accepted gates

### REL-DATA-02 — Account-safe Room identity

- Events, resources, passports, programmes, transactions and impact rows use `(accountId, id)` primary keys.
- The passport resource index is unique on `(accountId, resourceId)`.
- `CoreDaoAccountIsolationTest` stores the same record IDs for two accounts across all six projections and verifies isolated reads and upserts.
- Equivalent passport resource IDs coexist for two accounts.

### REL-DATA-03 — Scoped local mutations

- Normal archive and sync-state mutations require both `accountId` and record ID.
- Outbox uniqueness is `(accountId, tableName, recordId)`; failure and deletion mutations require the row's account.
- Repository writes capture one active account ID and use it for both the cached projection and outbox row.
- Whole-database and other-account deletion methods remain restricted to explicit authentication lifecycle cleanup, not ordinary repository writes.
- `CoreDaoAccountIsolationTest` verifies that archive, outbox failure and outbox deletion cannot mutate another account's row.

### REL-DATA-05 — Versioned schemas and migration proof

- Room database version: 3.
- Exported schema 2 SHA-256: `151B86CB30A77B9A231C31FEC118586605AA39D956FF9F714BEF0C4DB9A26F4B`.
- Exported schema 3 SHA-256: `CF7BC9F1DD0AC0238094A46C7C5B0C70F42EBDC4D9F29059B5D8399C5E74ABAE`.
- Migration 2→3 preserves attributable fields, timestamps, archive state and sync state across all six account-scoped tables while removing blank-account rows.
- Migration 1→2→3 proves that legacy rows which receive an empty account during migration 1→2 are discarded rather than assigned to an invented owner.

## Commands and results

| Command | Result |
|---|---|
| `.\gradlew.bat :app:connectedDebugAndroidTest` | PASS — 6 instrumentation tests, 0 failures, 0 errors, API 35 AVD |
| `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug` | PASS — 21 JVM tests, 0 failures, 0 errors; debug APK assembled |
| `git diff --check` | PASS — no whitespace errors |

The first instrumentation attempt used the installed API 37 preview phone AVD and its test process was killed before test discovery. The same APK and test APK were then run on the stable API 35 AVD, where all six tests completed successfully.

## Source evidence

- `ReEvent/app/schemas/com.reevent.app.core.database.ReEventDatabase/2.json`
- `ReEvent/app/schemas/com.reevent.app.core.database.ReEventDatabase/3.json`
- `ReEvent/app/src/androidTest/java/com/reevent/app/core/database/CoreDaoAccountIsolationTest.kt`
- `ReEvent/app/src/androidTest/java/com/reevent/app/core/database/ReEventDatabaseMigrationTest.kt`
- `ReEvent/app/src/androidTest/java/com/reevent/app/core/database/CoreDaoEventTransactionTest.kt`

## Deferred gates

This evidence does not accept Room aggregate atomicity, environment-scoped cache identity, account-bound WorkManager execution, sign-out cancellation, Supabase authority, RLS, or remote deletion reconciliation.
