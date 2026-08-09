# Wong Loong Jie Solo Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (- [ ]) syntax for tracking.

**Goal:** Complete the independently controllable matching, impact, prototype-AI, QA, and release-preparation work while recording every external release blocker honestly.

**Architecture:** Add pure, feature-local Kotlin domain logic for circular recommendations and impact summaries, then adapt existing repository-backed Compose screens to render the resulting state. Reuse the shared TransactionWorkflow and existing partner-handover flow for all transaction validation and status changes. Preserve existing Room/Supabase models for the first MVP; use one small backwards-compatible event-transaction read only if needed for a complete dashboard channel breakdown. Keep AI deterministic and on-device, and treat environment, compliance, and AppGallery publication as evidence-gated work rather than simulated completion.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt ViewModel, Room, Supabase-backed repositories, JUnit 4, Android instrumentation tests, Huawei AppGallery Connect.

## Global Constraints

- Work from repository root ReEvent/ReEvent at commit 0a2de14 or a later reviewed main commit.
- Do not bypass repositories with direct DAO, Supabase, or Storage calls from a screen.
- Do not change existing Room entities, migrations, Supabase tables, or route types for the initial matching MVP.
- Reuse TransactionWorkflow.validatePartnerHandover, transactionTypeForProgramme, and its existing request lifecycle instead of creating a conflicting recovery-transaction mapper.
- If the dashboard needs completed event transactions, add only TransactionRepository.observeEventTransactions(eventId), the matching DAO query, and LocalFirstCoreRepository implementation; this addition must not change existing callers.
- Do not publish measured environmental claims without a documented factor source, version/date, and item mass or documented estimation rule.
- Use transaction UUID as impact-record UUID. A retry must update the same record, never create a duplicate.
- No remote AI service, secret, photo upload, fake privacy-policy URL, fake release account, or fake AppGallery approval.
- A checkmark means the specified test/build/manual evidence exists. Until Gradle 9.6.1 and Android Gradle Plugin 9.2.1 can be resolved, retain static-review wording only.
- Before the first Gradle verification on this workstation, use the Android Studio JBR (Java 21) rather than the default Java 8 shim and set the wrapper download timeout to 900000 ms. The checked-in 10000 ms timeout cannot reliably fetch the 140 MB Gradle 9.6.1 distribution on the observed connection.

---

## File Structure

| File | Responsibility |
|---|---|
| app/src/main/java/com/reevent/app/feature/matching/CircularAction.kt | Action enum and stable recommendation result types. |
| app/src/main/java/com/reevent/app/feature/matching/CircularRecommendationEngine.kt | Pure, deterministic condition/status/material/programme matching. |
| app/src/main/java/com/reevent/app/feature/matching/PrototypeAssessment.kt | Transparent offline prototype insight; no image upload or remote inference. |
| app/src/main/java/com/reevent/app/feature/impact/ImpactCalculator.kt | Pure recovery rate, channel totals, and safe estimate calculations. |
| app/src/main/java/com/reevent/app/feature/impact/ImpactDashboardState.kt | UI-ready summary, badges, and unavailable-estimate states. |
| app/src/main/java/com/reevent/app/feature/impact/ImpactRecordFactory.kt | Idempotent completed-transaction to ImpactRecord conversion. |
| app/src/main/java/com/reevent/app/ui/screens/FeatureViewModel.kt | Repository-only request creation, event-transaction observation, and idempotent completed-action impact write. |
| app/src/main/java/com/reevent/app/ui/screens/LiveFeatureScreens.kt | Live matching result, alternatives, explanation, and existing partner-handover action. |
| app/src/main/java/com/reevent/app/ui/screens/RestoredVisualLiveScreens.kt | Connect the existing impact visual to the new summary rather than fixed/empty values. |
| app/src/main/java/com/reevent/app/ui/screens/ImpactScreen.kt | Render channel chart values, factor-unavailable copy, and earned badges from state. |
| app/src/main/java/com/reevent/app/core/data/RepositoryInterfaces.kt | Optional read-only event transaction method. |
| app/src/main/java/com/reevent/app/core/database/CoreDao.kt | Optional account-scoped event transaction query. |
| app/src/main/java/com/reevent/app/core/data/LocalFirstCoreRepository.kt | Optional repository implementation of the event transaction flow. |
| app/src/main/java/com/reevent/app/core/data/TransactionWorkflow.kt | Existing shared validation, programme-to-transaction mapping, and status transitions; consume without duplicating. |
| app/src/test/java/com/reevent/app/feature/matching/CircularRecommendationEngineTest.kt | Matching rules and stable ordering tests. |
| app/src/test/java/com/reevent/app/feature/impact/ImpactCalculatorTest.kt | Calculation, invalid-input, duplicate, rounding, and badge tests. |
| app/src/androidTest/java/com/reevent/app/core/database/CoreDaoEventTransactionTest.kt | Account/event isolation test for the optional event-scoped query. |
| docs/impact/IMPACT_ESTIMATE_FACTORS.md | Source, version/date, scope, and values for every permitted demo estimate factor. |
| docs/qa/WONG_LOONG_JIE_E2E_TEST_MATRIX.md | End-to-end test cases, expected results, devices, accounts, evidence, defects, and retest fields. |
| docs/release/appgallery/README.md | Evidence-gated AppGallery release checklist and outcome log. |
| docs/release/appgallery/store-listing.md | Store metadata, screenshot, reviewer-instruction, and regional-release template. |
| docs/release/appgallery/data-permission-inventory.md | Actual data, permission, SDK, retention, and disclosure inventory. |
| docs/release/appgallery/privacy-policy-draft.md | Draft policy content to publish only after a public HTTPS owner is supplied. |
| gradle/wrapper/gradle-wrapper.properties | Reproducible Gradle bootstrap timeout for the required distribution. |

