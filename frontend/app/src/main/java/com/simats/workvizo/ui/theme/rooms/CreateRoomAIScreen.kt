
package com.simats.workvizo.ui.theme.rooms

import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
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
import java.util.*
import java.util.regex.Pattern

@Composable
fun CreateRoomAIScreen(
    navController: NavController,
    userId: String,
    userName: String
) {

    val context = LocalContext.current
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

    val calendar = Calendar.getInstance()

    /* ---------- DATE PICKER (NO PAST DATES) ---------- */
    fun openDatePicker(onPicked: (String) -> Unit) {
        val dialog = DatePickerDialog(
            context,
            { _, y, m, d ->
                val mm = (m + 1).toString().padStart(2, '0')
                val dd = d.toString().padStart(2, '0')
                onPicked("$y-$mm-$dd")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.minDate = System.currentTimeMillis()
        dialog.show()
    }

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
            startErr = "Select valid start date"
            valid = false
        }

        if (!dateRegex.matcher(endDate).matches()) {
            endErr = "Select valid end date"
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

            GradientInput("Project Name", Icons.Default.Title, name, nameErr, poppins) {
                name = it
            }

            GradientInput(
                "Project Description",
                Icons.Default.Description,
                description,
                descErr,
                poppins,
                minLines = 4
            ) { description = it }

            /* ---------- START DATE (CALENDAR) ---------- */
            DateInputAI(
                label = "Start Date",
                value = startDate,
                icon = Icons.Default.DateRange,
                error = startErr,
                font = poppins
            ) {
                openDatePicker { startDate = it }
            }

            /* ---------- END DATE (CALENDAR) ---------- */
            DateInputAI(
                label = "End Date",
                value = endDate,
                icon = Icons.Default.Event,
                error = endErr,
                font = poppins
            ) {
                openDatePicker { endDate = it }
            }

            GradientInput(
                "Number of People",
                Icons.Default.People,
                people,
                peopleErr,
                poppins
            ) { people = it.filter(Char::isDigit) }

            GradientInput(
                "Room Password",
                Icons.Default.Lock,
                password,
                passErr,
                poppins,
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

/* ---------- DATE INPUT (AI SCREEN) ---------- */
@Composable
fun DateInputAI(
    label: String,
    value: String,
    icon: ImageVector,
    error: String,
    font: FontFamily,
    onClick: () -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                enabled = false,
                label = { Text(label, fontFamily = font, color = Color.White) },
                leadingIcon = { Icon(icon, null, tint = Color.White) },
                placeholder = { Text("YYYY-MM-DD", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.White, fontFamily = font),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor =
                        if (error.isNotEmpty()) Color(0xFFFF6B6B) else Color.White.copy(0.6f),
                    disabledTextColor = Color.White,
                    disabledLabelColor = Color.White,
                    disabledLeadingIconColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp)
            )
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(error, color = Color(0xFFFF6B6B), fontSize = 12.sp, fontFamily = font)
        }

        Spacer(Modifier.height(14.dp))
    }
}

/* ---------- NORMAL INPUT ---------- */
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
