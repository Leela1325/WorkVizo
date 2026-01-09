package com.simats.workvizo.ui.theme.login
import com.simats.workvizo.api.ApiService
import android.net.Uri

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.RegisterResponse
import com.simats.workvizo.api.RetrofitClient
import retrofit2.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.workvizo.ui.viewmodel.UserSessionViewModel

@Composable
fun RegisterScreen(navController: NavController) {
    val apiService = remember {
        RetrofitClient.instance.create(ApiService::class.java)
    }
    val session: UserSessionViewModel = viewModel()

    val poppins = FontFamily(Font(R.font.poppins_bold))

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // DOB split
    // DOB split (YYYY-MM-DD)
    var yyyy by remember { mutableStateOf("") }
    var mm by remember { mutableStateOf("") }
    var dd by remember { mutableStateOf("") }


    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var serverMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    /* ---------------- VALIDATION ---------------- */

    val emailError =
        email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val dobValid =
        yyyy.length == 4 && mm.length == 2 && dd.length == 2


    val passMismatch =
        confirmPassword.isNotEmpty() && confirmPassword != password

    val formValid =
        fullName.isNotEmpty() &&
                email.isNotEmpty() &&
                !emailError &&
                dobValid &&
                password.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                !passMismatch

    val dobCombined = "$dd-$mm-$yyyy"

    /* ---------------- BACKGROUND ---------------- */

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF0D1B2A),
            Color(0xFF1B263B),
            Color(0xFF415A77)
        )
    )

    /* ---------------- BUTTON SHIMMER ---------------- */

    val infiniteTransition = rememberInfiniteTransition(label = "register_shimmer")

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -400f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    val registerButtonBrush = Brush.linearGradient(
        colors = if (formValid)
            listOf(
                Color(0xFF7F00FF),
                Color.White.copy(0.85f),
                Color(0xFFE100FF)
            )
        else
            listOf(
                Color.Gray.copy(0.55f),
                Color.Gray.copy(0.85f),
                Color.Gray.copy(0.55f)
            ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 320f, 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(20.dp)
    ) {

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(20.dp))

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.5.dp, Color.White.copy(0.6f), RoundedCornerShape(14.dp))
            )

            Spacer(Modifier.height(18.dp))

            Text("Create Account", fontFamily = poppins, fontSize = 32.sp, color = Color.White)
            Text(
                "Start your smart workflow journey",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.White.copy(0.85f)
            )

            Spacer(Modifier.height(28.dp))

            /* ---------------- FORM CARD ---------------- */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.copy(0.14f))
                    .border(1.2.dp, Color.White.copy(0.25f), RoundedCornerShape(26.dp))
                    .padding(30.dp)
            ) {

                Column {

                    AnimatedField("Full Name", fullName, { fullName = it }, Icons.Default.Person, poppins)
                    Spacer(Modifier.height(20.dp))

                    AnimatedField(
                        "Email",
                        email,
                        { email = it },
                        Icons.Default.Email,
                        poppins,
                        error = emailError,
                        errorText = "Invalid email"
                    )

                    Spacer(Modifier.height(20.dp))

                    /* ---------------- DOB ---------------- */

                    Text("Date of Birth", color = Color.White, fontFamily = poppins)
                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DobBox("YYYY", yyyy) { if (it.length <= 4) yyyy = it }
                        DobBox("MM", mm) { if (it.length <= 2) mm = it }
                        DobBox("DD", dd) { if (it.length <= 2) dd = it }
                    }


                    if (!dobValid && (dd.isNotEmpty() || mm.isNotEmpty() || yyyy.isNotEmpty())) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Enter DOB as YYYY-MM-DD",
                            color = Color.Yellow,
                            fontFamily = poppins,
                            fontSize = 12.sp
                        )

                    }

                    Spacer(Modifier.height(22.dp))

                    AnimatedPasswordField(
                        "Password",
                        password,
                        { password = it },
                        passwordVisible,
                        { passwordVisible = !passwordVisible },
                        poppins
                    )

                    Spacer(Modifier.height(20.dp))

                    AnimatedPasswordField(
                        "Confirm Password",
                        confirmPassword,
                        { confirmPassword = it },
                        confirmPasswordVisible,
                        { confirmPasswordVisible = !confirmPasswordVisible },
                        poppins,
                        error = passMismatch,
                        errorText = "Passwords do not match"
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            /* ---------------- REGISTER BUTTON ---------------- */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(registerButtonBrush)
            ) {
                Button(
                    onClick = {
                        isLoading = true
                        serverMessage = ""

                        apiService.register(
                            fullName,
                            email,
                            dobCombined,
                            password,
                            confirmPassword
                        ).enqueue(object : Callback<RegisterResponse> {


                            override fun onResponse(
                                call: Call<RegisterResponse>,
                                response: Response<RegisterResponse>
                            ) {
                                isLoading = false
                                val body = response.body()

                                if (response.isSuccessful && body?.status == "success") {

                                    val userId = body.user?.id ?: "temp"
                                    val userName = Uri.encode(fullName)

                                    // ✅ SAVE REGISTER DATA LOCALLY (THIS IS THE FIX)
                                    val prefs = navController.context
                                        .getSharedPreferences("workvizo_prefs", 0)

                                    prefs.edit()
                                        .putString("email", email)
                                        .putString("dob", dobCombined)
                                        .apply()

                                    session.userId.value = userId

                                    navController.navigate("onboard/$userId/$userName") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                                else {
                                    serverMessage = body?.message ?: "Registration failed"
                                }
                            }


                            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                                isLoading = false
                                serverMessage = "Network error"
                            }
                        })
                    },
                    enabled = formValid,
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                ) {
                    Text("REGISTER", fontFamily = poppins, fontSize = 18.sp, color = Color.White)
                }
            }

            if (serverMessage.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(serverMessage, color = Color.Yellow, fontFamily = poppins)
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.45f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

/* ---------------- REUSABLE COMPONENTS ---------------- */

@Composable
fun AnimatedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    font: FontFamily,
    error: Boolean = false,
    errorText: String = ""
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontFamily = font, color = Color.White) },
            leadingIcon = { Icon(icon, null, tint = Color.White) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),   // 🔥 more curved
            textStyle = TextStyle(color = Color.White, fontFamily = font),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(0.14f),
                unfocusedContainerColor = Color.White.copy(0.07f),
                focusedIndicatorColor = Color(0xFF4FC3F7),   // cyan
                unfocusedIndicatorColor = Color.White.copy(0.28f),
                cursorColor = Color(0xFF4FC3F7),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        if (error) {
            Spacer(Modifier.height(6.dp))
            Text(
                errorText,
                color = Color(0xFFFFD54F), // soft amber, not harsh yellow
                fontFamily = font,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AnimatedPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggle: () -> Unit,
    font: FontFamily,
    error: Boolean = false,
    errorText: String = ""
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontFamily = font, color = Color.White) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.White) },
            trailingIcon = {
                Icon(
                    if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    null,
                    tint = Color.White,
                    modifier = Modifier.clickable { onToggle() }
                )
            },
            visualTransformation =
                if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),   // 🔥 curved
            textStyle = TextStyle(color = Color.White, fontFamily = font),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(0.14f),
                unfocusedContainerColor = Color.White.copy(0.07f),
                focusedIndicatorColor = Color(0xFF4FC3F7),
                unfocusedIndicatorColor = Color.White.copy(0.28f),
                cursorColor = Color(0xFF4FC3F7),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        if (error) {
            Spacer(Modifier.height(6.dp))
            Text(
                errorText,
                color = Color(0xFFFFD54F),
                fontFamily = font,
                fontSize = 12.sp
            )
        }
    }
}


@Composable
fun DobBox(
    hint: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onChange(input.filter { it.isDigit() })
        },
        label = { Text(hint, color = Color.White) },
        singleLine = true,
        modifier = Modifier.width(92.dp),
        shape = RoundedCornerShape(16.dp),   // 🔥 softer curve
        textStyle = TextStyle(color = Color.White),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(0.14f),
            unfocusedContainerColor = Color.White.copy(0.07f),
            focusedIndicatorColor = Color(0xFF4FC3F7),
            unfocusedIndicatorColor = Color.White.copy(0.28f),
            cursorColor = Color(0xFF4FC3F7),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}


