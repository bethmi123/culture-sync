# CultureSync Validation Evidence Pack

Date: 2026-04-25  
Author: Bethmi Jayamila Dias

---

## Objective

Provide a structured checklist and file map for final validation evidence used in project submission and PRD traceability.

---

## Required Evidence Artifacts

### 1) Build and Execution
- Debug build pass output
- Release build pass output
- Installation and launch verification notes

### 2) Functional Testing
- Test matrix: `CultureSync_TestCases.csv`
- Unit test run summary
- Android instrumentation/UI test run summary

### 3) Performance and Stability
- Device metrics capture (CPU/memory/startup/frames)
- Long-session stability run notes
- Crash/ANR observations

### 4) User Validation
- UAT participant records
- SUS score summary and interpretation

### 5) Documentation Traceability
- `README.md`
- `PRD_COVERAGE_STATUS.md`
- `CULTURESYNC_COMPLETE_SYSTEM_DOCUMENTATION.md`
- `FINAL_REPORT_EVIDENCE_APPENDIX.md`
- `PRODUCTION_READINESS_CHECKLIST.md`

---

## Evidence Collection Workflow

1. Build debug and release variants.
2. Run smoke tests on physical Android device.
3. Execute core module scenarios from test matrix.
4. Capture device metrics and stability observations.
5. Record UAT responses and compute SUS.
6. Consolidate outputs into final report package.

---

## Status Snapshot

| Evidence Area | Status |
|---|---|
| Build success evidence | Available |
| Functional matrix | Available |
| Architecture/PRD docs | Available |
| Device benchmark pack | Pending final run |
| UAT/SUS final data | Pending completion |

---

## Submission Notes

- Keep only necessary academic artifacts.
- Exclude runtime junk outputs (logs, hprof, generated coverage folders).
- Confirm all referenced evidence files are present before final archive/zip.

# CultureSync Validation Evidence Pack

Use this pack to close the remaining PRD v2.0 evidence requirements for integration, performance, and UAT.

## 1) Required Evidence Outputs

Store all outputs under `validation-evidence/`:

- `performance/device_metrics_<date>.txt`
- `integration/integration_checklist_<date>.md`
- `uat/uat_results_<date>.csv`
- `uat/sus_summary_<date>.md`
- `screenshots/` (key app screens in EN + SI)

## 2) Quick Start

1. Connect Android device with USB debugging enabled.
2. Build and install release APK:
   - `./gradlew :app:assembleRelease`
   - `adb install -r app/build/outputs/apk/release/app-release.apk`
3. Run metrics capture script:
   - `bash tools/collect_device_metrics.sh`
4. Fill:
   - `tools/integration_checklist_template.md`
   - `tools/uat_results_template.csv`
   - `tools/sus_summary_template.md`

## 3) PRD Mapping

- PRD §10.2 Integration Testing -> `integration_checklist_<date>.md`
- PRD §10.3 Performance Testing -> `device_metrics_<date>.txt`
- PRD §10.4 UAT -> `uat_results_<date>.csv`, `sus_summary_<date>.md`
- PRD §16 Definition of Done -> all evidence artifacts above + screenshots

## 4) Acceptance Targets (from PRD)

- FPS: `>= 25` sustained
- Overlay latency: `< 100ms`
- DTW compute: `< 50ms` per cycle
- Memory: `< 200MB`
- Battery: `< 15% / 30 minutes`
- UAT SUS: `>= 70`
- UAT participants: `>= 10`
