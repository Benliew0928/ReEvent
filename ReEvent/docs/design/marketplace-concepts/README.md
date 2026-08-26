# Marketplace refinement concepts

Generated with the built-in ImageGen tool on 2026-08-26. The three supplied
role-home references were used as style references only. These images are design
targets, not production raster UI assets; cards, icons, controls, and motion should
remain code-native in Jetpack Compose.

## Verified marketplace contract

The current Android and Supabase-backed flow supports:

- browsing active, server-published resource listings;
- searching by resource title, category, or material;
- filtering by listing action and category;
- listing actions for Borrow, Rent, Buy, Donate, and Exchange;
- published quantity, condition, material, category, optional terms, ReCoin buy or
  rent prices, and default Borrow/Rent duration;
- organiser-only publishing of owned, active, synced resources;
- quantity-based marketplace requests and owner-authorised lifecycle actions;
- request, approval, in-transit, active, return, completion, rejection, and
  cancellation states;
- authorised resource-passport access; and
- offline cache plus pending/failed lifecycle sync feedback.

The concepts intentionally do not claim favourites, ratings, popularity, carts,
checkout, chat, listing-level impact estimates, or reliable distance sorting.

## Concepts

### 1. The Curated Circular Market

Discovery-first editorial catalogue. It provides the closest continuation of the
home redesign and gives resource photography and material storytelling the most
space. The organiser publish action remains available without displacing browsing.

Best for: clarity, visual continuity, and the lowest implementation risk.

### 2. Material Compass

A tactile material-led explorer backed by the existing material strings and an
accessible chip/list equivalent. The compass can be drawn in Compose Canvas while
the results remain standard cards.

Best for: a memorable circular-economy identity and playful exploration.

Risk: material values are free-form, so implementation should normalize and group
known materials while keeping an “Other” path.

### 3. The Live Exchange Board

Workflow-first marketplace with Browse, My listings, and Activity modes. It surfaces
existing lifecycle counts and publish-ready inventory alongside discovery.

Best for: organisers and repeat users who manage many requests and handovers.

Risk: it is the densest direction and needs careful role adaptation so Participant
and Partner screens do not show empty organiser-only modules.

## Final prompts

### Prompt 1

```text
Use case: ui-mockup
Asset type: high-fidelity native Android marketplace screen, portrait 430 × 1024, no phone frame
Input images: Images 1–3 are style references only. Preserve their warm editorial design language, high-contrast serif headings, clean humanist sans body type, botanical restraint, spacing, rounded geometry, and quiet premium mood. Do not copy their homepage layout.
Primary request: Concept 1, “The Curated Circular Market” — a discovery-led marketplace that feels like an elegant editorial catalogue while remaining realistically implementable in Jetpack Compose.
Scene/backdrop: warm ivory #FBFAF6 canvas with barely visible paper grain.
Composition/framing: full mobile screen. Top header with small circular initials avatar and the exact heading “Circular market”. Below it, a large rounded forest-green editorial feature card titled “Resources worth another life”, with a restrained sage botanical sprig, a small exact label “NEW THIS WEEK”, and a clear outlined button “Explore all”. Then an integrated search field reading “Search resources”. Under it, a horizontally scrollable row of short filter chips: “All”, “Borrow”, “Buy”, “Rent”, “Donate”. Main content is an asymmetric but clean editorial resource catalogue: one wide featured resource card for “Oak café chairs” showing “12 chairs”, “Wood · Good”, action chips “Borrow” and “Buy”, plus a small passport icon/button; then two compact resource cards partially visible for “Canvas banners” and “Display plinths”. Include a tasteful floating or pinned organizer action “List a resource”. Bottom navigation has Home, Resources selected, Partners, Account.
Style/medium: realistic shippable product UI screenshot, not concept art; delicate line icons; high-contrast editorial serif for display headings and readable sans-serif for controls.
Color palette: canvas #FBFAF6, forest #00513F, deep forest #003E33, sage #E6EBD8, mist #EFF3EE, ink #103F34, muted gray #6B707C, gold accent #CDA42B, line #DDE2D6.
Materials/textures: subtle warm paper grain only; minimal elevation; thin borders; soft photographic resource thumbnails with natural material texture, no stock-photo gloss.
Text: Render the specified labels exactly and keep additional text minimal and meaningful.
Constraints: practical Android spacing; 48dp-equivalent touch targets; no ratings, no hearts, no favourites, no cart, no checkout, no popularity claims, no fake impact values, no prices except supported marketplace terms, no map distance, no logo, no watermark, no device chrome. Keep the bottom navigation fully visible.
```

### Prompt 2

