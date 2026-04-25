# CultureSync Complete System Documentation

Date: 2026-04-25  
Project: CultureSync (Android AR-based Beeralu heritage learning)  
Author: Bethmi Jayamila Dias  
Repository root: `culture-sync`

---

## 1. System Purpose

CultureSync is a mobile learning platform for Beeralu lace practice. It combines:
- real-time hand landmark detection,
- DTW-based motion comparison,
- visual AR-style corrective feedback,
- role-based content and admin tooling,
- offline-first persistence with optional sync.

---

## 2. Architecture Overview

### Android App (`app/`)
- UI layer: onboarding, auth, home, practice, progress, profile, settings, admin
- Data layer: Room DB + entities + DAOs
- ML layer: MediaPipe hand tracking + DTW engine + template manager
- Sync layer: Retrofit-based remote sync and user/session endpoints

### Backend (`culturesync-backend/`)
- Express routes for auth/users/sessions/techniques/sync
- MongoDB models
- JWT auth middleware + error handling + logging

---

## 3. Runtime Practice Pipeline

1. Camera feed captured via CameraX.
2. MediaPipe hand model extracts 21 landmarks.
3. Landmark window compared with expert template using DTW.
4. Per-joint and aggregate accuracy derived.
5. Overlay updates with color-coded feedback.
6. Session data stored locally and optionally synced.

---

## 4. Data and Storage

### Local
- Room database for users, techniques, sessions, frames, and app state.
- Session-first local writes support offline operation.

### Remote
- Backend endpoints for auth, profile, sessions, techniques, sync.
- JWT token flow for authenticated operations.

---

## 5. Admin Template Lifecycle

1. Admin/artisan records or imports demo videos.
2. On-device trainer extracts/normalizes landmarks and builds template JSON.
3. Validate flow imports trained template and runs schema sanity checks.
4. Approved template is published for learner access.

Backward compatibility is supported for legacy draft file naming:
- current: `trained_template.json`
- legacy: `generated_template.json`

---

## 6. Build and Tooling

- Build system: Gradle (KTS), AGP, Kotlin
- JVM memory tuned to avoid Dex merge OOM:
  - `org.gradle.jvmargs=-Xmx4g ...`
  - `kotlin.daemon.jvmargs=-Xmx2g`

Latest check:
- `./gradlew :app:assembleDebug` -> **BUILD SUCCESSFUL**

---

## 7. Testing and Validation

- Unit tests under `app/src/test/...`
- Android instrumentation/UI tests under `app/src/androidTest/...`
- Functional test matrix maintained in `CultureSync_TestCases.csv`

---

## 8. Security and Reliability Notes

- Auth token based access for protected routes
- Release build hardening via ProGuard/R8 rules
- Offline support for resilience during network loss
- Crash dumps/log/coverage outputs excluded from submission artifacts

---

## 9. Known Constraints

- Real-device evidence collection requires `adb` and connected hardware.
- Final UAT/SUS metrics depend on participant execution and data completion.

---

## 10. Submission Status

- Core app implementation: complete
- Build gate: pass
- Documentation set: restored and being finalized
- Evidence package: partially complete pending final captures

# CultureSync Complete System Documentation

Date: 2026-04-23  
Project: CultureSync (Android AR-based Beeralu heritage learning)  
Author: Bethmi Jayamila Dias  
Repository root: `culture-sync`

---

## 1. Project Purpose and Scope

CultureSync is a native Android app for learning Beeralu lace hand techniques using:
- live hand tracking (MediaPipe),
- AR overlay feedback,
- DTW-based motion comparison,
- offline-first local persistence,
- optional cloud sync,
- admin tooling for template onboarding and training.

Primary outcomes:
- learners receive real-time motion accuracy guidance,
- session history and analytics are persisted,
- new techniques can be onboarded by admin via multi-demo video capture and template training.

---

## 2. Implementation Status Summary

### 2.1 Code and Build Status

- Unit tests: passing (`:app:testDebugUnitTest`)
- Release build: passing (`:app:assembleRelease`)
- Debug install on physical Android device: successful