## Interfaces

The matching feature produces a result that screens can render without re-running business rules:

~~~
enum class CircularAction {
    REUSE, SHARE, RENT_OR_LEND, SELL_OR_DONATE,
    REPAIR, REFURBISH, TAKE_BACK, RECYCLE, DISPOSAL
}

data class RecommendationCandidate(
    val action: CircularAction,
    val score: Int,
    val compatibleProgrammeIds: List<String>,
    val explanation: String
)

data class RecommendationResult(
    val primary: RecommendationCandidate?,
    val alternatives: List<RecommendationCandidate>,
    val ineligibilityReason: String?
)

object CircularRecommendationEngine {
    fun recommend(
        resource: ResourceItem,
        programmes: List<CircularProgramme>
    ): RecommendationResult
}
~~~

The impact feature is pure and receives the data needed to show transparent
metrics:

~~~
data class ImpactDashboardState(
    val recoveryRate: Float?,
    val reusedCount: Int,
    val repairedCount: Int,
    val donatedCount: Int,
    val recycledCount: Int,
    val materialDivertedKg: Double?,
    val emissionsAvoidedKg: Double?,
    val valueRecoveredCents: Long?,
    val chartValues: List<Float>,
    val badge: ImpactBadge?,
    val unavailableEstimateReason: String?
)

enum class ImpactBadge { FIRST_RECOVERY, CIRCULAR_STARTER, HIGH_RECOVERY }

data class ImpactEstimateFactor(
    val kgPerUnit: Double,
    val co2eKgPerMaterialKg: Double,
    val source: String,
    val versionOrAccessDate: String
)

data class ImpactEstimateKey(
    val material: String,
    val transactionType: TransactionType
)

data class ImpactEstimatePolicy(
    val factors: Map<ImpactEstimateKey, ImpactEstimateFactor>
)

object ImpactCalculator {
    fun summarize(
        resources: List<ResourceItem>,
        transactions: List<CircularTransaction>,
        records: List<ImpactRecord>
    ): ImpactDashboardState
}

object ImpactRecordFactory {
    fun create(
        transaction: CircularTransaction,
        resource: ResourceItem,
        policy: ImpactEstimatePolicy,
        now: Long
    ): ImpactRecord?
}
~~~

### Test fixture contract

Each matching test uses a fixed timestamp and creates its resources/programmes
with the production data classes rather than mocks:

~~~kotlin
private const val NOW = 1L

private fun resource(
    condition: ResourceCondition = ResourceCondition.GOOD,
    status: ResourceStatus = ResourceStatus.AVAILABLE,
    material: String = "Acrylic"
) = ResourceItem("resource-id", "event-id", "owner-id", "Test resource", "Signage",
    material, condition, 1, "item", status, 1_000, emptyList(), NOW, NOW)

private fun programme(id: String, type: ProgrammeType, materials: List<String>) =
    CircularProgramme(id, "partner-id", id, type, materials, "Test location", true, NOW, NOW)

private fun reuse(id: String, materials: List<String>) = programme(id, ProgrammeType.REUSE, materials)
private fun repair(id: String, materials: List<String>) = programme(id, ProgrammeType.REPAIR, materials)
private fun recycle(id: String, materials: List<String>) = programme(id, ProgrammeType.RECYCLE, materials)
~~~

Each impact test creates a distinct transaction UUID and derives the expected
record from a fixed factor policy:

~~~kotlin
private fun transaction(id: String, type: TransactionType, status: TransactionStatus) =
    CircularTransaction(id, "event-id", "resource-id", "owner-id", "partner-id",
        "partner-id", type, status, 1, NOW, NOW)

