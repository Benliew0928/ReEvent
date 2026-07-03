# ReEvent UI-Only Development Plan

## 1. Purpose

This document explains how to develop the full ReEvent app UI as close as possible to the generated image boards, while keeping the actual app interface native, responsive, and realistic.

This plan focuses only on the **UI layer**. It does not cover backend, authentication logic, database, QR scanning logic, API integration, or real business functions yet.

The goal is to build a polished static/prototype UI in Android using **Jetpack Compose**, with realistic mock data and high-quality image assets.

---

## 2. Core UI Strategy

The generated image boards should be treated as **visual references**, not as app backgrounds.

### Correct Approach

Use:

- Jetpack Compose for layout, text, buttons, cards, chips, forms, navigation, dashboards, and charts.
- Images only for realistic media/content areas such as hero photos, item thumbnails, resource photos, partner logos, and map mockups.
- Mock data for all screen content.
- Clickable navigation between screens, but no real data operations yet.

### Avoid

- Do not use the entire generated phone screen as a static image.
- Do not make buttons, text, forms, cards, or navigation bars out of images.
- Do not use image slices for things Compose can build natively.
- Do not use fake decorative gradients or generic recycling icons everywhere.

### Design Rule

**Images create realism. Compose creates the interface.**

---

## 3. Current UI Reference Images

Generated UI boards are stored in:

```text
C:\MobileApp\reevent_ui_generated
```

Reference files:

```text
reevent-ui-board-01-core-flow.png
reevent-ui-board-02-circular-workflow.png
reevent-ui-board-03-impact-support.png
```

Use these images to guide:

- Screen structure
- Spacing
- Card hierarchy
- Data density
- Component shape
- Image placement
- Navigation style
- Overall visual quality

Do not copy the slightly grey color filter exactly. The actual app should be warmer and richer.

---

## 4. UI-Only Build Scope

### 4.1 Included

Build static UI for:

1. Onboarding / Role Select
2. Sign In
3. Organiser Command Centre
4. Add Event Resource
5. Circular Marketplace
6. Digital Resource Passport
7. AI Circular Match
8. Circular Partner Map
9. Event Impact Dashboard
10. Organisation Profile
11. Circular Partner Programme
12. Participant Return Flow

### 4.2 Excluded For Now

Do not build real:

- Authentication
- Backend connection
- Room database
- QR scanner camera
- AI image recognition
- Maps SDK
- Payment/deposit logic
- Upload logic
- Push notifications
- Real form submission

For now, these should be simulated with:

- Static mock data
- Local screen state
- Fake success states
- Clickable UI navigation
- Placeholder images

---

## 5. Design Direction

The UI should feel like a premium event operations tool, not a generic eco/recycling app.

### 5.1 Desired Feeling

The app should feel:

- Professional
- Warm
- Trustworthy
- Operational
- Modern
- Practical
- High quality
- Built for real events

### 5.2 Avoided Feeling

Avoid a UI that feels:

- AI-generated
- Cold grey
- Generic green recycling
- Childish
- Too empty
- Too decorative
- Too much like a landing page
- Like a flat image pasted into an app

---

## 6. Warmer Visual System

The generated boards are slightly grey and cool. The actual Compose UI should use a warmer base with richer accents.

### 6.1 Color Tokens

Use this palette as the app foundation:

```kotlin
val ReEventBackground = Color(0xFFF8F2E8)
val ReEventSurface = Color(0xFFFFFCF6)
val ReEventSurfaceAlt = Color(0xFFF1E7DA)

val ReEventInk = Color(0xFF14201B)
val ReEventTextSecondary = Color(0xFF56635D)
val ReEventTextMuted = Color(0xFF7A867F)
val ReEventLine = Color(0xFFE3D8CA)

val ReEventGreen = Color(0xFF08785F)
val ReEventGreenRich = Color(0xFF009B72)
val ReEventMint = Color(0xFFDDF4E9)

val ReEventBlue = Color(0xFF2F6FED)
val ReEventBlueSoft = Color(0xFFE8F0FF)

val ReEventAmber = Color(0xFFF6A83A)
val ReEventAmberSoft = Color(0xFFFFF1D2)

val ReEventCoral = Color(0xFFE56B6F)
val ReEventCoralSoft = Color(0xFFFFE6E8)
```

