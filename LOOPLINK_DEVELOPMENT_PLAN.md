# LoopLink Development Plan

## Project Title

**LoopLink: An AI-Assisted Community Circular Economy Platform for Sharing, Repairing, and Repurposing Resources**

## 1. Assignment Fit Summary

LoopLink is designed specifically for the UCCD3223 Mobile Application Development group assignment theme:

> Harnessing Mobile Technology for the Circular Economy and Sustainable Consumption

The app supports **UN Sustainable Development Goal 12: Responsible Consumption and Production** by helping users reduce unnecessary purchases, extend product lifecycles, repair usable items, donate unwanted goods, and match reusable materials with people who can create new value from them.

LoopLink is not just a second-hand marketplace. It is a local circular economy system with four connected functions:

1. **Share** items instead of buying new ones.
2. **Repair** items instead of discarding them.
3. **Rehome** items through donation, resale, or giveaway.
4. **Repurpose** waste materials by matching them with people who need them.

This makes the project strongly aligned with the assignment's Bloom's Taxonomy Level 6 design components:

| Assignment Design Component | How LoopLink Satisfies It |
|---|---|
| Peer-to-Peer Resource Optimisation | Users can lend, borrow, give away, sell cheaply, or request items from others in the community. |
| Behavioural Transformation | Eco-score, badges, circularity score, leaderboard, reminders, and AI recommendations encourage sustainable choices. |
| Closing the Loop | Waste producers can list reusable materials, and makers, students, recyclers, clubs, or small businesses can claim them. |

## 2. High-Scoring Strategy

The goal is to build a project that can score highly in both Part 1 and Part 2.

### Part 1 Proposal Marking Strategy

| Marking Area | Weight | LoopLink Strategy |
|---|---:|---|
| Creativity, novelty, usefulness, relevance | 40% | Present LoopLink as a community circular economy engine, not a normal marketplace. Emphasise AI item scanning, circularity score, repair-first logic, and waste-to-value matching. |
| Completeness of idea | 30% | Include clear problem statement, user personas, use cases, workflow diagrams, feature list, data model, API plan, monetisation or civil-use potential, and risk analysis. |
| Overall design, mainly UI | 20% | Include polished wireframes for Home, Scan, Listing, Map, Impact, Item Detail, Create Listing, and Profile screens. |
| Neat documentation layout | 10% | Use a professional report structure, tables, diagrams, consistent headings, screenshots or mockups, and concise writing. |

### Part 2 Implementation Marking Strategy

| Marking Area | Weight | LoopLink Strategy |
|---|---:|---|
| Program completeness and functionality | 30% | Build a working app with authentication, local database, listings, search, item scanner, impact tracker, map, CRUD operations, and external API integration. |
| Actual design and creativity | 10% | Use a polished Jetpack Compose UI with clean navigation, sustainability-themed but professional colors, item cards, icons, and progress indicators. |
| Potential for civil or commercial use | 5% | Show how campuses, condominiums, councils, recycling centres, and NGOs could use it. |
| Tidiness of source code | 5% | Use MVVM architecture, clean packages, repository pattern, meaningful naming, GitHub commits, and comments only where useful. |
| Documentation and individual contribution | 20% | Clearly label each member's role, GitHub commits, features contributed, screenshots, and technical explanation. |
| Presentation and demo | 15% | Prepare a smooth demo scenario: scan item, create listing, search nearby listings, request item, view impact score, show external API result. |
| Q&A technical understanding | 15% | Every member must understand their module, data flow, API use, local storage, and sustainability logic. |

## 3. Recommended Technical Stack

The recommended implementation is **native Android using Kotlin and Jetpack Compose**. This is appropriate for a Mobile Application Development course and allows strong control over local storage, UI, permissions, camera, and API calls.

### Core Stack

| Layer | Recommended Tool | Purpose |
|---|---|---|
| UI | Kotlin + Jetpack Compose | Modern Android UI development. |
| Architecture | MVVM + Repository Pattern | Keeps code clean, testable, and easy to explain during Q&A. |
| Local Storage | Room Database | Stores, updates, and retrieves listings, users, saved items, transactions, and impact records on the mobile device. |
| Networking | Retrofit + Kotlin Serialization or Moshi | Connects to external REST APIs. |
| Backend | Supabase | Authentication, cloud database, image storage, and optional real-time updates. |
| Maps | OpenStreetMap with Nominatim or a map SDK | Finds nearby repair shops, recycling centres, donation points, and community listings. |
| AI Item Analysis | OpenAI Vision API, Google ML Kit, or TensorFlow Lite | Classifies item type and suggests circular actions. |
| Image Handling | CameraX or Android Photo Picker | Lets users capture or upload item photos. |
| Version Control | GitHub | Shows individual source code contributions. |

### Official Reference Links

