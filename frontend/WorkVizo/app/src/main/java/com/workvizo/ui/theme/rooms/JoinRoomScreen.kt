package com.workvizo.ui.theme.rooms

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.workvizo.R
import com.workvizo.api.ApiService
import com.workvizo.api.RetrofitClient
import retrofit2.*

@Composable
fun JoinRoomScreen(
    navController: NavController,
    userId: String,
    userName: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var roomCode by remember { mutableStateOf("") }
    var roomPassword by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    /* ---------- BACKGROUND ---------- */
    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {

        /* ---------- TOP LOGO ONLY ---------- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(44.dp)
            )
        }

        /* ---------- CONTENT ---------- */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Join Workspace",
                fontFamily = poppins,
                fontSize = 30.sp,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Enter the Room Code and Password shared by your team leader to join the workspace and start collaborating.",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.White.copy(0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(32.dp))

            /* ---------- CARD ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.copy(0.08f))
                    .border(
                        1.dp,
                        Color.White.copy(0.15f),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(20.dp)
            ) {

                Column {

                    JoinInput(
                        label = "Room Code",
                        value = roomCode,
                        font = poppins,
                        isPassword = false
                    ) { roomCode = it.uppercase() }

                    Spacer(Modifier.height(16.dp))

                    JoinInput(
                        label = "Room Password",
                        value = roomPassword,
                        font = poppins,
                        isPassword = true
                    ) { roomPassword = it }

                    Spacer(Modifier.height(26.dp))

                    Button(
                        onClick = {

                            if (roomCode.isBlank() || roomPassword.isBlank()) {
                                errorMessage = "Room code and password are required."
                                showError = true
                                return@Button
                            }

                            loading = true

                            api.verifyJoinRoom(roomCode, roomPassword, userId)
                                .enqueue(object : Callback<Map<String, Any>> {

                                    override fun onResponse(
                                        call: Call<Map<String, Any>>,
                                        response: Response<Map<String, Any>>
                                    ) {
                                        loading = false
                                        val body = response.body()

                                        if (body?.get("status") == "success") {
                                            navController.navigate("joined_room_success")

                                        } else {
                                            errorMessage = body?.get("message") as String
                                            showError = true
                                        }
                                    }

                                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                        loading = false
                                        errorMessage = "Network error. Please try again."
                                        showError = true
                                    }
                                })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF)
                        )
                    ) {
                        Text(
                            "Join Room",
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(60.dp))
        }

        /* ---------- LOADING ---------- */
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.45f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        /* ---------- ERROR POPUP ---------- */
        if (showError) {
            AlertDialog(
                shape = RoundedCornerShape(22.dp),
                containerColor = Color(0xFF1F2A44),
                onDismissRequest = { showError = false },
                confirmButton = {
                    TextButton(onClick = { showError = false }) {
                        Text("OK", fontFamily = poppins, color = Color(0xFF00E5FF))
                    }
                },
                title = {
                    Text("Join Failed", fontFamily = poppins, color = Color.White)
                },
                text = {
                    Text(
                        errorMessage,
                        fontFamily = poppins,
                        color = Color.White.copy(0.9f)
                    )
                }
            )
        }
    }
}

/* ---------- INPUT FIELD ---------- */

@Composable
fun JoinInput(
    label: String,
    value: String,
    font: FontFamily,
    isPassword: Boolean,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontFamily = font) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation =
            if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = font,
            fontSize = 16.sp
        ),
        shape = RoundedCornerShape(18.dp)
    )
}