```text
Use case: ui-mockup
Asset type: high-fidelity native Android marketplace screen, portrait 430 × 1024, no phone frame
Input images: Images 1–3 are style references only. Preserve their warm editorial palette, serif-and-sans typography pairing, botanical restraint, refined spacing, rounded cards, thin borders, and premium quiet mood. Do not copy their homepage composition.
Primary request: Concept 2, “Material Compass” — an imaginative but code-feasible marketplace where people browse circular resources by material and action using a tactile visual explorer.
Scene/backdrop: warm ivory #FBFAF6 with subtle paper grain.
Composition/framing: full mobile screen. Top line has the exact serif heading “Find by material”, a compact search icon button, and a circular initials avatar “AR”. Under it, place a distinctive large rounded mist/sage interactive compass card. In the center is a deep-forest circle labeled exactly “All resources” and around it are four clean orbit-like selectable material nodes labeled “Wood”, “Fabric”, “Metal”, “Plastic”, connected by delicate circular linework; “Wood” is selected in gold/sage. Add one short line “Choose a material to explore what can circulate next.” Beneath the compass, show action filter chips “Borrow”, “Buy”, “Rent”, “Donate”, “Exchange”. Below, a section heading “Available in wood” with a small exact count “18 resources”. Display two layered, slightly overlapping but fully usable resource cards like a curated deck: front card “Oak café chairs”, “12 chairs · Good”, chips “Borrow” and “Buy”, a clear “View passport” action, plus a photo detail of wood grain/chair silhouette; second card partially visible “Display plinths”, “3 plinths · Good”. Add a small persistent “My activity · 2 active” drawer/strip above bottom navigation. Bottom navigation has Home, Resources selected, Partners, Account.
Style/medium: realistic shippable product UI screenshot, not fantasy concept art; the radial explorer should be feasible with Compose Canvas and accessible alternative chips; clean line icons; editorial serif headings and readable humanist sans controls.
Color palette: canvas #FBFAF6, forest #00513F, deep forest #003E33, sage #E6EBD8, mist #EFF3EE, ink #103F34, muted #6B707C, gold #CDA42B, line #DDE2D6.
Materials/textures: extremely subtle paper grain, soft natural material photography, minimal elevation.
Text: Render the specified labels exactly and keep extra copy minimal.
Constraints: practical Android spacing and 48dp-equivalent targets; no ratings, no hearts, no favourites, no cart, no checkout, no popularity claims, no fake impact metrics, no unsupported map distance, no logo, no watermark, no device chrome. Keep bottom navigation fully visible and do not let the cards overlap controls.
```

### Prompt 3

```text
Use case: ui-mockup
Asset type: high-fidelity native Android marketplace screen, portrait 430 × 1024, no phone frame
Input images: Images 1–3 are style references only. Preserve their warm editorial theme, forest/sage palette, serif display typography, clean sans-serif controls, botanical restraint, thin borders, generous spacing, and refined rounded cards. Do not copy their homepage hierarchy.
Primary request: Concept 3, “The Live Exchange Board” — a marketplace designed as an active circular-workflow board, balancing discovery with requests and handovers. It should feel more operational and interactive than a normal shopping catalogue while staying calm and premium.
Scene/backdrop: warm ivory #FBFAF6 with very subtle paper grain.
Composition/framing: full mobile screen. Header with exact serif heading “Circular exchange”, a small circular avatar “AR”, and a compact role-aware gold-accent button “List resource”. Immediately below is a three-option segmented control with exact labels “Browse”, “My listings”, “Activity”; Browse is selected. Add one slim search field “Search resources” and a filter icon with a small active-count badge “2”. Then a wide deep-forest rounded “Open loops” panel that behaves like a horizontally paged status board: show three compact lifecycle columns with exact labels and counts “Requested 3”, “Approved 2”, “In transit 1”, connected by a thin flowing loop line; include one small text action “View activity”. Below, heading “Ready to circulate”. Show a clean two-column resource grid with strong material photography and concise data. Card one: “Oak café chairs”, “12 chairs · Wood”, action chips “Borrow”, “Buy”, passport icon. Card two: “Canvas banners”, “5 banners · Fabric”, action chip “Donate”, passport icon. Under the grid, include a light sage expandable strip “Publish-ready · 4 resources” with a chevron, showing how organizers can surface synced inventory without dominating discovery. Bottom navigation has Home, Resources selected, Events, Partners, Impact.
Style/medium: realistic shippable native Android product UI, not abstract concept art; modular dashboard rhythm; subtle paged-card affordances; elegant line icons; editorial serif headings and readable humanist sans.
Color palette: canvas #FBFAF6, forest #00513F, deep forest #003E33, sage #E6EBD8, mist #EFF3EE, ink #103F34, muted #6B707C, gold #CDA42B, line #DDE2D6.
Materials/textures: subtle paper grain, natural resource photography, minimal elevation and thin rules.
Text: Render specified labels exactly, keep other copy minimal.
Constraints: realistic Android spacing; accessible 48dp-equivalent controls; no ratings, no hearts, no favourites, no cart, no checkout, no popularity claims, no fake impact numbers, no unsupported distance, no chat, no logo, no watermark, no phone frame. Keep the bottom navigation fully visible.
```
