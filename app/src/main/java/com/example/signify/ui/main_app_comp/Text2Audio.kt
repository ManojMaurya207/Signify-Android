package com.example.signify.ui.main_app_comp


import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Text2Audio(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("Hi I am Manoj, How are you ?") }
    val textToSpeech = remember { mutableStateOf<TextToSpeech?>(null) }
    var availableVoices by remember { mutableStateOf<List<Voice>>(emptyList()) }
    var selectedVoice by remember { mutableStateOf<Voice?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) } // Track dropdown state

    // Initialize TextToSpeech
    LaunchedEffect(Unit) {
        textToSpeech.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.value?.language = Locale.US
                availableVoices = textToSpeech.value?.voices?.toList() ?: emptyList()
                selectedVoice = availableVoices.firstOrNull()
            }
        }
    }

    // Cleanup TTS when not needed
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech.value?.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { speakText(textToSpeech.value, text, selectedVoice) }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Male voice = hi-in-x-hid-local or hi-in-x-hie-local or en-in-x-end-local
        // Female voice = hi-in-x-hia-local

        // **FIXED DROPDOWN MENU**
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedVoice?.name ?: "Select a voice",
                onValueChange = {},
                readOnly = true,
                label = { Text("Choose Voice") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .menuAnchor() // Fix: Attach to ExposedDropdownMenuBox
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                availableVoices.forEach { voice ->
                    DropdownMenuItem(
                        text = { Text(voice.name) },
                        onClick = {
                            selectedVoice = voice
                            isDropdownExpanded = false // Close dropdown on selection
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { speakText(textToSpeech.value, text, selectedVoice) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Convert to Speech")
        }
    }
}

fun speakText(tts: TextToSpeech?, text: String, voice: Voice?) {
    if (!text.isBlank() && tts != null) {
        voice?.let { tts.voice = it }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
