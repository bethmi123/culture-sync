# Chapter 6 Defense-Safe Validation Document

Author: Codebase Validation Pass  
Project: CultureSync  
Purpose: Produce a defense-safe, evidence-aligned Chapter 6 implementation narrative

---

## 1) What this document does

This document aligns thesis implementation claims with the **current codebase** so Chapter 6 can be defended safely in viva.

It provides:

- a mismatch matrix (claim vs implementation),
- verified feature inventory (what is truly implemented now),
- high-value code blocks for core logic,
- defense-safe wording guidance,
- a clear list of future-work items that should not be presented as already implemented.

---

## 2) Executive risk summary

### High-risk thesis/code mismatches (must correct before submission)

1. **DTW free-start initialization axis mismatch**
   - Thesis text states free-start as `dtw[0][j] = 0`.
   - Current `DTWEngine` sets `dtw[i][0] = 0f`.
   - Defense risk: core algorithm credibility challenge.

2. **AR coordinate remapping overclaim**
   - Thesis describes explicit FILL_CENTER crop-offset correction formula.
   - Current `AROverlayView` draws normalized points using `x * width`, `y * height`.
   - Defense risk: examiner may ask to show exact remapping implementation.

3. **Phantom connection removal claim mismatch**
   - Thesis states `Pair(0,9)` and `Pair(0,13)` were removed.
   - Current `AROverlayView` still includes both connections.

4. **Timestamp guard overclaim**
   - Thesis describes monotonic timestamp guard logic in frame processing.
   - Current `HandTrackingService` uses `SystemClock.uptimeMillis()` with no explicit non-increasing timestamp drop guard.

5. **Admin DBA trainer pipeline overclaim**
   - Thesis describes full on-device VIDEO extraction + iterative DBA + convergence.
   - Current admin flow is upload/validate/approve-reject; no visible `OnDeviceTemplateTrainer` implementation class in current code.

6. **Backend API namespace mismatch**
   - Thesis lists `/api/...`.
   - Current backend/app client uses `/api/v1/...`.

7. **Rate-limit mismatch**
   - Thesis mentions auth rate limit `10 requests / 15 minutes`.
   - Current backend applies global limiter `100 / 15 minutes` (non-test mode).

8. **Test status overclaim**
   - Chapter language implying all tests pass is unsafe.
   - Current unit test run reports failures.

---

## 3) Defense-safe feature inventory (implemented and defensible)

The following can be defended confidently with current code:

- Native Android app (`minSdk 24`, Kotlin).
- CameraX + MediaPipe hand tracking integration.
- Real-time hand landmark flow using `SharedFlow`.
- DTW-based learner/template comparison engine.
- Per-joint accuracy mapping and color-coded overlay rendering.
- Room-backed offline-first persistence for session data.
- Sync manager that pushes pending sessions when online.
- Backend API with Express, JWT auth, Helmet security middleware.
- Bilingual UI architecture support (resource-driven strings).
- Full activity set for learner/admin flows (20 files currently present).
- Unit/integration test scaffolding exists (but not all tests pass currently).

---

## 4) Mismatch matrix: claim vs real implementation

## 4.1 DTW algorithm details

### Thesis claim (unsafe as written)
- Free-start DTW explicitly documented as `dtw[0][j] = 0`.

### Code reality
- `DTWEngine.compare()` initializes with:
  - `dtw[i][0] = 0f` for all `i`
  - best match selected from `dtw[i][m]` over `i`.

### Defense-safe rewrite
- "CultureSync implements a subsequence DTW variant with free-start behavior in the current matrix orientation, allowing flexible temporal alignment between learner sequence and expert template."

### Evidence code
```kotlin
// DTWEngine.kt
val dtw = Array(n + 1) { FloatArray(m + 1) { Float.MAX_VALUE } }
for (i in 0..n) dtw[i][0] = 0f

for (i in 1..n) {
    for (j in 1..m) {
        if (abs(i - j) > band) continue
        val cost = poseDistance(normLearner[i - 1], normExpert[j - 1])
        dtw[i][j] = cost + min(dtw[i - 1][j - 1], min(dtw[i - 1][j], dtw[i][j - 1]))
    }
}
```

---

## 4.2 Z-coordinate usage

### Thesis claim
- z coordinate excluded from distance due to monocular depth noise.

### Code reality
- Distance function uses x/y only (`euclidean`), z not used in DTW cost.
- Normalization still transforms z into frame representation, but distance excludes z.

### Defense-safe wording
- "The current DTW distance metric is 2D (`x`,`y`) and does not include `z` in cost computation."

### Evidence code
```kotlin
private fun euclidean(a: HandLandmark, b: HandLandmark): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    val dist = sqrt(dx*dx + dy*dy)
    return if (dist.isNaN() || dist.isInfinite()) ARConstants.MAX_EXPECTED_DTW_DISTANCE else dist
}
```

---

## 4.3 AR overlay coordinate mapping

