# ReEvent Assignment Progress

**Use this file for day-to-day progress.** It tracks what can be shown in the Android assignment: modules, pages, and user-visible functions. It deliberately does **not** count release pipelines, AppGallery work, legal operations, or every production test case.

**Current assignment view:** 8 modules are **Done**, 5 are **In progress**, and 3 **Need work**. The server-authoritative transaction stage is **done** for assignment scope and recorded below.

## Status key

- **Done** — working and ready to show in a demo.
- **In progress** — screen/function exists, but needs a visible gap closed.
- **Needs work** — present as a mock, placeholder, or missing flow.

## Modules

| Module | Status | What is currently there | Next product task |
|---|---|---|---|
| Onboarding and navigation | **Done** | Welcome flow, typed role navigation, account-specific back stacks | Keep polish work small unless a flow breaks |
| Sign-in and role setup | **In progress** | Email sign-in/sign-up, session restore, and one-time role selection | Finish confirmation and password-recovery journey |
| Organiser home | **Done** | Home dashboard, quick actions, event/resource entry points | Refine real-data empty states |
| Event management | **Done** | Event list, create, edit, detail, archive entry point | Improve date/location validation UX |
| Resource inventory | **Done** | Add, edit, list and resource detail/passport entry points | Finish photo upload/replacement UX |
| Marketplace | **Done** | Browse listings and request/decision UI paths | Make all filters and detail data fully server-backed |
| Transaction lifecycle | **Done** | Server-authoritative request, approve, handover, receipt, return, settlement, impact and retry-safe completion | Demonstrate the same flow through two signed Android sessions |
| Participant return | **Needs work** | Return screen and scanner entry point exist | Replace the fake QR panel with the assigned real passport QR and return action |
| Digital passports | **In progress** | Persistent passport model, QR generation, passport screen and history foundations | Finish public-safe lookup and real assignment context |
| QR scanner | **In progress** | Camera/scanner screen and scan routing exist | Complete physical scan-to-authorised-action journey |
| Partner workbench | **In progress** | Partner workspace and programme foundations exist | Finish real recovery handover and passport navigation |
| Partner map | **Needs work** | Map page design exists | Replace static mock with real markers or an honest list fallback |
| Matching | **In progress** | Deterministic matching screen and rules exist | Improve quantity, capacity, distance explanation and alternatives |
| Impact dashboard | **Done** | Completed lifecycle creates real impact/reward data; organiser impact screen exists | Improve presentation and explain factor limitations |
| Offline and account switching | **Done** | Room cache, durable command queue, account/environment isolation and sign-out cleanup | Surface pending/failed sync state in the UI |
| Profile, help and deletion | **Needs work** | Profile page and support entry points exist | Finish password reset, account deletion, and clear support details |

## Pages to show in the assignment

| Page | Main function | Status |
|---|---|---|
| Welcome / sign-in / role choice | Enter the correct role experience | **In progress** |
| Organiser Home | View event/resource actions and impact shortcut | **Done** |
| Events List / Editor / Detail | Create and manage event information | **Done** |
| Add / Edit Resource | Capture resource details and inventory | **Done** |
| Resource Passport | Show QR/passport identity and history | **In progress** |
| Marketplace | Discover a listing and request an allowed action | **Done** |
| Participant Return | Scan and return an assigned resource | **Needs work** — real QR still required |
| Partner Workbench | Manage partner programmes and recovery work | **In progress** |
| Partner Map | Find a recovery option | **Needs work** — mock map must be replaced |
| Matching | Explain suggested circular options | **In progress** |
| Impact | Show reuse, ReCoins and impact after completion | **Done** |
| QR Scanner | Scan a real passport and open the right context | **In progress** |
| Profile | Show active account and supported settings | **Needs work** |

## Recently completed

- [x] **Server-authoritative transaction lifecycle (Stage 3)** — staging proof completed for organiser/participant RENT, partner wrong-actor denial, exact-once settlement/impact, and lost-response retry. The retry response bug was fixed in `ReEvent/supabase/migrations/0008_idempotent_replay_response.sql`.
- [x] **Staging demo setup** — three disposable staging accounts and a staging-bound debug APK are ready for the Android demo.

## Focus next — product work only

1. Make Participant Return use a real assigned QR/passport instead of the fake QR panel.
2. Finish the partner map with real data or a clean list fallback, then connect its action to a real programme request.
3. Finish photo upload/display so a resource can be demonstrated with an actual image.
4. Complete password recovery and account deletion so the profile area is not a dead end.
5. Run the organiser-to-participant transaction once through two signed app sessions and capture a short demo video or screenshots.

## Old tracker

`docs/REEVENT_RELEASE_TRUTH_CHECKLIST.md` is an archived production-release audit with 168 granular acceptance items. It no longer controls assignment progress. Keep it only if you want a future production checklist; otherwise it can be deleted after you have reviewed this file.