private fun available() = resource()
private fun recovered() = resource(status = ResourceStatus.RECOVERED)
private fun completed(type: TransactionType) =
    transaction("33333333-3333-3333-3333-333333333333", type, TransactionStatus.COMPLETED)
private fun pending(type: TransactionType) =
    transaction("44444444-4444-4444-4444-444444444444", type, TransactionStatus.PENDING)
private fun record(materialKg: Double, emissionsKg: Double, cents: Long) =
    ImpactRecord("55555555-5555-5555-5555-555555555555", "event-id", "resource-id",
        "33333333-3333-3333-3333-333333333333", materialKg, emissionsKg, cents, NOW, NOW)

private val policy = ImpactEstimatePolicy(
    mapOf(
        ImpactEstimateKey("acrylic", TransactionType.REPAIR) to
            ImpactEstimateFactor(1.0, 1.0, "documented demo source", "2026-08-02")
    )
)
~~~

### Task 0: Establish a reproducible Gradle verification baseline

**Files:**

- Modify: gradle/wrapper/gradle-wrapper.properties
- Modify: docs/qa/WONG_LOONG_JIE_E2E_TEST_MATRIX.md only if a build verification is still blocked after the retry.

**Consumes:** a valid Java 17+ JDK (prefer Android Studio JBR when its installation is complete), the checked-in Gradle wrapper, and the reachable Gradle distribution URL.

**Produces:** A repeatable local command that can resolve Gradle 9.6.1 and run the existing unit-test task without relying on the machine-wide Java 8 shim.

- [x] **Step 1: Use a valid JDK for Gradle commands**

Set `JAVA_HOME` only for the current command shell; do not alter the machine-wide Java configuration. The installed Android Studio JBR was missing `lib/jvm.cfg` on 2026-08-09, so the verified command used the installed Java 17 JDK instead.

- [x] **Step 2: Increase only the wrapper download timeout**

Change `networkTimeout` from 10000 to 900000 milliseconds. Do not alter the
distribution URL, Gradle version, retry count, or application dependencies.

- [x] **Step 3: Verify the baseline before feature tests**

Run: gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain

Expected: Gradle can resolve its distribution and reach the existing test task.
If the build itself then fails, record the exact failure as a pre-existing
baseline issue before changing application logic.

### Task 1: Build and test the deterministic matching engine

**Files:**

- Create: app/src/main/java/com/reevent/app/feature/matching/CircularAction.kt
- Create: app/src/main/java/com/reevent/app/feature/matching/CircularRecommendationEngine.kt
- Create: app/src/test/java/com/reevent/app/feature/matching/CircularRecommendationEngineTest.kt
- Modify: app/src/main/java/com/reevent/app/core/data/ProgrammeMatcher.kt only to delegate to the new engine or delete it after every current caller has migrated.

**Consumes:** Existing ResourceItem, ResourceCondition, ResourceStatus, CircularProgramme, and ProgrammeType values.

**Produces:** RecommendationResult with a primary action, alternatives, stable score, compatible partner-programme IDs, explanation, and ineligibility reason.

- [x] **Step 1: Write the failing matching tests**

~~~kotlin
@Test fun good_available_acrylic_prefers_reuse_and_exact_partner() {
    val result = CircularRecommendationEngine.recommend(
        resource(condition = ResourceCondition.GOOD, status = ResourceStatus.AVAILABLE, material = "Acrylic"),
        programmes = listOf(reuse("generic", emptyList()), reuse("exact", listOf("Acrylic")))
    )
    assertEquals(CircularAction.REUSE, result.primary?.action)
    assertEquals(listOf("exact", "generic"), result.primary?.compatibleProgrammeIds)
}

@Test fun needs_repair_prefers_repair_then_recycle() {
    val result = CircularRecommendationEngine.recommend(
        resource(condition = ResourceCondition.NEEDS_REPAIR, status = ResourceStatus.AVAILABLE, material = "Fabric"),
        programmes = listOf(repair("repair", listOf("Fabric")), recycle("recycle", listOf("Fabric")))
    )
    assertEquals(CircularAction.REPAIR, result.primary?.action)
    assertEquals(CircularAction.RECYCLE, result.alternatives.first().action)
}

@Test fun unavailable_resource_returns_reason_without_a_match() {
    val result = CircularRecommendationEngine.recommend(
        resource(condition = ResourceCondition.GOOD, status = ResourceStatus.RESERVED, material = "Acrylic"),
        programmes = listOf(reuse("exact", listOf("Acrylic")))
    )
    assertNull(result.primary)
    assertEquals("This resource is not available for a new recovery route.", result.ineligibilityReason)
}

@Test fun unknown_material_uses_only_generic_programmes() {
    val result = CircularRecommendationEngine.recommend(
        resource(condition = ResourceCondition.GOOD, status = ResourceStatus.AVAILABLE, material = ""),
        programmes = listOf(reuse("generic", emptyList()), reuse("acrylic", listOf("Acrylic")))
    )
    assertEquals(listOf("generic"), result.primary?.compatibleProgrammeIds)
}

