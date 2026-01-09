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
fun ChangeDobScreen(
    navController: NavController,
    userId: String
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var password by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF021024),
            Color(0xFF052659),
            Color(0xFF021024)
        )
    )

    /* ---------- SUCCESS ---------- */
    if (showSuccess) {
        DobSuccessPopup {
            showSuccess = false
            navController.popBackStack()
        }
    }

    /* ---------- ERROR ---------- */
    if (errorMessage.isNotEmpty()) {
        DobErrorPopup(errorMessage) {
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
            "Change Date of Birth",
            fontFamily = poppinsBold,
            fontSize = 24.sp,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Enter DOB in YYYY-MM-DD format and verify with password",
            fontFamily = poppinsBold,
            fontSize = 13.sp,
            color = Color.White.copy(0.7f)
        )

        Spacer(Modifier.height(28.dp))

        /* ---------- DOB INPUT ---------- */
        DobInputField(
            label = "New Date of Birth",
            hint = "YYYY-MM-DD",
            value = dob,
            onValueChange = { dob = it }
        )

        PasswordInputField(
            label = "Password",
            hint = "Enter your password",
            value = password,
            show = showPassword,
            onToggle = { showPassword = !showPassword },
            onValueChange = { password = it }
        )

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = {
                if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(dob)) {
                    errorMessage = "Invalid date format"
                    return@Button
                }

                isSubmitting = true
                api.changeDob(userId, password, dob)
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
                                    response.body()?.message ?: "Update failed"
                            }
                        }

                        override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                            isSubmitting = false
                            errorMessage = "Network error"
                        }
                    })
            },
            enabled = password.isNotBlank() && dob.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00E5FF)
            )
        ) {
            Text(
                if (isSubmitting) "Updating..." else "Update DOB",
                fontFamily = poppinsBold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}
@Composable
fun DobSuccessPopup(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            onDismiss()
        }
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F3B))
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = Color(0xFF00FF87), modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text("DOB Updated",
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    color = Color.White, fontSize = 20.sp)
                Text("Your date of birth was updated successfully",
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    color = Color.White.copy(0.7f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun DobErrorPopup(message: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0A0A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Error, null,
                    tint = Color.Red, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(10.dp))
                Text(message,
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    color = Color.White, fontSize = 14.sp)
            }
        }
    }
}
@Composable
fun DobInputField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    Column {
        Text(
            text = label,
            fontFamily = poppinsBold,
            fontSize = 14.sp,
            color = Color.White
        )

        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = hint, fontFamily = poppinsBold)
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
@Composable
fun PasswordInputField(
    label: String,
    hint: String,
    value: String,
    show: Boolean,
    onToggle: () -> Unit,
    onValueChange: (String) -> Unit
) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    Column {
        Text(
            text = label,
            fontFamily = poppinsBold,
            fontSize = 14.sp,
            color = Color.White
        )

        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = hint, fontFamily = poppinsBold)
            },
            visualTransformation = if (show)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (show)
                            Icons.Default.VisibilityOff
                        else
                            Icons.Default.Visibility,
                        contentDescription = null,
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
