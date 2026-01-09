package com.simats.workvizo.ui.theme.login

import android.accounts.AccountManager
import android.app.Activity
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import com.simats.workvizo.ui.viewmodel.UserSessionViewModel
import retrofit2.*

@Composable
fun LoginScreen(navController: NavController) {

    val session: UserSessionViewModel = viewModel()
    val apiService = RetrofitClient.instance.create(ApiService::class.java)

    val context = LocalContext.current
    val activity = context as Activity

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val poppinsRegular = FontFamily(Font(R.font.poppins_bold))

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var serverMessage by remember { mutableStateOf("") }

    /* ---------------- VALIDATION ---------------- */

    val emailError =
        email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val passwordError =
        password.isNotEmpty() && password.length < 6

    val formValid =
        email.isNotEmpty() && password.isNotEmpty() &&
                !emailError && !passwordError

    /* ---------------- BACKGROUND ---------------- */

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF0B132B),
            Color(0xFF1C2541),
            Color(0xFF3A86FF)
        )
    )

    /* ---------------- EMAIL PICKER (SIMPLE GOOGLE EMAIL ACCESS) ---------------- */

    val emailPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK && result.data != null) {

                val pickedEmail =
                    result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)

                if (!pickedEmail.isNullOrEmpty()) {

                    serverMessage = "Checking $pickedEmail"

                    apiService.login(
                        pickedEmail,
                        "",
                        "google"
                    ).enqueue(object : Callback<LoginResponse> {

                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            val body = response.body()

                            if (body?.status == "success" && body.user != null) {
                                val userId = body.user.id
                                val userName = Uri.encode(body.user.name)
                                session.userId.value = userId

                                navController.navigate("home/$userId/$userName") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                serverMessage =
                                    body?.message ?: "Account does not exists"
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            serverMessage = "Network error"
                        }
                    })
                }
            }
        }

    /* ---------------- SHIMMER ---------------- */

    val infiniteTransition = rememberInfiniteTransition(label = "login_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -400f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val animatedButtonBrush = Brush.linearGradient(
        colors = if (formValid)
            listOf(Color(0xFF00E5FF), Color.White.copy(0.85f), Color(0xFF3A86FF))
        else
            listOf(Color.Gray.copy(0.55f), Color.Gray.copy(0.85f), Color.Gray.copy(0.55f)),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 300f, 0f)
    )

    /* ---------------- UI ---------------- */

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(22.dp)
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(0.18f))
                .border(1.5.dp, Color.White.copy(0.35f), RoundedCornerShape(30.dp))
                .padding(30.dp)
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    "WorkVizo",
                    fontSize = 36.sp,
                    fontFamily = poppinsBold,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    "Proof-driven tasks • Smart teams",
                    fontSize = 14.sp,
                    fontFamily = poppinsRegular,
                    color = Color.White.copy(0.85f)
                )

                Spacer(modifier = Modifier.height(30.dp))

                GlassTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.Email,
                    isError = emailError
                )

                Spacer(modifier = Modifier.height(20.dp))

                GlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    isError = passwordError
                )

                Spacer(modifier = Modifier.height(26.dp))

                /* ---------------- LOGIN BUTTON ---------------- */

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(animatedButtonBrush)
                ) {
                    Button(
                        onClick = {
                            serverMessage = "Logging in..."
                            apiService.login(email, password)
                                .enqueue(object : Callback<LoginResponse> {

                                    override fun onResponse(
                                        call: Call<LoginResponse>,
                                        response: Response<LoginResponse>
                                    ) {
                                        val body = response.body()
                                        if (body?.status == "success" && body.user != null) {
                                            val userId = body.user.id
                                            val userName = Uri.encode(body.user.name)
                                            session.userId.value = userId
                                            navController.navigate("home/$userId/$userName") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            serverMessage = body?.message ?: "Login failed"
                                        }
                                    }

                                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                        serverMessage = "Network error"
                                    }
                                })
                        },
                        enabled = formValid,
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            "LOGIN",
                            fontSize = 18.sp,
                            fontFamily = poppinsBold,
                            color = Color.White
                        )
                    }
                }

                /* ---------------- OR ---------------- */

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color.White.copy(0.4f))
                    Text("  OR  ", color = Color.White, fontFamily = poppinsBold)
                    Divider(modifier = Modifier.weight(1f), color = Color.White.copy(0.4f))
                }

                /* ---------------- GOOGLE EMAIL PICKER ---------------- */

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .clickable {
                            val intent = AccountManager.newChooseAccountIntent(
                                null,
                                null,
                                arrayOf("com.google"),
                                false,
                                null,
                                null,
                                null,
                                null
                            )
                            emailPickerLauncher.launch(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "Google",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Continue with Google",
                            fontFamily = poppinsBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                if (serverMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(serverMessage, color = Color.White, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No account?",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = poppinsRegular
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Sign Up",
                        color = Color(0xFF00E5FF),
                        fontSize = 14.sp,
                        fontFamily = poppinsBold,
                        modifier = Modifier.clickable {
                            navController.navigate("register")
                        }
                    )
                }

            }
        }
        Text(
            text = "2026 © Powered by SIMATS Engineering",
            color = Color.White.copy(0.75f),
            fontSize = 12.sp,
            fontFamily = poppinsRegular,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
    /* ---------------- FOOTER ---------------- */



}

/* ---------------- GLASS TEXT FIELD ---------------- */

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    isError: Boolean = false
) {
    val borderBrush = Brush.horizontalGradient(
        if (isError) listOf(Color.Red, Color.Red)
        else listOf(Color(0xFF00E5FF), Color(0xFF3A86FF))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(2.dp, borderBrush, RoundedCornerShape(18.dp))
            .background(Color.White.copy(0.08f))
            .padding(horizontal = 6.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.White) },
            leadingIcon = { Icon(icon, null, tint = Color.White) },
            trailingIcon = if (isPassword) {
                {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
                        tint = Color.White,
                        modifier = Modifier.clickable { onTogglePassword?.invoke() }
                    )
                }
            } else null,
            visualTransformation =
                if (isPassword && !passwordVisible)
                    PasswordVisualTransformation()
                else VisualTransformation.None,
            singleLine = true,
            isError = isError,
            textStyle = TextStyle(color = Color.White),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedLabelColor = if (isError) Color.Red else Color(0xFF00E5FF),
                unfocusedLabelColor = Color.White,
                errorLabelColor = Color.Red,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorTextColor = Color.White,
                cursorColor = Color(0xFF00E5FF)
            )
        )
    }
}