### 6.2 Color Usage

| UI Area | Color |
|---|---|
| App background | `ReEventBackground` |
| Cards | `ReEventSurface` |
| Secondary panels | `ReEventSurfaceAlt` |
| Main text | `ReEventInk` |
| Secondary text | `ReEventTextSecondary` |
| Borders | `ReEventLine` |
| Primary buttons | `ReEventGreen` |
| Success chips | `ReEventMint` + `ReEventGreen` |
| Operational states | `ReEventBlueSoft` + `ReEventBlue` |
| Pending states | `ReEventAmberSoft` + `ReEventAmber` |
| Error/damaged states | `ReEventCoralSoft` + `ReEventCoral` |

### 6.3 Why This Palette Is Better

This palette keeps the sustainability identity but adds:

- Warmer background
- Stronger green
- Better contrast
- Richer status colors
- Less grey filter
- More premium feeling

---

## 7. Typography Plan

Use a clean modern sans-serif.

Recommended:

- Android default Material font if speed matters.
- Inter if the team can add font files.
- Google Font alternative: Manrope, Plus Jakarta Sans, or DM Sans.

### 7.1 Type Scale

```kotlin
Display / Metric: 32sp, Bold
Screen Title: 24sp, SemiBold
Section Title: 18sp, SemiBold
Card Title: 16sp, SemiBold
Body: 14sp, Regular
Small Body: 13sp, Regular
Caption: 12sp, Medium
Navigation Label: 11sp, Medium
```

### 7.2 Typography Rules

- Do not use negative letter spacing.
- Keep headings compact inside dashboards.
- Use metric numbers large and confident.
- Use body copy sparingly.
- Avoid long paragraphs inside cards.
- Keep form labels small but readable.

---

## 8. Spacing, Radius, and Layout Tokens

```kotlin
val Space4 = 4.dp
val Space8 = 8.dp
val Space12 = 12.dp
val Space16 = 16.dp
val Space20 = 20.dp
val Space24 = 24.dp
val Space32 = 32.dp

val ScreenPadding = 20.dp
val CardRadius = 8.dp
val ButtonRadius = 8.dp
val ImageRadius = 8.dp
val ChipRadius = 18.dp
val BottomSheetRadius = 24.dp

val CardStroke = 1.dp
val ButtonHeight = 52.dp
val BottomNavHeight = 76.dp
```

### 8.1 Layout Rules

- Use 20dp screen padding.
- Use 12dp gaps between cards.
- Use 20dp gaps between major sections.
- Keep cards at 8dp radius.
- Buttons should be 52dp high.
- Avoid overly pill-shaped large cards.
- Keep images clipped to 8dp or structured hero shapes.

---

## 9. Image Usage Plan

Images should be used where they create realism and domain identity.

### 9.1 Use Images For

| Screen | Image Usage |
|---|---|
| Onboarding | Large event setup hero image |
| Command Centre | Small resource thumbnails |
| Add Event Resource | Item photo upload preview |
| Marketplace | Listing thumbnails |
| Resource Passport | Large item photo and QR card |
| AI Circular Match | Assessed item photo |
| Partner Map | Map image or native map later |
| Impact Dashboard | Optional badge image or chart illustration |
| Organisation Profile | Organisation avatar/logo |
| Partner Programme | Partner logo and material thumbnails |
| Participant Return | Item photo and QR/passport card |

### 9.2 Do Not Use Images For

- Buttons
- Chips
- Cards
- Text
- Icons
- Form fields
- Progress rings
- Bottom navigation
- Dashboard metrics