@Test fun no_compatible_programme_returns_a_clear_reason() {
    val result = CircularRecommendationEngine.recommend(
        resource(condition = ResourceCondition.RECYCLE_ONLY, status = ResourceStatus.AVAILABLE, material = "Acrylic"),
        programmes = listOf(repair("repair", listOf("Acrylic")))
    )
    assertNull(result.primary)
    assertEquals("No active partner programme supports this recovery route.", result.ineligibilityReason)
}
~~~

- [x] **Step 2: Run the new unit-test class and confirm it fails because the feature types do not yet exist**

Run: gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain

Expected: the matching test source fails to compile before implementation. If Gradle cannot resolve its required distribution/plugins, record that environment blocker in the test matrix and do not mark this step complete.

- [x] **Step 3: Implement action eligibility, programme-type mapping, and deterministic scores**

~~~kotlin
private val eligibleStatuses = setOf(ResourceStatus.AVAILABLE)

private fun actionOrder(condition: ResourceCondition): List<CircularAction> = when (condition) {
    ResourceCondition.NEW, ResourceCondition.GOOD ->
        listOf(CircularAction.REUSE, CircularAction.SHARE, CircularAction.RENT_OR_LEND,
            CircularAction.SELL_OR_DONATE, CircularAction.REPAIR, CircularAction.REFURBISH,
            CircularAction.TAKE_BACK, CircularAction.RECYCLE, CircularAction.DISPOSAL)
    ResourceCondition.FAIR ->
        listOf(CircularAction.REUSE, CircularAction.REPAIR, CircularAction.REFURBISH,
            CircularAction.TAKE_BACK, CircularAction.RECYCLE, CircularAction.DISPOSAL)
    ResourceCondition.NEEDS_REPAIR ->
        listOf(CircularAction.REPAIR, CircularAction.REFURBISH, CircularAction.TAKE_BACK,
            CircularAction.RECYCLE, CircularAction.DISPOSAL)
    ResourceCondition.RECYCLE_ONLY ->
        listOf(CircularAction.RECYCLE, CircularAction.DISPOSAL)
}
~~~

Map REUSE programmes to reuse/share/rent/sell-donate, REPAIR programmes to
repair/refurbish, BUY_BACK programmes to take-back, and RECYCLE/COLLECTION
programmes to recycle. Filter inactive or material-incompatible programmes,
place exact material before generic material, sort equal candidates by
lower-cased programme name then ID, and write the displayed explanation from
actual resource/programme fields.

- [x] **Step 4: Re-run matching tests, run existing ProgrammeMatcherTest, and inspect a good, repair, recycle-only, unknown-material, and no-match case**

Run: gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain

Expected: all matching assertions pass, existing matcher coverage is preserved or replaced with equivalent coverage, and each result is deterministic.

- [ ] **Step 5: Commit the isolated matching-domain change**

~~~text
git add app/src/main/java/com/reevent/app/feature/matching
git add app/src/main/java/com/reevent/app/core/data/ProgrammeMatcher.kt
git add app/src/test/java/com/reevent/app/feature/matching
git commit -m "feat: add deterministic circular recommendation engine"
~~~

### Task 2: Integrate matching results with the existing partner-handover workflow

**Files:**

- Modify: app/src/main/java/com/reevent/app/ui/screens/LiveFeatureScreens.kt
- Modify: app/src/test/java/com/reevent/app/feature/matching/CircularRecommendationEngineTest.kt
- Read only: app/src/main/java/com/reevent/app/core/data/TransactionWorkflow.kt
- Read only: app/src/main/java/com/reevent/app/ui/screens/FeatureViewModel.kt

**Consumes:** RecommendationCandidate, ResourceItem, CircularProgramme,
TransactionWorkflow, and FeatureViewModel.createPartnerHandover.

**Produces:** A visible primary recommendation, alternatives, explanation, and
a persisted PENDING CircularTransaction through the existing shared partner
handover contract.

- [ ] **Step 1: Write the failing recommendation-to-programme eligibility test**

~~~kotlin
@Test fun repair_recommendation_exposes_only_repair_programmes_for_handover() {
    val result = CircularRecommendationEngine.recommend(
        resource(condition = ResourceCondition.NEEDS_REPAIR, material = "Fabric"),
        programmes = listOf(repair("repair", listOf("Fabric")), recycle("recycle", listOf("Fabric")))
    )
    assertEquals(CircularAction.REPAIR, result.primary?.action)
    assertEquals(listOf("repair"), result.primary?.compatibleProgrammeIds)
}
~~~

- [x] **Step 2: Run the new matcher test with the existing transaction-workflow coverage**

