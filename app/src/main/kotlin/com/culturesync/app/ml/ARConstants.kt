package com.culturesync.app.ml

import android.graphics.Color

/**
 * All production constants for the AR/DTW pipeline.
 * No magic numbers shall exist outside this file. — PRD §14
 */
object ARConstants {
    // ── Accuracy Thresholds (FR-AR-03) ─────────────────────────────
    const val GREEN_ACCURACY_THRESHOLD  = 0.85f
    const val YELLOW_ACCURACY_THRESHOLD = 0.65f

    // ── Overlay Colours ────────────────────────────────────────────
    val COLOR_GREEN:  Int = Color.parseColor("#00C853")
    val COLOR_YELLOW: Int = Color.parseColor("#FFD600")
    val COLOR_RED:    Int = Color.parseColor("#FF1744")

    // ── Rendering Constants (FR-AR-04) ─────────────────────────────
    const val LINE_STROKE_WIDTH = 8f       // dp — line stroke width
    const val DOT_RADIUS        = 12f      // dp — joint dot radius
    const val OVERLAY_ALPHA     = 200      // 0–255 overlay paint alpha

    // ── DTW Constants (FR-DTW-01 / FR-DTW-04) ──────────────────────
    const val DTW_FRAME_WINDOW             = 60     // frames in learner buffer for DTW
    const val DTW_COMPUTE_INTERVAL_FRAMES  = 30     // run DTW every N frames (~1 s at 30 FPS)
    const val DTW_SAKOE_CHIBA_BAND         = 0.10f  // 10% of sequence length
    const val MAX_EXPECTED_DTW_DISTANCE    = 15.0f  // normalisation divisor — PRD §14

    // ── Session Constants ──────────────────────────────────────────
    const val SESSION_FRAME_BATCH_SIZE     = 30     // batch write size for SessionFrameEntity
    const val MAX_SESSION_DURATION_MINUTES = 30     // auto-close after 30 min

    // ── MediaPipe Configuration (FR-HT-01) ─────────────────────────
    const val MEDIAPIPE_NUM_HANDS              = 2
    const val MEDIAPIPE_DETECTION_CONFIDENCE   = 0.7f
    const val MEDIAPIPE_PRESENCE_CONFIDENCE    = 0.7f
    const val MEDIAPIPE_TRACKING_CONFIDENCE    = 0.7f

    // ── SharedFlow Buffer (§4.2) ───────────────────────────────────
    const val SHARED_FLOW_BUFFER = 300

    // ── Backend / Network ──────────────────────────────────────────
    const val JWT_EXPIRY_HOURS      = 24
    const val RATE_LIMIT_MAX        = 100
    const val RATE_LIMIT_WINDOW_MS  = 900_000   // 15 minutes
}
