package com.simats.workvizo.ui.theme.home

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
fun PrivacyPolicyScreen(
    navController: NavController
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF05061E),
            Color(0xFF0B0F3B),
            Color(0xFF1B1464),
            Color(0xFF05061E)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        /* ---------- HEADER ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        /* ---------- TITLE ---------- */
        Text(
            text = "PRIVACY POLICY",
            fontFamily = poppinsBold,
            fontSize = 26.sp,
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))

        /* ---------- CONTENT CARD ---------- */
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF7C7CFF), Color(0xFF00E5FF))
                )
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(0.08f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {

                PolicySection(
                    "Introduction",
                    "WorkVizo respects your privacy and is committed to protecting the personal information of its users. This Privacy Policy explains how we collect, use, and safeguard your data while you use our platform.",
                    poppinsBold
                )

                PolicySection(
                    "Information We Collect",
                    "We may collect personal information such as your name, email address, date of birth, and usage data to improve your experience and provide better services.",
                    poppinsBold
                )

                PolicySection(
                    "How We Use Your Information",
                    "Your information is used to manage your account, generate project schedules, improve AI recommendations, and enhance application performance.",
                    poppinsBold
                )

                PolicySection(
                    "AI & Data Usage",
                    "WorkVizo uses AI features only for project-related scheduling and suggestions. Your personal data is never used for unrelated purposes.",
                    poppinsBold
                )

                PolicySection(
                    "Data Security",
                    "We implement industry-standard security measures to protect your data against unauthorized access, alteration, or disclosure.",
                    poppinsBold
                )

                PolicySection(
                    "Third-Party Services",
                    "We do not sell or share your personal data with third parties except when required by law or to provide essential services.",
                    poppinsBold
                )

                PolicySection(
                    "Your Rights",
                    "You have the right to access, update, or delete your personal information. You may contact us anytime for assistance.",
                    poppinsBold
                )

                PolicySection(
                    "Changes to This Policy",
                    "WorkVizo may update this Privacy Policy periodically. Continued use of the app signifies your acceptance of the revised policy.",
                    poppinsBold
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Last Updated: December 2025",
                    fontFamily = poppinsBold,
                    fontSize = 12.sp,
                    color = Color(0xFF8EC5FF)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

/* ===================================================== */
/* ================= REUSABLE SECTION ================== */
/* ===================================================== */

@Composable
fun PolicySection(
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
        Spacer(Modifier.height(16.dp))
    }
}