Run: gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain

Expected: the new matcher assertion initially fails; after the engine is
implemented, it passes together with TransactionWorkflowTest. The latter is
the shared source of truth for handover validation, programme-type mapping,
approval/cancellation, and completion transitions.

- [x] **Step 3: Connect the live matching screen without duplicating transaction logic**

Update MatchingLiveScreen to display the primary action, score, explanation,
alternatives, and a compatible-programme selector. For a selected eligible
programme, call the existing
viewModel.createPartnerHandover(user, resource, programme). Do not create a
new transaction factory, change TransactionWorkflow, or reimplement its
programme-to-transaction mapping. Disable the handover action for disposal,
no-match, and ineligible resource states.

- [ ] **Step 4: Verify the persisted handover path**

Run: gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain

Manual expected result: an AVAILABLE resource with an exact active partner
shows its route first; selecting that partner invokes the existing PENDING
handover path with the expected partner ID; a non-available resource offers no
handover action.

- [ ] **Step 5: Commit only the matching UI integration**

~~~text
git add app/src/main/java/com/reevent/app/ui/screens/LiveFeatureScreens.kt
git add app/src/test/java/com/reevent/app/feature/matching/CircularRecommendationEngineTest.kt
git commit -m "feat: connect circular matches to partner handovers"
~~~

### Task 3: Add event-scoped transactions and pure impact summaries

**Files:**

- Modify: app/src/main/java/com/reevent/app/core/data/RepositoryInterfaces.kt
- Modify: app/src/main/java/com/reevent/app/core/database/CoreDao.kt
- Modify: app/src/main/java/com/reevent/app/core/data/LocalFirstCoreRepository.kt
- Modify: app/build.gradle.kts
- Create: app/src/main/java/com/reevent/app/feature/impact/ImpactDashboardState.kt
- Create: app/src/main/java/com/reevent/app/feature/impact/ImpactCalculator.kt
- Create: app/src/test/java/com/reevent/app/feature/impact/ImpactCalculatorTest.kt
- Create: app/src/androidTest/java/com/reevent/app/core/database/CoreDaoEventTransactionTest.kt

**Consumes:** Existing account-scoped transaction table, including completed
transactions produced by the shared TransactionWorkflow, resources, and
ImpactRecord values.

**Produces:** An account-safe event transaction flow and an ImpactDashboardState with recovery rate, channel counts, totals, chart data, badge result, and explicit unavailable-estimate state.

- [x] **Step 1: Write the failing calculation tests**

~~~kotlin
@Test fun summary_counts_only_completed_supported_channels() {
    val summary = ImpactCalculator.summarize(
        resources = listOf(available(), recovered()),
        transactions = listOf(completed(TransactionType.REPAIR), pending(TransactionType.DONATION),
            completed(TransactionType.RECYCLE)),
        records = listOf(record(materialKg = 2.5, emissionsKg = 4.0, cents = 500))
    )
    assertEquals(1, summary.repairedCount)
    assertEquals(1, summary.recycledCount)
    assertEquals(0, summary.donatedCount)
    assertEquals(0.5f, summary.recoveryRate)
}

@Test fun empty_inputs_have_no_rate_or_badge() {
    val summary = ImpactCalculator.summarize(emptyList(), emptyList(), emptyList())
    assertNull(summary.recoveryRate)
    assertNull(summary.badge)
}
~~~

- [x] **Step 2: Add the smallest backward-compatible event read**

Add this repository method and implement it using the existing account scope:

~~~kotlin
interface TransactionRepository {
    fun observeTransactions(userId: String): Flow<List<CircularTransaction>>
    fun observeEventTransactions(eventId: String): Flow<List<CircularTransaction>>
    suspend fun saveTransaction(transaction: CircularTransaction): AppResult<CircularTransaction>
    suspend fun archiveTransaction(transactionId: String): AppResult<Unit>
}
~~~

The DAO query must filter accountId and eventId, exclude archived transactions,
and order by updatedAt descending. The Android test must insert two account IDs
and two event IDs, then assert the observed flow returns only the current
account and requested-event record.

Add the test runner and test-only dependencies before creating that source:

~~~kotlin
defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
}

dependencies {
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
}
~~~

The Android test uses AndroidJUnit4, ApplicationProvider, and an in-memory
ReEventDatabase:

~~~kotlin
@RunWith(AndroidJUnit4::class)
class CoreDaoEventTransactionTest {
    private lateinit var database: ReEventDatabase

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), ReEventDatabase::class.java
        ).allowMainThreadQueries().build()
    }
}
~~~

- [x] **Step 3: Implement transparent summary rules**

