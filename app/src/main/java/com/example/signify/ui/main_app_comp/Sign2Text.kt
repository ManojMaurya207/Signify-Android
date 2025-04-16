package com.example.signify.ui.main_app_comp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.signify.ui.utils.GestureClassifier
import com.example.signify.ui.utils.HandLandmarkerHelper
import com.example.signify.ui.utils.VoicePreference
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun Sign2Text(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    var recognizedText by remember { mutableStateOf("") } // append prediction here
    val textToSpeech = remember { mutableStateOf<TextToSpeech?>(null) }
    var availableVoices by remember { mutableStateOf<List<Voice>>(emptyList()) }
    var selectedVoice by remember { mutableStateOf<Voice?>(null) }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(Unit) {
        val savedVoiceName = VoicePreference.getSavedVoiceName(context)
        tts.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                availableVoices = tts.value?.voices?.toList() ?: emptyList()
                selectedVoice = availableVoices.firstOrNull { it.name == savedVoiceName }
                    ?: availableVoices.firstOrNull()
                tts.value?.voice = selectedVoice
            }
        }
    }

    var lastPrediction by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        textToSpeech.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.value?.language = Locale.US
                availableVoices = textToSpeech.value?.voices?.toList() ?: emptyList()
                selectedVoice = availableVoices.firstOrNull()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            textToSpeech.value?.shutdown()
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val previewView = remember { PreviewView(context) }
    val clipboardManager = LocalClipboardManager.current
    val HAND_CONNECTIONS = remember {
        listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 4,         // Thumb
            0 to 5, 5 to 6, 6 to 7, 7 to 8,         // Index
            0 to 9, 9 to 10, 10 to 11, 11 to 12,    // Middle
            0 to 13, 13 to 14, 14 to 15, 15 to 16,  // Ring
            0 to 17, 17 to 18, 18 to 19, 19 to 20   // Pinky
        )
    }
    // Define different colors for left and right hands.
    val leftHandColor = Color.Blue
    val rightHandColor = Color.Green
    val predictionHistory = remember { mutableStateListOf<String>() }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var isCameraLoading by remember { mutableStateOf(true) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    var detectedText by remember { mutableStateOf("Waiting for hand sign...") }

    val landmarksList = remember { mutableStateListOf<List<NormalizedLandmark>>() }
    val handednessList = remember { mutableStateListOf<String>() }

    val gestureClassifier = remember { GestureClassifier(context).apply { loadModel() } }
    val handLandmarkerHelper = remember {
        HandLandmarkerHelper(
            context = context,
            runningMode = RunningMode.LIVE_STREAM,
            handLandmarkerHelperListener = object : HandLandmarkerHelper.LandmarkerListener {
                override fun onResults(result: HandLandmarkerResult, input: Bitmap, inferenceTime: Long) {
                    if (result.landmarks().isNotEmpty()) {
                        landmarksList.clear()
                        landmarksList.addAll(result.landmarks())
                        handednessList.clear()
                        handednessList.addAll(result.handedness().map { it[0].categoryName() })

                        val firstLandmarks = landmarksList[0]
                        val baseX = firstLandmarks[0].x()
                        val baseY = firstLandmarks[0].y()

                        // Normalize input for model
                        val normalizedInput = firstLandmarks.flatMap {
                            listOf(it.x() - baseX, it.y() - baseY)
                        }.toFloatArray()

                        // Predict
                        val prediction = gestureClassifier.predict(normalizedInput)

                        // Update prediction history
                        if (predictionHistory.size >= 5) {
                            predictionHistory.removeAt(0)
                        }
                        predictionHistory.add(prediction)

                        // Compute stable prediction
                        val stablePrediction = predictionHistory
                            .groupingBy { it }
                            .eachCount()
                            .maxByOrNull { it.value }
                            ?.key ?: "..."

                        // Append only if it's new, valid, and not uncertain
                        if (
                            stablePrediction != lastPrediction &&
                            stablePrediction != "Uncertain" &&
                            stablePrediction != "..." &&
                            stablePrediction.length == 1
                        ) {
                            recognizedText += stablePrediction
                            lastPrediction = stablePrediction
                        }
                        detectedText = "Predicted: $stablePrediction "
                        Log.d("GestureClassifier", "Recognized Text: $recognizedText")
                    } else {
                        landmarksList.clear()
                        handednessList.clear()
                        detectedText = "Waiting for hand sign..."
                    }
                }

                override fun onError(error: String, errorCode: Int) {
                    Log.e("HandLandmarker", "Error: $error")
                }
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
        if (!it) showPermissionDialog = true
    }

    fun bindCamera() = coroutineScope.launch {
        isCameraLoading = true
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            val preview = Preview.Builder().setTargetResolution(Size(640, 480)).build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysisUseCase = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        previewView.bitmap?.let {
                            handLandmarkerHelper.detectLiveStream(it, SystemClock.uptimeMillis())
                        }
                        imageProxy.close()
                    }
                }
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysisUseCase)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isCameraLoading = false
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    LaunchedEffect(lensFacing) {
        if (hasCameraPermission) bindCamera()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (hasCameraPermission) {
            // 📸 Camera Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier.height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

                    // 🎯 Landmark Overlay
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        landmarksList.forEachIndexed { index, handLandmarks ->
                            val handType = handednessList.getOrNull(index) ?: "Unknown"
                            val handColor = if (handType.equals("Left", ignoreCase = true)) leftHandColor else rightHandColor

                            HAND_CONNECTIONS.forEach { (start, end) ->
                                val startL = handLandmarks.getOrNull(start)
                                val endL = handLandmarks.getOrNull(end)
                                if (startL != null && endL != null) {
                                    drawLine(
                                        color = handColor,
                                        start = Offset(startL.x() * canvasWidth, startL.y() * canvasHeight),
                                        end = Offset(endL.x() * canvasWidth, endL.y() * canvasHeight),
                                        strokeWidth = 4f
                                    )
                                }
                            }

                            handLandmarks.forEachIndexed { i, landmark ->
                                drawCircle(
                                    color = handColor,
                                    radius = 6f,
                                    center = Offset(landmark.x() * canvasWidth, landmark.y() * canvasHeight)
                                )
                                drawContext.canvas.nativeCanvas.drawText(
                                    "$i",
                                    landmark.x() * canvasWidth,
                                    landmark.y() * canvasHeight,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.YELLOW
                                        textSize = 28f
                                        isAntiAlias = true
                                    }
                                )
                            }
                        }
                    }

                    // 🔁 Flip Camera Button
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                                CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Flip Camera",
                            tint = Color.White
                        )
                    }

                    if (isCameraLoading) {
                        CircularProgressIndicator()
                    }
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Camera permission is required to use this feature.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }

        // 📝 Detected Sign Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = detectedText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            clipboardManager.setText(AnnotatedString(detectedText))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionContainer {
                        OutlinedTextField(
                            value = recognizedText,
                            onValueChange = { recognizedText = it },
                            label = { Text("Recognized Text") },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // 🔊 Convert to Speech Button
        Button(
            onClick = {selectedVoice?.let { tts.value?.voice = it }
                tts.value?.speak(
                    recognizedText,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text("Convert to Speech")
        }

        // 🚫 Permission Dialog
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Permission Required") },
                text = { Text("This app requires camera access to function properly. Please grant permission in settings.") },
                confirmButton = {
                    Button(onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text("Grant Permission")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
