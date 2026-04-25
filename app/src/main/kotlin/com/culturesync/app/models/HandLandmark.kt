package com.culturesync.app.models

data class HandLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val landmarkIndex: Int = 0
)

data class HandFrame(
    val landmarks: List<HandLandmark>,
    val handedness: String, // "Left" or "Right"
    val timestamp: Long = System.currentTimeMillis()
)

/** MediaPipe 21-landmark index constants */
object LandmarkIndex {
    const val WRIST        = 0
    const val THUMB_CMC    = 1;  const val THUMB_MCP   = 2;  const val THUMB_IP    = 3;  const val THUMB_TIP   = 4
    const val INDEX_MCP    = 5;  const val INDEX_PIP   = 6;  const val INDEX_DIP   = 7;  const val INDEX_TIP   = 8
    const val MIDDLE_MCP   = 9;  const val MIDDLE_PIP  = 10; const val MIDDLE_DIP  = 11; const val MIDDLE_TIP  = 12
    const val RING_MCP     = 13; const val RING_PIP    = 14; const val RING_DIP    = 15; const val RING_TIP    = 16
    const val PINKY_MCP    = 17; const val PINKY_PIP   = 18; const val PINKY_DIP   = 19; const val PINKY_TIP   = 20
}
