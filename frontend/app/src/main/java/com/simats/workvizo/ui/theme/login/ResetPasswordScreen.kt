package com.simats.workvizo.ui.theme.login
import com.simats.workvizo.api.ApiService

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    email: String
) {
    val apiService = remember {
        RetrofitClient.instance.create(ApiService::class.java)
    }


    val poppins = FontFamily(Font(R.font.poppins_bold))

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    val strength = remember(password) { passwordStrength(password) }

    val strengthColor by animateColorAsState(
        when (strength) {
            "WEAK" -> Color.Red
            "MEDIUM" -> Color.Yellow
            "STRONG" -> Color.Green
            else -> Color.Gray
        },
        label = ""
    )

    /* ---------------- BACKGROUND ---------------- */
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

        /* ---------------- BACK ---------------- */
        Icon(
            Icons.Default.ArrowBack,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .size(26.dp)
                .clickable { navController.popBackStack() }
        )

        /* ---------------- CARD ---------------- */
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(0.35f)
            )
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "RESET PASSWORD",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Create a strong new password",
                    fontFamily = poppins,
                    color = Color.White.copy(0.75f)
                )

                Spacer(Modifier.height(28.dp))

                /* ---------------- NEW PASSWORD ---------------- */
                GradientPasswordField(
                    value = password,
                    label = "NEW PASSWORD",
                    onValueChange = { password = it },
                    poppins = poppins
                )

                Spacer(Modifier.height(10.dp))

                /* ---------------- STRENGTH ---------------- */
                Text(
                    "STRENGTH: $strength",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    color = strengthColor
                )

                Spacer(Modifier.height(22.dp))

                /* ---------------- CONFIRM PASSWORD ---------------- */
                GradientPasswordField(
                    value = confirmPassword,
                    label = "CONFIRM PASSWORD",
                    onValueChange = { confirmPassword = it },
                    poppins = poppins
                )

                Spacer(Modifier.height(30.dp))

                /* ---------------- BUTTON ---------------- */
                Button(
                    onClick = {
                        when {
                            password.length < 6 -> {
                                message = "PASSWORD TOO SHORT"
                                success = false
                                return@Button
                            }
                            password != confirmPassword -> {
                                message = "PASSWORDS DO NOT MATCH"
                                success = false
                                return@Button
                            }
                        }

                        loading = true
                        message = ""

                        apiService.resetPassword(email, password)
                            .enqueue(object : Callback<Any> {
                                override fun onResponse(
                                    call: Call<Any>,
                                    response: Response<Any>
                                ) {
                                    loading = false
                                    if (response.isSuccessful) {
                                        success = true
                                        navController.navigate("password_changed") {
                                            popUpTo("reset_password/{email}") { inclusive = true }
                                        }

                                    } else {
                                        success = false
                                        message = "FAILED TO UPDATE PASSWORD"
                                    }
                                }

                                override fun onFailure(call: Call<Any>, t: Throwable) {
                                    loading = false
                                    success = false
                                    message = "NETWORK ERROR"
                                }
                            })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Cyan
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color.Black
                        )
                    } else {
                        Text(
                            "CHANGE PASSWORD",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
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

/* ---------------- PASSWORD FIELD ---------------- */

@Composable
fun GradientPasswordField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    poppins: FontFamily
) {
    val borderGradient = Brush.horizontalGradient(
        listOf(Color.Cyan, Color.Magenta, Color.Blue)
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = poppins) },
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null)
        },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderGradient, RoundedCornerShape(18.dp)),
        textStyle = TextStyle(
            fontFamily = poppins,
            fontWeight = FontWeight.Bold,
            color = Color.White
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color.Cyan,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.Cyan,
            unfocusedLabelColor = Color.White.copy(0.7f),
            focusedContainerColor = Color.Black.copy(0.25f),
            unfocusedContainerColor = Color.Black.copy(0.25f)
        )
    )
}

/* ---------------- PASSWORD STRENGTH ---------------- */

fun passwordStrength(password: String): String {
    return when {
        password.length < 6 -> "WEAK"
        password.any { it.isDigit() } &&
                password.any { it.isUpperCase() } &&
                password.any { "!@#$%^&*".contains(it) } -> "STRONG"
        else -> "MEDIUM"
    }
}
