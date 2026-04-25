# CultureSync

> **Preserving Beeralu Bobbin Lace Making through Augmented Reality**
>
> BSc Computer Science Final Year Project - University of Bedfordshire  
> Bethmi Jayamila Dias - Version 2.0 - April 2026

---

## Overview

CultureSync is an Android application for learning the traditional Sri Lankan craft of **Beeralu bobbin lace making** through guided practice and real-time feedback.

The app combines:
- MediaPipe hand landmark tracking (21 points),
- Dynamic Time Warping (DTW) based motion comparison,
- AR-style visual feedback overlay,
- offline-first storage with optional sync workflows,
- role-based flows for learners and content managers.

---

## Core Features

- **Onboarding and role selection**
  - First-launch onboarding screens
  - Role-aware experience (student/artisan/admin-style workflows)

- **Technique learning**
  - Technique list and detail views
  - Expert demo video playback

- **AR-guided practice**
  - Live camera feed with hand tracking
  - Real-time overlay feedback
  - Session score generation and summary

- **Progress tracking**
  - Session history
  - Accuracy trends and summary stats
  - Leaderboard and profile views

- **Template management**
  - Record/import expert demos
  - On-device training pipeline
  - Template validation and publish workflow
  - Backward-compatible draft import (`trained_template.json` and legacy `generated_template.json`)

---

## Technology Stack

- **Platform:** Android (Kotlin)
- **Camera / Vision:** CameraX, MediaPipe Tasks
- **AR / Rendering:** Custom overlay views
- **Scoring:** DTW-based comparison engine
- **Persistence:** Room (SQLite)
- **Networking:** Retrofit / OkHttp (where enabled)
- **Build:** Gradle (KTS)

Backend (included in repository under `culturesync-backend/`):
- Node.js / Express
- MongoDB

---

## Repository Structure

- `app/` - Android app source code
- `culturesync-backend/` - Backend API source code
- `gradle/`, `build.gradle.kts`, `settings.gradle.kts` - Build configuration
- `CultureSync_TestCases.csv` - Test case matrix

---

## Build and Run (Android)

### Prerequisites

- Android Studio (latest stable)
- JDK 17+
- Android SDK configured
- A physical device or emulator

### Commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

For release build:

```bash
./gradlew :app:assembleRelease
```

---

## Testing

Run unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Run instrumentation tests (device/emulator required):

```bash
./gradlew :app:connectedDebugAndroidTest
```

---

## Submission Notes

- This repository has been cleaned for university submission.
- Runtime junk files (coverage outputs, crash dumps, local logs) are excluded via `.gitignore`.
- If admin draft templates exist from older runs, the app can still read legacy imports.

---

## License

Academic project submission.  
Use and distribution according to university project guidelines.

# CultureSync

> **Preserving Beeralu Bobbin Lace Making through Augmented Reality**

CultureSync is an Android app for learning the ancient Sri Lankan craft of **Beeralu bobbin lace making** using real-time hand gesture recognition and Augmented Reality. The app tracks 21 hand landmarks via MediaPipe, compares movements to expert templates using Dynamic Time Warping, and overlays a colour-coded AR skeleton giving live per-joint accuracy feedback.

> BSc Computer Science Final Year Project — University of Bedfordshire  
> Bethmi Jayamila Dias · Version 2.0 · April 2026

---

## Screenshots

| Onboarding | Role Selection | Home Dashboard |
|:-----------:|:--------------:|:--------------:|
| ![Onboarding](screenshots/onboarding.png) | ![Role Selection](screenshots/user_type_selection.png) | ![Home](screenshots/home_student.png) |

| Cultural Stories | Progress | Settings | Profile |
|:----------------:|:--------:|:--------:|:-------:|
| ![Cultural Stories](screenshots/cultural_stories.png) | ![Progress](screenshots/progress.png) | ![Settings](screenshots/settings.png) | ![Profile](screenshots/profile.png) |

---