### 9.3 Required Image Assets

Create or collect these assets:

```text
onboarding_event_setup.png
resource_badge_holders.png
resource_acrylic_sign.png
resource_display_stand.png
resource_fabric_banners.png
resource_gift_bags.png
resource_reusable_cup.png
partner_greencycle_logo.png
map_partner_mock.png
impact_badge_high_recovery.png
```

### 9.4 Recommended Android Asset Location

Use:

```text
app/src/main/res/drawable-nodpi/
```

Why:

- Keeps generated photos from being density-scaled incorrectly.
- Works well for large raster UI media.
- Easy to reference with `painterResource`.

### 9.5 Onboarding Hero Image Treatment

The onboarding upper half should use a real/generated image.

Recommended Compose pattern:

```kotlin
Image(
    painter = painterResource(R.drawable.onboarding_event_setup),
    contentDescription = "Event booths and reusable resource crates",
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .clip(
            RoundedCornerShape(
                bottomStart = 28.dp,
                bottomEnd = 28.dp
            )
        ),
    contentScale = ContentScale.Crop
)
```

Then build role cards and buttons using Compose below it.

### 9.6 Image Style Guidelines

Images should show:

- University career fairs
- Booths
- Signage
- Reusable crates
- Badge holders
- Acrylic signs
- Display stands
- Fabric banners
- Reusable cups
- Recovery collection points

Avoid images that show only:

- Leaves
- Recycling bins
- Abstract green backgrounds
- Generic corporate people
- Blurry stock photos
- Empty event halls

---

## 10. Compose Project UI Structure

Suggested UI package structure:

```text
com.reevent.app
  ui
    theme
      Color.kt
      Type.kt
      Shape.kt
      Theme.kt
    components
      ReEventButton.kt
      ReEventCard.kt
      ReEventChip.kt
      ReEventTopBar.kt
      ReEventBottomNav.kt
      MetricCard.kt
      ResourceCard.kt
      RoleCard.kt
      FormField.kt
      PhotoUploadBox.kt
      ProgressRing.kt
      PassportTimeline.kt
      PartnerCard.kt
      ImpactChart.kt
    screens
      onboarding
      auth
      organiser
      resource
      marketplace
      passport
      match
      partner
      impact
      profile
      participant
    navigation
      ReEventNavGraph.kt
    previewdata
      MockData.kt
```

---

## 11. Reusable Components To Build First

Do not start by building screens directly. Build the component system first.

### 11.1 Foundation Components

1. `ReEventScaffold`
2. `ReEventTopBar`
3. `ReEventBottomNav`
4. `ReEventButton`
5. `ReEventOutlinedButton`
6. `ReEventCard`
7. `ReEventChip`
8. `ReEventTextField`

### 11.2 Domain Components

1. `RoleCard`
2. `MetricCard`
3. `ResourceCard`
4. `UrgentActionCard`
5. `PhotoUploadBox`
6. `CircularScoreCard`
7. `PassportHeroCard`
8. `PassportTimeline`
9. `PartnerBottomSheet`
10. `ImpactTile`
11. `MaterialAcceptanceChip`
12. `ReturnStatusCard`

### 11.3 Why Components First

The generated images look polished because the same visual language repeats:

- Same buttons
- Same card radii
- Same chip style
- Same metric card layout
- Same bottom navigation
- Same form fields

Building reusable components first prevents the app from looking inconsistent.

---

## 12. UI Screen Implementation Plan

## Phase 1: UI Foundations

Goal:

Create the design system in Compose.

Tasks:

- Create color tokens.
- Create typography scale.
- Create shape/radius tokens.
- Create spacing constants.
- Create reusable buttons.
- Create cards.
- Create chips.
- Create top app bar.
- Create bottom nav.
- Add sample image assets.

Deliverables:

- `Theme.kt`
- `Color.kt`
- `Type.kt`
- `Shape.kt`
- Reusable base components

Exit criteria:

