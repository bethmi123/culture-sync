package com.culturesync.app.ui.practice

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.culturesync.app.ml.ARConstants
import com.culturesync.app.models.HandFrame

/**
 * Transparent overlay drawn on top of the CameraX PreviewView (FR-AR-01).
 *
 * Renders the 21 MediaPipe hand landmarks and their connections with
 * green / yellow / red colour coding based on per-landmark DTW accuracy (FR-AR-03).
 *
 * Augmented with a Ghost/Guide overlay showing the matched expert frame (PRD §5.5).
 */
class AROverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var learnerFrame: HandFrame? = null
    private var expertFrame: HandFrame? = null
    private var accuracyMap: Map<Int, Float> = emptyMap()
    private var overallAccuracy: Float = 0f

    private val connections = listOf(
        0 to 1,  1 to 2,  2 to 3,  3 to 4,
        0 to 5,  5 to 6,  6 to 7,  7 to 8,
        0 to 9,  9 to 10, 10 to 11, 11 to 12,
        0 to 13, 13 to 14, 14 to 15, 15 to 16,
        0 to 17, 17 to 18, 18 to 19, 19 to 20,
        5 to 9,  9 to 13, 13 to 17
    )

    private val learnerLinePaint = Paint().apply {
        strokeWidth = ARConstants.LINE_STROKE_WIDTH
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val learnerDotPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val expertPaint = Paint().apply {
        color = Color.argb(100, 255, 255, 255) // Semi-transparent white ghost
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 52f
        isAntiAlias = true
        isFakeBoldText = true
    }

    private val promptPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        isAntiAlias = true
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val bgPaint = Paint().apply {
        color = Color.argb(150, 15, 23, 42) // brand_deep with alpha
        style = Paint.Style.FILL
    }

    /** Update with both learner and matched expert frames for ghost overlay. */
    fun updateOverlay(
        learner: HandFrame?,
        expert: HandFrame? = null,
        accuracy: Map<Int, Float> = emptyMap(),
        overall: Float = 0f
    ) {
        learnerFrame = learner
        expertFrame = expert
        accuracyMap = accuracy
        overallAccuracy = overall
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val lFrame = learnerFrame
        val eFrame = expertFrame

        if (lFrame == null || lFrame.landmarks.size < 21) {
            canvas.drawText(
                context.getString(com.culturesync.app.R.string.status_point_hand),
                width / 2f,
                height / 2f,
                promptPaint
            )
            return
        }

        // 1. Draw Expert "Ghost" Overlay (PRD §5.5)
        eFrame?.let { ghost ->
            connections.forEach { (a, b) ->
                val lmA = ghost.landmarks[a]
                val lmB = ghost.landmarks[b]
                canvas.drawLine(
                    lmA.x * width, lmA.y * height,
                    lmB.x * width, lmB.y * height,
                    expertPaint
                )
            }
        }

        // 2. Draw Learner Skeleton with Accuracy Colors (FR-AR-03, FR-AR-04)
        connections.forEach { (a, b) ->
            val lmA = lFrame.landmarks[a]
            val lmB = lFrame.landmarks[b]
            val avg = ((accuracyMap[a] ?: 0.5f) + (accuracyMap[b] ?: 0.5f)) / 2f
            learnerLinePaint.color = accuracyColor(avg)
            learnerLinePaint.alpha = ARConstants.OVERLAY_ALPHA  // must re-apply after color (FR-AR-04)
            canvas.drawLine(
                lmA.x * width, lmA.y * height,
                lmB.x * width, lmB.y * height,
                learnerLinePaint
            )
        }

        // 3. Draw Landmark Dots (FR-AR-04)
        lFrame.landmarks.forEachIndexed { idx, lm ->
            val acc = accuracyMap[idx] ?: 0.5f
            learnerDotPaint.color = accuracyColor(acc)
            learnerDotPaint.alpha = ARConstants.OVERLAY_ALPHA  // must re-apply after color (FR-AR-04)
            val r = if (idx in listOf(4, 8, 12, 16, 20)) ARConstants.DOT_RADIUS + 4f
                    else ARConstants.DOT_RADIUS
            canvas.drawCircle(lm.x * width, lm.y * height, r, learnerDotPaint)
        }

        // 4. Accuracy Badge
        if (overallAccuracy > 0f) {
            val label = "${overallAccuracy.toInt()}%"
            val textW = textPaint.measureText(label)
            val pad = 24f
            val rect = RectF(width - textW - pad * 2 - 24f, 24f, width - 24f, 100f)
            canvas.drawRoundRect(rect, 20f, 20f, bgPaint)
            textPaint.color = accuracyColor(overallAccuracy / 100f)
            canvas.drawText(label, rect.left + pad, rect.bottom - pad, textPaint)
        }
    }

    private fun accuracyColor(accuracy: Float) = when {
        accuracy >= ARConstants.GREEN_ACCURACY_THRESHOLD  -> ARConstants.COLOR_GREEN
        accuracy >= ARConstants.YELLOW_ACCURACY_THRESHOLD -> ARConstants.COLOR_YELLOW
        else                                              -> ARConstants.COLOR_RED
    }
}