## About the Craft

**Beeralu lace making** is a 600-year-old Sri Lankan craft with Portuguese colonial origins, predominantly practised in the coastal city of Galle. It involves weaving fine thread through a pattern of pins using wooden bobbins, producing intricate lacework. After the 2004 tsunami devastated Galle, master artisan M.B. Priyani led the revival of the craft. CultureSync was built to help preserve and digitise this knowledge before it is lost.

---

## How It Works

```
Phone Camera (CameraX)
        ↓
MediaPipe Hand Landmarker  →  21 landmarks per frame at ~30 FPS
        ↓
DTWEngine (FastDTW)  →  compare landmark buffer to expert template
        ↓
AR Overlay  →  per-joint colour: green (good) / yellow (close) / red (off)
        ↓
Session saved to Room DB  →  synced to MongoDB Atlas when online
```

**Three phases per technique:** Setup → Main Movement → Release.  
Reaching 85% accuracy on a phase auto-advances to the next one.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.1.20 |
| Build | AGP 8.5.2, Gradle 8.x |
| Camera | CameraX 1.3.1 |
| AR / Tracking | MediaPipe 0.10.14 (hand_landmarker), ARCore 1.51.0 |
| ML / DTW | Custom DTWEngine.kt (FastDTW variant) |
| Local DB | Room 2.6.1 (EncryptedSharedPreferences for auth) |
| Networking | Retrofit 2.9.0, OkHttp 4.12.0 |
| Charts | MPAndroidChart 3.1.0 |
| Video | Media3 ExoPlayer 1.2.1 |
| UI | Material Components 1.12.0 |
| Backend | Node.js ≥20, Express 4.22.1 |
| Database | MongoDB Atlas (Mongoose 7.8.9) |
| Auth | JWT, Helmet 8.1.0, express-rate-limit |
| Logging | Winston |

---

## System Requirements

| Requirement | Minimum |
|-------------|---------|
| Android | 8.0 (API 26) or higher |
| Camera | Rear-facing (required) |
| ARCore | Google Play Services for AR (required) |
| RAM | 3 GB recommended |
| Storage | 150 MB free |
| Internet | Required for login and sync; offline practice supported |

---

## Setup

### 1. Clone the repo

```bash
git clone <repo-url>
cd culture-sync
```

### 2. Add the MediaPipe model (required)

> **The app will crash on the Practice screen without this file.**

