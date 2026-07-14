# WONG LOONG JIE - Matching, Impact, and AppGallery Deployment Track

**Owns:** circular pathway recommendation, impact calculations/dashboard, optional AI enhancement, integration QA, and HUAWEI AppGallery deployment.

**Current progress:** `[##--------] 20%` for matching and impact UI, `[#---------] 10%` for optional AI, `[----------] 0%` for QA and AppGallery deployment. The app currently presents fixed partner recommendations and fixed impact figures; no rules, calculations, AI call, automated tests, release package, AppGallery listing, or compliance review are present.

## Pages and Code Ownership

Primary pages:

- `AiMatchScreen`
- `ImpactScreen`
- matching-result/recovery-request components created by this track

Primary non-UI ownership:

- circular matching rules and tests
- impact calculation rules and tests
- end-to-end demo test plan
- AppGallery release package, metadata, privacy declaration, policy-compliance record, testing, and release management

Feature package target:

```text
feature/matching/
feature/impact/
```

Do not add an AI service before the rule-based matching flow works and has tests. The main plan explicitly allows a stable mock AI fallback.

## Delivery Order

### Checkpoint 1 - Rule-based circular matching `[ ]`

- [ ] Agree the resource status, material, condition, partner-programme, and recovery-request contracts with LIEW KAIY BIN, WONG JIE YING, and MAH JUIN HONG.
- [ ] Create the circular action enum and a deterministic rule-based matching engine.
- [ ] Rank reuse, share, rent/lend, sell/donate, repair, refurbish, take-back, recycle, then disposal according to the main-plan priority.
- [ ] Return the best route, alternatives, explanation, score, and compatible partner/programme IDs.
- [~] Replace the fixed recommendation and static partner cards in `AiMatchScreen` with engine output.
- [ ] Allow the user to create a recovery request that MAH JUIN HONG can display and process.
- [ ] Unit-test normal, damaged, unknown-material, and no-match cases.

**Completion evidence:** changing a resource's material or condition produces a predictable, tested recommendation and valid alternatives.

### Checkpoint 2 - Measurable impact `[ ]`

- [ ] Agree which completed resource, transaction, return, repair, donation, and recycling events feed impact records.
- [ ] Implement recovery-rate, reused/repaired/donated/recycled counts, waste avoided, money saved, and CO2e calculations.
- [ ] Store/recompute impact records through LIEW KAIY BIN's repository contract.
- [~] Replace fixed metrics, chart values, and progress percentage in `ImpactScreen` with computed values.
- [~] Replace the static badge visual with rules that award badges/challenges from impact data.
- [ ] Unit-test calculations, including zero events, invalid quantities, duplicate events, and rounding.

**Completion evidence:** completing a resource outcome updates the dashboard figures consistently and repeatably.

### Checkpoint 3 - Optional AI enhancement `[ ]`

- [ ] Confirm the image input/output contract with WONG JIE YING.
- [ ] Choose one safe approach: a remote AI/ML endpoint or a deterministic local/demo classifier.
- [ ] Display category/material/condition confidence and a clear fallback when AI is unavailable.
- [ ] Feed accepted AI results into the rule-based engine; never make AI the only recovery route.
- [ ] Test API failure, low confidence, unsupported image, and offline fallback.

**Completion evidence:** the rule-based recommendation still works with no AI connection; AI can only enrich it.

### Checkpoint 4 - QA and release readiness `[ ]`

- [ ] Maintain the end-to-end test script: sign in -> add resource -> passport/QR -> match -> marketplace or partner request -> impact.
- [ ] Collect test cases from every teammate and run unit, integration, manual UI, offline, permission, and screen-size checks.
- [ ] Verify all UI data is repository-backed; remove or isolate remaining `MockData` demo-only paths.
- [ ] Test on at least one Huawei/HMS device and one non-Huawei Android device; record device, OS version, app version, and result.
- [ ] Record bugs, owners, severity, and retest evidence.

**Completion evidence:** the agreed end-to-end path completes without a crash on target devices and has a tested fallback for external failures.

### Checkpoint 5 - HUAWEI AppGallery deployment `[ ]`

