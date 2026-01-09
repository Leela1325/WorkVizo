package com.simats.workvizo.ui.theme.rooms

/* ---------- IMPORTS ---------- */
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.simats.workvizo.R
import android.net.Uri


/* ===================================================== */
/* ============ ROOM CREATED SUCCESS =================== */
/* ===================================================== */

@Composable
fun RoomCreatedSuccessScreen(
    navController: NavController,
    userId: String,
    userName: String,
    roomCode: String,
    roomPassword: String
)
{
    val decodedUserName = Uri.decode(userName)

    val poppins = FontFamily(Font(R.font.poppins_bold, FontWeight.Bold))

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF11998E),
            Color(0xFF38EF7D),
            Color(0xFF11998E)
        )
    )

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.created)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {

        /* ---------- BACK BUTTON → HOME ---------- */
        IconButton(
            onClick = {
                navController.navigate("home/$userId/$decodedUserName")
                {
                    popUpTo(0)
                    launchSingleTop = true
                }

            },
            modifier = Modifier
                .padding(16.dp)
                .size(42.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.2f))
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        /* ---------- MAIN CONTENT ---------- */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(220.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "ROOM CREATED SUCCESSFULLY 🎉",
                fontFamily = poppins,
                fontSize = 22.sp,
                color = Color.White
            )

            Spacer(Modifier.height(20.dp))

            InfoCard("ROOM CODE", roomCode, poppins)
            Spacer(Modifier.height(12.dp))
            InfoCard("ROOM PASSWORD", roomPassword, poppins)

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    val safeUserName = Uri.encode(userName)

                    navController.navigate("rooms_overview/$userId/$safeUserName") {
                        popUpTo("home/$userId/$safeUserName") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ) {
                Text(
                    "GO TO ROOMS",
                    fontFamily = poppins,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

/* ===================================================== */
/* ================= INFO CARD ========================= */
/* ===================================================== */

@Composable
fun InfoCard(
    label: String,
    value: String,
    poppins: FontFamily
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontFamily = poppins, fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(6.dp))
            Text(value, fontFamily = poppins, fontSize = 20.sp, color = Color.Black)
        }
    }
}
