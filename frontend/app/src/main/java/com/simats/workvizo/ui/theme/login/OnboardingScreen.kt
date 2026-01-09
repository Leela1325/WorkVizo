package com.simats.workvizo.ui.theme.login

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.simats.workvizo.R

@Composable
fun OnboardingScreen( navController: NavController,
                      userId: String,
                      userName: String) {

    val poppins = remember {
        FontFamily(Font(R.font.poppins_bold, FontWeight.Bold))
    }

    var slideIndex by remember { mutableStateOf(0) }

    val gradient = Brush.linearGradient(
        listOf(
            Color(0xFF0A0E3F),
            Color(0xFF142F65),
            Color(0xFF1D5FA2),
            Color(0xFF23A6D5),
            Color(0xFF23D5AB)
        )
    )

    val slideTitles = listOf(
        "Proof-driven Task Management",
        "Smarter Team Collaboration",
        "Real Progress Tracking",
        "Automated Reporting"
    )

    val slideDescriptions = listOf(
        "Submit tasks with evidence and prevent fake completions effortlessly.",
        "Work together with clarity, transparency, and accountability.",
        "Monitor real-time progress and know exactly what is pending.",
        "Automatically generate progress reports — zero manual work."
    )

    val lottieFiles = listOf(
        R.raw.onboard1,
        R.raw.onboard2,
        R.raw.onboard3,
        R.raw.onboard4
    )

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(lottieFiles[slideIndex])
    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(18.dp)
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(55.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- ANIMATION ZONE ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress }
                )
            }

            // ✅ THIS IS THE FIX — consistent gap for all screens
            Spacer(modifier = Modifier.height(24.dp))

            /* ---------- TEXT ZONE ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Crossfade(targetState = slideIndex) { index ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    2.dp,
                                    Color(0xFFADE8F4),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = slideTitles[index],
                                color = Color(0xFFADE8F4),
                                fontFamily = poppins,
                                fontSize = 24.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = slideDescriptions[index],
                            color = Color.White.copy(alpha = 0.92f),
                            fontFamily = poppins,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 28.dp)
                        )
                    }
                }
            }

            /* ---------- BUTTON ZONE ---------- */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (slideIndex > 0) {
                    TextButton(onClick = { slideIndex-- }) {
                        Text(
                            "Back",
                            fontFamily = poppins,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                } else {
                    Spacer(Modifier.width(60.dp))
                }

                Button(
                    onClick = {
                        if (slideIndex < slideTitles.lastIndex)
                            slideIndex++
                        else{
                            navController.navigate("home/$userId/$userName") {
                                popUpTo("onboard") { inclusive = true }
                            }}

                    },
                    modifier = Modifier
                        .height(55.dp)
                        .width(150.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = if (slideIndex == slideTitles.lastIndex) "Finish" else "Next",
                        color = Color(0xFF0A0E3F),
                        fontFamily = poppins,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