- [ ] Read the current [AppGallery release flow](https://developer.huawei.com/consumer/en/appgallery/) and [Policy Center](https://developer.huawei.com/consumer/en/policy-center/) immediately before release; record the access date below because Huawei can update its policies.
- [ ] Ensure the release owner has a verified HUAWEI Developer account and the correct AppGallery Connect team role; create the app using the final package name.
- [ ] Choose the release countries/regions, free/paid option, and distribution plan. Do not enable Chinese mainland distribution until the team has independently confirmed the applicable qualifications and app-filing requirements.
- [ ] Prepare truthful localized store metadata: app name, description, category, age rating, icon, screenshots, copyright/rights information, and support contact. Store assets must match the shipped app and must not use unlicensed third-party content.
- [ ] Publish a stable HTTPS privacy-policy URL in AppGallery Connect and provide an easy-to-find in-app privacy statement before personal data is collected.
- [ ] Create a data-and-permission inventory for the actual release: account/profile data, resource photos, camera/QR use, scan history, location/map data, offline storage, network requests, analytics, and every third-party SDK. Remove anything not used.
- [ ] Make the privacy policy and AppGallery privacy declaration match that inventory: controller/contact, data categories, purpose, sharing/processors, storage/retention, security approach, selected-market rights, consent withdrawal, deletion/request channel, and change contact.
- [ ] Verify the app requests camera, media/photo, location, notifications, and any other sensitive permissions only when needed; explain the feature, allow denial, and keep the related feature safely unavailable if denied.
- [ ] Verify legal rights to the ReEvent name, launcher icon, images, fonts, map data, libraries, and all copied/generated content; retain the licence/attribution evidence in the repository or release record.
- [ ] Create a signed non-debug APK or App Bundle with a new version code. Keep signing keys, keystores, API keys, reviewer credentials, and configuration secrets out of Git.
- [ ] Verify the release build does not require unavailable Google-only services, installs cleanly on a Huawei/HMS device, and has no known crash, ANR, broken navigation, inaccessible login, or dead link.
- [ ] Provide clear review instructions and a working reviewer account if sign-in, roles, or restricted areas are required; include any test steps needed to reach QR, matching, marketplace, and partner features.
- [ ] Use AppGallery Connect open testing before production where possible, fix verified findings, then submit for review. Use phased release and monitor crash/ANR/user feedback after approval.
- [ ] Record the AppGallery version, package hash, regions, policy-review date, test devices, reviewer notes, release decision, and remediation owner in the change log.

**Completion evidence:** AppGallery Connect accepts the release configuration and package, the privacy/permission review matches the shipped app, and the app is approved or any review rejection has a named fix and retest plan.

### AppGallery Compliance Notes

Huawei's current public release flow requires a verified developer to create the app in AppGallery Connect, configure localized app information, choose distribution settings, and provide a privacy-policy URL before submitting for release. [Official AppGallery release guide](https://developer.huawei.com/consumer/en/appgallery/)

Huawei's Policy Center identifies app review, age rating, naming, copyright, and rejection policies as release controls. The store also surfaces app privacy information and permissions to users, so the declared policy, app behaviour, and requested permissions must agree. [Policy Center](https://developer.huawei.com/consumer/en/policy-center/) and [Huawei privacy transparency guidance](https://consumer.huawei.com/en/privacy/protect/)

AppGallery Connect supports APK and App Bundle distribution, along with open testing and phased release. [Distribution services](https://developer.huawei.com/consumer/en/agconnect/distribute/)

This is a release-readiness checklist, not legal advice. Privacy, consumer, copyright, and app-filing obligations depend on the countries/regions selected in AppGallery Connect. Re-check official policy pages and obtain qualified legal advice for the selected markets before release.

## Inputs and Handoffs

| Needs from / gives to | Contract | Status |
|---|---|---|
| Needs from LIEW KAIY BIN | Resource/partner/transaction/impact repositories and stable data model | `[ ]` |
| Needs from WONG JIE YING | Resource condition/material/status, passport and QR lifecycle events | `[ ]` |
| Needs from MAH JUIN HONG | Marketplace transaction and partner handover outcomes | `[ ]` |
| Gives to MAH JUIN HONG | Recommended recovery route, compatible partner, and recovery-request payload | `[ ]` |

## AI Agent Working Rules

Before changing code:

- [ ] Pull/rebase and read Phases 6, 9, 10, 11, and 12 in `../REEVENT_FULL_APP_DEVELOPMENT_PLAN.md`.
- [ ] Treat matching and impact as pure, tested domain logic; do not calculate critical values only inside composables.
- [ ] Use transparent rule explanations and safe fallback states; do not invent AI/API results.
- [ ] Avoid changing shared contracts without LIEW KAIY BIN's coordination.

Before marking an item `[x]`:

- [ ] Run the relevant unit tests and a manual screen check.
- [ ] Verify calculations use actual persisted action data, not fixed dashboard numbers.
- [ ] Verify the full demo path still works after cross-track merges.
- [ ] Update this tracker and the matching main-plan phases with evidence.

Status rule: `[x]` means implemented and verified; `[~]` means UI/mock/partially integrated; `[ ]` means no evidence of implementation.

## Handoff and Progress Update

After every checkpoint, update its checkbox and top progress bar, add a dated test/build note below, update the matching phase in the main plan, then push/pull and resolve conflicts before taking the next item.

## Change Log

- 2026-07-14 - Tracker created from the audited full development plan.
- 2026-07-14 - Replaced delivery-documentation work with HUAWEI AppGallery deployment and compliance checkpoints; official sources last checked on this date.