### 2.2 Device Evidence Snapshot (latest run)

From `validation-evidence/performance/device_metrics_20260423_000827.txt`:
- Launch `TotalTime`: 2499ms and 2736ms (passes `<3s` target)
- Memory `TOTAL PSS`: 143135 KB (~143 MB, passes `<200MB` target)
- Render stats:
  - Total frames rendered: 471
  - Janky frames: 1.27%
  - 50th percentile: 9ms
  - 90th percentile: 40ms
  - 95th percentile: 65ms
  - 99th percentile: 200ms

Still requiring dedicated evidence runs:
- sustained FPS benchmark over full practice duration,
- battery discharge over 30-minute unplugged session,
- full UAT/SUS completion.

### 2.3 Practical Completion Statement

- Functional implementation: complete for core product and admin onboarding.
- Scientific/production proof package: partially complete; remaining evidence collection still required.

---

## 3. Technology Stack

Android side:
- Kotlin
- CameraX
- MediaPipe Tasks Vision (`hand_landmarker.task`)
- ARCore lifecycle integration
- Room (SQLite)
- Retrofit/OkHttp/Gson
- Media3 ExoPlayer
- MPAndroidChart

Tooling:
- Gradle/AGP
- Proguard/R8 release minification
- Python utility (`tools/create_template.py`) for offline-assisted training path

---

## 4. High-Level Architecture

### 4.1 Runtime Pipeline (Learner Practice)

1. CameraX captures frames.
2. Frame is converted and rotated correctly for inference.
3. MediaPipe HandLandmarker detects landmarks.
4. Landmarks flow through SharedFlow.
5. DTW compares learner motion to expert template (phase-bounded).
6. Accuracy result drives AR overlay coloring and UI badges.
7. Session/frame metrics are persisted to Room.
8. Session closes with aggregate statistics and result display.

### 4.2 Data and Sync Architecture

- Source of truth for app UI: Room.
- Cloud sync is optional and asynchronous.
- Offline mode remains usable for core flows.

---

## 5. Feature Inventory (End User)

Implemented user flows include:
- onboarding and user type selection,
- register/login/forgot password,
- home dashboard,
- technique list/detail,
- expert offline video playback,
- AR practice session,
- session result summary,
- progress dashboard + history + leaderboard,
- profile and settings,
- cultural stories.

Localization:
- English and Sinhala resources present for main user-facing flows.

---

## 6. AR and DTW System Details

### 6.1 Hand Tracking

- Running mode: LIVE_STREAM for practice
- Confidence thresholds aligned in constants
- Timestamp monotonic guard implemented
- rotation preprocessing implemented
- queue saturation handled without app crash

### 6.2 AR Overlay

- FILL_CENTER coordinate mapping accounted for
- Per-joint/per-segment color thresholds:
  - Green `>= 0.85`
  - Yellow `>= 0.65`
  - Red `< 0.65`
- No-hands prompt rendered when no valid frame exists

### 6.3 DTW Engine

- Kotlin implementation with normalized landmark comparison
- Calibration-aware normalization path (`calibrationMaxDistance`)
- Phase-bounded runtime matching behavior
- Unit tested under multiple conditions

---

## 7. Admin and Template Lifecycle (Complete)

### 7.1 Admin Data Draft

Per-technique draft stores:
- technique key/name/difficulty
- recorded demo video file paths
- imported or exported template artifacts

Draft storage path:
- app internal files under `admin_drafts/<technique_key>/`

### 7.2 Demo Collection

Admin can:
- record new videos via camera capture,
- add existing videos from storage picker,
- build 5+ demo sets per technique.

### 7.3 Training Modes

#### A) On-device training (implemented)

`OnDeviceTemplateTrainer` performs:
1. landmark extraction from videos (MediaPipe VIDEO mode),
2. frame normalization,
3. iterative DBA,
4. pairwise DTW calibration (`D_max * 1.5`),
5. template JSON serialization.