Download `hand_landmarker.task` (~5 MB) from [MediaPipe Solutions](https://developers.google.com/mediapipe/solutions/vision/hand_landmarker) and place it at:

```
app/src/main/assets/models/hand_landmarker.task
```

### 3. Build and install

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Set API URL

After first launch, go to **Settings → API Server URL** and point it at your backend (e.g. `http://10.0.2.2:3000/` for Android emulator, or your LAN IP for a real device).

---

## Backend Setup

```bash
cd culturesync-backend
npm install
cp .env.example .env
# Edit .env — fill in MONGODB_URI and JWT_SECRET
npm start
```

Backend runs on port `3000` by default. All endpoints are under `/api/v1/`.

### API Documentation (Swagger UI)

After starting the backend, open:

- `http://localhost:3000/api-docs`

The interactive docs are generated from `culturesync-backend/swagger.yaml` and include full request/response schemas, auth requirements, validations, and example payloads for all implemented endpoints.

---

## User Roles

| Role | Who | What they can do |
|------|-----|-----------------|
| **Student** | Learner | Browse techniques, watch demos, AR practice, track progress, cultural stories |
| **Artisan** | Expert craftsperson | Everything a Student can + record, train, and upload new technique templates |
| **Admin** | System administrator | Everything an Artisan can + admin validation tools in Settings |

> Admin accounts cannot be self-registered — the role is assigned directly in the database.

---

## Built-in Techniques

| Technique | Difficulty | Template Duration |
|-----------|-----------|:-----------------:|
| Basic Crossing (Lena Gassima) | Beginner | ~2.4s |
| Cross Pattern | Beginner | ~2.4s |
| Twisted Crossing | Intermediate | ~2.4s |
| Diamond Pattern | Advanced | ~2.4s |
| Flower Pattern | Advanced | ~2.4s |
| Wave Pattern | Advanced | ~2.4s |

Templates are stored as JSON under `app/src/main/assets/templates/` and loaded into Room on first launch. Artisans and admins can add new techniques via the in-app Admin Portal.

---

## Project Structure

```
app/src/main/kotlin/com/culturesync/app/
├── CultureSyncApp.kt           # Application class, Hilt, locale init
├── data/
│   ├── local/                  # Room database, DAOs, entities
│   └── remote/                 # Retrofit API service, SyncManager
├── ml/
│   ├── HandTrackingService.kt  # MediaPipe integration, CameraX pipeline
│   ├── DTWEngine.kt            # FastDTW implementation
│   ├── ARConstants.kt          # Thresholds, skeleton joint pairs
│   ├── AccuracyResult.kt       # Per-joint accuracy model
│   └── TemplateManager.kt      # Load/cache expert JSON templates
├── models/                     # Domain models (ExpertTemplate, User, …)
└── ui/
    ├── auth/                   # LoginActivity, RegisterActivity, ForgotPasswordActivity
    ├── home/                   # HomeActivity (role-aware dashboard)
    ├── onboarding/             # OnboardingActivity, OnboardingAdapter
    ├── practice/               # PracticeActivity, AROverlayView, SessionResultActivity
    ├── techniques/             # TechniqueListActivity, TechniqueDetailActivity
    ├── progress/               # ProgressDashboardActivity, SessionHistoryActivity
    ├── culture/                # CulturalStoriesActivity
    ├── profile/                # ProfileActivity
    ├── settings/               # SettingsActivity
    └── admin/                  # AdminUploadActivity, AdminValidateActivity

culturesync-backend/
├── routes/                     # auth.js, users.js, sessions.js, techniques.js, sync.js
├── models/                     # Mongoose schemas
├── middleware/                 # JWT auth, error handler, rate limiter
└── seed/                       # seedData.js (6 built-in techniques)
```

---

## API Summary

All endpoints prefixed `/api/v1/`. Authentication uses Bearer JWT.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/auth/register` | Register (Student or Artisan) |
| `POST` | `/auth/login` | Login → returns JWT |
| `GET` | `/users/:id` | Get own profile |
| `PUT` | `/users/:id` | Update profile |
| `GET` | `/techniques` | List all techniques (sorted by difficulty) |
| `GET` | `/techniques/:id` | Single technique |
| `POST` | `/sessions` | Create practice session |
| `GET` | `/sessions?userId=` | Get sessions for user |
| `PUT` | `/sessions/:id` | Update session (e.g. mark COMPLETED) |
| `POST` | `/sync/push` | Push offline sessions to cloud |

---

## Accuracy System

DTW scores are mapped to labels shown on the Session Result screen:

| Score | Label | Colour |
|-------|-------|--------|
| ≥ 80% | Excellent! | Green |
| 70–79% | Good Job! | Yellow |
| 50–69% | Average | Orange |
| < 50% | Keep Practicing | Red |

AR overlay joint colours: **green** (accurate) · **yellow** (close) · **red** (off)

---

## Language Support

The app supports **English** and **Sinhala**. Toggle in Settings — takes effect immediately via `AppCompatDelegate.setApplicationLocales`. All UI text, technique names, and cultural story content are localised.

---

## Known Limitations

- `hand_landmarker.task` must be manually placed before building (not included in repo due to size)
- Forgot Password is a local reset only — no email verification is sent
- Admin role must be assigned directly in the MongoDB database
- AR Practice is restricted to Students and Artisans (not Admin)
- Sessions auto-stop after 10 minutes

---

## License

Academic project — University of Bedfordshire, 2026.
