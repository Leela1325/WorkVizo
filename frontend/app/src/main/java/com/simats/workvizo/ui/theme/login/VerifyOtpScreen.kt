package com.simats.workvizo.ui.theme.login

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R

@Composable

fun VerifyOtpScreen(
    navController: NavController,
    email: String
)
{

    val poppins = FontFamily(Font(R.font.poppins_bold))

    var otp by remember { mutableStateOf("") }
    val isOtpValid = otp.length == 6

    // ✨ Button animation
    val scale by animateFloatAsState(
        targetValue = if (isOtpValid) 1f else 0.96f,
        label = "scale"
    )

    // 🌈 NEW BACKGROUND
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Verify OTP",
                fontFamily = poppins,
                fontSize = 32.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the 6-digit code sent to your email",
                color = Color.White.copy(0.8f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 🔢 SINGLE OTP INPUT
            OutlinedTextField(
                value = otp,
                onValueChange = {
                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                        otp = it
                    }
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 22.sp
                ),
                placeholder = {
                    Text(
                        "Enter OTP",
                        color = Color.White.copy(0.4f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                visualTransformation = VisualTransformation.None,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(0.08f),
                    unfocusedContainerColor = Color.White.copy(0.05f),
                    focusedIndicatorColor = Color(0xFF00E5FF),
                    unfocusedIndicatorColor = Color.White.copy(0.3f),
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // ✅ VERIFY BUTTON
            Button(
                onClick = {
                    // 👉 NEXT: call verify OTP API
                    navController.navigate("reset_password")
                },
                enabled = isOtpValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .scale(scale),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    disabledContainerColor = Color.White.copy(0.3f)
                )
            ) {
                Text(
                    text = "VERIFY OTP",
                    fontFamily = poppins,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Didn’t receive OTP? Resend",
                color = Color.White.copy(0.7f),
                fontSize = 13.sp
            )
        }
    }
}
