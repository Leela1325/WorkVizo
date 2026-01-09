package com.simats.workvizo.ui.theme.rooms

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.simats.workvizo.R

@Composable
fun JoinedRoomSuccessScreen(
    navController: NavController,
    roomCode: String,
    userId: String,
    userName: String
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364),
            Color(0xFF6A11CB)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {

        /* ---------- CONTENT ---------- */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.joinedroom)
            )

            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(240.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Joined Successfully",
                fontFamily = poppinsBold,
                fontSize = 30.sp,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Welcome to the room. Your assigned tasks are ready.",
                fontFamily = poppinsBold,
                fontSize = 14.sp,
                color = Color.White.copy(0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            Button(
                onClick = {
                    navController.navigate(
                        "room_details/$roomCode/$userId/$userName"
                    ) {
                        popUpTo(
                            "join_room/$userId/$userName"
                        ) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF)
                )
            ) {
                Text(
                    text = "Go to Room",
                    fontFamily = poppinsBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }
    }
}

