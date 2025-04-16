package com.example.signify.ui.screens

import ProfilePage
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.signify.ui.utils.VoicePreference
import com.example.signify.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun SettingPage(
    modifier: Modifier = Modifier,
    authNavController: NavController,
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    var showDialog by remember { mutableStateOf(false) }
    var showRatingSheet by remember { mutableStateOf(false) }
    var userRating by remember { mutableStateOf(0f) }

    // 🧠 TTS + Voice Setup
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    var availableVoices by remember { mutableStateOf<List<Voice>>(emptyList()) }
    var selectedVoice by remember { mutableStateOf<Voice?>(null) }
    var showVoiceDropdown by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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


    DisposableEffect(Unit) {
        onDispose {
            tts.value?.shutdown()
        }
    }

    // Load current rating from Firestore
    LaunchedEffect(userId) {
        fetchUserRating(firestore, userId) { rating -> userRating = rating }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ProfilePage(authViewModel = authViewModel, navController = authNavController)

        HorizontalDivider(modifier = Modifier.height(5.dp))

        // 🔧 Preferences
        SettingSection("Preferences") {

            ToggleSetting(
                title = "Dark Mode",
                icon = Icons.Default.Brightness4,
                isChecked = isDarkTheme,
                onCheckedChange = { onToggleTheme() }
            )

            // 🔊 Voice Selector
            Box {
                SettingOption(
                    title = "Voice: ${selectedVoice?.locale?.displayLanguage ?: "Select"}",
                    icon = Icons.Default.RecordVoiceOver,
                    onClick = { showVoiceDropdown = true }
                )

                DropdownMenu(
                    expanded = showVoiceDropdown,
                    onDismissRequest = { showVoiceDropdown = false }
                ) {
                    availableVoices
                        .filter { voice ->
                            val lang = voice.locale.language
                            lang == "en" || lang == "hi" || lang == "ja" || lang == "pa"
                        }
                        .sortedWith(compareBy<Voice> {
                            // Group females first based on name (voice names ending in "f" or "female-like" tags)
                            when {
                                it.name.contains("f", ignoreCase = true) -> 0
                                it.name.contains("m", ignoreCase = true) -> 1
                                else -> 2
                            }
                        }.thenBy { it.locale.displayLanguage }) // Then by language alphabetically
                        .forEach { voice ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${voice.locale.displayLanguage} - ${voice.name}" +
                                                if (voice.name.contains("f", true)) " (Female)"
                                                else if (voice.name.contains("m", true)) " (Male)"
                                                else ""
                                    )
                                },
                                onClick = {
                                    selectedVoice = voice
                                    showVoiceDropdown = false
                                    tts.value?.voice = voice
                                    coroutineScope.launch {
                                        VoicePreference.saveVoiceName(context, voice.name)
                                    }

                                    selectedVoice?.let { tts.value?.voice = it }
                                    tts.value?.speak(
                                        "Hello, this is your selected voice in Signify.",
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        null
                                    )
                                }
                            )
                        }
                }

            }

//            // 🔊 Preview Voice
//            Button(
//                onClick = {
//                    selectedVoice?.let { tts.value?.voice = it }
//                    tts.value?.speak(
//                        "Hello, this is your selected voice in Signify.",
//                        TextToSpeech.QUEUE_FLUSH,
//                        null,
//                        null
//                    )
//                },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("🔊 Preview Voice")
//            }
        }

        // ℹ️ About Section
        SettingSection("About") {
            SettingOption("App Info", Icons.Default.Info) { showDialog = true }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        Button(
                            onClick = { showDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("OK", style = MaterialTheme.typography.bodyLarge)
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("About Signify", style = MaterialTheme.typography.titleLarge)
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Signify - Version 1.0.0", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Developed by Manoj Maurya", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Signify helps bridge the communication gap by providing instant sign language translation and learning resources.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
            }

            SettingOption("Rate Us", Icons.Default.Star) { showRatingSheet = true }
            if (showRatingSheet) {
                RatingBottomSheet(
                    userId = userId,
                    firestore = firestore,
                    onRatingSubmit = { rating, comment ->
                        submitUserReview(firestore, userId, rating, comment, context)
                    },
                    onDismiss = { showRatingSheet = false }
                )
            }
        }

        // 👤 Account Section
        SettingSection("Account") {
            var showLogoutDialog by remember { mutableStateOf(false) }

            SettingOption("Logout", Icons.Default.ExitToApp) {
                showLogoutDialog = true
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Confirm Logout") },
                    text = { Text("Are you sure you want to log out?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                authViewModel.signout()
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                showLogoutDialog = false
                            }
                        ) {
                            Text("Logout")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}



@Composable
fun ToggleSetting(title: String, icon: ImageVector, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onCheckedChange(!isChecked) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = isChecked, onCheckedChange = onCheckedChange, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun SettingOption(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}


@Composable
fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray))
        content()
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    userId: String,
    firestore: FirebaseFirestore,
    onRatingSubmit: (Float, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        firestore.collection("reviews")
            .whereEqualTo("userid", userId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val review = documents.documents.first()
                    rating = review.getDouble("rating")?.toFloat() ?: 0f
                    comment = review.getString("comment") ?: ""
                }
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How was your experience?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your feedback helps us improve!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Star Rating UI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..5) {
                    val starColor = if (i <= rating) Color(0xFFFFC107) else Color(0xFFDADADA)
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star $i",
                        tint = starColor,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { rating = i.toFloat() }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comment Box
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Add a comment (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        onRatingSubmit(rating, comment)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text(text = "Submit Feedback", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// Function to fetch user rating from Firestore
private fun fetchUserRating(firestore: FirebaseFirestore, userId: String, onResult: (Float) -> Unit) {
    firestore.collection("reviews")
        .whereEqualTo("userid", userId)
        .limit(1)
        .get()
        .addOnSuccessListener { documents ->
            val rating = documents.documents.firstOrNull()?.getDouble("rating")?.toFloat() ?: 0f
            onResult(rating)
        }
}

// Function to submit/update user review in Firestore
private fun submitUserReview(
    firestore: FirebaseFirestore,
    userId: String,
    rating: Float,
    comment: String,
    context: android.content.Context
) {
    firestore.collection("reviews")
        .whereEqualTo("userid", userId)
        .limit(1)
        .get()
        .addOnSuccessListener { documents ->
            val reviewRef = documents.documents.firstOrNull()?.reference ?: firestore.collection("reviews").document()
            val reviewData = mapOf(
                "userid" to userId,
                "comment" to comment,
                "time_stamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "rating" to rating
            )

            reviewRef.set(reviewData)
                .addOnSuccessListener { Toast.makeText(context, "Thanks for your feedback!", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { Toast.makeText(context, "Failed to save review.", Toast.LENGTH_SHORT).show() }
        }
}