- Android Room local database: https://developer.android.com/training/data-storage/room
- Supabase Kotlin documentation: https://supabase.com/docs/reference/kotlin/introduction
- Supabase Android Kotlin quickstart: https://supabase.com/docs/guides/getting-started/quickstarts/kotlin
- Nominatim geocoding usage policy: https://operations.osmfoundation.org/policies/nominatim/
- OpenAI image and vision documentation: https://developers.openai.com/api/docs/guides/images-vision

## 4. Minimum Assignment Requirements Checklist

| Requirement | LoopLink Implementation |
|---|---|
| Custom designed launcher icon | Create a LoopLink icon using a loop symbol, location pin, and leaf/resource motif. Add adaptive icon support for Android. |
| Stores data on mobile device | Use Room Database for users, listings, saved items, borrow requests, impact events, search history, and cached API responses. |
| Updates data on mobile device | Allow users to edit listing status, item condition, request status, profile details, and impact records. |
| Retrieves data from mobile device | Display saved listings, user activity, item history, and offline cached listings. |
| Connects to at least one external endpoint | Use Supabase API, Nominatim API, and optionally OpenAI Vision API. |
| Working mobile app | Build complete user flows, not only static screens. |
| Demonstrates idea and concept | Prepare a demo dataset and a guided presentation flow. |
| Report with screenshots | Capture polished screenshots from all main screens. |
| Source code appendix | Attach or link relevant source code and GitHub repository. |
| GitHub contribution tracking | Each member commits code to their assigned feature branch. |

## 5. Core App Concept

### Problem Statement

Many people own items they rarely use, throw away repairable products, or discard materials that others could reuse. Existing marketplaces usually focus on buying and selling, but they do not actively guide users toward the most sustainable action.

LoopLink solves this by recommending the best circular action for each item:

- Lend it
- Borrow it
- Repair it
- Donate it
- Sell it cheaply
- Recycle it
- Repurpose it
- Match it with someone who needs that material

### Target Users

Primary target users:

- University students
- Campus clubs and societies
- Hostel residents
- Condominium residents
- Local neighbourhood communities

Secondary target users:

- Repair shops
- Recycling centres
- Donation centres
- Cafes or small businesses with reusable waste
- Makers, artists, gardeners, and upcycling hobbyists

### Example Use Cases

1. A student needs a drill for one day and borrows one from a nearby user instead of buying a new one.
2. A user scans an old laptop, and LoopLink recommends repair or donation instead of disposal.
3. A cafe lists used coffee grounds, and a gardening club claims them for compost.
4. A student has cardboard boxes after moving house, and another student claims them for packaging.
5. A broken fan is listed as repairable, and a repair volunteer accepts the request.
6. A user tracks how many items they saved from landfill and how much estimated waste they reduced.

## 6. Unique Selling Points

LoopLink should be presented as outstanding because it combines multiple circular economy behaviours into one intelligent platform.

### 6.1 AI Circular Action Assistant

The user uploads or captures a photo of an item. The app analyses the image and asks a few condition questions.

Example output:

```text
Item: Electric kettle
Detected category: Small appliance
Condition: Not working
Recommended action: Repair or spare-part recovery
Circularity score: 76/100
Suggested next steps:
1. Check nearby repair volunteers
2. List as repairable item
3. If repair fails, send to e-waste centre
```

### 6.2 Circularity Score

Every item receives a score based on:

- Item condition
- Reusability
- Repairability
- Demand in the community
- Material type
- Environmental impact
- Distance to interested users or centres

Example:

```text
Circularity Score = 82/100
Best path: Lend or donate
Estimated impact: 2.5 kg waste avoided, RM45 saved
```

### 6.3 Community Impact Dashboard

The app shows personal and community-level impact:

- Items reused
- Items repaired
- Items borrowed instead of bought
- Waste diverted from landfill
- Estimated money saved
- Estimated carbon impact
- Top contributors

### 6.4 Waste-to-Value Matching

Users can list reusable material, not only finished products.

Examples:

| Waste / Surplus Material | Possible Receiver |
|---|---|
| Coffee grounds | Gardeners, compost groups |
| Cardboard boxes | Students moving house, small sellers |
| Fabric scraps | Fashion students, craft clubs |
| Glass jars | Home food sellers |
| Old electronics | Repair volunteers, parts harvesters |
| Plastic containers | Art or science projects |

### 6.5 Repair-First Workflow

Before users throw something away, LoopLink recommends repair options:

- DIY repair tips
- Nearby repair services
- Volunteer repair events
- Spare part listings
- Repairability rating

## 7. Feature Scope

To build a perfect-mark project, divide features into three levels: must-have, high-score, and extraordinary.

### 7.1 Must-Have Features

These are required for a complete assignment submission.

1. User registration and login
2. User profile
3. Create item listing
4. View item listings
5. Search and filter listings
6. Update listing status
7. Delete or archive listing
8. Save favourite listings
9. Local Room database
10. External API connection
11. Custom launcher icon
12. Screenshots for report

### 7.2 High-Score Features

These features make the project stronger than a basic CRUD app.

