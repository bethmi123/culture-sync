# CultureSync Production Readiness Checklist

Date: 2026-04-25  
Author: Bethmi Jayamila Dias

---

## A. Build and Release

- [x] Debug build succeeds (`:app:assembleDebug`)
- [ ] Release build re-validated (`:app:assembleRelease`)
- [ ] Final smoke test completed on target device(s)
- [ ] Versioning and release metadata reviewed

---

## B. Core Functional Readiness

- [x] Authentication flow stable (register/login/logout/forgot)
- [x] Onboarding + role selection complete
- [x] Technique list/detail/demo playback complete
- [x] AR practice and scoring loop functional
- [x] Progress/history/leaderboard/profile/settings flows available
- [x] Admin upload/validate workflow functional

---

## C. Data and Sync

- [x] Local Room persistence functioning
- [x] Session save path functional
- [x] Sync endpoints integrated
- [ ] Offline-to-online sync scenario re-validated on final build

---

## D. Performance and Stability

- [ ] Device memory profile captured
- [ ] Long-session stability run completed (no ANR/crash)
- [ ] FPS/latency evidence collected for report
- [x] Build OOM mitigated via Gradle JVM tuning

---

## E. Security and Configuration

- [x] Auth token flow in place
- [x] ProGuard/R8 config present
- [x] Runtime junk artifacts excluded from submission
- [ ] Final backend environment values verified before demo

---

## F. Submission Package

- [x] README restored
- [x] System documentation restored
- [x] PRD coverage file restored
- [x] Evidence appendix restored
- [x] Validation evidence pack restored
- [ ] Final doc consistency and formatting pass completed

---

## Final Gate

- [ ] **Ready for submission**

# CultureSync Build Readiness Checklist

Scope: AR accuracy quality + video training reliability for non-PlayStore production builds.

## 1) Scientific Accuracy Validation (Benchmark + Metrics)

- [ ] Build benchmark dataset with at least 6 techniques x 3 lighting conditions x 3 camera angles.
- [ ] Capture both right and left hand sessions, including mirrored/front-camera scenarios.
- [ ] Compute DTW metrics: mean accuracy, p50/p95, false-positive/false-negative rates.
- [ ] Set acceptance gates:
  - Mean accuracy >= 0.85 for in-class correct motions
  - p95 accuracy >= 0.75
  - Incorrect gesture rejection rate >= 90%
- [ ] Track per-joint error distributions to identify unstable landmarks.
- [ ] Re-calibrate `calibrationMaxDistance` per template when dataset shifts.

## 2) Production Reliability Validation (Crash-Free + Stability)

- [x] Release network policy enforces HTTPS only.
- [x] Debug-only localhost cleartext policy exists for emulator/local backend.
- [ ] Run instrumentation tests for practice flow:
  - camera permission denied/accepted
  - start/stop session
  - session auto-close at timeout
- [ ] Add playback reliability tests for expert video:
  - starts automatically
  - pauses/resumes across lifecycle
  - loops continuously
  - fallback UI when video asset is missing
- [ ] Execute 30-minute soak tests on at least 3 Android devices.
- [ ] Confirm no ANRs or native crashes during camera + AR workload.

## 3) Build Gate (Release Candidate)

- [ ] `./gradlew :app:testDebugUnitTest` passes
- [ ] `./gradlew :app:assembleRelease` passes
- [ ] Manual smoke pass complete:
  - login
  - technique list/detail
  - expert video playback
  - AR practice + results screen
  - progress/history views

## 4) CI Enhancements

- [ ] Fail CI if readiness tests fail (`ProductionReadinessValidationTest`).
- [ ] Publish test reports and trend metrics for accuracy benchmarks.

