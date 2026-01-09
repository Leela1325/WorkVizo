package com.simats.workvizo.ui.theme.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.simats.workvizo.R
import androidx.compose.ui.geometry.Offset


@Composable
fun GetStartedScreen(navController: NavController) {

    val poppins = FontFamily(Font(R.font.poppins_bold, weight = FontWeight.Bold))

    val composition = rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.workflow_lottie)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition.value,
        iterations = LottieConstants.IterateForever
    )

    // ⭐ NEW MIXED DARK GRADIENT (DIAGONAL)
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D1B2A), // Navy
            Color(0xFF1B263B), // Deep Blue
            Color(0xFF283B70), // Indigo
            Color(0xFF0077B6), // Blue
            Color(0xFF00A8E8)  // Aqua Glow
        ),
        start = Offset(0f, 0f),
        end = Offset(1200f, 1800f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Lottie Animation
        LottieAnimation(
            composition = composition.value,
            progress = { progress },
            modifier = Modifier
                .height(260.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Turn tasks into proof. Work smarter.\nBuild trust. Track real progress.",
            fontFamily = poppins,
            fontSize = 20.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { navController.navigate("login") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text(
                text = "Get Started",
                fontFamily = poppins,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1B2A)
            )
        }
    }
}
