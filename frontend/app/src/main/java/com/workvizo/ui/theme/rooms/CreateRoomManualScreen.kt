package com.workvizo.ui.theme.rooms

/* ---------- IMPORTS ---------- */
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workvizo.ui.viewmodel.RoomCreationViewModel

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.workvizo.R
import com.workvizo.api.*
import com.workvizo.api.RetrofitClient
import retrofit2.*

/* ===================================================== */
/* ============ CREATE ROOM (MANUAL) =================== */
/* ===================================================== */

@Composable
fun CreateRoomManualScreen(
    navController: NavController,
    userId: String,
    userName: String
)
 {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    /* ---------- FORM STATE ---------- */
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("1") }
    var password by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    /* ---------- VALIDATION ---------- */
    val formValid =
        name.isNotBlank() &&
                description.isNotBlank() &&
                startDate.isNotBlank() &&
                endDate.isNotBlank() &&
                password.isNotBlank()

    /* ---------- BACKGROUND ---------- */
    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF020024),
            Color(0xFF090979),
            Color(0xFF1B1464),
            Color(0xFF020024)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .animateContentSize()
        ) {

            /* ---------- HEADER ---------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Text(
                        "CREATE ROOM",
                        fontFamily = poppinsBold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(Modifier.height(26.dp))

            /* ---------- INPUTS ---------- */

            DarkInput("Room Name", Icons.Default.MeetingRoom, name, poppinsBold) { name = it }
            Spacer(Modifier.height(16.dp))

            DarkInput("Description", Icons.Default.Description, description, poppinsBold, 4) {
                description = it
            }

            Spacer(Modifier.height(16.dp))
            DarkInput("Start Date (YYYY-MM-DD)", Icons.Default.DateRange, startDate, poppinsBold) {
                startDate = it
            }

            Spacer(Modifier.height(16.dp))
            DarkInput("End Date (YYYY-MM-DD)", Icons.Default.Event, endDate, poppinsBold) {
                endDate = it
            }

            Spacer(Modifier.height(16.dp))
            DarkInput("Number of People", Icons.Default.People, people, poppinsBold) {
                people = it.filter(Char::isDigit)
            }

            Spacer(Modifier.height(16.dp))
            DarkInput(
                "Room Password (Required)",
                Icons.Default.Lock,
                password,
                poppinsBold,
                isPassword = true
            ) { password = it }

            Spacer(Modifier.height(30.dp))

            /* ---------- CONFIRM BUTTON ---------- */

            Button(
                onClick = {

                    loading = true
                    error = ""

                    api.createRoom(
                        name = name,
                        description = description,
                        startDate = startDate,
                        endDate = endDate,
                        scheduleType = "manual",
                        roomType = "manual",
                        numberOfPeople = people.toIntOrNull() ?: 1,
                        roomPassword = password,
                        createdBy = userId
                    ).enqueue(object : Callback<CreateRoomResponse> {

                        override fun onResponse(
                            call: Call<CreateRoomResponse>,
                            response: Response<CreateRoomResponse>
                        ) {
                            loading = false
                            val body = response.body()

                            if (body?.status == "success") {

                                // ✅ FIXED NAVIGATION (MATCHES NAVGRAPH)
                                navController.navigate(
                                    "task_scheduling/$userId/$userName/${body.room_id}/${body.room_code}/$password"
                                )



                            } else {
                                error = body?.message ?: "Room creation failed"
                            }
                        }

                        override fun onFailure(call: Call<CreateRoomResponse>, t: Throwable) {
                            loading = false
                            error = "Network error"
                        }
                    })
                },
                enabled = formValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("CONFIRM & CREATE ROOM")
            }

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = Color.Red, fontFamily = poppinsBold)
            }

            Spacer(Modifier.height(40.dp))
        }

        /* ---------- LOADING ---------- */
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
    }
}

/* ===================================================== */
/* ================= DARK INPUT ======================== */
/* ===================================================== */

@Composable
fun DarkInput(
    label: String,
    icon: ImageVector,
    value: String,
    font: FontFamily,
    minLines: Int = 1,
    isPassword: Boolean = false,
    onChange: (String) -> Unit
) {

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontFamily = font, color = Color.White) },
        leadingIcon = { Icon(icon, null, tint = Color(0xFF00E5FF)) },
        visualTransformation =
            if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        minLines = minLines,
        textStyle = TextStyle(color = Color.White, fontFamily = font, fontSize = 16.sp),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF0B0F3B),
            unfocusedContainerColor = Color(0xFF0B0F3B),
            focusedIndicatorColor = Color(0xFF00E5FF),
            unfocusedIndicatorColor = Color.White.copy(0.3f),
            cursorColor = Color(0xFF00E5FF),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}
