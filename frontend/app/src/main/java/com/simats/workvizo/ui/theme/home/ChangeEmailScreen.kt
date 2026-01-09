package com.simats.workvizo.ui.theme.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*

@Composable
fun ChangeEmailScreen(
    navController: NavController,
    userId: String
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var oldEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF05061E),
            Color(0xFF0B0F3B),
            Color(0xFF1B1464)
        )
    )

    /* ---------- SUCCESS POPUP (EXISTING ONE) ---------- */
    if (showSuccess) {
        EmailSuccessPopup {
            showSuccess = false
            navController.popBackStack()
        }
    }


    /* ---------- ERROR POPUP ---------- */
    if (errorMessage.isNotEmpty()) {
        EmailErrorPopup(message = errorMessage) {
            errorMessage = ""
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

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Change Email Address",
            fontFamily = poppinsBold,
            fontSize = 24.sp,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Verify your credentials before updating your email.",
            fontFamily = poppinsBold,
            fontSize = 13.sp,
            color = Color.White.copy(0.7f)
        )

        Spacer(Modifier.height(28.dp))

        EmailField("Current Email", "Enter old email", oldEmail) { oldEmail = it }
        PasswordField(
            "Password",
            "Enter password",
            password,
            showPassword,
            { showPassword = !showPassword }
        ) { password = it }
        EmailField("New Email", "Enter new email", newEmail) { newEmail = it }

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = {
                when {
                    oldEmail == newEmail ->
                        errorMessage = "New email must be different from old email"

                    !oldEmail.contains("@") || !newEmail.contains("@") ->
                        errorMessage = "Please enter valid email addresses"

                    else -> {
                        isSubmitting = true
                        api.changeEmail(userId, oldEmail, password, newEmail)
                            .enqueue(object : Callback<GenericResponse> {
                                override fun onResponse(
                                    call: Call<GenericResponse>,
                                    response: Response<GenericResponse>
                                ) {
                                    isSubmitting = false
                                    if (response.body()?.status == "success") {
                                        showSuccess = true
                                    } else {
                                        errorMessage =
                                            response.body()?.message
                                                ?: "Invalid credentials"
                                    }
                                }

                                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                    isSubmitting = false
                                    errorMessage = "Network error. Try again."
                                }
                            })
                    }
                }
            },
            enabled = oldEmail.isNotBlank() &&
                    password.isNotBlank() &&
                    newEmail.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00FF87)
            )
        ) {
            Text(
                text = if (isSubmitting) "Saving..." else "Save Changes",
                fontFamily = poppinsBold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}

/* ---------------- INPUTS ---------------- */

@Composable
fun EmailField(
    title: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    Column {
        Text(title, fontFamily = poppinsBold, color = Color.White)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(hint, fontFamily = poppinsBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = poppinsBold,
                color = Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(0.4f),
                cursorColor = Color(0xFF00E5FF)
            )
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun PasswordField(
    title: String,
    hint: String,
    value: String,
    show: Boolean,
    onToggle: () -> Unit,
    onValueChange: (String) -> Unit
) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    Column {
        Text(title, fontFamily = poppinsBold, color = Color.White)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(hint, fontFamily = poppinsBold) },
            visualTransformation = if (show)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(
                        if (show) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        null,
                        tint = Color.White
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = poppinsBold,
                color = Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(0.4f),
                cursorColor = Color(0xFF00E5FF)
            )
        )
        Spacer(Modifier.height(16.dp))
    }
}

/* ---------------- ERROR POPUP ---------------- */

@Composable
fun EmailErrorPopup(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0A0A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = message,
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}
@Composable
fun EmailSuccessPopup(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = {}) {

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            onDismiss()
        }

        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0B0F3B)
            )
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
                    text = "Email Updated",
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 20.sp,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Your email address has been updated successfully",
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 13.sp,
                    color = Color.White.copy(0.75f)
                )
            }
        }
    }
}

