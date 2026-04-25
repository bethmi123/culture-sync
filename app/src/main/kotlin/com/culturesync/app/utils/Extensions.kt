package com.culturesync.app.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import com.culturesync.app.ml.ARConstants
import java.text.SimpleDateFormat
import java.util.*

import com.culturesync.app.R

fun Context.toast(msg: String) =
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Context.toast(resId: Int) =
    Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show()

fun View.show()  { visibility = View.VISIBLE }
fun View.hide()  { visibility = View.GONE }
fun View.invis() { visibility = View.INVISIBLE }

/** Returns accuracy label string resource ID using ARConstants thresholds (for UI with localisation). */
fun Float.toAccuracyLabelRes(): Int = when {
    this >= ARConstants.GREEN_ACCURACY_THRESHOLD  -> R.string.accuracy_excellent
    this >= ARConstants.YELLOW_ACCURACY_THRESHOLD -> R.string.accuracy_good
    this >= 0.50f                                 -> R.string.accuracy_keep_practising
    else                                          -> R.string.accuracy_needs_work
}

/**
 * Context-free accuracy label for 0–100 float values.
 * Uses Constants integer thresholds (ACCURACY_EXCELLENT=85, ACCURACY_GOOD=70, ACCURACY_FAIR=50).
 * Used by unit tests and any context-free call sites.
 */
fun Float.toAccuracyLabel(): String = when {
    this >= com.culturesync.app.utils.Constants.ACCURACY_EXCELLENT -> "Excellent"
    this >= com.culturesync.app.utils.Constants.ACCURACY_GOOD      -> "Good"
    this >= com.culturesync.app.utils.Constants.ACCURACY_FAIR      -> "Keep Practising"
    else                                                           -> "Needs Work"
}

/** Context-aware accuracy label resolved from string resources. */
fun Float.toAccuracyLabel(context: Context): String = context.getString(this.toAccuracyLabelRes())

fun Long.toReadableDate(): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(this))

/** Context-free time formatter: "Xm Ys" or "Ys". */
fun Int.toTimeString(): String {
    val mins = this / 60
    val secs = this % 60
    return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
}

fun Float.toTimeString(): String = this.toInt().toTimeString()

/** Context-aware time formatter using localised "m"/"s" labels. */
fun Int.toTimeString(context: Context): String {
    val mins = this / 60
    val secs = this % 60
    val m = context.getString(R.string.label_m)
    val s = context.getString(R.string.label_s)
    return if (mins > 0) "${mins}$m ${secs}$s" else "${secs}$s"
}

fun Float.toTimeString(context: Context): String = this.toInt().toTimeString(context)
