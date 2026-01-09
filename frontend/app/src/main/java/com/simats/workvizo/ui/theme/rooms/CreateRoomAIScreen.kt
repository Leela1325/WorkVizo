package com.simats.workvizo.ui.theme.rooms

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*
import java.util.regex.Pattern

@Composable
fun CreateRoomAIScreen(
    navController: NavController,
    userId: String,
    userName: String
) {

    val safeUserName = Uri.encode(userName)
    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    /* ---------- STATE ---------- */
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("1") }
    var password by remember { mutableStateOf("") }

    /* ---------- ERROR STATE ---------- */
    var nameErr by remember { mutableStateOf("") }
    var descErr by remember { mutableStateOf("") }
    var startErr by remember { mutableStateOf("") }
    var endErr by remember { mutableStateOf("") }
    var peopleErr by remember { mutableStateOf("") }
    var passErr by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }

    val dateRegex = remember {
        Pattern.compile("""\d{4}-\d{2}-\d{2}""")
    }

    fun clearErrors() {
        nameErr = ""
        descErr = ""
        startErr = ""
        endErr = ""
        peopleErr = ""
        passErr = ""
    }

    fun validate(): Boolean {
        clearErrors()
        var valid = true

        if (name.isBlank()) {
            nameErr = "Project name is required"
            valid = false
        }

        if (description.isBlank()) {
            descErr = "Description is required"
            valid = false
        }

        if (!dateRegex.matcher(startDate).matches()) {
            startErr = "Invalid date format (YYYY-MM-DD)"
            valid = false
        }

        if (!dateRegex.matcher(endDate).matches()) {
            endErr = "Invalid date format (YYYY-MM-DD)"
            valid = false
        }

        if (startErr.isEmpty() && endErr.isEmpty() && startDate > endDate) {
            endErr = "End date must be after start date"
            valid = false
        }

        if (people.toIntOrNull() == null || people.toInt() <= 0) {
            peopleErr = "Enter valid people count"
            valid = false
        }

        if (password.length < 4) {
            passErr = "Password must be at least 4 characters"
            valid = false
        }

        return valid
    }

    /* ---------- UI ---------- */

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A002B),
                        Color(0xFF3A0CA3),
                        Color(0xFF7209B7)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            Text(
                "CREATE ROOM USING AI",
                fontFamily = poppins,
                fontSize = 22.sp,
                color = Color.White
            )

            Spacer(Modifier.height(20.dp))

            GradientInput(
                label = "Project Name",
                icon = Icons.Default.Title,
                value = name,
                error = nameErr,
                font = poppins
            ) { name = it }

            GradientInput(
                label = "Project Description",
                icon = Icons.Default.Description,
                value = description,
                error = descErr,
                font = poppins,
                minLines = 4
            ) { description = it }

            GradientInput(
                label = "Start Date (YYYY-MM-DD)",
                icon = Icons.Default.DateRange,
                value = startDate,
                error = startErr,
                font = poppins
            ) { startDate = it }

            GradientInput(
                label = "End Date (YYYY-MM-DD)",
                icon = Icons.Default.Event,
                value = endDate,
                error = endErr,
                font = poppins
            ) { endDate = it }

            GradientInput(
                label = "Number of People",
                icon = Icons.Default.People,
                value = people,
                error = peopleErr,
                font = poppins
            ) { people = it.filter(Char::isDigit) }

            GradientInput(
                label = "Room Password",
                icon = Icons.Default.Lock,
                value = password,
                error = passErr,
                font = poppins,
                isPassword = true
            ) { password = it }

            Spacer(Modifier.height(26.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                onClick = {
                    if (!validate()) return@Button

                    loading = true

                    api.createRoom(
                        name = name,
                        description = description,
                        startDate = startDate,
                        endDate = endDate,
                        scheduleType = "ai",
                        roomType = "ai",
                        numberOfPeople = people.toInt(),
                        roomPassword = password,
                        createdBy = userId
                    ).enqueue(object : Callback<CreateRoomResponse> {

                        override fun onResponse(
                            call: Call<CreateRoomResponse>,
                            response: Response<CreateRoomResponse>
                        ) {
                            loading = false
                            val body = response.body() ?: return

                            if (body.status == "success") {


                                    // 🔥 PASS DATA TO NEXT SCREEN
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("project_description", description)

                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("start_date", startDate)

                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("end_date", endDate)

                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("people_count", people.toInt())

                                    // 🔥 NOW NAVIGATE
                                    navController.navigate(
                                        "ai_schedule_create/$userId/$safeUserName/${body.room_id}/${body.room_code}/$password"
                                    )


                            }
                        }

                        override fun onFailure(call: Call<CreateRoomResponse>, t: Throwable) {
                            loading = false
                        }
                    })
                }
            ) {
                Text("CREATE WITH AI", fontFamily = poppins)
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
    }
}

/* ---------- INPUT ---------- */

@Composable
fun GradientInput(
    label: String,
    icon: ImageVector,
    value: String,
    error: String,
    font: FontFamily,
    minLines: Int = 1,
    isPassword: Boolean = false,
    onChange: (String) -> Unit
) {
    Column {

        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label, fontFamily = font, color = Color.White) },
            leadingIcon = { Icon(icon, null, tint = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            visualTransformation =
                if (isPassword) PasswordVisualTransformation()
                else VisualTransformation.None,
            textStyle = TextStyle(color = Color.White, fontFamily = font),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor =
                    if (error.isNotEmpty()) Color(0xFFFF6B6B) else Color(0xFFFFC107),
                unfocusedIndicatorColor =
                    if (error.isNotEmpty()) Color(0xFFFF6B6B) else Color.White.copy(0.6f),
                cursorColor = Color(0xFFFFC107)
            ),
            shape = RoundedCornerShape(18.dp)
        )

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                error,
                color = Color(0xFFFF6B6B),
                fontSize = 12.sp,
                fontFamily = font
            )
        }

        Spacer(Modifier.height(14.dp))
    }
}
