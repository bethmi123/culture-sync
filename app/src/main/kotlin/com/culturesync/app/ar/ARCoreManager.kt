package com.culturesync.app.ar

import android.content.Context
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*

/**
 * Wraps the ARCore session lifecycle.
 * Used alongside CameraX — ARCore provides tracking/depth, not the camera feed.
 */
class ARCoreManager(private val context: Context) {

    private var session: Session? = null

    fun checkAvailability(): ArCoreApk.Availability =
        ArCoreApk.getInstance().checkAvailability(context)

    fun isSupported(): Boolean {
        val availability = checkAvailability()
        return availability == ArCoreApk.Availability.SUPPORTED_INSTALLED ||
               availability == ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD ||
               availability == ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED
    }

    fun createSession(): Session? {
        return try {
            session = Session(context)
            val config = Config(session).apply {
                focusMode = Config.FocusMode.AUTO
                lightEstimationMode = Config.LightEstimationMode.DISABLED
                planeFindingMode = Config.PlaneFindingMode.DISABLED  // not needed for hand AR
            }
            session?.configure(config)
            session
        } catch (e: UnavailableArcoreNotInstalledException) {
            null
        } catch (e: UnavailableApkTooOldException) {
            null
        } catch (e: UnavailableSdkTooOldException) {
            null
        } catch (e: UnavailableDeviceNotCompatibleException) {
            null
        }
    }

    fun onResume()  { try { session?.resume()  } catch (_: Exception) {} }
    fun onPause()   { session?.pause() }
    fun onDestroy() { session?.close(); session = null }
}
