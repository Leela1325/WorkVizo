package com.workvizo.ui.theme.rooms
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.workvizo.R
import com.workvizo.api.ApiService
import com.workvizo.api.CreateRoomResponse
import com.workvizo.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun CreateRoomAIScreen(
    navController: NavController,
    userId: String,
    userName: String
) {

    val safeUserName = Uri.encode(userName)
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("1") }
    var password by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF1A002B),
            Color(0xFF3A0CA3),
            Color(0xFF7209B7),
            Color(0xFF1A002B)
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CREATE ROOM USING AI",
                        fontFamily = poppinsBold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                }

                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            /* ---------- INFO ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF7209B7), Color(0xFF560BAD))
                        )
                    )
                    .padding(18.dp)
            ) {
                Text(
                    text = "AI will automatically generate tasks and schedule.",
                    fontFamily = poppinsBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            GradientInput("Project / Room Name", Icons.Default.Title, name, poppinsBold) {
                name = it
            }

            Spacer(modifier = Modifier.height(16.dp))

            GradientInput(
                "Project Description",
                Icons.Default.Description,
                description,
                poppinsBold,
                minLines = 4
            ) {
                description = it
            }

            Spacer(modifier = Modifier.height(16.dp))
            GradientInput("Start Date (YYYY-MM-DD)", Icons.Default.DateRange, startDate, poppinsBold) {
                startDate = it
            }

            Spacer(modifier = Modifier.height(16.dp))
            GradientInput("End Date (YYYY-MM-DD)", Icons.Default.Event, endDate, poppinsBold) {
                endDate = it
            }

            Spacer(modifier = Modifier.height(16.dp))
            GradientInput("Number of People", Icons.Default.People, people, poppinsBold) {
                people = it.filter(Char::isDigit)
            }

            Spacer(modifier = Modifier.height(16.dp))
            GradientInput(
                "Room Password",
                Icons.Default.Lock,
                password,
                poppinsBold,
                isPassword = true
            ) {
                password = it
            }

            Spacer(modifier = Modifier.height(30.dp))

            /* ---------- CREATE ---------- */
            Button(
                onClick = {

                    error = ""

                    if (
                        name.isBlank() ||
                        description.isBlank() ||
                        startDate.isBlank() ||
                        endDate.isBlank() ||
                        password.isBlank()
                    ) {
                        error = "All fields are required"
                        return@Button
                    }

                    loading = true

                    api.createRoom(
                        name = name,
                        description = description,
                        startDate = startDate,
                        endDate = endDate,
                        scheduleType = "ai",
                        roomType = "ai",
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

                            if (response.isSuccessful && body?.status == "success") {

                                val roomCode = body.room_code ?: body.room_id

                                if (!roomCode.isNullOrEmpty()) {

                                    navController.navigate(
                                        "ai_schedule_create/$userId/$safeUserName/$roomCode/${password}"
                                    ) {
                                        popUpTo("create_room_ai") { inclusive = true }
                                    }
                                } else {
                                    error = "Invalid room data"
                                }

                            } else {
                                error = body?.message ?: "Creation failed"
                            }
                        }

                        override fun onFailure(call: Call<CreateRoomResponse>, t: Throwable) {
                            loading = false
                            error = "Network error"
                        }
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                )
            ) {
                Text(
                    text = "CREATE WITH AI",
                    fontFamily = poppinsBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = error, color = Color.Red, fontFamily = poppinsBold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

/* ---------- INPUT ---------- */

@Composable
fun GradientInput(
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
        leadingIcon = { Icon(icon, null, tint = Color.White) },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        visualTransformation = if (isPassword)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        textStyle = TextStyle(color = Color.White, fontFamily = font),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color(0xFFFFC107),
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.6f),
            cursorColor = Color(0xFFFFC107)
        ),
        shape = RoundedCornerShape(18.dp)
    )
}
