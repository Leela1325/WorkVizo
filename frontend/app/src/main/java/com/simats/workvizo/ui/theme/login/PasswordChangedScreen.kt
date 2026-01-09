package com.simats.workvizo.ui.theme.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.simats.workvizo.R

@Composable
fun PasswordChangedScreen(navController: NavController) {

    val poppins = FontFamily(Font(R.font.poppins_bold))

    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364),
            Color(0xFF6A11CB),
            Color(0xFF2575FC)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {

        /* ---------- ANIMATION ---------- */
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.changed)
        )
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        )

        /* ---------- CONTENT (SLIGHTLY ABOVE CENTER) ---------- */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(120.dp))

            Text(
                "PASSWORD CHANGED!",
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Your password has been updated successfully.\nYou can now login using your new password.",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            /* ---------- GRADIENT BUTTON ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00F5A0),
                                Color(0xFF00D9F5),
                                Color(0xFF00A3FF)
                            )
                        )
                    )
            ) {
                Button(
                    onClick = {
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        "BACK TO LOGIN",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
