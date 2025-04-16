package com.example.signify.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.signify.ui.main_app_comp.MaintenanceMessage
import com.example.signify.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LearnPage(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    navController: NavController
) {
    val firestore = FirebaseFirestore.getInstance()

    val videoIds = listOf(
        "qcdivQfA41Y",
        "DBQINq0SsAw",
        "CGqXy3JOZRs",
        "NUMx7Iva6GQ"
    )

    val trendingCourses = listOf(
        "Basic Sign Language for Everyday Conversations",
        "Advanced ASL: Expressing Emotions & Complex Ideas",
        "Sign Language Interpretation: Bridging the Communication Gap"
    )

    var lessonList by remember { mutableStateOf<List<LessonMeta>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        try {
            val snapshot = firestore.collection("lessons").get().await()
            val fetchedLessons = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("lessonTitle")
                if (title != null) LessonMeta(id = doc.id, title = title) else null
            }
            lessonList = fetchedLessons
        } catch (e: Exception) {
            Log.e("LearnPage", "Error loading lessons", e)
        } finally {
            loading = false
        }
    }



    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        SectionTitle("Learn with Videos 🎥")
        if (videoIds.isNotEmpty()) {
            VideoSection(videoIds)
        } else {
            Spacer(modifier = modifier.height(70.dp))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = modifier.height(80.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle("Sign Language Lessons 📚")
        if (loading){
            CircularProgressIndicator()
        }
        else{
            LessonList(lessonList = lessonList, navController)
        }

        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle("Trending Courses 🚀")
        CourseSection(trendingCourses)
    }
}



@Composable
fun LessonList(
    lessonList: List<LessonMeta>,
    navController: NavController
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedLessonId by remember { mutableStateOf<String?>(null) }

    Column {
        lessonList.forEach { lesson ->
            LessonCard(lesson) {
                selectedLessonId = lesson.id
                showDialog = true
            }
        }

        if (showDialog && selectedLessonId != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        navController.navigate("lessonPage/${selectedLessonId}")
                    }) {
                        Text("Try Beta Version")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                },
                text = {
                    MaintenanceMessage()
                },
                modifier = Modifier.padding(top=200.dp,bottom=200.dp, start = 8.dp, end = 8.dp).fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

@Composable
fun LessonCard(lesson: LessonMeta, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = lesson.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}





@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun VideoSection(videoIds: List<String>) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val flingBehavior = rememberSnapFlingBehavior(listState) // Added snap behavior

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            val nextIndex = (listState.firstVisibleItemIndex + 1) % videoIds.size
            coroutineScope.launch {
                listState.animateScrollToItem(nextIndex, scrollOffset = 0)
            }
        }
    }

    LazyRow(state = listState, flingBehavior = flingBehavior ) {
        itemsIndexed(videoIds) { _, videoId ->
            val itemOffset = listState.firstVisibleItemScrollOffset / 100f
            val scale = 1f - (itemOffset * 0.2f).coerceIn(0f, 0.2f)
            var alpha = 1f - (itemOffset * 0.5f).coerceIn(0f, 0.5f)

            VideoCard(
                videoId = videoId,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

@Composable
fun VideoCard(videoId: String,modifier: Modifier) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Card(
        modifier = Modifier
            .width(screenWidth * 0.925f)
            .height(200.dp)
            .padding(6.dp)
            .graphicsLayer {
                shadowElevation = 10f
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        YouTubeVideo(videoId)
    }
}


@Composable
fun CourseSection(trendingCourses: List<String>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(trendingCourses) { course ->
            CourseCard(course)
        }
    }
}

@Composable
fun CourseCard(courseTitle: String) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(100.dp)
            .padding(6.dp)
            .graphicsLayer {
                shadowElevation = 10f
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = courseTitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeVideo(videoId: String) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
        }
    }

    LaunchedEffect(videoId) {
        webView.loadUrl("https://www.youtube.com/embed/$videoId")
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { webView }
    )
}


data class LessonMeta(
    val id: String = "",
    val title: String = ""
)