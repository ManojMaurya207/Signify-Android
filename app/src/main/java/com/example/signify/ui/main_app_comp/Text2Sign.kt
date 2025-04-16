package com.example.signify.ui.main_app_comp

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signify.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Text2Sign(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    var text by remember { mutableStateOf("Hi") }
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()
    var isAnimating by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var animationSpeed by remember { mutableStateOf(1f) } // Default speed is 1x

    val validChars = text.lowercase().filter { it.isLetterOrDigit() || it == ' ' }
    val defaultImageRes = getResourceIdForLetter(context, "space") ?: R.drawable.space

    fun startAnimation() {
        if (isAnimating) return
        coroutineScope.launch {
            isAnimating = true
            for (i in validChars.indices) {
                currentIndex = i
                delay((500 / animationSpeed).toLong()) // Adjust delay based on speed
            }
            delay((500 / animationSpeed).toLong())
            currentIndex = -1
            isAnimating = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            ,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Box {
                    Crossfade(targetState = currentIndex) { index ->
                        val imageRes = if (index in validChars.indices) {
                            getResourceIdForLetter(context, validChars[index].toString()) ?: defaultImageRes
                        } else {
                            defaultImageRes
                        }
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = "Sign Image",
                            modifier = Modifier
                                .size(350.dp)
                                .padding(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .shadow(4.dp, shape = CircleShape)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                            .clickable {
                                showSpeedDialog = true
                            }
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Flip Camera",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter text", color = Color(0xFFB0B0B0)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {}),
                modifier = Modifier.fillMaxWidth()
                    .padding(5.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color(0xFF6BCB77),
                    focusedBorderColor = Color(0xFF6BCB77),
                    unfocusedBorderColor = Color(0xFF757575)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { startAnimation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6BCB77))
            ) {
                Text("Convert to Sign", fontSize = 18.sp, color = Color.White)
            }
        }

        // Speed Control Dialog
        if (showSpeedDialog) {
            SpeedControlDialog(
                currentSpeed = animationSpeed,
                onSpeedChange = { animationSpeed = it },
                onDismiss = { showSpeedDialog = false }
            )
        }
    }
}

@Composable
fun SpeedControlDialog(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speedOptions = listOf(0.25f,0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f) // Snap points
    var selectedSpeed by remember { mutableStateOf(currentSpeed) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = {
                onSpeedChange(selectedSpeed)
                onDismiss()
            }) {
                Text("OK", color = Color(0xFF6BCB77))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel", color = Color(0xFFB0B0B0))
            }
        },
        title = { Text("Adjust Animation Speed", color = Color.White) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Speed: ${selectedSpeed}x",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Slider(
                    value = selectedSpeed,
                    onValueChange = { newSpeed ->
                        selectedSpeed = speedOptions.minByOrNull { kotlin.math.abs(it - newSpeed) } ?: 1f
                    },
                    valueRange = 0.25f..1.75f,
                    steps = speedOptions.size - 2,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF6BCB77),
                        activeTrackColor = Color(0xFF6BCB77)
                    )
                )
            }
        },
        containerColor = Color(0xFF252525)
    )
}

fun getResourceIdForLetter(context: Context, letter: String): Int? {
    val resourceName = when {
        letter == " " -> "space"
        letter in "0".."9" -> "a$letter"
        else -> letter
    }

    val resId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    return if (resId != 0) resId else null
}
