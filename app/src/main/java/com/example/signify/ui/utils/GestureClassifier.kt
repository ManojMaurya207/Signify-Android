package com.example.signify.ui.utils

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class GestureClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null

    // Load your gesture classifier TFLite model from the assets folder.
    fun loadModel() {
        try {
            val assetFileDescriptor: AssetFileDescriptor = context.assets.openFd("keypoint_classifier.tflite")
            val fileChannel: FileChannel = FileInputStream(assetFileDescriptor.fileDescriptor).channel
            val startOffset: Long = assetFileDescriptor.startOffset
            val declaredLength: Long = assetFileDescriptor.declaredLength
            val modelBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            interpreter = Interpreter(modelBuffer)

            // ✅ Now that interpreter is initialized, log input tensor shape
            val inputShape = interpreter!!.getInputTensor(0).shape()
            Log.d("TFLite", "Model input shape: ${inputShape.contentToString()}")
            // You can also log data type if needed
            Log.d("TFLite", "Model input type: ${interpreter!!.getInputTensor(0).dataType()}")

            val outputShape = interpreter!!.getOutputTensor(0).shape()
            Log.d("TFLite", "Model Output shape: ${outputShape.contentToString()}")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun predict(input: FloatArray): String {
        val output = Array(1) { FloatArray(NUM_CLASSES) }

        try {
            interpreter?.run(input, output)

            Log.d("GestureClassifier", "Input Vector: ${input.joinToString()}")
            Log.d("GestureClassifier", "Model Output: ${output[0].joinToString()}")


            val predictedIndex = output[0].indexOfMax()
            val confidence = output[0][predictedIndex]
            return if (confidence > 0.3f) {
                CLASS_LABELS.getOrElse(predictedIndex) { "Unknown" }
            } else {
                "Uncertain"
            }

        } catch (e: Exception) {
            Log.e("GestureClassifier", "Prediction error", e)
            return "Error"
        }
    }


    companion object {
        const val NUM_CLASSES = 26
        val CLASS_LABELS = ('A'..'Z').map { it.toString() } // Or your own labels

//        val CLASS_LABELS = listOf(
//            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
//            "k", "l", "m", "n", "o", "p", "q", "thumb_up", "victory"
//        )
//        const val NUM_CLASSES = 19
    }
}

// Extension function to find index of max value
fun FloatArray.indexOfMax(): Int {
    var maxIndex = 0
    for (i in indices) {
        if (this[i] > this[maxIndex]) {
            maxIndex = i
        }
    }
    return maxIndex
}
