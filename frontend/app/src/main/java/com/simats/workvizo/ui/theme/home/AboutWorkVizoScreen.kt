package com.simats.workvizo.ui.theme.home

import androidx.compose.animation.core.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R

@Composable
fun AboutWorkVizoScreen(
    navController: NavController
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    /* ---------- BACKGROUND GRADIENT ---------- */
    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF04051C),
            Color(0xFF0C1042),
            Color(0xFF1A176A),
            Color(0xFF04051C)
        )
    )

    /* ---------- FLOATING LOGO ANIMATION ---------- */
    val infiniteTransition = rememberInfiniteTransition(label = "logo_float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        /* ---------- HEADER ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {

            // Back button (LEFT)
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Center title
            Text(
                text = "ABOUT",
                fontFamily = poppinsBold,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            // Logo (RIGHT)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "WorkVizo Logo",
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterEnd)
            )
        }


        Spacer(Modifier.height(32.dp))

        /* ---------- FLOATING LOGO ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "WorkVizo Logo",
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        translationY = floatY
                    }
            )
        }

        Spacer(Modifier.height(24.dp))

        /* ---------- TITLE ---------- */
        Text(
            text = "WORKVIZO",
            fontFamily = poppinsBold,
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(12.dp))

        /* ---------- TAGLINE ---------- */
        Text(
            text = "Smart Project Planning. Simplified.",
            fontFamily = poppinsBold,
            fontSize = 14.sp,
            color = Color(0xFF8EC5FF),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(28.dp))

        /* ---------- CONTENT CARD ---------- */
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF7C7CFF),
                        Color(0xFF00E5FF),
                        Color(0xFF00FF87)
                    )
                )
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(0.08f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                AboutSection(
                    "What is WorkVizo?",
                    "WorkVizo is an intelligent project management and scheduling platform designed to help individuals and teams plan, organize, and execute projects efficiently using smart workflows and AI-powered insights.",
                    poppinsBold
                )

                AboutSection(
                    "Why WorkVizo?",
                    "We focus on simplicity, clarity, and productivity. WorkVizo removes complexity from planning so you can focus on execution and results.",
                    poppinsBold
                )

                AboutSection(
                    "AI with Purpose",
                    "WorkVizo’s AI is strictly project-focused. It generates schedules, suggestions, and improvements only based on your project data—nothing unrelated.",
                    poppinsBold
                )

                AboutSection(
                    "Built for Teams & Individuals",
                    "Whether you're managing a personal task list or coordinating a team project, WorkVizo adapts to your workflow seamlessly.",
                    poppinsBold
                )

                AboutSection(
                    "Our Vision",
                    "To empower people and teams with intelligent tools that make project management intuitive, efficient, and enjoyable.",
                    poppinsBold
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Version 1.0 • © 2025 WorkVizo",
                    fontFamily = poppinsBold,
                    fontSize = 12.sp,
                    color = Color(0xFF8EC5FF)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
@Composable
fun AboutSection(
    title: String,
    content: String,
    poppinsBold: FontFamily
) {
    Column {
        Text(
            text = title,
            fontFamily = poppinsBold,
            fontSize = 16.sp,
            color = Color(0xFF7C7CFF)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = content,
            fontFamily = poppinsBold,
            fontSize = 14.sp,
            color = Color.White.copy(0.9f)
        )
        Spacer(Modifier.height(18.dp))
    }
}
