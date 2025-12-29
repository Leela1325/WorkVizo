package com.workvizo.ui.theme.rooms

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.workvizo.R
import com.workvizo.api.*
import retrofit2.*

@Composable
fun EditRoomDetailsScreen(
    navController: NavController,
    roomCode: String,
    userId: String
) {

    /* ---------------- STATE ---------------- */

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var roomId by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(true) }
    var showConfirm by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    /* ---------------- BACKGROUND ---------------- */

    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF141E30),
            Color(0xFF243B55),
            Color(0xFF141E30)
        )
    )

    /* ---------------- FETCH ROOM DETAILS ---------------- */

    LaunchedEffect(Unit) {
        api.getRoomDetails(roomCode)
            .enqueue(object : Callback<Map<String, Any>> {

                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    val body = response.body()
                    if (body != null) {
                        val room = body["room"] as? Map<*, *>
                        if (room != null) {
                            roomId = room["id"]?.toString() ?: ""
                            name = room["name"]?.toString() ?: ""
                            description = room["description"]?.toString() ?: ""
                            startDate = room["start_date"]?.toString() ?: ""
                            endDate = room["end_date"]?.toString() ?: ""
                            people = room["number_of_people"]?.toString() ?: ""
                        }
                    }
                    loading = false
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    loading = false
                }
            })
    }

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    /* ---------------- UI ---------------- */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        /* ---------- TOP BAR ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "EDIT ROOM DETAILS",
            fontFamily = poppins,
            fontSize = 26.sp,
            color = Color.White
        )

        Text(
            "Update room information. Leave fields unchanged to keep previous values.",
            fontFamily = poppins,
            fontSize = 14.sp,
            color = Color.White.copy(0.75f)
        )

        Spacer(Modifier.height(26.dp))

        RoomInput(
            label = "Room Name",
            placeholder = "Enter room name",
            value = name,
            font = poppins
        ) { name = it }

        RoomInput(
            label = "Description",
            placeholder = "Describe the purpose of this room",
            value = description,
            font = poppins,
            maxLines = 4
        ) { description = it }

        RoomInput(
            label = "Start Date",
            placeholder = "YYYY-MM-DD",
            value = startDate,
            font = poppins
        ) { startDate = it }

        RoomInput(
            label = "End Date",
            placeholder = "YYYY-MM-DD",
            value = endDate,
            font = poppins
        ) { endDate = it }

        RoomInput(
            label = "Number of People",
            placeholder = "Maximum participants",
            value = people,
            font = poppins
        ) { people = it }

        Spacer(Modifier.height(30.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF00C853)),
            onClick = {
                if (name.isBlank() || description.isBlank()) return@Button
                showConfirm = true
            }
        ) {
            Text("SAVE CHANGES", fontFamily = poppins)
        }
    }

    /* ---------------- CONFIRM POPUP ---------------- */

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = Color(0xFF1C2541),
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text("CONFIRM UPDATE", fontFamily = poppins, color = Color.White)
            },
            text = {
                Text(
                    "Do you want to save these changes?",
                    fontFamily = poppins,
                    color = Color.White.copy(0.8f)
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(Color(0xFF00C853)),
                    onClick = {

                        saving = true

                        api.editRoom(
                            roomId = roomId,
                            requestedBy = userId,
                            name = name,
                            description = description,
                            startDate = startDate,
                            endDate = endDate,
                            numberOfPeople = people
                        ).enqueue(object : Callback<GenericResponse> {

                            override fun onResponse(
                                call: Call<GenericResponse>,
                                response: Response<GenericResponse>
                            ) {
                                saving = false
                                showConfirm = false
                                navController.popBackStack()
                            }

                            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                saving = false
                                showConfirm = false
                            }
                        })
                    }
                ) {
                    Text("CONFIRM", fontFamily = poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("CANCEL", fontFamily = poppins, color = Color.White)
                }
            }
        )
    }
}

/* ---------------- REUSABLE INPUT ---------------- */

@Composable
fun RoomInput(
    label: String,
    placeholder: String,
    value: String,
    font: FontFamily,
    maxLines: Int = 1,
    onValueChange: (String) -> Unit
) {

    Text(label, fontFamily = font, color = Color.White)
    Spacer(Modifier.height(6.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                fontFamily = font,
                color = Color.White.copy(0.6f)
            )
        },
        textStyle = TextStyle(
            fontFamily = font,
            color = Color.White
        ),
        maxLines = maxLines,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF00E676),
            unfocusedBorderColor = Color.White.copy(0.4f),
            focusedContainerColor = Color(0xFF141E30),
            unfocusedContainerColor = Color(0xFF141E30),
            cursorColor = Color(0xFF00E676)
        )
    )

    Spacer(Modifier.height(16.dp))
}
