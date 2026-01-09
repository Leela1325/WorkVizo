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
import kotlinx.coroutines.delay
import retrofit2.*

@Composable
fun ChangePasswordScreen(
    navController: NavController,
    userId: String
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val bgGradient = Brush.verticalGradient(
        listOf(Color(0xFF0D021F), Color(0xFF2A0845), Color(0xFF0D021F))
    )

    /* ---------- SUCCESS POPUP ---------- */
    if (showSuccess) {
        PasswordSuccessPopup(
            title = "Password Updated!",
            message = "Your password has been changed successfully"
        ) {
            showSuccess = false
            navController.popBackStack()
        }
    }

    /* ---------- ERROR POPUP ---------- */
    if (errorMessage != null) {
        ErrorPopup(message = errorMessage!!) {
            errorMessage = null
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
            text = "Change Password",
            fontFamily = poppinsBold,
            fontSize = 24.sp,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Use a strong password to keep your account secure.",
            fontFamily = poppinsBold,
            fontSize = 13.sp,
            color = Color.White.copy(0.7f)
        )

        Spacer(Modifier.height(28.dp))

        /* ---------- PASSWORD INPUTS ---------- */

        PasswordField(
            label = "Current Password",
            value = oldPassword,
            show = showOld,
            onToggle = { showOld = !showOld },
            onValueChange = { oldPassword = it }
        )

        PasswordField(
            label = "New Password",
            value = newPassword,
            show = showNew,
            onToggle = { showNew = !showNew },
            onValueChange = { newPassword = it }
        )

        PasswordField(
            label = "Confirm Password",
            value = confirmPassword,
            show = showConfirm,
            onToggle = { showConfirm = !showConfirm },
            onValueChange = { confirmPassword = it }
        )

        Spacer(Modifier.height(30.dp))

        /* ---------- SUBMIT BUTTON ---------- */
        Button(
            onClick = {

                /* ---------- VALIDATIONS (ADDED ONLY) ---------- */
                when {
                    oldPassword.isBlank() ||
                            newPassword.isBlank() ||
                            confirmPassword.isBlank() -> {
                        errorMessage = "All fields are required"
                        return@Button
                    }

                    newPassword.length < 8 -> {
                        errorMessage = "New password must be at least 8 characters"
                        return@Button
                    }

                    newPassword == oldPassword -> {
                        errorMessage = "New password must be different from old password"
                        return@Button
                    }

                    newPassword != confirmPassword -> {
                        errorMessage = "New password and confirm password do not match"
                        return@Button
                    }
                }

                /* ---------- API CALL (UNCHANGED) ---------- */
                isSubmitting = true

                api.changePassword(
                    userId = userId,
                    oldPassword = oldPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword   // ✅ THIS FIXES THE ERROR
                ).enqueue(object : Callback<GenericResponse> {

                    override fun onResponse(
                        call: Call<GenericResponse>,
                        response: Response<GenericResponse>
                    ) {
                        isSubmitting = false
                        val body = response.body()

                        if (body?.status == "success") {
                            showSuccess = true
                        } else {
                            errorMessage = body?.message ?: "Something went wrong"
                        }
                    }

                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        isSubmitting = false
                        errorMessage = "Network error. Please try again."
                    }
                })
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF4081)
            )
        ) {
            Text(
                text = if (isSubmitting) "Updating..." else "Update Password",
                fontFamily = poppinsBold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

    }
}

/* ================= PASSWORD FIELD ================= */

@Composable
fun PasswordField(
    label: String,
    value: String,
    show: Boolean,
    onToggle: () -> Unit,
    onValueChange: (String) -> Unit
) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = poppinsBold) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        visualTransformation =
            if (show) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector =
                        if (show) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        },
        textStyle = LocalTextStyle.current.copy(
            fontFamily = poppinsBold,
            color = Color.White
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFF4081),
            unfocusedBorderColor = Color.White.copy(0.4f),
            cursorColor = Color(0xFFFF4081)
        )
    )
}
@Composable
fun PasswordSuccessPopup(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    Dialog(onDismissRequest = {}) {
        LaunchedEffect(Unit) {
            delay(2000)
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
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF00FF87),
                    modifier = Modifier.size(60.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = title,
                    fontFamily = poppinsBold,
                    fontSize = 18.sp,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = message,
                    fontFamily = poppinsBold,
                    fontSize = 13.sp,
                    color = Color.White.copy(0.7f)
                )
            }
        }
    }
}

@Composable
fun ErrorPopup(
    message: String,
    onDismiss: () -> Unit
) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    Dialog(onDismissRequest = {}) {
        LaunchedEffect(Unit) {
            delay(2000)
            onDismiss()
        }

        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A0000)
            )
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Error",
                    fontFamily = poppinsBold,
                    fontSize = 18.sp,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = message,
                    fontFamily = poppinsBold,
                    fontSize = 13.sp,
                    color = Color.White.copy(0.8f)
                )
            }
        }
    }
}
