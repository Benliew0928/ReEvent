# ReEvent automated testing

Automated validation is maintained separately from device/manual acceptance. The commands below do not deploy, mutate staging, publish, or require an emulator.

## Android

Run from `ReEvent/` on Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug :app:assembleRelease
```

The local unit-test source set contains business-logic tests plus Robolectric/Compose semantic tests. Instrumented Room/account-isolation tests remain under `app/src/androidTest`; this cleanup compiles them but does not claim new device execution.

## Supabase contracts

Run from `ReEvent/supabase/tests/`:

```powershell
npm ci
npm test
```

The contract harness creates a fresh PGlite database and applies the preserved migration history. It verifies the server-authoritative lifecycle, RPC/RLS boundaries, account deletion, photos, and public Passport projection.

## Website

`ReEventWebsite/` is an independent nested Git repository. Run from that directory:

```powershell
npm ci
npm run lint
npm run typecheck
npm run build
npm test
```

`npm test` performs a production build before running the route suite. The suite covers the current home page, legal/support pages, account-deletion guidance, and an invalid public Passport token. TypeScript checking is non-incremental and does not create a tracked cache file.

## Repository checks

Run from the workspace root:

```powershell
node scripts/check-encoding.mjs
git diff --check
git -C ReEventWebsite diff --check
```

The encoding scanner is zero-dependency. It covers active source, SQL, configuration, and Markdown while excluding archives, dependencies, generated output, caches, and binary assets.

Manual/device acceptance remains governed by `docs/REEVENT_ASSIGNMENT_PROGRESS.md`; passing these commands does not check any manual box.
