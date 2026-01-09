package com.simats.workvizo.ui.theme.rooms

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
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*
import java.util.regex.Pattern

@Composable
fun EditRoomDetailsScreen(
    navController: NavController,
    roomCode: String,
    userId: String
) {

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

    /* ---------------- VALIDATIONS ---------------- */

    val dateRegex = remember {
        Pattern.compile("""\d{4}-\d{2}-\d{2}""")
    }

    val isNameValid = name.trim().length >= 3
    val isDescValid = description.trim().length >= 10
    val isStartValid = dateRegex.matcher(startDate).matches()
    val isEndValid = dateRegex.matcher(endDate).matches()
    val isPeopleValid = people.all { it.isDigit() } && people.isNotBlank() && people.toInt() > 0

    val isDateOrderValid =
        isStartValid && isEndValid && startDate <= endDate

    val formValid =
        isNameValid &&
                isDescValid &&
                isStartValid &&
                isEndValid &&
                isDateOrderValid &&
                isPeopleValid

    /* ---------------- FETCH ROOM ---------------- */

    LaunchedEffect(Unit) {
        api.getRoomDetails(roomCode).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                val room = response.body()?.get("room") as? Map<*, *> ?: return
                roomId = room["id"].toString()
                name = room["name"].toString()
                description = room["description"].toString()
                startDate = room["start_date"].toString()
                endDate = room["end_date"].toString()
                people = room["number_of_people"].toString()
                loading = false
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                loading = false
            }
        })
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    /* ---------------- UI ---------------- */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF141E30), Color(0xFF243B55))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text("EDIT ROOM DETAILS", fontFamily = poppins, fontSize = 26.sp, color = Color.White)

        Spacer(Modifier.height(24.dp))

        RoomInputField("Room Name", "Min 3 characters", name, poppins, !isNameValid) { name = it }
        RoomInputField("Description", "Min 10 characters", description, poppins, !isDescValid, 4) { description = it }
        RoomInputField("Start Date", "YYYY-MM-DD", startDate, poppins, !isStartValid) { startDate = it }
        RoomInputField("End Date", "YYYY-MM-DD", endDate, poppins, !isEndValid || !isDateOrderValid) { endDate = it }
        RoomInputField("Number of People", "Digits only", people, poppins, !isPeopleValid) { people = it }

        Spacer(Modifier.height(28.dp))

        Button(
            enabled = formValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            onClick = { showConfirm = true }
        ) {
            Text("SAVE CHANGES", fontFamily = poppins)
        }
    }

    /* ---------------- CONFIRM ---------------- */

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = Color(0xFF1C2541),
            icon = {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00E676))
            },
            title = { Text("Confirm Update", fontFamily = poppins, color = Color.White) },
            text = { Text("Save room changes?", fontFamily = poppins, color = Color.White) },
            confirmButton = {
                Button(onClick = {
                    api.editRoom(
                        roomId, userId, name, description, startDate, endDate, people
                    ).enqueue(object : Callback<GenericResponse> {
                        override fun onResponse(
                            call: Call<GenericResponse>,
                            response: Response<GenericResponse>
                        ) {
                            showConfirm = false
                            navController.popBackStack()
                        }

                        override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                            showConfirm = false
                        }
                    })
                }) {
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

/* ---------------- RENAMED INPUT (NO CONFLICT) ---------------- */

@Composable
fun RoomInputField(
    label: String,
    placeholder: String,
    value: String,
    font: FontFamily,
    isError: Boolean,
    maxLines: Int = 1,
    onChange: (String) -> Unit
) {
    Text(label, fontFamily = font, color = Color.White)
    Spacer(Modifier.height(6.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = {
            Text(placeholder, fontFamily = font, color = Color.White.copy(0.6f))
        },
        maxLines = maxLines,
        isError = isError,
        textStyle = TextStyle(color = Color.White, fontFamily = font),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) Color.Red else Color(0xFF00E676),
            unfocusedBorderColor = Color.White.copy(0.4f),
            focusedContainerColor = Color(0xFF141E30),
            unfocusedContainerColor = Color(0xFF141E30),
            cursorColor = Color(0xFF00E676)
        )
    )

    Spacer(Modifier.height(16.dp))
}
