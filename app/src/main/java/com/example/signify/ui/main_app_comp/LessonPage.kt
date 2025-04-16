import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nl.dionsegijn.konfetti.compose.*
import nl.dionsegijn.konfetti.core.*
import nl.dionsegijn.konfetti.core.emitter.*
import nl.dionsegijn.konfetti.core.Position
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.sp
import java.util.concurrent.TimeUnit


data class Question(
    val mediaUrl : String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = ""
)

data class Lesson(
    val title: String = "",
    val questions: List<Question> = emptyList()
)


@Composable
fun LessonPage(
    lessonId: String,
    navController: NavController
) {
    var lesson by remember { mutableStateOf<Lesson?>(null) }
    var loading by remember { mutableStateOf(true) }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var showResults by remember { mutableStateOf(false) }

    LaunchedEffect(lessonId) {
        FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(lessonId)
            .get()
            .addOnSuccessListener { doc ->
                lesson = doc.toObject(Lesson::class.java)
                loading = false
            }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        lesson?.let { lessonData ->
            val totalQuestions = lessonData.questions.size

            if (showResults) {
                val totalQuestions = lessonData.questions.size
                val showConfetti = score == totalQuestions

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🎉 Lesson Completed!",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your Score: $score / $totalQuestions",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Text("Back to Learn")
                        }
                    }

                    if (showConfetti) {
                        KonfettiView(
                            modifier = Modifier.fillMaxSize(),
                            parties = listOf(
                                Party(
                                    emitter = Emitter(duration = 6, TimeUnit.SECONDS).perSecond(150),
                                    spread = 360,
                                    colors = listOf(
                                        Color(0xFFFFC107),
                                        Color(0xFF4CAF50),
                                        Color(0xFF2196F3),
                                        Color(0xFFFF5722),
                                    ).map { it.toArgb() },
                                    position = Position.Relative(0.5, 0.0)
                                )
                            )
                        )
                    }
                }
            } else {
                val currentQuestion = lessonData.questions.getOrNull(currentQuestionIndex)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Progress bar
                    val animatedProgress by animateFloatAsState(
                        targetValue = currentQuestionIndex / totalQuestions.toFloat(),
                        animationSpec = tween(
                            durationMillis = 500, // Increase duration for smoother transition
                            easing = FastOutSlowInEasing
                        ),
                        label = "SmoothProgress"
                    )

                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Text(
                        text = lessonData.title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    currentQuestion?.let {
                        LessonPageContent(
                            question = it,
                            index = currentQuestionIndex + 1,
                            onCorrect = {
                                score++
                            },
                            onNext = {
                                if (currentQuestionIndex < totalQuestions - 1) {
                                    currentQuestionIndex++
                                } else {
                                    showResults = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LessonPageContent(
    question: Question,
    index: Int,
    onCorrect: () -> Unit,
    onNext: () -> Unit
) {
    val questionKey = question.mediaUrl + question.question
    var selectedOption by remember(questionKey) { mutableStateOf<String?>(null) }
    var isAnswered by remember(questionKey) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Q$index",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = question.question.ifBlank { "What does this sign mean?" },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            AsyncImage(
                model = question.mediaUrl,
                contentDescription = "Question Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 2x2 Grid for Options
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight(0.5f)
            ) {
                items(question.options) { option ->
                    val isSelected = selectedOption == option
                    val isCorrect = question.answer == option

                    val backgroundColor by animateColorAsState(
                        targetValue = when {
                            !isAnswered -> MaterialTheme.colorScheme.surface
                            isSelected && isCorrect -> Color(0xFFB2FFB2) // Light green
                            isSelected && !isCorrect -> Color(0xFFFFC1C1) // Light red
                            else -> MaterialTheme.colorScheme.surface
                        },
                        label = "OptionColor"
                    )

                    val textColor by animateColorAsState(
                        targetValue = when {
                            !isAnswered -> MaterialTheme.colorScheme.onSurface
                            isSelected -> Color.Black
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        label = "TextColor"
                    )

                    Card(
                        onClick = {
                            if (!isAnswered) {
                                selectedOption = option
                                isAnswered = true
                                if (option == question.answer) onCorrect()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAnswered,
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                color = textColor,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }


            }


            Spacer(modifier = Modifier.height(12.dp))

            if (isAnswered) {
                val isCorrect = selectedOption == question.answer
                Text(
                    text = if (isCorrect) "✅ Correct!" else "❌ Incorrect. Correct Answer: ${question.answer}",
                    color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Button(
                    onClick = onNext,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Next")
                }
            }
        }
    }
}



