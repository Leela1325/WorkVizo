package com.workvizo.ui.theme.rooms

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.workvizo.R
import kotlinx.coroutines.delay
import android.net.Uri



@Composable
fun AiScheduleCreateScreen(
    navController: NavController,
    userId: String,
    userName: String,
    roomCode: String,
    roomPassword: String
)
{
    val decodedUserName = Uri.decode(userName)

    val poppins = FontFamily(Font(R.font.poppins_bold))

    /* ---------------- STATE ---------------- */
    var showNext by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(5000)
        showNext = true
    }

    /* ---------------- BACKGROUND ---------------- */
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364)
        )
    )

    /* ---------------- LOTTIE ---------------- */
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.aicreate)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        /* ---------- BACK BUTTON ---------- */
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        /* ---------- APP LOGO ---------- */
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(46.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        /* ---------- CONTENT ---------- */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            /* LOTTIE */
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            /* TITLE */
            Text(
                text = "Creating Schedule",
                fontFamily = poppins,
                fontSize = 24.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            /* SUB TEXT */
            Text(
                text = "Our AI is analyzing your project details,\nallocating tasks, and optimizing timelines.",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            /* INFO CARD */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(18.dp)
            ) {
                Text(
                    text = "What’s happening?",
                    fontFamily = poppins,
                    fontSize = 16.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Smart task breakdown\n• Balanced workload distribution\n• Deadline-aware planning\n• Team-optimized workflow",
                    fontFamily = poppins,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            /* NEXT BUTTON */
            AnimatedVisibility(
                visible = showNext,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = {

                        val safeUserName = Uri.encode(decodedUserName)

                        navController.navigate(
                            "ai_schedule_preview/$userId/$safeUserName/$roomCode/$roomPassword"
                        )
                    }
                    ,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A11CB)
                    )
                ) {
                    Text(
                        text = "Next",
                        fontFamily = poppins,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
