package com.simats.workvizo.ui.theme.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import kotlinx.coroutines.delay
import retrofit2.*

@Composable
fun DeleteProfileScreen(
    navController: NavController,
    userId: String
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var showConfirm by remember { mutableStateOf(false) }
    var showDeleted by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF1A0000),
            Color(0xFF330000),
            Color(0xFF1A0000)
        )
    )

    /* ---------- FINAL RED POPUP ---------- */
    if (showDeleted) {
        DeleteSuccessPopup {
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* ---------- HEADER ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
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

        Spacer(Modifier.height(30.dp))

        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(90.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Delete Account",
            fontFamily = poppinsBold,
            fontSize = 28.sp,
            color = Color.White
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = """
• Your account will be permanently removed
• All rooms created by you will be deleted
• This action cannot be undone
• You will lose access immediately
            """.trimIndent(),
            fontFamily = poppinsBold,
            fontSize = 14.sp,
            color = Color.White.copy(0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = { showConfirm = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F)
            )
        ) {
            Text(
                "CONFIRM DELETE",
                fontFamily = poppinsBold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }

    /* ---------- CONFIRMATION DIALOG ---------- */
    if (showConfirm) {
        Dialog(onDismissRequest = { showConfirm = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A0000)
                )
            ) {
                Column(
                    modifier = Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(60.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Are you absolutely sure?",
                        fontFamily = poppinsBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        OutlinedButton(
                            onClick = { showConfirm = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, Color.White)
                        ) {
                            Text("Cancel", fontFamily = poppinsBold)
                        }

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = {
                                isDeleting = true
                                api.deleteProfile(userId)
                                    .enqueue(object : Callback<GenericResponse> {
                                        override fun onResponse(
                                            call: Call<GenericResponse>,
                                            response: Response<GenericResponse>
                                        ) {
                                            isDeleting = false
                                            if (response.body()?.status == "success") {
                                                showConfirm = false
                                                showDeleted = true
                                            }
                                        }

                                        override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                            isDeleting = false
                                        }
                                    })
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Text(
                                if (isDeleting) "Deleting..." else "Delete",
                                fontFamily = poppinsBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DeleteSuccessPopup(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = {}) {

        LaunchedEffect(Unit) {
            delay(2000)
            onDismiss()
        }

        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1B0000)
            )
        ) {
            Column(
                modifier = Modifier.padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Account Deleted",
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 20.sp,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Your account has been permanently removed",
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 13.sp,
                    color = Color.White.copy(0.7f)
                )
            }
        }
    }
}
