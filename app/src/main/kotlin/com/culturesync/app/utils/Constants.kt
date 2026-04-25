package com.culturesync.app.utils

object Constants {
    // Backend API — dynamically provided via BuildConfig (debug vs release)
    val BASE_URL = com.culturesync.app.BuildConfig.BASE_URL

    const val CONNECT_TIMEOUT_SEC = 15L
    const val READ_TIMEOUT_SEC    = 30L

    // Technique keys — must match assets/templates/<id>.json filenames
    const val TECHNIQUE_BASIC   = "beeralu_basic"
    const val TECHNIQUE_CROSS   = "beeralu_cross"
    const val TECHNIQUE_TWIST   = "beeralu_twist"
    const val TECHNIQUE_DIAMOND = "beeralu_diamond"
    const val TECHNIQUE_FLOWER  = "beeralu_flower"
    const val TECHNIQUE_WAVE    = "beeralu_wave"

    // Accuracy label thresholds (0–100 integer percentage scale)
    // AR overlay colour thresholds (0.85/0.65) live in ARConstants as floats.
    // These integer values are used by context-free toAccuracyLabel() and unit tests.
    const val ACCURACY_EXCELLENT = 85   // >= 85% → "Excellent"
    const val ACCURACY_GOOD      = 70   // >= 70% → "Good"
    const val ACCURACY_FAIR      = 50   // >= 50% → "Keep Practising"

    // DTW pipeline frame constants (mirrors ARConstants for use in non-Android test contexts)
    const val DTW_COMPARE_INTERVAL_FRAMES = 30  // run DTW every N frames (~1 s at 30 FPS)
    const val MIN_FRAMES_FOR_DTW          = 10  // minimum learner buffer depth before first comparison
    const val MAX_BUFFER_FRAMES           = 60  // maximum frames retained in the learner window
}
