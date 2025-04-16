package com.example.signify.ui.screens.login

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.signify.R

@Composable
fun MyLableText(modifier: Modifier = Modifier, value: String) {
    Text(
        text = value,
        modifier = modifier
            .fillMaxWidth(),
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        ),
        textAlign = TextAlign.Center
    )
}


@Composable
fun MyHeadingText(modifier: Modifier = Modifier, value: String) {
    Text(
        text = value,
        modifier = modifier
            .fillMaxWidth(),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ),
        textAlign = TextAlign.Center
    )
}

@Composable
fun AnimatedImageView(
    rawResId: Int,
    modifier: Modifier = Modifier,
    iterations: Int = Int.MAX_VALUE // Corrected parameter name
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawResId))

    LottieAnimation(
        composition = composition,
        modifier = modifier.animateContentSize(),
        iterations = iterations // Use the passed iterations parameter
    )
}


@Composable
fun GoogleSignInButton(onClicked: () -> Unit) {
    OutlinedButton(
        onClick = { onClicked() },
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continue with Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}



//------------------OnBoarding Components
@Composable
fun DotsIndicator(totalDots: Int, selectedIndex: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(75.dp)
    ) {
        repeat(totalDots) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == selectedIndex) 13.dp else 9.dp)
                    .background(
                        color = if (index == selectedIndex) Color(0xFF4F46E5) else Color.LightGray,
                        shape = CircleShape
                    )
                    .padding(2.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
fun OnBoardingPageView(page: OnBoardingData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.height(650.dp)
    ) {
        Image(painter = painterResource(id = page.imageRes), contentDescription = null)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = page.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = page.description, fontSize = 16.sp, textAlign = TextAlign.Center, color = Color.Gray)
    }
}


data class OnBoardingData(
    val imageRes: Int,
    val title: String,
    val description: String,
)