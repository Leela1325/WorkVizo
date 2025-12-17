package com.workvizo.ui.theme.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.workvizo.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    val poppins = FontFamily(Font(R.font.poppins_bold, weight = FontWeight.Bold))

    // ------------------------------
    // ✨ Logo Scale Animation
    // ------------------------------
    val scaleAnim = remember { Animatable(0.4f) }
    val logoOffset = remember { Animatable(40f) } // float-up animation

    // ------------------------------
    // ✨ Fade-in animations
    // ------------------------------
    val titleAlpha = remember { Animatable(0f) }
    val descAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {

        // Stage 1: Scale logo
        scaleAnim.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(850, easing = FastOutSlowInEasing)
        )

        // Stage 2: Float upward + settle
        logoOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(900, easing = LinearOutSlowInEasing)
        )

        // Stage 3: Fade in title & description
        titleAlpha.animateTo(1f, tween(600))
        descAlpha.animateTo(1f, tween(900))

        delay(2500)

        navController.navigate("get_started") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // ---------------------------------
    // PREMIUM GRADIENT (No pink)
    // ---------------------------------
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A40),  // Deep Navy
            Color(0xFF322E6C),  // Royal Indigo
            Color(0xFF6A5ACD)   // Soft Purple Glow
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // ---------------------------------
            // LOGO + GLOW EFFECT
            // ---------------------------------
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(y = logoOffset.value.dp)
                    .scale(scaleAnim.value)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(75.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ---------------------------------
            // TITLE FADE-IN
            // ---------------------------------
            Text(
                text = "WorkVizo",
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = Color.White,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ---------------------------------
            // DESCRIPTION FADE-IN
            // ---------------------------------
            Text(
                text = "Proof-driven tasks. Smarter teamwork.",
                fontFamily = poppins,
                fontSize = 15.sp,
                color = Color(0xFFEFEFEF).copy(alpha = 0.92f),
                modifier = Modifier.alpha(descAlpha.value)
            )
        }
    }
}
