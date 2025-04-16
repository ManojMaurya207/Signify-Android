package com.example.signify.ui.utils

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class HandLandmarkerHelper(
    var minHandDetectionConfidence: Float = DEFAULT_HAND_DETECTION_CONFIDENCE,
    var minHandTrackingConfidence: Float = DEFAULT_HAND_TRACKING_CONFIDENCE,
    var minHandPresenceConfidence: Float = DEFAULT_HAND_PRESENCE_CONFIDENCE,
    var maxNumHands: Int = DEFAULT_NUM_HANDS,
    var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    val handLandmarkerHelperListener: LandmarkerListener? = null
) {
    private var handLandmarker: HandLandmarker? = null
    private val backgroundExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        try {
            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath(MODEL_FILE)

            // Set delegate
            when (currentDelegate) {
                DELEGATE_CPU -> baseOptionsBuilder.setDelegate(Delegate.CPU)
                DELEGATE_GPU -> baseOptionsBuilder.setDelegate(Delegate.GPU)
            }

            val baseOptions = baseOptionsBuilder.build()

            val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(minHandDetectionConfidence)
                .setMinTrackingConfidence(minHandTrackingConfidence)
                .setMinHandPresenceConfidence(minHandPresenceConfidence)
                .setNumHands(maxNumHands)
                .setRunningMode(runningMode)

            // If live stream, set listeners
            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            val options = optionsBuilder.build()

            handLandmarker = HandLandmarker.createFromOptions(context, options)

        } catch (e: Exception) {
            handLandmarkerHelperListener?.onError("HandLandmarker failed to initialize: ${e.message}", ERROR_INIT)
        }
    }

    private var latestBitmap: Bitmap? = null

    fun detectLiveStream(bitmap: Bitmap, frameTime: Long) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw IllegalArgumentException("Live stream mode is not enabled")
        }

        latestBitmap = bitmap  // Save original bitmap
        val mpImage = BitmapImageBuilder(bitmap).build()
        try {
            handLandmarker?.detectAsync(mpImage, frameTime)
        } catch (e: Exception) {
            handLandmarkerHelperListener?.onError("Failed to run detection: ${e.message}", ERROR_INFERENCE)
        }
    }

    private fun returnLivestreamResult(result: HandLandmarkerResult, input: MPImage) {
        backgroundExecutor.execute {
            latestBitmap?.let { bitmap ->
                handLandmarkerHelperListener?.onResults(result, bitmap, System.currentTimeMillis())
            }
        }
    }

    private fun returnLivestreamError(error: RuntimeException) {
        handLandmarkerHelperListener?.onError("LiveStream error: ${error.message}", ERROR_INFERENCE)
    }

    fun clear() {
        handLandmarker?.close()
        handLandmarker = null
    }

    interface LandmarkerListener {
        fun onResults(result: HandLandmarkerResult, input: Bitmap, inferenceTime: Long)
        fun onError(error: String, errorCode: Int)
    }

    companion object {
        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5f
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5f
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5f
        const val DEFAULT_NUM_HANDS = 1
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val MODEL_FILE = "hand_landmarker.task"

        const val ERROR_INIT = 1001
        const val ERROR_INFERENCE = 1002
    }

    // Converts a list of NormalizedLandmark objects (each with x, y, z) into a FloatArray.
    fun List<NormalizedLandmark>.toFloatArray(): FloatArray {
        val output = FloatArray(this.size * 3)
        this.forEachIndexed { i, landmark ->
            output[i * 3] = landmark.x()
            output[i * 3 + 1] = landmark.y()
            output[i * 3 + 2] = landmark.z()
        }
        return output
    }
}