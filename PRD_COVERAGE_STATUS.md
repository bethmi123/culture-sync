# CultureSync PRD Coverage Status

**Version:** 2.0  
**Date:** 2026-04-25  
**Project:** BSc Final Year Project - University of Bedfordshire  
**Author:** Bethmi Jayamila Dias

---

## Overall Status

- Product implementation: **Complete for core scope**
- Build verification: **Pass** (`:app:assembleDebug`)
- Production evidence package: **Partially complete**
- Final submission documentation: **In progress / restored**

---

## Module Coverage Snapshot

| PRD Area | Status | Notes |
|---|---|---|
| Authentication (Register/Login/Logout/Forgot) | Covered | Client + backend routes available |
| Onboarding + Role selection | Covered | First-launch flow implemented |
| Home Dashboard (role-aware) | Covered | Student/Artisan/Admin behavior mapped |
| Technique Library + Detail | Covered | Technique list, detail, demo playback |
| AR Practice + DTW scoring | Covered | MediaPipe tracking + DTW engine integrated |
| Session Result + Feedback | Covered | Score summary and flow complete |
| Progress + History + Leaderboard | Covered | Local stats + list/chart screens present |
| Cultural Stories | Covered | Localized story content supported |
| Profile + Settings | Covered | Language, API URL, session controls |
| Admin Upload / Validate workflow | Covered | Train/import/approve template workflow |
| Sync / offline-first behavior | Covered | Local persistence with sync paths |

---

## Quality and Verification

### Build/Runtime

- `:app:assembleDebug` -> **PASS**
- `:app:assembleRelease` -> previously validated in project cycle
- OOM build issue addressed via `gradle.properties` JVM tuning

### Testing

- Unit test suites exist in `app/src/test/...`
- Android UI/integration test coverage exists across auth/home/practice/admin flows
- Test case matrix available in `CultureSync_TestCases.csv`

---

## Known Submission Gaps

- Some long-form evidence docs were deleted and are being restored.
- Device-run evidence artifacts depend on real hardware sessions (`adb` + phone).
- UAT/SUS final numbers require completed participant data entries.

---

## Definition of Done (Submission)

| Item | Status |
|---|---|
| App builds successfully | Pass |
| Core PRD modules implemented | Pass |
| Tests and test cases present | Pass |
| Evidence docs packaged | In progress |
| Final report appendix linked | In progress |

---

## Immediate Next Actions

1. Finalize and verify restored documentation set.
2. Re-run release build and smoke test critical flows.
3. Package evidence files and screenshots for final university submission.

