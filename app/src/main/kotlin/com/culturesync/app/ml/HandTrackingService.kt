package com.culturesync.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.culturesync.app.models.HandFrame
import com.culturesync.app.models.HandLandmark
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Manages MediaPipe Hand Landmarker in LIVE_STREAM mode (FR-HT-01 – FR-HT-05).
 *
 * Configuration per PRD:
 *  - numHands = 2
 *  - minHandDetectionConfidence = 0.7f
 *  - minHandPresenceConfidence  = 0.7f
 *  - minTrackingConfidence      = 0.7f
 *  - CPU delegate by default (FR-HT-03)
 *  - Model from assets/models/hand_landmarker.task (FR-HT-02)
 *
 * Results are emitted via [handFrameFlow] as Pair(leftHand, rightHand).
 * SharedFlow buffer capacity = 300 (ARConstants.SHARED_FLOW_BUFFER) per §4.2.
 */
class HandTrackingService(private val context: Context) {

    private var handLandmarker: HandLandmarker? = null

    private val _handFrameFlow = MutableSharedFlow<Pair<HandFrame?, HandFrame?>>(
        replay = 0,
        extraBufferCapacity = ARConstants.SHARED_FLOW_BUFFER
    )
    val handFrameFlow: SharedFlow<Pair<HandFrame?, HandFrame?>> = _handFrameFlow

    fun initialize() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("models/hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(ARConstants.MEDIAPIPE_NUM_HANDS)
            .setMinHandDetectionConfidence(ARConstants.MEDIAPIPE_DETECTION_CONFIDENCE)
            .setMinHandPresenceConfidence(ARConstants.MEDIAPIPE_PRESENCE_CONFIDENCE)
            .setMinTrackingConfidence(ARConstants.MEDIAPIPE_TRACKING_CONFIDENCE)
            .setResultListener { result, _ -> processResult(result) }
            .setErrorListener { error ->
                // FR-HT-05: Dropped frames logged but must not crash
                android.util.Log.e("HandTracking", "MediaPipe error", error)
            }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    /** Call this for each camera frame (runs on camera executor thread). */
    fun processFrame(bitmap: Bitmap) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        handLandmarker?.detectAsync(mpImage, SystemClock.uptimeMillis())
    }

    private fun processResult(result: HandLandmarkerResult) {
        var leftHand: HandFrame? = null
        var rightHand: HandFrame? = null

        result.landmarks().forEachIndexed { index, landmarks ->
            if (landmarks.size < 21) return@forEachIndexed
            val handedness = result.handednesses().getOrNull(index)
                ?.firstOrNull()?.categoryName() ?: "Right"
            val frame = HandFrame(
                landmarks = landmarks.mapIndexed { i, lm ->
                    HandLandmark(lm.x(), lm.y(), lm.z(), i)
                },
                handedness = handedness
            )
            if (handedness == "Left") leftHand = frame else rightHand = frame
        }

        // FR-HT-05: tryEmit returns false on queue saturation — logged, not crashed
        if (!_handFrameFlow.tryEmit(Pair(leftHand, rightHand))) {
            android.util.Log.w("HandTracking", "SharedFlow buffer full, frame dropped")
        }
    }

    fun close() {
        handLandmarker?.close()
        handLandmarker = null
    }
}
