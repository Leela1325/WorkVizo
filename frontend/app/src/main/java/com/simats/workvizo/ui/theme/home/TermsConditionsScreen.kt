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
fun TermsConditionsScreen(
    navController: NavController
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    /* ---------- BACKGROUND ---------- */
    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF060720),
            Color(0xFF0E1248),
            Color(0xFF1E1A6E),
            Color(0xFF060720)
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "WorkVizo Logo",
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        /* ---------- TITLE ---------- */
        Text(
            text = "TERMS & CONDITIONS",
            fontFamily = poppinsBold,
            fontSize = 26.sp,
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))

        /* ---------- CONTENT CONTAINER ---------- */
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

                TermsSection(
                    "Acceptance of Terms",
                    "By accessing or using WorkVizo, you agree to be bound by these Terms and Conditions. If you do not agree, please refrain from using the application.",
                    poppinsBold
                )

                TermsSection(
                    "Use of the Platform",
                    "WorkVizo is designed to assist users in project planning, task scheduling, and workflow management. You agree to use the app only for lawful and intended purposes.",
                    poppinsBold
                )

                TermsSection(
                    "User Responsibilities",
                    "You are responsible for maintaining the confidentiality of your account credentials and for all activities performed under your account.",
                    poppinsBold
                )

                TermsSection(
                    "AI-Generated Content",
                    "AI suggestions provided by WorkVizo are for guidance purposes only. Final decisions and project outcomes remain the responsibility of the user.",
                    poppinsBold
                )

                TermsSection(
                    "Data Usage & Privacy",
                    "Your personal data is handled in accordance with our Privacy Policy. We do not sell or misuse your information.",
                    poppinsBold
                )

                TermsSection(
                    "Account Suspension",
                    "We reserve the right to suspend or terminate accounts that violate these terms or engage in harmful or abusive behavior.",
                    poppinsBold
                )

                TermsSection(
                    "Limitation of Liability",
                    "WorkVizo shall not be liable for any direct or indirect damages arising from the use or inability to use the application.",
                    poppinsBold
                )

                TermsSection(
                    "Modifications to Terms",
                    "We may update these Terms & Conditions from time to time. Continued use of the app constitutes acceptance of the updated terms.",
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
@Composable
fun TermsSection(
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