Count only COMPLETED transactions. Map RESALE and RETURN to reused, REPAIR to
repaired, DONATION to donated, RECYCLE to recycled, and BUY_BACK to take-back
without adding it to a named chart channel. Recovery rate remains recovered or
handed-over resource lots divided by all current resource lots. Sum only
non-negative persisted ImpactRecord values. Round display values at the UI
boundary, not inside stored records. Return a clear unavailableEstimateReason
when records cannot support a requested material/CO2e/value estimate.

- [x] **Step 4: Run unit and instrumentation coverage**

Run: gradlew.bat :app:testDebugUnitTest connectedDebugAndroidTest --no-daemon --console=plain

Expected: pure calculator tests pass; the DAO test proves account/event
isolation on an emulator or device. If no device is available, leave the
instrumentation result pending and record device availability in the QA matrix.

2026-08-09 evidence: `:app:testDebugUnitTest`, `:app:compileDebugAndroidTestKotlin`, and `:app:connectedDebugAndroidTest` passed on the Android Studio `Medium_Phone` AVD (Android 17). The Room test confirmed account/event isolation.

- [ ] **Step 5: Commit the read contract and impact domain layer**

~~~text
git add app/src/main/java/com/reevent/app/core/data/RepositoryInterfaces.kt
git add app/src/main/java/com/reevent/app/core/database/CoreDao.kt
git add app/src/main/java/com/reevent/app/core/data/LocalFirstCoreRepository.kt
git add app/build.gradle.kts
git add app/src/main/java/com/reevent/app/feature/impact
git add app/src/test/java/com/reevent/app/feature/impact
git add app/src/androidTest/java/com/reevent/app/core/database
git commit -m "feat: calculate event impact summaries from completed outcomes"
~~~

### Task 4: Produce idempotent impact records and render the completed dashboard

**Files:**

- Modify: app/src/main/java/com/reevent/app/ui/screens/FeatureViewModel.kt
- Modify: app/src/main/java/com/reevent/app/ui/screens/RestoredVisualLiveScreens.kt
- Modify: app/src/main/java/com/reevent/app/ui/screens/ImpactScreen.kt
- Create: app/src/main/java/com/reevent/app/feature/impact/ImpactRecordFactory.kt
- Modify: app/src/test/java/com/reevent/app/feature/impact/ImpactCalculatorTest.kt
- Create: docs/impact/IMPACT_ESTIMATE_FACTORS.md

**Consumes:** Completed CircularTransaction, ResourceItem, a documented feature-local estimate policy, and ImpactRepository.

**Produces:** One upserted ImpactRecord per completed transaction when all required input factors are available, plus a dashboard that reflects live summary data and rule-based badges.

- [x] **Step 1: Add failing idempotency and invalid-input tests**

~~~kotlin
@Test fun completed_transaction_uses_its_uuid_for_one_repeatable_record() {
    val completed = transaction("11111111-1111-1111-1111-111111111111", TransactionType.REPAIR, TransactionStatus.COMPLETED)
    val record = ImpactRecordFactory.create(completed, resource(), policy, now = 20L)
    assertEquals(completed.id, record?.id)
}

@Test fun missing_mass_or_factor_returns_unavailable_instead_of_a_false_estimate() {
    val completed = transaction("22222222-2222-2222-2222-222222222222", TransactionType.RECYCLE, TransactionStatus.COMPLETED)
    assertNull(ImpactRecordFactory.create(completed, resource(material = "Wood"), policy, now = 20L))
}
~~~

- [x] **Step 2: Implement a documented estimate policy**

Create a small immutable policy object whose factors are keyed by normalised
material and completed action. It must specify kilograms per unit, CO2e per
kilogram, factor source, and factor version/date. Do not add an entry until the
source is recorded in docs/impact/IMPACT_ESTIMATE_FACTORS.md. The calculator
returns no record for missing or negative values and returns resource.valueCents
only as a recovered-value estimate with an explicit label.

- [x] **Step 3: Wire only completed actions to idempotent persistence**

Keep TransactionWorkflow as the source of truth for shared status transitions.
After `completeTransaction` has successfully saved its COMPLETED transaction
and its resulting resource status, call the factory and save the resulting
ImpactRecord through ImpactRepository. Refactor the direct lifecycle path only
as needed so its already-COMPLETED RETURN and TRANSFER transactions can pass
the saved transaction to the same factory. Use the transaction ID as the record
ID; a repeat call therefore upserts the same record. PENDING/CANCELLED actions,
camera scans, status-only edits, and unsupported/missing-factor outcomes must
not write impact. Record a non-fatal unavailable-estimate notice when the
factory returns no record; do not create a synthetic zero-value record.

- [x] **Step 4: Replace the remaining visual placeholders**

In ImpactVisualScreen, observe event transactions and build ImpactDashboardState
from resources, transactions, and records. Feed the state into ImpactScreen.
Replace empty chart values with reuse/repair/donation/recycle proportions,
replace static badge selection with named thresholds derived from summary data,
and render "estimate unavailable" rather than zero when a factor is missing.