This closes the previously missing functional gap.

#### B) Offline-assisted training (still supported)

`tools/create_template.py` remains available for workstation-assisted template export.

### 7.4 Validate and Publish

Admin validate flow supports:
- import trained JSON template,
- schema sanity checks (non-empty frames, valid calibration),
- approve -> publish to app template storage,
- reject -> draft cleanup.

---

## 8. Content System

- Technique templates live in app assets and/or admin-published files.
- Expert videos are offline playable using Media3 ExoPlayer.
- Cultural story screens are included for heritage context.

---

## 9. Security and Reliability

### 9.1 Security Measures

- Password hashing enforced (bcrypt)
- HTTPS policy hardened in release config
- Debug-only cleartext exceptions retained for local dev endpoints
- Release minification enabled

### 9.2 Build Reliability

Resolved build blockers:
- invalid image resource encoding issues,
- R8 missing class warnings via rules adjustments,
- Gradle JVM memory constraints for release build stability.

---

## 10. Testing and Evidence

### 10.1 Automated

- Unit test suite passes.
- Release assembly passes.

### 10.2 Evidence Pack (repo)

Files:
- `validation-evidence/performance/...`
- `validation-evidence/integration/...`
- `validation-evidence/uat/...`
- `FINAL_REPORT_EVIDENCE_APPENDIX.md`

Helper tooling:
- `tools/collect_device_metrics.sh`
- `tools/calc_sus_summary.py`

### 10.3 Current Remaining Evidence Tasks

Still needed for full scientific sign-off:
- sustained FPS and latency in active long session runs,
- unplugged battery discharge run,
- completed participant UAT entries and SUS final statistics.

---

## 11. Operational Flows

### 11.1 Install + Device Test Flow

1. Build debug/release APK.
2. Install on connected device via `adb`.
3. Launch app and run core practice path.
4. Run metrics script.
5. Save artifacts into `validation-evidence/`.

### 11.2 New Technique Onboarding Flow

1. Open Admin Upload.
2. Enter technique info.
3. Capture/import at least 5 demos.
4. Train on-device (or export for offline script).
5. Open Admin Validate.
6. Import trained JSON output if required.
7. Approve/publish.
8. Verify technique appears and works in learner flow.

---

## 12. Known Constraints and Honest Boundaries

- “All features implemented” is true for code-level scope.
- “Fully production proven” still depends on completing remaining device/UAT evidence targets.
- Offline-assisted workflow remains optional even though on-device training is now present.

---

## 13. File and Module Map (Key)

Core ML/AR:
- `app/src/main/kotlin/com/culturesync/app/ml/DTWEngine.kt`
- `app/src/main/kotlin/com/culturesync/app/ml/HandTrackingService.kt`
- `app/src/main/kotlin/com/culturesync/app/ui/practice/AROverlayView.kt`
- `app/src/main/kotlin/com/culturesync/app/ui/practice/PracticeActivity.kt`

Admin training:
- `app/src/main/kotlin/com/culturesync/app/ui/admin/AdminUploadActivity.kt`
- `app/src/main/kotlin/com/culturesync/app/ui/admin/AdminValidateActivity.kt`
- `app/src/main/kotlin/com/culturesync/app/ui/admin/AdminTemplateDraft.kt`
- `app/src/main/kotlin/com/culturesync/app/ui/admin/OnDeviceTemplateTrainer.kt`

Evidence and documentation:
- `PRD_COVERAGE_STATUS.md`
- `PRODUCTION_READINESS_CHECKLIST.md`
- `VALIDATION_EVIDENCE_PACK.md`
- `FINAL_REPORT_EVIDENCE_APPENDIX.md`
- `validation-evidence/...`

---

## 14. Final Statement

CultureSync currently delivers:
- complete end-user learning workflow,
- complete admin onboarding workflow including on-device training,
- robust build/test stability,
- measurable early device evidence.

To claim full production-grade scientific completion, finalize the remaining runtime evidence items (FPS/battery/UAT) using the provided scripts and templates.
