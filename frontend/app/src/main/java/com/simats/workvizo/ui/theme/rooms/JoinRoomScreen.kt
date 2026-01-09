package com.simats.workvizo.ui.theme.rooms

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
import com.simats.workvizo.R
import com.simats.workvizo.api.ApiService
import com.simats.workvizo.api.GenericResponse
import com.simats.workvizo.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                "Join Workspace",
                fontFamily = poppins,
                fontSize = 30.sp,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Enter the Room Code and Password shared by your team leader to join the workspace.",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.White.copy(0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.copy(0.08f))
                    .border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {

                Column {

                    JoinInput("Room Code", roomCode, poppins, false) {
                        roomCode = it.uppercase()
                    }

                    Spacer(Modifier.height(16.dp))

                    JoinInput("Room Password", roomPassword, poppins, true) {
                        roomPassword = it
                    }

                    Spacer(Modifier.height(26.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF)
                        ),
                        onClick = {

                            if (roomCode.isBlank() || roomPassword.isBlank()) {
                                errorMessage = "Room code and password are required"
                                showError = true
                                return@Button
                            }

                            loading = true

                            api.verifyJoinRoom(roomCode, roomPassword, userId)
                                .enqueue(object : Callback<GenericResponse> {

                                    override fun onResponse(
                                        call: Call<GenericResponse>,
                                        response: Response<GenericResponse>
                                    ) {

                                        if (response.body()?.status == "success") {

                                            api.joinRoom(roomCode, userId, roomPassword)
                                                .enqueue(object : Callback<GenericResponse> {

                                                    override fun onResponse(
                                                        call: Call<GenericResponse>,
                                                        response: Response<GenericResponse>
                                                    ) {
                                                        loading = false

                                                        if (response.body()?.status == "success") {
                                                            navController.navigate(
                                                                "joined_room_success/$roomCode/$userId/$userName"
                                                            )
                                                        } else {
                                                            errorMessage =
                                                                response.body()?.message ?: "Join failed"
                                                            showError = true
                                                        }
                                                    }

                                                    override fun onFailure(
                                                        call: Call<GenericResponse>,
                                                        t: Throwable
                                                    ) {
                                                        loading = false
                                                        errorMessage = "Network error"
                                                        showError = true
                                                    }
                                                })

                                        } else {
                                            loading = false
                                            errorMessage =
                                                response.body()?.message ?: "Invalid room"
                                            showError = true
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<GenericResponse>,
                                        t: Throwable
                                    ) {
                                        loading = false
                                        errorMessage = "Network error"
                                        showError = true
                                    }
                                })
                        }
                    ) {
                        Text("Join Room", fontFamily = poppins, color = Color.Black)
                    }
                }
            }
        }

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                confirmButton = {
                    TextButton(onClick = { showError = false }) {
                        Text("OK", fontFamily = poppins)
                    }
                },
                title = { Text("Join Failed", fontFamily = poppins) },
                text = { Text(errorMessage, fontFamily = poppins) }
            )
        }
    }
}

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
        shape = RoundedCornerShape(18.dp)
    )
}
