# LoopLink UI Prototype Notes

## Figma Prototype

Figma file:

https://www.figma.com/design/4pMwXhyFdlPTYovoOhyLY8

## What Was Created

The file contains an editable mobile prototype for LoopLink with:

1. Style guide and visual direction
2. Onboarding screen
3. Login screen
4. Home dashboard
5. Browse listings
6. Listing detail
7. Create listing
8. AI scan result
9. Nearby map
10. Impact dashboard
11. Profile screen

## Prototype Flow

The screens are linked as a simple clickable flow:

```text
Onboarding -> Login -> Home -> AI Scan -> Create Listing -> Browse -> Listing Detail -> Impact -> Map -> Profile -> Home
```

If Figma does not automatically start from the onboarding screen, manually set **01 Onboarding** as the prototype start frame.

## UI Direction

The design uses a premium civic-tech style:

- Deep teal as the primary action color
- Mint and fresh green for sustainability cues
- Warm amber only for rewards and repair highlights
- Soft off-white surfaces
- Crisp Inter typography
- Subtle shadows instead of heavy borders
- Real visual assets for hero imagery, listing photos, and badges

## Implementation Guidance

When converting this UI to Android:

- Use Jetpack Compose with Material 3.
- Use code for real UI elements: buttons, fields, cards, bottom navigation, chips, search, and forms.
- Use bitmap assets for onboarding, empty states, demo item imagery, and badges.
- Avoid making the entire screen a static image.
- Keep border radius, spacing, and typography consistent with the Figma file.

## Suggested Compose Components

Create reusable components:

- `LoopButton`
- `LoopChip`
- `LoopListingCard`
- `LoopImpactCard`
- `LoopBottomNav`
- `CircularityScoreBadge`
- `LoopTextField`
- `LoopScreenScaffold`

This will keep the actual Android app clean, consistent, and easy to explain during Q&A.