1. AI item category suggestion
2. Circularity score
3. Impact tracker
4. Borrow/request workflow
5. Map of nearby listings, repair shops, recycling points, or donation centres
6. Listing image upload
7. User ratings
8. Badges and eco-points
9. Offline cache
10. GitHub contribution evidence

### 7.3 Extraordinary Features

These features can make the project memorable in presentation and Q&A.

1. Smart recommendation engine
2. Waste-to-value matching algorithm
3. QR code item handover confirmation
4. Return reminder for borrowed items
5. Repair event mode for campus clubs
6. Organisation account type for NGOs or recycling centres
7. Leaderboard by community or practical group
8. Public community impact page
9. Admin moderation dashboard
10. App store submission or publishing attempt

## 8. App Modules

### Module 1: Authentication and Profile

Purpose:

- Let users create accounts and identify who owns, borrows, donates, or repairs items.

Features:

- Register
- Login
- Logout
- Edit profile
- Select community, such as UTAR, hostel, condominium, or neighbourhood
- View badges and eco-score

Suggested fields:

- Name
- Email
- Phone number, optional
- Community
- Profile image
- Role: user, repair volunteer, organisation, admin
- Eco-points
- Rating

### Module 2: Item Listing

Purpose:

- Let users create circular economy listings.

Listing types:

- Lend
- Borrow request
- Give away
- Sell cheaply
- Repair needed
- Repair offered
- Material available
- Recycling or donation point

Listing fields:

- Title
- Description
- Category
- Listing type
- Condition
- Quantity
- Location
- Image
- Availability period
- Status
- Owner ID
- Circularity score
- Estimated impact

### Module 3: AI Scan and Recommendation

Purpose:

- Make the app feel intelligent and innovative.

Flow:

1. User captures or uploads item photo.
2. App sends image to AI endpoint or on-device classifier.
3. App receives detected category and item suggestion.
4. User confirms or edits the result.
5. App asks condition questions.
6. App calculates circularity score.
7. App recommends best action.

Example condition questions:

- Is the item working?
- Is it clean and usable?
- Is any part broken?
- How often do you use it?
- Would you lend, donate, sell, repair, or recycle it?

### Module 4: Matching Engine

Purpose:

- Match users based on item category, listing type, location, and need.

Matching logic:

```text
Match Score =
  category match
+ listing type compatibility
+ location distance score
+ condition suitability
+ user trust score
+ urgency score
+ sustainability score
```

Example matches:

- "User A has cardboard boxes" matched with "User B needs moving boxes".
- "User A has broken headphones" matched with "repair volunteer".
- "Cafe has coffee grounds" matched with "gardening club".

### Module 5: Map and External Services

Purpose:

- Satisfy external endpoint requirement and improve usefulness.

Possible external endpoints:

1. Supabase REST or SDK for cloud data.
2. Nominatim API for geocoding and reverse geocoding.
3. OpenAI Vision API for image understanding.
4. OpenStreetMap-based map data.

Map features:

- Show nearby listings.
- Show repair shops.
- Show recycling centres.
- Show donation points.
- Show distance from user.
- Let users search a location.

Important note:

- If using the public Nominatim service, follow its official usage policy and keep requests light. Cache geocoding results locally using Room.

### Module 6: Request and Transaction Flow

Purpose:

- Demonstrate real app behaviour beyond static listings.

Request statuses:

- Pending
- Accepted
- Rejected
- Ready for pickup
- Completed
- Cancelled
- Returned, for borrowed items

Example flow:

1. Borrower taps "Request Item".
2. Owner receives request.
3. Owner accepts.
4. App generates pickup instructions or QR code.
5. Borrower marks item received.
6. Owner marks item returned or completed.
7. Both users rate the transaction.
8. Impact record is created.

### Module 7: Impact and Gamification

Purpose:

- Demonstrate behavioural transformation.

Impact metrics:

- Items shared
- Items repaired
- Items donated
- Materials repurposed
- Estimated waste avoided
- Estimated money saved
- Eco-points earned

Badges:

- First Loop
- Sharing Champion
- Repair Hero
- Waste-to-Worth Maker
- Campus Saver
- Zero Waste Week
- Circular Leader

Leaderboard types:

- Individual
- Practical group
- Campus community
- Organisation

### Module 8: Admin and Moderation

Purpose:

- Improve commercial/civil use potential.

Admin features:

- View reported listings
- Remove inappropriate listings
- Approve organisation accounts
- View community analytics
- Manage categories

This module is optional but impressive.

## 9. Suggested Screen List

Build enough screens to make the app feel complete.

### Essential Screens

1. Splash screen
2. Onboarding screen
3. Login screen
4. Register screen
5. Home dashboard
6. Browse listings
7. Listing detail
8. Create listing
9. Edit listing
10. AI scan screen
11. AI recommendation result
12. Map screen
13. Requests screen
14. Request detail
15. Impact dashboard
16. Profile screen
17. Settings screen

### Optional High-Impact Screens

1. Community leaderboard
2. Repair volunteer directory
3. Waste material matching screen
4. QR handover confirmation
5. Admin moderation screen
6. App store-ready about page