- [~] **Step 5: Verify regression and commit**

Run: gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain

Manual expected result: completing a supported lifecycle action or partner
handover creates at most one record after restart; replaying the same completed
transaction does not increase its total; the impact board shows live recovery
rate/channel metrics/badge; no-factor data is labelled unavailable.

2026-08-09 evidence: `:app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain` passed with Java 17. The Android Studio `Medium_Phone` AVD installed and launched the debug app, and onboarding -> sign-in was visually verified. The authenticated transaction/restart path remains pending a safe organiser/partner test account.

~~~text
git add app/src/main/java/com/reevent/app/feature/impact
git add app/src/main/java/com/reevent/app/ui/screens/FeatureViewModel.kt
git add app/src/main/java/com/reevent/app/ui/screens/RestoredVisualLiveScreens.kt
git add app/src/main/java/com/reevent/app/ui/screens/ImpactScreen.kt
git add app/src/test/java/com/reevent/app/feature/impact
git add docs/impact/IMPACT_ESTIMATE_FACTORS.md
git commit -m "feat: persist completed-action impact and render dashboard"
~~~

### Task 5: Add the optional transparent offline prototype insight

**Files:**

- Create: app/src/main/java/com/reevent/app/feature/matching/PrototypeAssessment.kt
- Modify: app/src/main/java/com/reevent/app/ui/screens/LiveFeatureScreens.kt
- Create: app/src/test/java/com/reevent/app/feature/matching/PrototypeAssessmentTest.kt

**Consumes:** User-confirmed resource category, material, and condition.

**Produces:** A clearly labelled prototype insight that enriches, but cannot replace, the rule-based recommendation.

- [x] **Step 1: Write failing prototype tests**

~~~kotlin
@Test fun confirmed_fields_generate_an_explainable_prototype_insight() {
    val result = PrototypeAssessment.assess(category = "Signage", material = "Acrylic", condition = ResourceCondition.GOOD)
    assertEquals("Prototype estimate from confirmed resource details", result.disclosure)
    assertEquals(CircularAction.REUSE, result.suggestedAction)
}

@Test fun missing_material_returns_no_confident_assessment() {
    assertNull(PrototypeAssessment.assess(category = "Signage", material = "", condition = ResourceCondition.GOOD))
}
~~~

- [x] **Step 2: Implement deterministic local assessment**

Use only values already confirmed in the resource form. Display the suggested
category/material/condition, confidence label, method disclosure, and
rule-based match result. The feature must say it is a prototype, must offer
manual correction, and must never transmit the photo or assert that pixels
were classified.

- [ ] **Step 3: Verify that a failed/missing assessment cannot block matching**

Run: gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain

Expected: no input or low confidence displays the fallback copy while
CircularRecommendationEngine still returns a result from the saved resource.

- [ ] **Step 4: Commit the optional enhancement separately**

~~~text
git add app/src/main/java/com/reevent/app/feature/matching
git add app/src/main/java/com/reevent/app/ui/screens/LiveFeatureScreens.kt
git add app/src/test/java/com/reevent/app/feature/matching
git commit -m "feat: add transparent offline prototype insight"
~~~

### Task 6: Create evidence-driven QA and release-preparation deliverables

**Files:**

- Create: docs/qa/WONG_LOONG_JIE_E2E_TEST_MATRIX.md
- Create: docs/release/appgallery/README.md
- Create: docs/release/appgallery/store-listing.md
- Create: docs/release/appgallery/data-permission-inventory.md
- Create: docs/release/appgallery/privacy-policy-draft.md
- Modify: docs/WONG_LOONG_JIE_MATCHING_IMPACT_DEPLOYMENT_TRACK.md

**Consumes:** Working app build, available test accounts/devices, actual permissions/SDKs, and explicit release-account inputs.

**Produces:** A reproducible test matrix, defect/retest record, store-material templates, data/permission inventory, policy draft, and a truthfully blocked-or-complete AppGallery release record.

- [ ] **Step 1: Create the end-to-end test matrix**

Include one row each for: sign-in; organiser event/resource creation; QR
passport; invalid/cancelled/offline scan; good/fair/needs-repair/recycle-only
 matching; no-match; partner-handover request; partner transaction completion; restart
persistence; impact record idempotency; no-factor display; optional prototype
fallback; small-screen overflow; denied camera; offline sync; Huawei/HMS
device; and non-Huawei Android device. Each row must have preconditions,
steps, expected result, actual result, device/OS/app version, evidence link,
defect ID, owner, and retest result.

- [ ] **Step 2: Build a factual data and permissions inventory**