- A preview screen shows all components with the warm ReEvent palette.

---

## Phase 2: Onboarding and Sign In

Goal:

Build the first impression screens.

### Screen 1: Onboarding / Role Select

Must include:

- Event setup hero image at top.
- ReEvent brand mark/text.
- Headline: `Circular events start before teardown`
- Supporting copy.
- Three role cards:
  - Event Organiser
  - Participant
  - Circular Partner
- Primary button: `Choose role`
- Secondary sign-in link.

Image usage:

- Use `onboarding_event_setup.png` for the upper hero.

Native UI:

- Brand text
- Role cards
- Button
- Sign-in link

### Screen 2: Sign In

Must include:

- ReEvent logo/brand
- Email field
- Password field
- Sign in button
- Google sign-in visual button, static only
- Trust strip with three columns:
  - Universities
  - Community Events
  - Circular Partners

Deliverables:

- `OnboardingScreen.kt`
- `SignInScreen.kt`

Exit criteria:

- Both screens visually match the generated board but use warmer colors.

---

## Phase 3: Organiser Core Screens

Goal:

Build the main organiser UI flow.

### Screen 3: Organiser Command Centre

Must include:

- Top app bar
- Event title: `University Career Fair 2026`
- Date
- Recovery progress card
- Circular progress ring
- Metric cards:
  - Registered resources: 273
  - Recovered so far: 187
- Urgent actions:
  - Badge holders
  - Acrylic signs
  - Display stands
- Bottom action row:
  - Scan item
  - Add resource
- Bottom nav

Images:

- Use small resource thumbnails.

Native UI:

- Progress card
- Metric cards
- Buttons
- Bottom nav
- List rows

### Screen 4: Add Event Resource

Must include:

- Top app bar
- Resource name field
- Category dropdown-style field
- Material dropdown-style field
- Quantity field
- Unit field
- Condition segmented control
- Event location field
- Photo upload card
- End-of-event plan selector
- Generate Passport button

Images:

- Use uploaded preview image for acrylic sign.

Native UI:

- All form controls
- Segmented control
- Button

Deliverables:

- `OrganiserCommandCentreScreen.kt`
- `AddEventResourceScreen.kt`

Exit criteria:

- Screens can be navigated using mock buttons.
- No form logic required yet.

---

## Phase 4: Marketplace and Passport Screens

Goal:

Build the circular resource browsing and passport experience.

### Screen 5: Circular Marketplace

Must include:

- Search field
- Filter chips:
  - All
  - Rent
  - Donate
  - Repair
  - Take-back
- Resource listings:
  - Display stands
  - Fabric banners
  - Gift bags
- Listing cards with image, status chip, distance/quantity, and action.
- Bottom nav.

Images:

- Listing thumbnails.

Native UI:

- Search
- Chips
- Cards
- Navigation

### Screen 6: Digital Resource Passport

Must include:

- Large item photo
- QR code image or generated placeholder
- Item ID: `RE-AX-020`
- Availability chip
- Item details grid
- Event history timeline
- Recommended next action card
- Create recovery request button

Images:

- Acrylic sign image
- QR code image placeholder

Native UI:

- Timeline
- Detail grid
- CTA button

Deliverables:

- `CircularMarketplaceScreen.kt`
- `ResourcePassportScreen.kt`

Exit criteria:

- Tapping marketplace card can navigate to passport screen using mock navigation.

---

## Phase 5: AI Match and Partner Map

Goal:

Build the advanced visual screens that make the project look high value.

### Screen 7: AI Circular Match

Must include:

- Assessed item photo
- Detected category
- Material
- Condition
- Circular score
- Progress ring
- Ranked recommendations:
  1. Reuse at next event
  2. Transfer to another organiser
  3. Recycle damaged pieces
- Explanation card
- Create recovery request button

Images:

- Scratched acrylic sign photo.

Native UI:

- Score ring
- Recommendation cards
- Explanation card

### Screen 8: Circular Partner Map