## 10. UI Design Direction

The app should look modern, practical, and trustworthy. Avoid making it look like a simple student prototype.

### Visual Personality

- Clean
- Helpful
- Local community focused
- Environmentally responsible
- Professional enough for real deployment

### Suggested Color System

Use a balanced palette instead of only green.

| Role | Suggested Color |
|---|---|
| Primary | Deep teal |
| Secondary | Fresh green |
| Accent | Warm amber |
| Background | Off-white or very light grey |
| Text | Charcoal |
| Warning | Orange or red |
| Success | Green |

### Component Ideas

- Listing cards with image, type tag, distance, condition, and impact estimate.
- Circularity score ring.
- Eco-impact cards.
- Segmented controls for listing types.
- Filter chips for category and distance.
- Bottom navigation with Home, Browse, Scan, Map, Profile.
- Icons for share, repair, recycle, donate, borrow, and impact.

## 11. Data Model

### User

```text
user_id
name
email
phone
profile_image_url
community_id
role
eco_points
rating
created_at
updated_at
```

### Community

```text
community_id
name
type
location_name
latitude
longitude
created_at
```

### Listing

```text
listing_id
owner_id
title
description
category
listing_type
condition
quantity
image_url
local_image_path
latitude
longitude
location_name
availability_start
availability_end
status
circularity_score
estimated_waste_saved
estimated_money_saved
created_at
updated_at
```

### Request

```text
request_id
listing_id
requester_id
owner_id
message
status
pickup_time
return_time
created_at
updated_at
```

### ImpactEvent

```text
impact_id
user_id
listing_id
request_id
action_type
waste_saved_kg
money_saved_rm
points_earned
created_at
```

### Badge

```text
badge_id
name
description
icon_name
points_required
```

### UserBadge

```text
user_badge_id
user_id
badge_id
earned_at
```

### CachedApiResult

```text
cache_id
endpoint_name
query_key
response_json
created_at
expires_at
```

## 12. Local Storage Plan

Use Room Database for:

1. User profile cache
2. Listings cache
3. Saved listings
4. User-created listings
5. Draft listings
6. Request history
7. Impact history
8. Cached location search results
9. Offline queue for actions made without internet

This directly satisfies:

- Store data on the mobile device
- Update data on the mobile device
- Retrieve data from the mobile device

## 13. External Endpoint Plan

Use at least one external endpoint, but ideally use three to make the project impressive.

### Endpoint 1: Supabase

Purpose:

- Authentication
- Cloud database
- Image storage
- Sync listings across users

Why it helps marks:

- Shows real backend integration.
- Makes the app feel publishable.
- Supports multiple users during demo.

### Endpoint 2: Nominatim / OpenStreetMap

Purpose:

- Convert place names to coordinates.
- Convert coordinates to readable addresses.
- Help users find nearby repair, donation, or recycling locations.

Why it helps marks:

- Supports location-based community sharing.
- Strengthens practical usefulness.

### Endpoint 3: AI Vision

Purpose:

- Identify item category from image.
- Recommend circular action.

Why it helps marks:

- Demonstrates technological integration.
- Matches the assignment encouragement to explore AI.

## 14. Architecture Plan

Recommended package structure:

```text
com.looplink.app
  core
    network
    database
    navigation
    ui
    util
  data
    local
      dao
      entity
    remote
      dto
      api
    repository
  domain
    model
    usecase
  feature
    auth
    home
    listing
    scan
    map
    request
    impact
    profile
    admin
```

Recommended architecture:

```text
UI Screen
  -> ViewModel
  -> Use Case
  -> Repository
  -> Local Room DAO and Remote API
```

Why this scores well:

- Easy to explain.
- Clean and maintainable.
- Supports offline-first behaviour.
- Makes individual contribution separation clear.

## 15. Development Phases

### Calendar Plan

This timeline assumes work starts from **18 June 2026**.

| Date Range | Main Focus | Target Output |
|---|---|---|
| 18 June - 22 June 2026 | Team setup, project registration, app concept finalisation | GitHub repo, team roles, registered idea, app concept summary |
| 23 June - 30 June 2026 | Proposal research and UI wireframes | Problem research, personas, user journey, wireframes |
| 1 July - 10 July 2026 | Proposal writing and refinement | Complete Part 1 proposal draft |
| 11 July - 14 July 2026 | Proposal polish and submission preparation | Final checked proposal, cover page, formatting |
| 15 July 2026 before 5:00 PM | Part 1 submission | Proposal submitted on time |
| 16 July - 25 July 2026 | App foundation, Room database, navigation, UI system | Running Android app foundation |
| 26 July - 5 August 2026 | Authentication, profile, listing CRUD | Core app with local storage and backend sync |
| 6 August - 16 August 2026 | AI scan, circularity score, map, external APIs | Innovation features and endpoint integration |
| 17 August - 25 August 2026 | Request flow, impact dashboard, matching engine | Complete user workflows |
| 26 August - 31 August 2026 | Testing, bug fixing, UI polish | Stable demo build and screenshots |
| 1 September - 4 September 2026 | Final report and presentation preparation | Final report, demo script, source code appendix |
| 5 September 2026 before 5:00 PM | Part 2 submission | Working app and report submitted on time |
| Week 13 practical session | Presentation and Q&A | Live demo and individual technical explanation |