List actual account/profile data, resource metadata, resource photos, passport
and QR scan history, location/map data if introduced, Room/DataStore storage,
Supabase requests, CameraX/ML Kit/ZXing/Supabase SDKs, and CAMERA/INTERNET
permissions. For every item record purpose, collection condition, storage,
retention/deletion process, sharing/processor, user control, and release
evidence. Mark unknown fields as release blockers rather than inventing them.

- [ ] **Step 3: Prepare AppGallery material without submitting it**

Populate the store-listing template with final app name, package name,
description, category, age rating, screenshots, icon, copyright/licences,
support contact, countries, pricing, reviewer credentials, and review steps
only when facts are supplied. In the README, track developer-account
verification, role, signing key custody, release build hash, AppGallery open
test, Huawei-device result, review decision, and remediation owner.

- [ ] **Step 4: Execute only evidence-available QA**

Run standard wrapper unit/build commands once Gradle dependencies are
available. Run connectedDebugAndroidTest on an emulator/device. Test the
complete demo flow: organiser sign-in, add acrylic signboard, open/scan
passport, show reuse then repair/recycle route, create recovery request, record
completed outcome, and show updated dashboard. Record every failure in the
matrix and retest after its fix.

- [ ] **Step 5: Update tracker progress only from evidence and commit the documents**

~~~text
git add docs/qa docs/release/appgallery docs/WONG_LOONG_JIE_MATCHING_IMPACT_DEPLOYMENT_TRACK.md
git commit -m "docs: add matching impact QA and AppGallery evidence record"
~~~

### Task 7: Gate actual AppGallery submission on supplied authority and evidence

**Files:**

- Modify: app/build.gradle.kts only after an owner supplies release signing design and final version code.
- Modify: docs/release/appgallery/README.md with actual package hash, regions, test results, and review outcome.

**Consumes:** Verified HUAWEI Developer/AppGallery Connect account and team role, a legal package owner, release signing material stored outside Git, public privacy-policy URL, final metadata/assets, reviewer account, target regions, and tested Huawei device.

**Produces:** A signed non-debug APK or App Bundle, complete AppGallery listing/privacy declaration, open-test evidence, and either an approval record or a rejection/remediation record.

- [ ] **Step 1: Confirm the release authority inputs before changing build configuration**

Record developer account/team role, package owner, selected regions, free/paid
setting, legal content rights, public HTTPS privacy URL, signing-key owner,
release version code, reviewer account, and Huawei/HMS device. If any is
missing, keep the release status blocked and do not create a local fake.

- [ ] **Step 2: Create and verify the signed release build**

Use supplied secure signing configuration outside Git, increment versionCode,
assemble the release artifact, compute its SHA-256 hash, install it on a
Huawei/HMS device, and record install/login/QR/matching/impact results.

- [ ] **Step 3: Run AppGallery open testing and submit only after QA passes**

Upload the signed artifact, complete truthful metadata and privacy declaration,
provide reviewer instructions/test account, run open testing, record crash/ANR
and feedback results, fix verified defects, and submit for review using the
selected rollout approach.

- [ ] **Step 4: Record the real release decision**

Update the release README and personal track with submission date, version,
hash, regions, policy-review date, device evidence, reviewer notes, decision,
and remediation owner. Do not mark deployment complete merely because a build
file or listing draft exists.

## Expected Deliverables

1. Deterministic, tested circular recommendation engine with an honest no-match
   state, primary route, alternatives, score, explanation, and compatible
   partner-programme IDs.
2. Repository-backed matching screen connected to the persisted, test-covered
   existing partner-handover transaction contract.
3. Pure, tested impact calculator and dashboard with live recovery rate,
   completed-channel counts, transparent estimates, no-factor state,
   idempotent impact records, chart data, and rule-based badge result.
4. Optional offline prototype insight that is clearly disclosed and never
   blocks the rule-based route.
5. Unit/instrumentation/manual test evidence, a defect/retest log, and a
   repeatable end-to-end demo script.
6. AppGallery metadata, data/permission inventory, policy draft, release
   checklist, and release evidence record.
7. A signed and published AppGallery release only after every external account,
   policy, signing, device, and store-review prerequisite is supplied and
   verified; until then, a documented release blocker is the correct
   deliverable.

## Plan Self-Review

- Scope coverage: matching, impact, optional AI, QA, and AppGallery release
  preparation are all represented; marketplace/partner completion is supplied
  by the shared TransactionWorkflow, and this track only consumes its existing
  partner-handover contract.
- Compatibility: the plan keeps existing models and repositories intact except
  for one additive event transaction read required for complete dashboard
  channel counts.
- Evidence: every code checkpoint has a test/build/manual outcome, and every
  release claim requires a specific external artifact or result.
- Honesty: unresolved factor, Supabase, device, account, signing, and policy
  inputs remain explicit blockers rather than implementation assumptions.
