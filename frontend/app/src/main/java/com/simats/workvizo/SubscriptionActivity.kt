package com.simats.workvizo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.workvizo.R

@Composable
fun SubscriptionScreen(
    navController: NavController,
    onSubscribe: () -> Unit
) {
    val poppins = FontFamily(Font(R.font.poppins_bold, FontWeight.Bold))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E27))
    ) {

        // ✅ SKIP BUTTON (TOP RIGHT)
        TextButton(
            onClick = {
                navController.navigate("get_started") {
                    popUpTo("subscribe") { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Skip",
                color = Color(0xFFB8C5D6),
                fontFamily = poppins
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "WorkVizo PREMIUM",
                fontFamily = poppins,
                fontSize = 30.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            FeatureCard(
                icon = "⚡",
                title = "Ad-Free Experience",
                desc = "Pure learning, no interruptions"
            )

            FeatureCard(
                icon = "💎",
                title = "AI Scheduling",
                desc = "Advanced schedule creation & tracking"
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSubscribe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "START PREMIUM",
                    color = Color.Black,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: String,
    title: String,
    desc: String
) {
    val poppins = FontFamily(Font(R.font.poppins_bold, FontWeight.Bold))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1F3A)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(text = icon, fontSize = 28.sp)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = title,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = desc,
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    color = Color(0xFF7C8AA8)
                )
            }

            Text(
                text = "✓",
                color = Color(0xFF4CAF50),
                fontSize = 20.sp
            )
        }
    }
}