### Phase 0: Team Setup and Project Definition

Estimated duration: 2-3 days

Goals:

- Finalise idea.
- Assign team roles.
- Set up GitHub repository.
- Set up project management board.
- Create naming, branching, and commit rules.

Tasks:

1. Create GitHub repository.
2. Add README with project overview.
3. Create branches:
   - `main`
   - `develop`
   - `feature/auth`
   - `feature/listings`
   - `feature/scan`
   - `feature/map`
   - `feature/impact`
4. Decide target platform: Android native Kotlin.
5. Decide backend: Supabase.
6. Decide APIs: Supabase, Nominatim, AI Vision.
7. Create initial app icon concept.
8. Register project idea in the assignment spreadsheet.

Deliverables:

- GitHub repo
- Project README
- Team role table
- Initial app concept summary

### Phase 1: Research and Proposal Preparation

Estimated duration: 5-7 days

Goals:

- Prepare a strong Part 1 proposal.
- Produce wireframes.
- Show clear assignment relevance.

Proposal structure:

1. Cover page
2. Introduction
3. Problem statement
4. Target users
5. SDG 12 alignment
6. Proposed solution
7. Key features
8. Technical architecture
9. UI wireframes
10. Expected impact
11. Feasibility
12. Team roles
13. Timeline
14. Conclusion

Proposal must be within 4-10 pages, excluding optional appendix.

Tasks:

1. Research circular economy problems in campus or local communities.
2. Define 3-4 user personas.
3. Create user journey.
4. Create low-fidelity wireframes.
5. Create high-fidelity mockups if time allows.
6. Create architecture diagram.
7. Create data flow diagram.
8. Create feature priority table.
9. Write proposal.
10. Review grammar and formatting.

Deliverables:

- Part 1 proposal report
- UI wireframes
- Idea registration in spreadsheet

Hard deadline:

- **15 July 2026 before 5:00 PM**

### Phase 2: App Foundation

Estimated duration: 5-7 days

Goals:

- Build the app skeleton and technical foundation.

Tasks:

1. Create Android project.
2. Set package name.
3. Add app launcher icon.
4. Configure Gradle dependencies.
5. Add Jetpack Compose navigation.
6. Create theme, typography, colors, and reusable UI components.
7. Add Room database.
8. Add Retrofit networking.
9. Add Supabase configuration.
10. Add base error handling.
11. Add loading and empty states.

Deliverables:

- Running Android app
- Navigation shell
- Local database foundation
- Network layer foundation
- Custom launcher icon

Acceptance criteria:

- App launches successfully.
- Bottom navigation works.
- Room database can insert and retrieve test data.
- External API test call succeeds.

### Phase 3: Authentication and Profile

Estimated duration: 4-6 days

Goals:

- Users can create accounts and maintain profiles.

Tasks:

1. Build login screen.
2. Build register screen.
3. Connect to Supabase Auth.
4. Store user profile locally.
5. Create profile screen.
6. Add edit profile.
7. Add role selection.
8. Add logout.
9. Handle validation errors.

Deliverables:

- Working login and registration
- Profile screen
- Local user cache

Acceptance criteria:

- User can register.
- User can login.
- User data is cached locally.
- User can edit profile.

### Phase 4: Listing CRUD

Estimated duration: 7-10 days

Goals:

- Build the core circular listing system.

Tasks:

1. Create listing entity.
2. Create listing DAO.
3. Create listing repository.
4. Build browse listings screen.
5. Build listing detail screen.
6. Build create listing screen.
7. Build edit listing screen.
8. Add image upload or local image path.
9. Add listing type selection.
10. Add category filters.
11. Add status update.
12. Add delete or archive listing.
13. Sync listings with Supabase.

Deliverables:

- Create, read, update, delete listings
- Local and remote listing sync
- Browse and detail screens

Acceptance criteria:

- User can create a listing.
- Listing is saved locally.
- Listing appears in browse screen.
- User can update listing.
- User can retrieve listing after app restart.
- Listing can sync with external backend.

### Phase 5: AI Scan and Circularity Score

Estimated duration: 7-12 days

Goals:

- Add the most innovative feature of the app.

Tasks:

1. Add camera or image picker.
2. Build scan screen.
3. Send image to AI endpoint or classifier.
4. Parse category result.
5. Let user confirm category.
6. Ask condition questions.
7. Calculate circularity score.
8. Recommend best circular action.
9. Create listing draft from AI result.
10. Cache scan result locally.

Circularity score formula example:

```text
Base score = 50
+ 15 if item is reusable
+ 10 if item is repairable
+ 10 if demand exists in community
+ 10 if material can be repurposed
+ 5 if nearby receiver exists
- 20 if item is unsafe or unusable
```

