package com.example.signify.ui.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.signify.R
import com.example.signify.viewmodel.AuthViewModel

@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
) {

    val pages = listOf(
        OnBoardingData(
            imageRes = R.drawable.girl_taking_notes, // Replace with actual drawable
            title = "Effortless Sign Recognition",
            description = "Capture or upload an image to instantly recognize and translate signs into text."
        ),
        OnBoardingData(
            imageRes = R.drawable.book_lover,
            title = "Learn Sign Language Anytime",
            description = "Access a vast library of ISL signs, lessons, and interactive learning modules at your convenience."
        ),
        OnBoardingData(
            imageRes = R.drawable.learning_bro,
            title = "Enhance Your Skills with Quizzes",
            description = "Test and improve your sign language proficiency with engaging daily quizzes and challenges."
        ),
        OnBoardingData(
            imageRes = R.drawable.nerd_bro,
            title = "Bridge the Communication Gap",
            description = "Join us in making communication seamless and inclusive for everyone, everywhere."
        )
    )


    val pagerState = rememberPagerState { pages.size }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        HorizontalPager(state = pagerState) { page ->
            OnBoardingPageView(page = pages[page])
        }




        if (pagerState.currentPage ==pages.size - 1) {
            OutlinedButton(
                onClick = {
                    navController.navigate("login")
                },
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF776FFF),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .height(75.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp).padding(bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "GET STARTED",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        else{
            // Dots Indicator
            DotsIndicator(totalDots = pages.size, selectedIndex = pagerState.currentPage)
        }
    }
}













