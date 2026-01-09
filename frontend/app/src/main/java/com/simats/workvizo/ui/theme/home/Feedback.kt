package com.simats.workvizo.ui.theme.home
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import kotlinx.coroutines.delay
import retrofit2.*

@Composable
fun FeedbackScreen(
    navController: NavController,
    userId: String
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var feedback by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF060720),
            Color(0xFF0E1248),
            Color(0xFF1E1A6E),
            Color(0xFF060720)
        )
    )

    val feedbackComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.feedback)
    )

    /* ---------- AUTO DISMISS POPUP ---------- */
    if (showSuccessPopup) {
        SuccessPopup {
            showSuccessPopup = false
            navController.popBackStack()
        }
    }

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
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        /* ---------- LOTTIE ---------- */
        LottieAnimation(
            composition = feedbackComposition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "WE VALUE YOUR FEEDBACK",
            fontFamily = poppinsBold,
            fontSize = 22.sp,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(20.dp))

        /* ---------- INPUT FIELD ---------- */
        OutlinedTextField(
            value = feedback,
            onValueChange = { feedback = it },
            placeholder = {
                Text(
                    "Tell us what you think about WorkVizo...",
                    fontFamily = poppinsBold,
                    color = Color.White.copy(0.6f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = poppinsBold,
                color = Color.White
            ),
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(0.35f),
                cursorColor = Color(0xFF00E5FF)
            )
        )

        Spacer(Modifier.height(28.dp))

        /* ---------- SUBMIT BUTTON ---------- */
        Button(
            onClick = {
                isSubmitting = true
                api.submitFeedback(userId, feedback).enqueue(object : Callback<GenericResponse> {
                    override fun onResponse(
                        call: Call<GenericResponse>,
                        response: Response<GenericResponse>
                    ) {
                        isSubmitting = false
                        if (response.body()?.status == "success") {
                            showSuccessPopup = true
                        }
                    }

                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        isSubmitting = false
                    }
                })
            },
            enabled = feedback.isNotBlank() && !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (feedback.isNotBlank())
                    Color(0xFF00FF87)
                else
                    Color.Gray
            )
        ) {
            Text(
                text = if (isSubmitting) "Submitting..." else "Submit Feedback",
                fontFamily = poppinsBold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}
@Composable
fun SuccessPopup(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { }) {

        // Auto dismiss after 2 seconds
        LaunchedEffect(Unit) {
            delay(2000)
            onDismiss()
        }

        val scaleAnim = remember { Animatable(0.7f) }

        LaunchedEffect(Unit) {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0B0F3B)
            ),
            modifier = Modifier
                .padding(16.dp)
                .scale(scaleAnim.value)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF00FF87),
                    modifier = Modifier.size(64.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Feedback Submitted!",
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 20.sp,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Thank you for helping us improve WorkVizo",
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 13.sp,
                    color = Color.White.copy(0.75f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