Must include:

- Search field
- Filter chips
- Map image/mock area
- Partner pins if using native drawing
- Bottom sheet for GreenCycle Materials
- Partner details
- Request pickup button
- Bottom nav

Images:

- Use `map_partner_mock.png` for UI-only phase.

Native UI:

- Search
- Chips
- Bottom sheet
- Button
- Partner details

Deliverables:

- `AiCircularMatchScreen.kt`
- `CircularPartnerMapScreen.kt`

Exit criteria:

- AI match screen looks advanced but remains static.
- Map screen looks convincing without real Maps SDK.

---

## Phase 6: Impact, Profile, Partner, and Participant Screens

Goal:

Complete the remaining UI screens needed for a full app prototype.

### Screen 9: Event Impact Dashboard

Must include:

- Event title
- Recovery rate hero card
- Circular progress ring
- Impact tiles:
  - Items reused: 126
  - Money saved: RM 850
  - Waste avoided: 20 kg
  - Repair wins: 1
- Recovery by category chart
- Bottom nav

Native UI:

- Metric cards
- Chart
- Progress ring

### Screen 10: Organisation Profile

Must include:

- Organisation profile card
- Trust score
- Completed events
- Inventory items
- Saved partners
- Impact score
- Menu list:
  - Event inventory
  - Saved partners
  - Contribution log
  - Reports
  - Settings
- Logout button style

Native UI:

- Profile card
- Menu rows
- Metric tiles

### Screen 11: Circular Partner Programme

Must include:

- Partner header for `GreenCycle Materials`
- Verified partner label
- Accepted material chips
- Minimum condition card
- Pickup availability card
- Incoming recovery requests
- Edit programme button

Images:

- Partner logo
- Small resource thumbnails

Native UI:

- Cards
- Chips
- Request list

### Screen 12: Participant Return Flow

Must include:

- Header: `Return your item`
- QR/passport card for reusable cup or badge holder
- Deposit status
- Return point
- Due back time
- Impact saved
- Confirm return button

Images:

- Reusable cup image
- QR code image

Native UI:

- Status cards
- Return info cards
- CTA button

Deliverables:

- `EventImpactDashboardScreen.kt`
- `OrganisationProfileScreen.kt`
- `CircularPartnerProgrammeScreen.kt`
- `ParticipantReturnFlowScreen.kt`

Exit criteria:

- All 12 UI screens are present and navigable.

---

## 13. UI Navigation Plan

Even without real functionality, the prototype should be clickable.

Recommended mock navigation:

```text
Onboarding -> Sign In -> Organiser Command Centre
Organiser Command Centre -> Add Event Resource
Add Event Resource -> Digital Resource Passport
Digital Resource Passport -> AI Circular Match
AI Circular Match -> Circular Partner Map
Circular Partner Map -> Event Impact Dashboard
Event Impact Dashboard -> Organisation Profile
Marketplace -> Digital Resource Passport
Organisation Profile -> Partner Programme
Participant Return Flow can be accessible from a demo menu
```

Use Navigation Compose with static routes.

No real data passing is required yet. IDs can be fake.

---

## 14. Mock Data Plan

Create a `MockData.kt` file.

Include:

```kotlin
object MockData {
    val eventName = "University Career Fair 2026"
    val eventDate = "10-12 Apr 2026"
    val registeredResources = 273
    val recoveredResources = 227
    val recoveryRate = 83
    val moneySaved = "RM 850"
    val wasteAvoided = "20 kg"
    val itemsReused = 126
}
```

Mock resources:

```text
Acrylic signboard
Badge holders
Display stands
Fabric banners
Gift bags
Reusable cup
Metal stands
```

Mock partner:

```text
GreenCycle Materials
Accepts acrylic signs, fabric banners, metal stands
2.4 km away
Pickup available
```

---

## 15. Image Generation Prompt Set

Use these prompts to create the actual app media assets.

### 15.1 Onboarding Hero