### Thesis claim (unsafe as written)
- Explicit FILL_CENTER crop-offset correction formula is used in overlay mapping.

### Code reality
- Current overlay render path draws landmarks directly from normalized coordinates to view space:
  - `canvas.drawLine(lmA.x * width, lmA.y * height, ...)`

### Defense-safe wording
- "Current implementation maps normalized MediaPipe coordinates directly to overlay view space; further camera-viewport remapping refinement remains an optimization area."

### Evidence code
```kotlin
canvas.drawLine(
    lmA.x * width, lmA.y * height,
    lmB.x * width, lmB.y * height,
    learnerLinePaint
)
```

---

## 4.4 Overlay connection topology

### Thesis claim (unsafe as written)
- Phantom connections `(0,9)` and `(0,13)` removed.

### Code reality
- Both currently exist in `connections`.

### Defense-safe wording
- "The overlay currently uses a custom 23-edge hand graph including palm-to-finger bridge links; future strict MediaPipe-topology mode is planned."

### Evidence code
```kotlin
private val connections = listOf(
    0 to 1, 1 to 2, 2 to 3, 3 to 4,
    0 to 5, 5 to 6, 6 to 7, 7 to 8,
    0 to 9, 9 to 10, 10 to 11, 11 to 12,
    0 to 13, 13 to 14, 14 to 15, 15 to 16,
    0 to 17, 17 to 18, 18 to 19, 19 to 20,
    5 to 9, 9 to 13, 13 to 17
)
```

---

## 4.5 Timestamp handling in MediaPipe frame submission

### Thesis claim (unsafe as written)
- Monotonic timestamp guard and duplicate-drop strategy are implemented.

### Code reality
- Current code uses `SystemClock.uptimeMillis()` and passes directly to `detectAsync`.
- No explicit `lastTimestamp` guard is visible in current hand tracking service.

### Defense-safe wording
- "The current implementation timestamps frames using `SystemClock.uptimeMillis()` for LIVE_STREAM processing; additional duplicate timestamp guarding is identified as a robustness hardening task."

### Evidence code
```kotlin
fun processFrame(bitmap: Bitmap) {
    val mpImage = BitmapImageBuilder(bitmap).build()
    handLandmarker?.detectAsync(mpImage, SystemClock.uptimeMillis())
}
```

---

## 4.6 Admin template training pipeline

### Thesis claim (unsafe as written)
- Full on-device DBA iterative trainer with convergence threshold is already implemented.

### Code reality
- `AdminUploadActivity` currently launches device camera video capture intent.
- `AdminValidateActivity` loads template file, toggles `validatedByArtisan`, approve/reject flow.
- No visible `OnDeviceTemplateTrainer` class in current code tree.

### Defense-safe wording
- "Current admin workflow supports capture/import, validation, and approval/rejection of templates. Full automated on-device DBA training pipeline is a planned enhancement."

### Evidence code
```kotlin
// AdminUploadActivity.kt
val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
if (intent.resolveActivity(packageManager) != null) {
    videoPicker.launch(intent)
}
```

```kotlin
// AdminValidateActivity.kt
val template: ExpertTemplate = Gson().fromJson(json, type)
val updated = template.copy(validatedByArtisan = true)
file.writeText(Gson().toJson(updated))
```

---

## 4.7 Backend endpoint paths and rate limiting

### Thesis claim (unsafe as written)
- Endpoint path style `/api/...`
- auth limit 10/15m

### Code reality
- API paths use `/api/v1/...`.
- Rate limit in backend app is `100` requests / 15 min (non-test).

### Defense-safe wording
- "Backend routes are versioned under `/api/v1/` and currently protected by a global request limiter (100 requests / 15 minutes) plus JWT-based route authorization."

### Evidence code
```javascript
// app.js
app.use(rateLimit({ windowMs: 15 * 60 * 1000, max: 100 }));

app.use('/api/v1/auth',        require('./src/routes/auth'));
app.use('/api/v1/users',       require('./src/routes/users'));
app.use('/api/v1/sessions',    require('./src/routes/sessions'));
app.use('/api/v1/techniques',  require('./src/routes/techniques'));
app.use('/api/v1/leaderboard', require('./src/routes/leaderboard'));
app.use('/api/v1/sync',        require('./src/routes/sync'));
```

---

## 5) Most valuable implementation logic (for Chapter 6 code highlights)

Use these as the strongest code-level proof points.

## 5.1 Core runtime loop (camera -> tracking -> DTW -> overlay)

Why valuable:
- shows real-time architecture integration,
- demonstrates practical scheduling and frame batching decisions,
- maps directly to your research question on in-session corrective feedback.