Deliverables:

- AI scan screen
- Recommendation screen
- Circularity score
- Listing draft generation

Acceptance criteria:

- User can upload or capture item image.
- App returns category or suggestion.
- App calculates circularity score.
- App recommends one or more actions.

Fallback if AI API is difficult:

- Use manual category selection plus rule-based recommendations.
- Keep AI as an optional enhancement.
- Still connect to external APIs through Supabase and Nominatim.

### Phase 6: Map and Location Services

Estimated duration: 5-8 days

Goals:

- Show location-based usefulness.

Tasks:

1. Add location permission handling.
2. Add map screen.
3. Show user location or selected community location.
4. Show listing markers.
5. Add search by place name.
6. Use Nominatim for geocoding.
7. Cache geocoding results locally.
8. Show nearby repair or recycling points.
9. Add distance sorting.

Deliverables:

- Map screen
- Location search
- Nearby listings or centres

Acceptance criteria:

- App calls external geocoding endpoint.
- App displays location result.
- App stores location result locally.
- User can browse nearby circular economy opportunities.

### Phase 7: Request and Transaction Flow

Estimated duration: 6-9 days

Goals:

- Make the app feel like a real working platform.

Tasks:

1. Create request entity.
2. Build request button on listing detail.
3. Build owner request inbox.
4. Build requester request history.
5. Add accept and reject actions.
6. Add complete transaction action.
7. Add return flow for borrowed items.
8. Add rating prompt.
9. Generate impact event after completion.

Deliverables:

- Request flow
- Transaction status tracking
- Ratings
- Impact event generation

Acceptance criteria:

- User can request an item.
- Owner can accept or reject.
- Status updates locally and remotely.
- Completed transaction creates impact record.

### Phase 8: Impact Dashboard and Gamification

Estimated duration: 5-8 days

Goals:

- Demonstrate behavioural transformation.

Tasks:

1. Create impact event entity.
2. Create impact calculation logic.
3. Build impact dashboard.
4. Add eco-points.
5. Add badge system.
6. Add community leaderboard.
7. Add charts or progress rings.
8. Add personal sustainability summary.

Deliverables:

- Impact dashboard
- Badges
- Leaderboard

Acceptance criteria:

- App displays personal impact.
- Completing actions increases points.
- Badges are awarded.
- Leaderboard displays ranked users.

### Phase 9: Waste-to-Value Matching

Estimated duration: 6-10 days

Goals:

- Make the project extraordinary and strongly aligned with "Closing the Loop".

Tasks:

1. Add material listing type.
2. Add "I need material" request type.
3. Build matching algorithm.
4. Show recommended matches.
5. Add match explanation.
6. Add accept match flow.
7. Add impact event for repurposed materials.

Deliverables:

- Material listing
- Need request
- Recommended matches
- Match detail screen

Acceptance criteria:

- User can list material.
- Another user can create material need.
- App recommends a match.
- Match can be completed.

### Phase 10: Polish, Testing, and Bug Fixing

Estimated duration: 7-10 days

Goals:

- Make the app stable and presentation-ready.

Tasks:

1. Test all screens.
2. Test offline behaviour.
3. Test app restart persistence.
4. Test API failure states.
5. Test form validation.
6. Test empty states.
7. Test slow network.
8. Improve UI spacing and typography.
9. Fix crashes.
10. Add sample demo data.
11. Create screenshots.

Deliverables:

- Stable release build
- Demo dataset
- Final screenshots

Acceptance criteria:

- No major crash during demo.
- Core flows work smoothly.
- App data persists locally.
- External endpoint integration works.

### Phase 11: Final Report

Estimated duration: 5-7 days

Goals:

- Prepare the Part 2 report.

Report structure:

1. Cover page
2. Introduction
3. Application objective
4. SDG 12 and circular economy alignment
5. System architecture
6. Features implemented
7. Features not implemented, if any
8. User interface screenshots
9. Database design
10. API integration
11. Technical implementation
12. Testing
13. Individual contributions
14. GitHub evidence
15. Conclusion
16. Appendix with source code

Main report must be under 10 pages. Source code can be attached in appendix.

Deliverables:

- Final report
- Screenshots
- Source code appendix
- GitHub link

Hard deadline:

- **5 September 2026 before 5:00 PM**

### Phase 12: Presentation and Demo Preparation

Estimated duration: 4-6 days

Goals:

- Prepare a confident 10-minute demo.

Suggested presentation flow:

1. Problem introduction: 1 minute
2. LoopLink concept: 1 minute
3. App architecture: 1 minute
4. Live demo: 5 minutes
5. Impact and future improvement: 1 minute
6. Team contribution summary: 1 minute

Demo script:

1. Login as a student.
2. Open home dashboard.
3. Scan an item photo.
4. Show AI recommendation and circularity score.
5. Create a listing from scan result.
6. Browse listings.
7. Request an item from another user.
8. Accept request using owner account or demo mode.
9. Complete transaction.
10. Show updated impact dashboard.
11. Show map with nearby circular opportunities.