# CultureSync: Complete User Manual
**Version:** 2.0 (Production Build)
**Date:** 2026-04-25
**Project:** BSc Final Year Project — University of Bedfordshire
**Author:** Bethmi Jayamila Dias

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [User Roles Overview](#2-user-roles-overview)
3. [System Requirements](#3-system-requirements)
4. [Getting Started — First Launch](#4-getting-started--first-launch)
   - 4.1 [Splash Screen](#41-splash-screen)
   - 4.2 [Onboarding Carousel](#42-onboarding-carousel)
   - 4.3 [User Type Selection](#43-user-type-selection)
   - 4.4 [Registration](#44-registration)
   - 4.5 [Login](#45-login)
   - 4.6 [Forgot Password](#46-forgot-password)
5. [Home Dashboard](#5-home-dashboard)
   - 5.1 [Student Dashboard](#51-student-dashboard)
   - 5.2 [Artisan Dashboard](#52-artisan-dashboard)
   - 5.3 [Admin Dashboard](#53-admin-dashboard)
6. [Techniques Library](#6-techniques-library)
   - 6.1 [Browse Techniques](#61-browse-techniques)
   - 6.2 [Technique Detail](#62-technique-detail)
   - 6.3 [Expert Video Demo](#63-expert-video-demo)
7. [AR Practice Session (Core Feature)](#7-ar-practice-session-core-feature)
   - 7.1 [Starting a Session](#71-starting-a-session)
   - 7.2 [The Practice Interface](#72-the-practice-interface)
   - 7.3 [How Accuracy Is Measured](#73-how-accuracy-is-measured)
   - 7.4 [Stopping a Session](#74-stopping-a-session)
   - 7.5 [Session Result Screen](#75-session-result-screen)
8. [Progress & Analytics](#8-progress--analytics)
   - 8.1 [Progress Dashboard](#81-progress-dashboard)
   - 8.2 [Session History](#82-session-history)
9. [Cultural Stories](#9-cultural-stories)
10. [Profile](#10-profile)
11. [Settings](#11-settings)
12. [Admin Portal — Full Workflow](#12-admin-portal--full-workflow)
    - 12.1 [Accessing the Admin Portal](#121-accessing-the-admin-portal)
    - 12.2 [Step 1 — Fill Technique Details](#122-step-1--fill-technique-details)
    - 12.3 [Step 2 — Record Expert Demonstrations](#123-step-2--record-expert-demonstrations)
    - 12.4 [Step 3 — Train On-Device](#124-step-3--train-on-device)
    - 12.5 [Step 4 — Go to Validate](#125-step-4--go-to-validate)
    - 12.6 [Step 5 — Import & Approve Template](#126-step-5--import--approve-template)
    - 12.7 [Rejecting a Template](#127-rejecting-a-template)
13. [Role-Based Feature Reference](#13-role-based-feature-reference)
14. [Backend API Reference](#14-backend-api-reference)
15. [Troubleshooting](#15-troubleshooting)

---

## 1. Introduction

CultureSync is an Android application designed to preserve and teach the art of **Beeralu bobbin lace making** — a 600-year-old Sri Lankan craft with Portuguese colonial origins. The app uses **Augmented Reality (AR)** and **hand gesture recognition** powered by Google MediaPipe to give learners real-time accuracy feedback as they practise each technique.

**What the app does:**
- Displays expert Beeralu technique demonstrations via video
- Uses the phone camera to track 21 hand landmarks in real time
- Compares the learner's hand movements to expert templates using the DTW (Dynamic Time Warping) algorithm
- Overlays a colour-coded skeleton on the camera feed showing accuracy per joint
- Tracks progress over time with charts and session history
- Provides artisans and admins with tools to record, train, and publish new technique templates

---

## 2. User Roles Overview

CultureSync has three active roles:

| Role | Who It Is | What They Can Do |
|------|-----------|-----------------|
| **Student** | Learner practising Beeralu | Browse techniques, watch demos, practise with AR, track progress |
| **Artisan** | Expert craftsperson | Everything a Student can do, plus upload & train new technique templates |
| **Admin** | System administrator | Everything an Artisan can do, plus access admin tools from Settings |

Role is selected during registration and cannot be changed by the user afterward.

---

## 3. System Requirements

| Requirement | Minimum |
|-------------|---------|
| Android Version | Android 8.0 (API 26) or higher |
| Camera | Rear-facing camera (required) |
| Internet | Required for login, sync, and leaderboard; offline practice is supported |
| ARCore | Must be installed (Google Play Services for AR) |
| RAM | 3 GB or more recommended |
| Storage | 150 MB free space |

> **Important:** The hand tracking model file (`hand_landmarker.task`, ~5 MB) must be present in `app/src/main/assets/models/` before building. Without it the app will crash when opening PracticeActivity.

---

## 4. Getting Started — First Launch

### 4.1 Splash Screen

**What you see:**
- CultureSync logo fades in with a staggered animation
- App name and tagline appear below
- App version number shown at the bottom

**What happens automatically:**
- The app checks whether you are already logged in
- If logged in → goes directly to the **Home Dashboard**
- If this is the first time opening the app → goes to **Onboarding**
- If not logged in but not first time → goes to **Login**

---

### 4.2 Onboarding Carousel

**When it appears:** First launch only (never shown again after you register or log in).

**What you see:**
- A 5-page swipeable carousel explaining the app
- Dot indicators at the bottom showing which page you are on
- **Next** button (bottom right) and **Skip** button (top right)

![Onboarding Screen](onboarding.png)

**The 5 pages:**

| Page | Title | Content |
|------|-------|---------|
| 1 | Welcome to CultureSync | Introduction to the app's mission |
| 2 | Learn from Experts | How expert knowledge is captured |
| 3 | Preserve Heritage | The cultural importance of Beeralu |
| 4 | Practice with AR | How the AR tracking works for students |
| 5 | Admin & Artisan Tools | How admins and artisans create content |

**Step-by-step:**
1. Swipe left or tap **Next** to move through each page
2. On the last page (Page 5), the Next button changes to **"Get Started"**
3. Tap **"Get Started"** → goes to **User Type Selection**
4. Alternatively, tap **Skip** at any time → goes to **Login**

---

### 4.3 User Type Selection

**When it appears:** After tapping "Get Started" on the onboarding carousel.

**What you see:**
- Two cards displayed side by side:
  - **Student** — for learners
  - **Artisan** — for expert craftspeople
- A **Continue** button at the bottom

![User Type Selection](user_type_selection.png)

**Step-by-step:**
1. Tap your role card — the selected card shows a gold border and scales up slightly
2. Default selection is **Student** if you do not tap a card
3. Tap **Continue** → goes to **Registration** with your selected role pre-filled

> **Note:** Admin accounts cannot be created through the app. Admin role is assigned directly in the database by a system administrator.

---

### 4.4 Registration

**When it appears:** After selecting a user type, or tapping "Register" on the Login screen.

**What you see:**
- **Name** field
- **Email** field
- **Password** field (minimum 6 characters)
- **Confirm Password** field
- **Register** button
- Link to go back to Login

**Step-by-step:**
1. Enter your **full name**
2. Enter your **email address** — must be unique (not already registered)
3. Enter a **password** (at least 6 characters)
4. Re-enter the same password in **Confirm Password**
5. Tap **Register**

**What happens:**
- The app validates all fields client-side first
- Attempts to register via cloud API
- If no internet → saves account locally (offline registration)
- On success → session is created and you are taken to **Home Dashboard**

**Common errors:**
| Error | Cause |
|-------|-------|
| "Passwords do not match" | Confirm Password field differs from Password |
| "Password must be at least 6 characters" | Password is too short |
| "Email already registered" | That email exists in the database |
| "Please fill in all fields" | One or more fields left empty |

---

### 4.5 Login

**When it appears:** For returning users, or after tapping "Skip" during onboarding.

**What you see:**
- **Email** field
- **Password** field
- **Login** button
- **"Forgot Password?"** link
- **"Don't have an account? Register"** link

**Step-by-step:**
1. Enter your registered **email address**
2. Enter your **password**
3. Tap **Login**

**What happens:**
- App tries cloud API login first
- If no internet → falls back to local Room database verification
- On success → session saved and you are taken to **Home Dashboard**

**Common errors:**
| Error | Cause |
|-------|-------|
| "Invalid email or password" | Credentials do not match any account |
| "Please enter email and password" | One or both fields are empty |

---

### 4.6 Forgot Password

**When it appears:** Tap "Forgot Password?" on the Login screen.

**What you see:**
- **Email** field
- **New Password** field
- **Confirm New Password** field
- **Reset Password** button
- **Back** button

**Step-by-step:**
1. Enter the **email address** associated with your account
2. Enter your **new password**
3. Re-enter it in **Confirm New Password**
4. Tap **Reset Password**
5. A success message appears → tap **Back** to return to Login
6. Log in using your new password

> **Note:** This is a local password reset. No email verification link is sent. The reset only works if the email exists in the local database.

---

## 5. Home Dashboard

The Home Dashboard is the central hub of the app. Its content and navigation options change based on your role.

### 5.1 Student Dashboard

**What you see:**

![Student Dashboard](home_student.png)

- **Welcome greeting** — "Hello, [First Name]!" at the top
- **Hero image carousel** — rotating cultural images
- **Feature cards:**
  - **Learn / Start Practising** → goes to Technique List
  - **My Progress** → goes to Progress Dashboard
  - **Cultural Stories** → goes to Cultural Stories
- **Stats cards** (auto-loaded from your session history):
  - Total Sessions completed
  - Average Accuracy (%)
  - Best Accuracy (%)
  - Total Practice Time
- **Bottom navigation bar:** Home | Practice | Progress | Profile
- **Top-right icons:** Profile icon | Settings icon

**Navigation from Student Dashboard:**

| Tap | Goes to |
|-----|---------|
| Learn / Start Practising card | Technique List |
| My Progress card | Progress Dashboard |
| Cultural Stories card | Cultural Stories |
| Profile icon (top right) | Profile screen |
| Settings icon (top right) | Settings screen |
| Practice (bottom nav) | Technique List |
| Progress (bottom nav) | Progress Dashboard |
| Profile (bottom nav) | Profile screen |

---

### 5.2 Artisan Dashboard

**What you see:**
- Same layout as Student Dashboard
- **Feature cards** differ:
  - **Learn / Start** → Technique List
  - **My Progress** → Progress Dashboard
  - **Template Upload** → Admin Upload screen (instead of Cultural Stories)
- Stats cards same as Student
- Settings icon reveals an **Admin** button in Settings

**Navigation from Artisan Dashboard:**

| Tap | Goes to |
|-----|---------|
| Learn / Start card | Technique List |
| My Progress card | Progress Dashboard |
| Template Upload card | Admin Upload (template recording) |
| Settings icon → Admin button | Admin Upload |

---

### 5.3 Admin Dashboard

**What you see:**
- Same layout as Student Dashboard
- **Feature cards:**
  - **Learn / Start** → Technique List
  - **My Progress** → Progress Dashboard
  - **Template Upload** → Admin Upload screen
- Full access to all admin tools via Settings

**Navigation from Admin Dashboard:**

| Tap | Goes to |
|-----|---------|
| Learn / Start card | Technique List |
| My Progress card | Progress Dashboard |
| Template Upload card | Admin Upload |
| Settings icon → Admin button | Admin Upload |

---

## 6. Techniques Library

### 6.1 Browse Techniques

**How to get here:** Tap "Learn / Start" card or "Practice" in the bottom navigation from Home.

**What you see:**
- A scrollable list of all published Beeralu techniques
- Each technique card shows:
  - **Technique name** (in your selected language)
  - **Difficulty badge**: Beginner / Intermediate / Advanced
  - **Duration** (in seconds)

**Techniques available (6 built-in):**

| Technique | Difficulty | Duration |
|-----------|-----------|----------|
| Basic Crossing (Lena Gassima) | Beginner | ~2.4s template |
| Cross Pattern | Beginner | ~2.4s template |
| Twisted Crossing | Intermediate | ~2.4s template |
| Diamond Pattern | Advanced | ~2.4s template |
| Flower Pattern | Advanced | ~2.4s template |
| Wave Pattern | Advanced | ~2.4s template |

**Techniques are sorted:** Beginner → Intermediate → Advanced

**Step-by-step:**
1. Scroll through the list to browse techniques
2. Tap any technique card to open its detail page

---

### 6.2 Technique Detail

**How to get here:** Tap a technique from the Technique List.

**What you see:**
- **Video player** (ExoPlayer) — auto-loads expert demonstration video
- **Technique name**
- **Description** — what the movement involves
- **Difficulty** label
- **Duration**
- **"Start Practice"** button (visible for Students and Artisans only)
- **Back** button

**Video loading behaviour:**
- First attempts to stream from the cloud backend
- Falls back to bundled offline video (`res/raw/demo_<techniqueKey>.mp4`) if no internet

**Step-by-step:**
1. Watch the expert demonstration video to understand the movement
2. Use the player controls to pause, rewind, or replay
3. When ready, tap **"Start Practice"** to enter the AR practice session
4. Tap **Back** to return to the Technique List

> **Role restriction:** Only Students and Artisans can see and use "Start Practice". Admin users can watch videos but cannot start practice sessions through this button.

---

### 6.3 Expert Video Demo

**How to get here:** From Technique Detail (dedicated expert video player screen).

**What you see:**
- Full-screen ExoPlayer with the expert demonstration video
- Video **loops continuously** (REPEAT_MODE_ALL) so you can study the movement repeatedly
- **Title** of the technique at the top
- **Back** button

**Step-by-step:**
1. Video starts playing automatically
2. Let it loop as many times as needed to memorise the hand movement pattern
3. Tap **Back** to return to Technique Detail

---

## 7. AR Practice Session (Core Feature)

The Practice Session is the most important feature of CultureSync. It uses your phone's rear camera and MediaPipe hand tracking to give you real-time accuracy feedback on your Beeralu technique.

### 7.1 Starting a Session

**How to get here:** Tap "Start Practice" from Technique Detail.

**What happens first:**
1. The app checks if **Camera permission** has been granted
2. If not granted → a permission dialog appears explaining why the camera is needed
3. Tap **Allow** on the system permission dialog
4. If denied → a rationale message appears; you must grant permission to continue

**After permission is granted:**
- Camera preview opens immediately
- Hand tracking is initialised (MediaPipe loads the model)
- A **"Point your hand at the camera"** prompt appears
- The **Start** button is visible at the bottom

**Step-by-step to begin:**
1. Hold your phone so the rear camera faces your hands
2. Make sure there is good lighting
3. Tap the **Start** button
4. A new practice session is created in the database with status IN_PROGRESS
5. The timer starts counting up

---

### 7.2 The Practice Interface

**What you see on screen:**

![Practice Screen](practice.png)

| Element | Description |
|---------|-------------|
| **Camera preview** | Live rear camera feed |
| **AR overlay** | Coloured skeleton drawn over your detected hand |
| **Accuracy % display** | Live accuracy score updated every second |
| **Technique name** | Shown at the top of the screen |
| **Timer (MM:SS)** | Shows how long the current session has been running |
| **"Point your hand at camera" prompt** | Appears when no hand is detected |
| **Stop button** | Ends the session and saves results |
| **Close (X) button** | Exits without saving (session is discarded) |

**AR overlay colour coding:**

| Colour | Meaning |
|--------|---------|
| Green | That joint matches the expert template well (high accuracy) |
| Yellow | That joint is close but not quite matching |
| Red | That joint is significantly off from the expert position |

**How to hold your hands:**
- Face your palm toward the camera
- Keep your hand fully visible within the camera frame
- Avoid rapid movements that blur the frame
- Hold each position for at least 1–2 seconds so DTW can process it

---

### 7.3 How Accuracy Is Measured

CultureSync uses **DTW (Dynamic Time Warping)** to compare your hand movements to the expert template.

**The process (technical overview for reference):**

1. CameraX captures frames at ~30 FPS
2. Each frame is passed to MediaPipe, which detects **21 hand landmarks** (finger joints, wrist, knuckles)
3. Every ~1 second, a buffer of ~120 frames is compared against the expert template for the current phase
4. DTW calculates a similarity score per joint
5. The **live accuracy %** on screen is updated
6. If you reach 85% accuracy on the current phase, you automatically advance to the next phase

**Three phases per technique:**
- **Setup** — initial hand position
- **Main movement** — the crossing/weaving action
- **Release** — final position

**Tips for higher accuracy:**
- Match the hand position shown in the expert video as closely as possible
- Move slowly and deliberately — rushing reduces DTW match scores
- Ensure your full hand (all 5 fingers) is visible, not cut off by the screen edge
- Good lighting dramatically improves landmark detection accuracy

---

### 7.4 Stopping a Session

**How to end a session:**
- Tap the **Stop** button at any time

**What happens:**
1. The session timer stops
2. Any remaining frames in the buffer are flushed to the database
3. Final accuracy is calculated as the mean of all frame accuracy scores
4. Best accuracy is recorded if this session beats your previous best
5. Session status is updated to **COMPLETED** in the local database
6. You are automatically taken to the **Session Result** screen

**Auto-stop:**
- The session automatically stops after **10 minutes** (to prevent runaway sessions)
- A warning appears at 9 minutes

---

### 7.5 Session Result Screen

**What you see after a session:**

| Element | Description |
|---------|-------------|
| **Technique name** | Which technique you just practised |
| **Accuracy score** (large, colour-coded) | Your final average accuracy percentage |
| **Circular progress indicator** | Visual ring filling up to your accuracy % |
| **Accuracy label** | Excellent / Good / Average / Keep Practicing |
| **Feedback message** | Personalised encouragement text |
| **Duration** | How long the session lasted |
| **Frame count** | Total frames captured and analysed |
| **Line chart** | Accuracy trend across the last 10 frames of the session |
| **"Practice Again"** button | Returns to Technique Detail to start another session |
| **"Home"** button | Returns to the Home Dashboard |

**Accuracy thresholds and labels:**

| Score | Label | Badge Colour | Feedback |
|-------|-------|-------------|---------|
| 80% or above | Excellent! | Green | "Outstanding performance!" |
| 70% – 79% | Good Job! | Yellow | "Great effort, keep going!" |
| 50% – 69% | Average | Orange | "Good start, keep practising!" |
| Below 50% | Keep Practicing | Red | "Don't give up, practice makes perfect!" |

**Accuracy trend chart:**
- Plots your accuracy across the last 10 frames of the session
- Drawn as a gold line with shaded fill
- Shows whether your accuracy improved or declined during the session

---

## 8. Progress & Analytics

### 8.1 Progress Dashboard

**How to get here:** Tap "My Progress" card on Home, or "Progress" in the bottom navigation.

**Who can access:** Students and Artisans.

**What you see:**

![Progress Analytics](progress.png)

- **Stats cards (4 tiles):**
  - Total Sessions completed
  - Average Accuracy across all sessions
  - Best Accuracy ever achieved
  - Total Practice Time (formatted as hours/minutes)
- **Accuracy trend chart** — line chart of your last 10 completed sessions
  - X-axis: session number (1 = oldest, 10 = most recent)
  - Y-axis: accuracy % (0–100)
  - Blue line with circular markers at each session
  - Cubic Bezier smoothing applied
- **"View History"** button — opens the full session history list

**All data is stored locally** (Room database). No internet connection is needed to view your progress.

---

### 8.2 Session History

**How to get here:** Tap "View History" on the Progress Dashboard.

**Who can access:** Students and Artisans.

**What you see:**
- A scrollable list of all **completed** practice sessions
- Each list item shows:
  - **Technique name**
  - **Date** (formatted in your device locale)
  - **Accuracy (%)** — colour-coded
  - **Duration**

**Accuracy colour coding in history:**

| Colour | Threshold |
|--------|-----------|
| Green | 80% or above |
| Orange | 70% – 79% |
| Red | Below 70% |

**Deleting a session:**
1. Swipe the session item **left**
2. The item disappears and is permanently deleted from the local database

> **Note:** This cannot be undone. Deleted sessions are also removed from your progress stats on the next screen load.

---

## 9. Cultural Stories

**How to get here:** Tap "Cultural Stories" card on the Student Home Dashboard, or from the navigation.

**Who can access:** Students (and all other roles).

**What you see:**

![Cultural Stories](cultural_stories.png)

- A vertically scrollable carousel of **3 story cards**
- Each card shows:
  - **Story image** (cultural illustration)
  - **Title**
  - **Description text**

**The three stories:**

| # | Story | Summary |
|---|-------|---------|
| 1 | The Origins of Beeralu Lace | 600-year history of the craft, Portuguese colonial introduction |
| 2 | Revival After the Tsunami | M.B. Priyani's work to revive the craft in Galle after the 2004 tsunami |
| 3 | The Craft Today | Current state of Beeralu lace making in Sri Lanka |

**Language support:** All story text is available in **English** and **Sinhala**. Change language in Settings.

---

## 10. Profile

**How to get here:** Tap the Profile icon (top right of Home), or "Profile" in the bottom navigation.

**Who can access:** All authenticated users.

**What you see:**

![User Profile](profile.png)

- **Name** and **email address**
- **Role** (Student / Artisan / Admin — localised label)
- **Stats summary:**
  - Total Sessions
  - Average Accuracy
  - Best Accuracy
  - Total Practice Time
- **Logout** button

**Logging out:**
1. Tap **Logout**
2. A confirmation dialog appears: "Are you sure you want to log out?"
3. Tap **Yes** to confirm
4. Your session is cleared and you are taken back to the **Login** screen

> **Note:** Logging out does not delete your practice data. All sessions and progress are saved in the local database and will still be there when you log back in.

---

## 11. Settings

**How to get here:** Tap the Settings icon (top right of Home Dashboard).

**Who can access:** All authenticated users.

**What you see:**

![Settings Screen](settings.png)

---

### 11.1 Language Toggle

- **Switch:** English ↔ Sinhala
- Toggle the switch to change the app language
- **Takes effect immediately** — no restart needed
- All UI text, technique names, and story content switch to the selected language

---

### 11.2 GPU Acceleration Toggle

- **Switch:** Enable / Disable hardware GPU delegate for MediaPipe
- When **ON**: Hand tracking runs on the GPU — faster inference, lower CPU load
- When **OFF**: Runs on CPU — more compatible with older devices but slower
- **Recommended:** Keep ON if your device supports it (Android 8+ with OpenGL ES 3.0)

---

### 11.3 API URL Configuration

- **Text field** showing the current backend API base URL
- **Save** button next to the field
- Change this if the backend is running on a different server
- After tapping Save, all subsequent API calls use the new URL

**Step-by-step to change API URL:**
1. Tap the API URL text field
2. Clear the existing URL and type the new one (include `http://` or `https://`)
3. Tap **Save**
4. A confirmation toast appears: "API URL updated"

---

### 11.4 Clear Session Data

- **Button:** "Clear Session Data"
- Tapping it shows a confirmation dialog
- On confirm: all rows in the `sessions` and `session_frames` tables are deleted
- Your account is NOT deleted; only practice history is cleared

**When to use:** If you want to reset all your practice data and start fresh.

---

### 11.5 Admin Button (Artisan & Admin only)

- Visible only if your role is **Artisan** or **Admin**
- Tap **"Admin"** → opens the **Admin Upload** screen
- This is an alternative entry point to the Admin Portal (same as tapping the Template Upload card on Home)

---

### 11.6 App Version

- The current build version of CultureSync is shown at the bottom of Settings
- This is read-only information

---

## 12. Admin Portal — Full Workflow

The Admin Portal allows **Artisans** and **Admins** to create new Beeralu technique templates that students can then practise with.

**The full workflow has 5 steps:**
1. Fill in technique details
2. Record expert demonstrations (minimum 5 videos)
3. Train the template on-device
4. Review and import the trained template
5. Approve (publish) or Reject the template

---

### 12.1 Accessing the Admin Portal

**Method 1 — From Home Dashboard:**
1. Log in as an Artisan or Admin
2. On the Home Dashboard, tap the **"Template Upload"** card

**Method 2 — From Settings:**
1. Tap the Settings icon (top right of Home)
2. Scroll down to the Admin section
3. Tap the **"Admin"** button

Both methods open the **Admin Upload** screen.

---

### 12.2 Step 1 — Fill Technique Details

**Screen: Admin Upload**

![Admin Upload](admin_upload.png)

**What you see:**
- **Technique Name** text field
- **Difficulty** spinner (dropdown): Beginner / Intermediate / Advanced
- **Demo counter** showing how many recordings have been collected
- Recording action buttons
- Training action buttons

**Step-by-step:**
1. Tap the **Technique Name** field and type the name of the technique (e.g., "Spiral Cross")
2. Tap the **Difficulty** spinner and select the appropriate level:
   - **Beginner** — simple, slow movements for new learners
   - **Intermediate** — moderate complexity
   - **Advanced** — complex multi-step movements
3. Proceed to Step 2 (recording)

---

### 12.3 Step 2 — Record Expert Demonstrations

**Minimum required: 5 demonstration videos**

The system needs multiple recordings of the same technique from different angles and speeds to build an accurate template.

**Option A: Record a new video directly**

1. Tap **"Record Demo"**
2. The phone camera app opens in video recording mode
3. Perform the technique clearly and steadily in front of the camera
4. Stop recording in the camera app
5. The video is saved to the draft directory
6. The **demo counter** increments (e.g., "1 demo recorded")
7. Repeat from step 1 until you have at least **5 demos**

**Option B: Import an existing video**

1. Tap **"Add Existing Video"**
2. The file picker opens — navigate to an existing video file on your device
3. Select the video
4. It is copied to the draft directory
5. The demo counter increments

**Tips for good demo recordings:**
- Film from directly in front, with your hands clearly visible
- Perform the movement at a steady, moderate pace — not too fast
- Ensure bright, even lighting with no harsh shadows on your hands
- Keep your entire hand in frame for all 21 landmarks to be detectable
- Perform the exact same technique sequence each time for consistency across demos

---

### 12.4 Step 3 — Train On-Device

**After collecting at least 5 demos, proceed to training.**

**Step-by-step:**
1. Confirm the demo counter shows **5 or more** recordings
2. Tap **"Train On-Device"**
3. A progress layout appears showing training status

**What happens during training:**
- `OnDeviceTemplateTrainer` processes each recorded video
- MediaPipe extracts the 21 hand landmarks from every frame of each video
- The system calibrates the DTW distance thresholds using all recordings
- A JSON template file is generated containing:
  - Technique metadata (name, difficulty, artisan name, timestamp)
  - All extracted landmark frames
  - Phase boundaries (setup, main movement, release)
  - Calibration distance thresholds
- The template is saved to the device's imported templates directory

**Training duration:** Depends on the length and number of videos. Typically 1–3 minutes for 5 short demos.

4. Wait for the progress indicator to complete
5. A success message appears: "Template trained successfully"
6. Proceed to Step 4

**Alternative — Export Template Job:**
- Tap **"Export Template Job"** instead of training on-device
- A JSON job instruction file is copied to your clipboard
- This JSON can be sent to an external ML pipeline for off-device training
- Use this option if on-device training fails or if you prefer cloud-based training

---

### 12.5 Step 4 — Go to Validate

1. After training completes, tap **"Go to Validate"**
2. The **Admin Validate** screen opens with the technique details pre-filled

---

### 12.6 Step 5 — Import & Approve Template

**Screen: Admin Validate**

![Admin Validate](admin_validate.png)

**What you see:**
- **Technique name** and **Difficulty** (read-only, passed from Upload screen)
- **Draft status panel:**
  - Number of demos recorded
  - Whether a trained template has been imported ("Template imported: Yes/No")
- **"Import Template"** button — load the trained JSON from a file
- **"Approve"** button — publish the template for all students
- **"Reject"** button — discard the draft entirely
- **Back** button — return to Admin Upload

**Step-by-step to approve:**

1. If the template is not yet imported, tap **"Import Template"**
   - The file picker opens — navigate to the trained template JSON file
   - Select it — the template is loaded and validated
   - Status updates to "Template imported: Yes"

2. Review the draft status to confirm everything looks correct:
   - At least 5 demos recorded
   - Template imported successfully

3. Tap **"Approve"**

**What happens during approval:**
- The app parses the template JSON
- **Schema validation checks:**
  - `frames` array is non-empty
  - `calibrationMaxDistance` is greater than 0
  - All required fields are present
- If validation passes:
  - `validatedByArtisan = true` is set in the template metadata
  - The template is written to the **published templates directory**
  - A toast appears: "Template approved and published!"
  - The screen closes and you return to Admin Upload
- If validation fails:
  - An error toast explains which field failed
  - The template is NOT published
  - Fix the template and try importing again

**After approval:**
- The new technique immediately appears in the **Technique List** for all users
- Students and Artisans can practise with it right away

---

### 12.7 Rejecting a Template

If the training results are poor or the technique is incorrect:

1. On the **Admin Validate** screen, tap **"Reject"**
2. A confirmation dialog appears
3. Tap **Yes** to confirm
4. The entire draft directory is deleted (all recordings + template files)
5. The screen closes and you return to Admin Upload
6. Start the process again from Step 1 (12.2)

---

## 13. Role-Based Feature Reference

This table shows exactly which features each role can access:

| Feature | Student | Artisan | Admin |
|---------|:-------:|:-------:|:-----:|
| View Techniques List | Yes | Yes | Yes |
| Watch Expert Video | Yes | Yes | Yes |
| Start AR Practice Session | Yes | Yes | No |
| View Session Result | Yes | Yes | No |
| View Progress Dashboard | Yes | Yes | Yes |
| View Session History | Yes | Yes | Yes |
| Delete Sessions from History | Yes | Yes | Yes |
| View Cultural Stories | Yes | Yes | Yes |
| Upload New Technique (Admin Upload) | No | Yes | Yes |
| Train Template On-Device | No | Yes | Yes |
| Validate & Approve Template | No | Yes | Yes |
| Reject Template | No | Yes | Yes |
| Access Admin section in Settings | No | Yes | Yes |
| Change Language (English/Sinhala) | Yes | Yes | Yes |
| Toggle GPU Acceleration | Yes | Yes | Yes |
| Change API URL | Yes | Yes | Yes |
| Clear Session Data | Yes | Yes | Yes |
| Logout | Yes | Yes | Yes |

---

## 14. Backend API Reference

The backend is a Node.js / Express server connected to MongoDB Atlas. All endpoints are prefixed with `/api/v1/`.

### Authentication Endpoints

#### Register a New User
```
POST /api/v1/auth/register
Body: { name, email, password, role }
Response 201: { token, user: { id, name, email, role } }
Response 409: Email already exists
Response 403: Cannot self-assign Admin role
```

#### Login
```
POST /api/v1/auth/login
Body: { email, password }
Response 200: { token, user: { id, name, email, role } }
Response 401: Invalid credentials
```

---

### User Endpoints (Authenticated)

#### Get User Profile
```
GET /api/v1/users/:id
Auth: Bearer JWT token
Response 200: { user (without passwordHash) }
Response 403: Not authorised (can only view own profile)
Response 404: User not found
```

#### Update User Profile
```
PUT /api/v1/users/:id
Auth: Bearer JWT token
Body: { name?, email?, role? }
Response 200: { updated user }
Response 400: Duplicate email or invalid data
Response 403: Cannot self-assign Admin role
```

---

### Session Endpoints (Authenticated)

#### Create a Practice Session
```
POST /api/v1/sessions
Auth: Bearer JWT token
Body: {
  techniqueId, techniqueName, startTime, endTime,
  durationSeconds, accuracyScore (0–100), frameCount
}
Response 201: { session document }
Response 400: Validation error (e.g. accuracyScore out of range)
```

#### Get Sessions for a User
```
GET /api/v1/sessions?userId=<userId>
Auth: Bearer JWT token
Response 200: [ sessions ] (max 50, sorted newest first)
Response 403: Not authorised to view other users' sessions
```

#### Update a Session
```
PUT /api/v1/sessions/:id
Auth: Bearer JWT token
Body: { endTime?, durationSeconds?, accuracyScore?, frameCount?, status? }
Response 200: { updated session }
Response 403: Can only update own sessions
Response 404: Session not found
```

---

### Technique Endpoints (Public)

#### Get All Published Techniques
```
GET /api/v1/techniques
Response 200: [ techniques ] (Beginner → Intermediate → Advanced)
```

#### Get Single Technique
```
GET /api/v1/techniques/:techniqueId
Response 200: { technique }
Response 404: Not found
```

---

### Sync Endpoint (Authenticated)

#### Push Offline Sessions to Cloud
```
POST /api/v1/sync/push
Auth: Bearer JWT token
Body: { sessions: [ { techniqueId, techniqueName, startTime, endTime,
         durationSeconds, accuracyScore, frameCount }, ... ] }
Response 201: { synced: <count> }
```

This endpoint is called automatically by the app when internet connectivity is restored after offline practice sessions.

---

## 15. Troubleshooting

### App crashes immediately when I tap "Start Practice"

**Cause:** The MediaPipe hand tracking model file is missing.

**Fix:** Ensure `hand_landmarker.task` (~5 MB) is placed at:
```
app/src/main/assets/models/hand_landmarker.task
```
This file must be manually downloaded and placed before building the app.

---

### Camera permission denied and I cannot enable it

**Fix:**
1. Open your phone's **Settings** app
2. Go to **Apps** → **CultureSync** → **Permissions**
3. Find **Camera** and set it to **Allow**
4. Return to the app and try again

---

### Hand tracking is not detecting my hand

**Possible causes and fixes:**

| Cause | Fix |
|-------|-----|
| Poor lighting | Move to a brighter area; avoid backlight |
| Hand partially out of frame | Keep entire hand visible within the camera boundary |
| Background too similar to skin | Use a plain, contrasting background |
| GPU delegate causing issues | Go to Settings → toggle GPU Acceleration OFF |

---

### My accuracy is always very low

**Tips to improve accuracy:**
- Watch the expert video demo repeatedly before practising
- Move slowly and deliberately — rushing produces poor DTW scores
- Ensure your hand matches the orientation in the expert demo (palm facing camera vs sideways)
- Keep your hand steady for 1–2 seconds at each position

---

### Cannot log in — "Invalid email or password"

- Confirm you are using the email and password from when you registered
- If forgotten, use **Forgot Password** on the Login screen
- If the backend is unreachable, ensure the API URL in Settings is correct

---

### Technique templates not appearing after admin approval

- Restart the app (the technique list is seeded on app launch)
- If the published template JSON is invalid, approval will silently fail — check the template schema

---

### Language does not change after toggling in Settings

- Language change takes effect immediately via `AppCompatDelegate.setApplicationLocales`
- If the UI does not update, go back to Home and return to the previous screen
- Ensure your device's Android version is 8.0 or higher

---

### Sync is not uploading my offline sessions

- Ensure you have an active internet connection
- Check the API URL in Settings is correct and the backend is running
- Sessions recorded offline are stored locally and will sync automatically the next time the app launches with internet access

---

*CultureSync User Manual — Version 2.0*
*University of Bedfordshire — BSc Computer Science Final Year Project*
*Date: 2026-04-25*