```text
Realistic university career fair setup with event booths, reusable plastic crates, foldable display stands, badge holders, signage, warm morning daylight, professional documentary photography, clean composition, no brand logos, no readable copyrighted text, no watermark.
```

### 15.2 Acrylic Signboard

```text
Realistic acrylic event signboard on a table, slight scratches visible, clean background, warm natural light, professional product photo, no logos, no watermark.
```

### 15.3 Display Stands

```text
Realistic reusable metal display stands in a university event hall, clean condition, warm indoor lighting, practical event setup, no people facing camera, no logos.
```

### 15.4 Fabric Banners

```text
Realistic rolled and hanging fabric event banners after a conference, ready for upcycling, warm neutral lighting, professional documentary product photo, no brand logos.
```

### 15.5 Gift Bags

```text
Realistic stack of unused kraft paper gift bags from a university event, clean table surface, warm light, practical reuse context, no logos.
```

### 15.6 Reusable Cup

```text
Realistic stainless steel reusable event cup with a small QR sticker, clean background, warm product photography, no brand logos, no watermark.
```

### 15.7 Partner Logo

```text
Simple professional circular partner logo for GreenCycle Materials, green and cream palette, recycling loop and material recovery concept, clean icon mark, no mockup background.
```

### 15.8 Map Mock

```text
Clean mobile map mock for circular partner locations near a university campus, soft neutral map colors, green and blue pins, no real map provider branding, no copyrighted map labels, app UI friendly.
```

---

## 16. How To Match The Generated Image Boards Closely

### 16.1 Screenshot Matching Workflow

For each screen:

1. Put the reference image beside the emulator screenshot.
2. Compare major layout blocks first.
3. Match top padding and screen margins.
4. Match card sizes.
5. Match image crop areas.
6. Match button heights.
7. Match typography sizes.
8. Match color warmth and contrast.
9. Fix text overflow.
10. Repeat until the screen feels close.

### 16.2 Priority When Matching

Match in this order:

1. Layout structure
2. Spacing
3. Typography hierarchy
4. Card shape and borders
5. Color warmth
6. Image placement
7. Icons
8. Small decorative details

Do not obsess over tiny pixel-perfect differences early.

---

## 17. UI Quality Checklist

Before considering a screen done:

- Text is readable.
- No text overlaps.
- No button text is clipped.
- Cards are not overly rounded.
- Background is warm, not grey.
- Primary green is rich enough.
- Status colors are meaningful.
- Images look like real event resources.
- The UI is not just a pasted image.
- Cards have consistent padding.
- Bottom navigation is aligned.
- Form fields have consistent height.
- All screens use the same design tokens.
- The screen looks good on at least two emulator sizes.

---

## 18. Recommended Development Order

Build in this order:

1. Theme tokens
2. Base components
3. Onboarding screen
4. Sign in screen
5. Bottom navigation
6. Organiser dashboard
7. Add event resource
8. Marketplace
9. Resource passport
10. AI circular match
11. Partner map
12. Impact dashboard
13. Organisation profile
14. Partner programme
15. Participant return flow
16. Final UI polish

Reason:

The organiser dashboard, resource passport, and AI match are the most important screens for presentation, so they should receive the most polish.

---

## 19. What To Show In The Proposal

For Part 1 proposal, include screenshots of:

1. Onboarding / Role Select
2. Organiser Command Centre
3. Add Event Resource
4. Digital Resource Passport
5. AI Circular Match
6. Event Impact Dashboard

These screens communicate the idea better than login or profile screens.

---

## 20. Final UI Goal

The final UI should look like a real mobile product that could be used by a university event organiser.

The strongest visual identity is:

```text
Warm event operations dashboard
+ realistic event resource images
+ native Compose cards and controls
+ circular economy status language
+ clear SDG 12 impact metrics
```

The app should not look like a generic green recycling app. It should look like a professional tool for managing event resources before, during, and after an event.