Q&A preparation:

- Every member must understand their module.
- Every member must be able to explain at least one technical implementation.
- Prepare answers for API, local storage, data model, security, sustainability, and limitations.

## 16. Team Role Plan

For a 4-person group:

| Member | Primary Responsibility | Secondary Responsibility |
|---|---|---|
| Member 1 | Project lead, architecture, Room database, GitHub management | Final integration and presentation flow |
| Member 2 | UI/UX, Jetpack Compose screens, app icon, wireframes | Screenshots and report design |
| Member 3 | Backend, Supabase, API integration, authentication | Map and geocoding |
| Member 4 | AI scan, circularity score, matching engine, impact dashboard | Testing and demo data |

Important:

- Each member must commit source code to GitHub.
- Each member should document what they contributed.
- Avoid having one person push all code because individual marks depend on contribution evidence and Q&A.

## 17. GitHub Workflow

Branch strategy:

```text
main
develop
feature/auth
feature/listings
feature/scan
feature/map
feature/impact
feature/report-assets
```

Rules:

1. Do not commit directly to `main`.
2. Each member works on a feature branch.
3. Use meaningful commit messages.
4. Create pull requests into `develop`.
5. Test before merging.
6. Tag final submission release.

Commit message examples:

```text
feat: add listing creation screen
feat: implement Room listing DAO
fix: handle empty API response on map screen
docs: add screenshots for final report
ui: polish impact dashboard cards
```

## 18. Testing Plan

### Functional Testing

| Test Case | Expected Result |
|---|---|
| Register new user | Account created and profile saved locally. |
| Login existing user | User enters home dashboard. |
| Create listing | Listing appears in browse screen and database. |
| Edit listing | Updated data appears after refresh and app restart. |
| Delete or archive listing | Listing no longer appears as active. |
| Scan item | App returns category and recommendation. |
| Search location | App returns geocoded result from external endpoint. |
| Request item | Request appears in owner inbox. |
| Complete request | Impact score updates. |
| Offline startup | Cached listings and profile still display. |

### UI Testing

Check:

- Text is readable.
- Buttons are easy to tap.
- Empty states are clear.
- Loading indicators appear.
- No screen looks unfinished.
- Dark mode, if supported, is readable.

### Demo Testing

Before presentation:

1. Use a stable emulator or physical phone.
2. Preload demo accounts.
3. Preload demo listings.
4. Prepare backup screenshots.
5. Prepare screen recording in case Wi-Fi fails.
6. Test API keys.
7. Test login before presentation.

## 19. Privacy, Security, and Ethics Plan

LoopLink handles user profiles, item photos, and location data, so the project should show responsible design.

### Privacy Requirements

1. Ask permission before using camera, gallery, or location.
2. Explain why each permission is needed.
3. Do not expose exact home addresses publicly.
4. Use approximate location for public listing display when possible.
5. Let users delete or archive their listings.
6. Do not upload images to AI services without user action.
7. Do not store unnecessary personal information.

### Security Requirements

1. Do not hardcode API keys in public GitHub commits.
2. Use environment files or local Gradle properties for secrets.
3. Use Supabase row-level security if cloud database access is enabled.
4. Validate all user input.
5. Prevent users from editing listings they do not own.
6. Use HTTPS endpoints only.
7. Add basic report or moderation flow for unsafe listings.

### Ethical AI Requirements

1. AI recommendations should be labelled as suggestions, not guaranteed truth.
2. Users should be able to correct wrong item categories.
3. The app should avoid recommending unsafe reuse for broken electronics, food, batteries, chemicals, or hygiene-sensitive items.
4. The circularity score should be explainable through clear factors, not hidden magic.

Why this matters:

- It strengthens Q&A answers.
- It makes the project feel publishable.
- It supports the assignment's focus on meaningful and responsible technology.

## 20. Risk Management

| Risk | Impact | Mitigation |
|---|---|---|
| AI API does not work during demo | High | Cache demo AI results locally and provide manual fallback. |
| Internet fails | High | Use Room offline cache and prepare screenshots or screen recording. |
| Supabase setup takes longer than expected | Medium | Start with local Room database, then add cloud sync later. |
| Map API rate limit | Medium | Cache geocoding results and use limited demo locations. |
| App crashes during demo | High | Use tested demo script and avoid untested paths. |
| Team contribution unclear | High | Enforce GitHub commits and contribution table from the beginning. |
| Scope becomes too large | Medium | Complete MVP first, then add extraordinary features. |

## 21. Feature Priority Matrix

