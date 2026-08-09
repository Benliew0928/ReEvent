# ReEvent Archive

**Created:** 2026-08-09  
**Purpose:** Keep historical, duplicate, generated, and proven-unreferenced material available without treating it as active release work or compiling it into the Android app.

Nothing in this directory is a current progress authority. The active release source of truth is `docs/REEVENT_RELEASE_TRUTH_CHECKLIST.md`; active technical contracts are under `docs/release/` and `docs/impact/`.

## Archive policy

- Archived files are preserved, not deleted.
- Files under `archive/legacy-android/` retain their original workspace-relative path beneath that directory so they can be restored deliberately.
- Archived Kotlin files are outside every Gradle source set and do not compile or ship.
- Historical plans/member trackers may contain obsolete percentages, links, roles, architecture, security assumptions, or feature promises.
- Generated/design-source images are reference material only. Runtime Android copies remain under the app's `res/` directories.
- Restore a file only after comparing it with the frozen ReEvent 1.0 contracts and proving it is still required.

## Historical plans

Moved from the workspace root to `archive/historical-plans/`:

- `REEVENT_CIRCULAR_EVENT_ECONOMY_PROJECT_PLAN.md`
- `REEVENT_FULL_APP_DEVELOPMENT_PLAN.md`
- `REEVENT_UI_DEVELOPMENT_PLAN.md`

These supplied historical intent during the release audit but no longer define completion.

## Member handoffs and old progress files

Moved to `archive/member-handoffs/`:

- `LIEW KAIY BIN Guide.md`
- `LIEW_KAIY_BIN_CORE_ACCOUNT_TRACK.md`
- `MAH_JUIN_HONG_MARKETPLACE_PARTNERS_TRACK.md`
- `WONG_JIE_YING_RESOURCE_QR_TRACK.md`
- `WONG_LOONG_JIE_MATCHING_IMPACT_DEPLOYMENT_TRACK.md`
- `superpowers/2026-08-02-wong-loong-jie-solo-completion.md`

They are retained only for contribution/history review. Their checkboxes and percentages must not be copied into the active tracker.

## Design and generated source material

Moved to `archive/design-source/`:

- `figma_assets/` — LoopLink-era exports, not referenced by ReEvent runtime;
- `figma_screenshots/` — historical ReEvent visual references;
- `reevent_logo_generated/` — generated logo concept sheets;
- `reevent_ui_generated/` — generated UI concept boards.

The assignment DOCX remains active at the workspace root. Android runtime drawables were not archived because each one is still referenced by compiled source.

## Legacy Android source

The original workspace-relative path is preserved beneath `archive/legacy-android/`.

Uncalled legacy Compose screens:

- `ReEvent/app/src/main/java/com/reevent/app/ui/screens/AddResourceScreen.kt`
- `ReEvent/app/src/main/java/com/reevent/app/ui/screens/AiMatchScreen.kt`
- `ReEvent/app/src/main/java/com/reevent/app/ui/screens/MarketplaceScreen.kt`
- `ReEvent/app/src/main/java/com/reevent/app/ui/screens/OnboardingScreen.kt`
- `ReEvent/app/src/main/java/com/reevent/app/ui/screens/PartnerWorkbenchScreen.kt`
- `ReEvent/app/src/main/java/com/reevent/app/ui/screens/ProfileScreen.kt`
- `ReEvent/app/src/main/java/com/reevent/app/ui/screens/SignInScreen.kt`

Unwired prototype assessment:

- `ReEvent/app/src/main/java/com/reevent/app/feature/matching/PrototypeAssessment.kt`
- `ReEvent/app/src/test/java/com/reevent/app/feature/matching/PrototypeAssessmentTest.kt`

Each archived screen entry function had zero callers outside its declaring file. `PrototypeAssessment` was called only by its own two tests and was not connected to runtime matching. Active routed Home, Impact, Passport, Participant Return, Partner Map, account-flow, event/resource, scanner, marketplace and partner-workbench implementations remain in the Android source set.

## Restore procedure

1. Read `docs/release/architecture.md`, `data-contract.md`, and `decisions.md`.
2. Copy the selected file from `archive/legacy-android/<original-path>` back to `<original-path>`.
3. Resolve it against the current domain/API rather than restoring its historical assumptions unchanged.
4. Run `ReEvent\gradlew.bat testDebugUnitTest`, `lintDebug`, and the relevant connected/UI tests.
5. Record why restoration was required in the release evidence/decision log.

## Cleanup verification

The archive move is accepted only when the active Android project compiles, its local tests pass, the release tracker still contains exactly 168 unique gates with 8 accepted, and active documentation has no stale root links to the moved plans/assets.

Verified on 2026-08-09:

- `:app:testDebugUnitTest` rebuilt the active Kotlin source successfully;
- 6 active unit-test suites ran 21 tests with 0 failures, 0 errors, and 0 skipped;
- no active Kotlin source references an archived screen or `PrototypeAssessment` symbol;
- active plan/design references point to their new archive paths;
- the release tracker remains at 8 of 168 accepted gates, with cleanup intentionally not counted as a release-feature gate.
