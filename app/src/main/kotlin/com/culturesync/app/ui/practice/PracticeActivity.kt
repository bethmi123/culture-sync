package com.culturesync.app.ui.practice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.culturesync.app.ml.HandTrackingService
import com.culturesync.app.ar.ARCoreManager
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.local.entities.SessionEntity
import com.culturesync.app.data.local.entities.SessionFrameEntity
import com.culturesync.app.data.repository.SessionRepository
import com.culturesync.app.databinding.ActivityPracticeBinding
import com.culturesync.app.ml.DTWEngine
import com.culturesync.app.ml.TemplateManager
import com.culturesync.app.ml.ARConstants
import com.culturesync.app.ml.AccuracyResult
import com.culturesync.app.models.HandFrame
import com.culturesync.app.utils.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CORE SCREEN — AR practice with real-time DTW hand tracking (PRD Screen #10).
 *
 * Pipeline: CameraX → MediaPipe → SharedFlow → DTWEngine → AROverlayView
 * Session data: IN_PROGRESS → COMPLETED with per-frame batch writes (FR-SES-01 – FR-SES-05)
 * Auto-close after MAX_SESSION_DURATION_MINUTES (FR-SES-03)
 */
class PracticeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPracticeBinding
    private lateinit var handTracker: HandTrackingService
    private lateinit var arCore: ARCoreManager
    private lateinit var dtwEngine: DTWEngine
    private lateinit var templateManager: TemplateManager
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var sessionMgr: SessionManager
    private lateinit var sessionRepo: SessionRepository
    private val gson = Gson()

    private var techniqueId   = 1          // Room FK — default to first technique
    private var techniqueKey  = "beeralu_basic"
    private var techniqueName = "Basic Crossing"

    private val learnerBuffer = mutableListOf<HandFrame>()
    private val frameBatch    = mutableListOf<SessionFrameEntity>()
    private var frameCounter    = 0
    private var currentAccuracy: AccuracyResult = AccuracyResult.EMPTY
    private var currentExpertFrame: HandFrame? = null
    private var bestAccuracy    = 0f
    private var sessionStart    = 0L
    private var sessionId       = -1
    private var isPracticing    = false
    private var sessionTimer: CountDownTimer? = null

    // Camera permission via ActivityResultContracts (PRD §12)
    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else {
            Toast.makeText(this, getString(com.culturesync.app.R.string.err_camera_required), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        techniqueKey  = intent.getStringExtra("technique_key") ?: "beeralu_basic"
        techniqueId   = intent.getIntExtra("technique_id", 1)
        techniqueName = intent.getStringExtra("technique_name") ?: getString(com.culturesync.app.R.string.tech_beeralu_basic_name)

        sessionMgr    = SessionManager(this)
        sessionRepo   = SessionRepository(AppDatabase.getInstance(this))
        handTracker   = HandTrackingService(this)
        arCore        = ARCoreManager(this)
        dtwEngine     = DTWEngine()
        templateManager = TemplateManager(this)
        cameraExecutor  = Executors.newSingleThreadExecutor()

        binding.tvTechniqueName.text = techniqueName
        binding.tvAccuracy.text      = getString(com.culturesync.app.R.string.default_accuracy)
        binding.progressAccuracy.progress = 0
        binding.tvStatus.text        = getString(com.culturesync.app.R.string.status_point_hand)

        binding.btnStartStop.setOnClickListener { togglePractice() }
        binding.btnBack.setOnClickListener { finish() }

        if (!templateManager.isTemplateAvailable(techniqueKey)) {
            Toast.makeText(this, getString(com.culturesync.app.R.string.err_template_not_found), Toast.LENGTH_LONG).show()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            // Show rationale if user denied before (PRD §12)
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                AlertDialog.Builder(this)
                    .setTitle(getString(com.culturesync.app.R.string.title_camera_permission))
                    .setMessage(getString(com.culturesync.app.R.string.msg_camera_rationale))
                    .setPositiveButton(getString(com.culturesync.app.R.string.btn_approve)) { _, _ -> cameraPermission.launch(Manifest.permission.CAMERA) }
                    .setNegativeButton(getString(com.culturesync.app.R.string.btn_cancel)) { _, _ -> finish() }
                    .show()
            } else {
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        handTracker.initialize()
        arCore.createSession()

        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview  = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
            analysis.setAnalyzer(cameraExecutor) { proxy -> onFrame(proxy) }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))

        // Collect landmarks from MediaPipe SharedFlow (§4.2)
        lifecycleScope.launch {
            handTracker.handFrameFlow.collect { (left, right) ->
                val frame = right ?: left ?: run {
                    withContext(Dispatchers.Main) {
                        binding.arOverlay.updateOverlay(learner = null)
                    }
                    return@collect
                }
                if (isPracticing) {
                    learnerBuffer.add(frame)
                    if (learnerBuffer.size > ARConstants.SHARED_FLOW_BUFFER) learnerBuffer.removeFirst()
                    frameCounter++

                    // Run DTW every COMPUTE_INTERVAL_FRAMES (~1s at 30 FPS) (FR-DTW-04)
                    if (frameCounter % ARConstants.DTW_COMPUTE_INTERVAL_FRAMES == 0 &&
                        learnerBuffer.size >= ARConstants.DTW_FRAME_WINDOW / 4) {
                        runDTW()
                    }

                    // Batch-write frame data (FR-SES-02)
                    if (sessionId > 0) {
                        val frameEntity = SessionFrameEntity(
                            sessionId = sessionId,
                            frameIndex = frameCounter,
                            timestampMs = System.currentTimeMillis(),
                            overallAccuracy = currentAccuracy.overallAccuracy,
                            perJointAccuracyJson = gson.toJson(currentAccuracy.perJointAccuracy)
                        )
                        frameBatch.add(frameEntity)
                        if (frameBatch.size >= ARConstants.SESSION_FRAME_BATCH_SIZE) {
                            val batch = frameBatch.toList()
                            frameBatch.clear()
                            lifecycleScope.launch(Dispatchers.IO) {
                                sessionRepo.saveFrameBatch(batch)
                            }
                        }
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
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun onFrame(proxy: ImageProxy) {
        val bmp = proxy.toBitmap()
        handTracker.processFrame(bmp)
        proxy.close()
    }

    private fun runDTW() {
        val template = templateManager.loadTemplate(techniqueKey) ?: return
        lifecycleScope.launch(Dispatchers.Default) {
            val snapshot = learnerBuffer.takeLast(ARConstants.DTW_FRAME_WINDOW).toList()
            val result = dtwEngine.compare(snapshot, template.frames)
            currentAccuracy = result
            currentExpertFrame = if (result.matchedExpertFrameIndex >= 0) 
                template.frames[result.matchedExpertFrameIndex] else null
            
            if (result.overallAccuracy > bestAccuracy) bestAccuracy = result.overallAccuracy

            withContext(Dispatchers.Main) {
                binding.tvAccuracy.text        = "${result.overallPercent.toInt()}%"
                binding.progressAccuracy.progress = result.overallPercent.toInt()
                binding.tvStatus.text = when {
                    result.overallAccuracy >= ARConstants.GREEN_ACCURACY_THRESHOLD  -> getString(com.culturesync.app.R.string.accuracy_excellent)
                    result.overallAccuracy >= ARConstants.YELLOW_ACCURACY_THRESHOLD -> getString(com.culturesync.app.R.string.accuracy_good)
                    result.overallAccuracy >= 0.50f                                -> getString(com.culturesync.app.R.string.accuracy_average)
                    else                                                           -> getString(com.culturesync.app.R.string.accuracy_low)
                }
            }
        }
    }

    private fun togglePractice() {
        if (!isPracticing) {
            // FR-SES-01: Create IN_PROGRESS session row before showing camera
            isPracticing   = true
            sessionStart   = System.currentTimeMillis()
            frameCounter   = 0
            bestAccuracy   = 0f
            currentAccuracy = AccuracyResult.EMPTY
            learnerBuffer.clear()
            frameBatch.clear()

            lifecycleScope.launch(Dispatchers.IO) {
                val entity = SessionEntity(
                    userId = sessionMgr.getUserId(),
                    techniqueId = techniqueId,
                    startTime = sessionStart,
                    status = "IN_PROGRESS"
                )
                sessionId = sessionRepo.createSession(entity).toInt()
            }

            binding.btnStartStop.text = getString(com.culturesync.app.R.string.label_stop)
            binding.tvStatus.text     = getString(com.culturesync.app.R.string.status_practising)

            // FR-SES-03: Auto-close after MAX_SESSION_DURATION_MINUTES
            sessionTimer = object : CountDownTimer(
                ARConstants.MAX_SESSION_DURATION_MINUTES * 60 * 1000L,
                1000L
            ) {
                override fun onTick(remaining: Long) {
                    val elapsed = (ARConstants.MAX_SESSION_DURATION_MINUTES * 60 * 1000L - remaining) / 1000
                    val min = elapsed / 60
                    val sec = elapsed % 60
                    binding.tvTimer?.text = String.format("%02d:%02d", min, sec)
                }
                override fun onFinish() {
                    if (isPracticing) togglePractice() // auto-close
                }
            }.start()

        } else {
            isPracticing = false
            sessionTimer?.cancel()
            sessionTimer = null
            binding.btnStartStop.text = getString(com.culturesync.app.R.string.label_start)
            closeSession()
        }
    }

    private fun closeSession() {
        val endTime  = System.currentTimeMillis()
        val duration = ((endTime - sessionStart) / 1000).toInt()

        // Flush remaining frame batch
        if (frameBatch.isNotEmpty() && sessionId > 0) {
            val remaining = frameBatch.toList()
            frameBatch.clear()
            lifecycleScope.launch(Dispatchers.IO) {
                sessionRepo.saveFrameBatch(remaining)
            }
        }

        // FR-SES-03: Close session with final accuracy = mean of all frame accuracies
        if (sessionId > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                val avgAccuracy = sessionRepo.getAverageFrameAccuracy(sessionId)
                    .let { if (it > 0f) it else currentAccuracy.overallAccuracy }
                val bestFrame = sessionRepo.getBestFrameAccuracy(sessionId)
                    .let { if (it > 0f) it else bestAccuracy }

                sessionRepo.closeSession(sessionId, endTime, duration, avgAccuracy, bestFrame)

                withContext(Dispatchers.Main) {
                    startActivity(
                        Intent(this@PracticeActivity, SessionResultActivity::class.java).apply {
                            putExtra("accuracy",       avgAccuracy * 100f)
                            putExtra("duration",       duration)
                            putExtra("technique_name", techniqueName)
                            putExtra("frame_count",    frameCounter)
                            putExtra("best_accuracy",  bestFrame * 100f)
                            putExtra("session_id",     sessionId)
                        }
                    )
                }
            }
        }
    }

    override fun onResume()  { super.onResume();  arCore.onResume() }
    override fun onPause()   { super.onPause();   arCore.onPause() }
    override fun onDestroy() {
        super.onDestroy()
        sessionTimer?.cancel()
        arCore.onDestroy()
        handTracker.close()
        cameraExecutor.shutdown()
    }
}