| Feature | Priority | Reason |
|---|---|---|
| Login/register | Must | Needed for user identity. |
| Local Room database | Must | Assignment minimum requirement. |
| Listing CRUD | Must | Core app function. |
| External API call | Must | Assignment minimum requirement. |
| Custom launcher icon | Must | Assignment minimum requirement. |
| Browse/search listings | Must | Core usability. |
| AI scan | High | Strong innovation and technological integration. |
| Circularity score | High | Makes the idea unique. |
| Impact dashboard | High | Strong behavioural transformation evidence. |
| Map | High | Strong practical usefulness. |
| Request flow | High | Makes app demonstrable. |
| Waste-to-value matching | Very High | Strongest "Closing the Loop" feature. |
| QR handover | Optional | Impressive but not essential. |
| Admin dashboard | Optional | Improves civil/commercial potential. |
| App store publishing | Optional | Extra marks possible. |

## 22. Suggested Demo Dataset

Create realistic sample listings:

| Item / Material | Type | Circular Action |
|---|---|---|
| Electric drill | Lend | Borrow instead of buying. |
| Old laptop | Repair needed | Repair or donate. |
| Cardboard boxes | Material available | Reuse for moving or packaging. |
| Coffee grounds | Material available | Compost for gardening club. |
| Textbook | Give away | Rehome to junior student. |
| Broken headphones | Repair needed | Repair volunteer or spare parts. |
| Glass jars | Material available | Reuse by home food seller. |
| Scientific calculator | Lend | Short-term borrowing. |

## 23. Proposal Report Outline

Use this for Part 1.

```text
1. Cover Page
2. Introduction
3. Problem Statement
4. Objectives
5. Target Users
6. Proposed Solution: LoopLink
7. Key Features
8. SDG 12 and Circular Economy Alignment
9. System Architecture
10. Initial UI Design
11. Feasibility and Technology
12. Expected Impact
13. Project Timeline
14. Conclusion
15. Appendix, if needed
```

## 24. Final Report Outline

Use this for Part 2.

```text
1. Cover Page
2. Introduction
3. Application Overview
4. Implemented Features
5. System Architecture
6. Database and Storage Design
7. External API Integration
8. User Interface Screenshots
9. Testing and Results
10. Limitations and Future Improvements
11. Individual Contributions
12. Conclusion
13. Appendix: Source Code
```

## 25. App Store Publishing Plan

The assignment mentions extra marks for submission or publication on Google Play Store and/or Huawei AppGallery.

Publishing plan:

1. Create production-ready app name, icon, and package name.
2. Add privacy policy.
3. Remove hardcoded API keys.
4. Add app screenshots.
5. Prepare short and full app descriptions.
6. Create release build.
7. Test signed APK or AAB.
8. Submit to Google Play Console or Huawei AppGallery if possible.

Even if full publication is not completed, showing serious preparation can strengthen the report and presentation.

## 26. Suggested Final MVP Definition

If time becomes limited, the minimum impressive version should include:

1. Login/register
2. Profile
3. Listing CRUD
4. Local Room database
5. Supabase backend sync
6. Nominatim location search
7. AI or rule-based item recommendation
8. Circularity score
9. Request flow
10. Impact dashboard
11. Custom app icon
12. Polished UI
13. Screenshots and GitHub evidence

This version is still strong enough to demonstrate originality, completeness, UI design, technical integration, and assignment compliance.

## 27. What Makes LoopLink Perfectly Matched

LoopLink is a strong project because it:

- Directly supports Circular Economy and SDG 12.
- Includes P2P resource optimisation.
- Includes behavioural transformation through gamification and impact tracking.
- Includes closing the loop through waste-to-value matching.
- Uses local mobile storage.
- Uses external APIs.
- Can integrate AI.
- Has strong civil and commercial potential.
- Can be demonstrated clearly in under 10 minutes.
- Produces many meaningful screenshots for the report.
- Allows each group member to contribute a clear module.

## 28. Final Success Checklist

Before submission, verify:

- [ ] Idea registered in spreadsheet.
- [ ] Proposal is 4-10 pages.
- [ ] Proposal includes UI design.
- [ ] App has custom launcher icon.
- [ ] App stores data locally.
- [ ] App updates local data.
- [ ] App retrieves local data.
- [ ] App connects to external endpoint.
- [ ] App has working CRUD features.
- [ ] App has screenshots.
- [ ] App report is under 10 pages before appendix.
- [ ] Source code is attached or linked.
- [ ] GitHub shows individual contributions.
- [ ] Roles of each member are documented.
- [ ] Demo script is tested.
- [ ] Backup screenshots or screen recording prepared.
- [ ] Every member can answer Q&A about their technical contribution.

## 29. Recommended Build Order

Build in this exact order:

1. App skeleton and navigation
2. Room database
3. Listing CRUD
4. Supabase sync
5. Authentication
6. Profile
7. External location API
8. Request flow
9. Impact dashboard
10. AI scan
11. Matching engine
12. UI polish
13. Testing
14. Report
15. Presentation

Reason:

- The assignment minimum requirements are secured early.
- The core app works before advanced features are added.
- If advanced AI features take longer, the project is still complete.

## 30. One-Sentence Pitch

**LoopLink helps communities reduce waste by using AI, local sharing, repair-first recommendations, and waste-to-value matching to keep products and materials in use for as long as possible.**
