package com.simats.workvizo.ui.theme.login
import com.simats.workvizo.api.ApiService

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.OtpResponse
import com.simats.workvizo.api.OtpVerifyResponse
import com.simats.workvizo.api.RetrofitClient
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val apiService = remember {
        RetrofitClient.instance.create(ApiService::class.java)
    }

    val poppins = FontFamily(Font(R.font.poppins_bold))

    var email by remember { mutableStateOf("") }
    var showOtp by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    val otpDigits = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = List(6) { FocusRequester() }

    var timer by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }

    /* TIMER */
    LaunchedEffect(showOtp) {
        if (showOtp) {
            timer = 60
            canResend = false
            while (timer > 0) {
                delay(1000)
                timer--
            }
            canResend = true
        }
    }

    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364),
            Color(0xFF6A11CB),
            Color(0xFF2575FC)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {

        Icon(
            Icons.Default.ArrowBack,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .size(26.dp)
                .clickable { navController.popBackStack() }
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(0.35f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "FORGOT PASSWORD",
                    fontFamily = poppins,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    if (!showOtp) "WE’LL SEND OTP TO YOUR EMAIL"
                    else "ENTER 6-DIGIT OTP",
                    fontFamily = poppins,
                    color = Color.White.copy(0.75f)
                )

                Spacer(Modifier.height(22.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    enabled = !showOtp,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    label = { Text("EMAIL", fontFamily = poppins) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color.White.copy(0.6f),
                        disabledBorderColor = Color.Cyan,
                        focusedContainerColor = Color.Black.copy(0.3f),
                        unfocusedContainerColor = Color.Black.copy(0.3f),
                        disabledContainerColor = Color.Black.copy(0.35f)
                    )
                )

                AnimatedVisibility(showOtp, enter = fadeIn(), exit = fadeOut()) {
                    Column {
                        Spacer(Modifier.height(22.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            repeat(6) { index ->
                                OtpBox(
                                    value = otpDigits[index],
                                    focusRequester = focusRequesters[index],
                                    font = poppins,
                                    onValueChange = {
                                        otpDigits[index] = it
                                        if (it.isNotEmpty() && index < 5) {
                                            focusRequesters[index + 1].requestFocus()
                                        }
                                    },
                                    onBackspace = {
                                        if (otpDigits[index].isEmpty() && index > 0) {
                                            focusRequesters[index - 1].requestFocus()
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            if (canResend) "RESEND OTP" else "RESEND IN $timer s",
                            fontFamily = poppins,
                            color = if (canResend) Color.Cyan else Color.White.copy(0.6f),
                            modifier = Modifier.clickable(enabled = canResend) {
                                showOtp = false
                                otpDigits.indices.forEach { otpDigits[it] = "" }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(26.dp))

                Button(
                    onClick = {

                        /* SEND OTP */
                        if (!showOtp) {

                            if (email.isBlank()) {
                                message = "EMAIL REQUIRED"
                                success = false
                                return@Button
                            }

                            loading = true
                            message = ""

                            apiService.sendOtp(email)

                                .enqueue(object : Callback<OtpResponse> {
                                    override fun onResponse(
                                        call: Call<OtpResponse>,
                                        response: Response<OtpResponse>
                                    ) {
                                        loading = false
                                        if (response.body()?.status == "success") {
                                            showOtp = true
                                            focusRequesters[0].requestFocus()
                                        } else {
                                            message = response.body()?.message ?: "FAILED"
                                        }
                                    }

                                    override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                                        loading = false
                                        message = "NETWORK ERROR"
                                    }
                                })

                        } else {

                            /* VERIFY OTP */
                            val otp = otpDigits.joinToString("")
                            if (otp.length != 6) {
                                message = "INVALID OTP"
                                success = false
                                return@Button
                            }

                            loading = true
                            message = ""

                            apiService.verifyOtp(email, otp)

                                .enqueue(object : Callback<OtpVerifyResponse> {
                                    override fun onResponse(
                                        call: Call<OtpVerifyResponse>,
                                        response: Response<OtpVerifyResponse>
                                    ) {
                                        loading = false
                                        val body = response.body()

                                        if (body?.status == "success") {
                                            success = true
                                            message = "OTP VERIFIED"

                                            navController.navigate("reset_password/$email") {
                                                popUpTo("forgot") { inclusive = true }
                                            }
                                        } else {
                                            success = false
                                            message = body?.message ?: "WRONG OTP"
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<OtpVerifyResponse>,
                                        t: Throwable
                                    ) {
                                        loading = false
                                        success = false
                                        message = "NETWORK ERROR"
                                    }
                                })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                ) {
                    Text(
                        when {
                            loading && !showOtp -> "SENDING OTP..."
                            loading && showOtp -> "VERIFYING..."
                            !showOtp -> "SEND OTP"
                            else -> "VERIFY OTP"
                        },
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        message,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = if (success) Color.Green else Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun OtpBox(
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit,
    font: FontFamily
) {
    BasicTextField(
        value = value,
        onValueChange = {
            if (it.length <= 1 && it.all { ch -> ch in '0'..'9' }) {
                onValueChange(it)
            }
            if (it.isEmpty()) onBackspace()
        },
        modifier = Modifier
            .width(48.dp)
            .height(54.dp)
            .focusRequester(focusRequester)
            .border(2.dp, Color.Cyan, RoundedCornerShape(14.dp))
            .background(Color.Black.copy(0.25f), RoundedCornerShape(14.dp)),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = font,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        ),
        decorationBox = { inner ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { inner() }
        }
    )
}