```kotlin
// PracticeActivity.kt (excerpt)
lifecycleScope.launch {
    handTracker.handFrameFlow.collect { (left, right) ->
        val frame = right ?: left ?: run {
            withContext(Dispatchers.Main) { binding.arOverlay.updateOverlay(learner = null) }
            return@collect
        }
        if (isPracticing) {
            learnerBuffer.add(frame)
            if (learnerBuffer.size > ARConstants.SHARED_FLOW_BUFFER) learnerBuffer.removeFirst()
            frameCounter++

            if (frameCounter % ARConstants.DTW_COMPUTE_INTERVAL_FRAMES == 0 &&
                learnerBuffer.size >= ARConstants.DTW_FRAME_WINDOW / 4) {
                runDTW()
            }
        }
        withContext(Dispatchers.Main) {
            binding.arOverlay.updateOverlay(
                learner = frame,
                expert = currentExpertFrame,
                accuracy = currentAccuracy.perJointMap(),
                overall = currentAccuracy.overallPercent
            )
        }
    }
}
```

---

## 5.2 DTW robustness guards and bounded scoring

Why valuable:
- demonstrates non-crashing behavior for malformed/empty inputs,
- bounded score normalization (`0..1`) is critical for stable UI feedback.

```kotlin
if (learnerFrames.isEmpty() || expertFrames.isEmpty()) return AccuracyResult.EMPTY
if (learnerFrames.any { it.landmarks.size != 21 } || expertFrames.any { it.landmarks.size != 21 }) {
    return AccuracyResult.EMPTY
}

private fun distanceToAccuracy(distance: Float): Float {
    if (distance.isNaN() || distance.isInfinite()) return 0f
    return (1f - distance / ARConstants.MAX_EXPECTED_DTW_DISTANCE).coerceIn(0f, 1f)
}
```

---

## 5.3 Overlay explainability logic (per-joint color feedback)

Why valuable:
- directly supports educational interpretation of corrective feedback,
- ties your UX to algorithmic per-joint output.

```kotlin
private fun accuracyColor(accuracy: Float) = when {
    accuracy >= ARConstants.GREEN_ACCURACY_THRESHOLD  -> ARConstants.COLOR_GREEN
    accuracy >= ARConstants.YELLOW_ACCURACY_THRESHOLD -> ARConstants.COLOR_YELLOW
    else                                              -> ARConstants.COLOR_RED
}
```

---

## 5.4 Offline-first sync semantics

Why valuable:
- confirms practical deployability under low connectivity constraints,
- supports your offline-first thesis motivation.

```kotlin
suspend fun syncPendingSessions() = withContext(Dispatchers.IO) {
    if (!isOnline()) return@withContext
    val token = sessionManager.getAuthToken()
    if (token.isEmpty()) return@withContext

    val unsyncedSessions = db.sessionDao().getUnsyncedSessions()
    if (unsyncedSessions.isEmpty()) return@withContext

    val response = RetrofitClient.api.syncPush(token = "Bearer $token", body = payload)
    if (response.isSuccessful) {
        unsyncedSessions.forEach { db.sessionDao().markSynced(it.id, "") }
    }
}
```

---

## 6) Test status: defense-safe statement

Current reality:

- Test framework and broad suite exist.
- Current run reported failures (not fully green).

Defense-safe wording:

- "A comprehensive unit test suite is in place for DTW behavior, parsing, and model constraints; at this snapshot, several tests are failing and are being tracked for alignment with the current implementation branch."

Avoid saying:

- "All tests pass."

---

## 7) Recommended Chapter 6 “defense-safe” versioning language

Add this at chapter start:

> "Chapter 6 reports implementation status as of the current production branch snapshot. Where design intent and implementation differ, the implemented behavior is stated explicitly and deferred enhancements are identified as future work."

Add this near algorithm section:

> "The DTW module currently implements a subsequence-style dynamic programming alignment with constrained warping and bounded distance-to-accuracy normalization. Matrix initialization orientation reflects the current implementation branch and should be interpreted in that context."

Add this near admin section:

> "The current admin pipeline supports capture/import, validation, and publication flow. Full automatic on-device DBA computation is an identified extension and is not claimed as completed in this branch."

---

## 8) Feature list for viva (what to present confidently)

Use this checklist during viva Q&A.

- Real-time hand tracking pipeline integrated with camera feed.
- Landmark streaming via shared coroutine flow.
- DTW comparison with subsequence matching behavior.
- Per-joint and overall accuracy calculation.
- Color-coded AR overlay feedback loop.
- Session capture, frame batching, and Room persistence.
- Sync mechanism to backend when network is available.
- JWT-based backend auth and route protection.
- Offline functional core workflow.
- Full learner/admin activity ecosystem present.

---

## 9) Items to position as future work (not completed claims)

- Full automated DBA trainer from multiple admin videos.
- Explicit monotonic timestamp duplicate-drop guard in tracker.
- Strict MediaPipe canonical connection topology mode.
- Formal FILL_CENTER remapping/crop-offset calibration layer in overlay.
- Fully green test suite reconciliation on this branch snapshot.

---

## 10) Final defense guidance

In viva, prioritize **implementation truth over ideal architecture wording**.

Strong stance:
- "Here is what is implemented and measured now."
- "Here is what is designed and partially scaffolded."
- "Here is what is future work."

This increases credibility and usually scores better than overclaiming.

