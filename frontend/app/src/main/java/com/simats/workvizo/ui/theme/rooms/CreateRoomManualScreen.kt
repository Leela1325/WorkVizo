package com.simats.workvizo.ui.theme.rooms

import android.app.DatePickerDialog
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*
import java.util.*

@Composable
fun CreateRoomManualScreen(
    navController: NavController,
    userId: String,
    userName: String
) {

    val context = LocalContext.current
    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    /* ---------- STATES ---------- */
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("1") }
    var password by remember { mutableStateOf("") }

    /* ---------- TOUCHED FLAGS ---------- */
    var nameTouched by remember { mutableStateOf(false) }
    var descTouched by remember { mutableStateOf(false) }
    var startTouched by remember { mutableStateOf(false) }
    var endTouched by remember { mutableStateOf(false) }
    var peopleTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    var submitAttempted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var apiError by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()

    /* ---------- DATE PICKER ---------- */
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

    /* ---------- VALIDATION ---------- */
    val nameValid = name.length >= 3
    val descValid = desc.length >= 10
    val startValid = startDate.isNotEmpty()
    val endValid = endDate.isNotEmpty() && endDate >= startDate
    val peopleValid = (people.toIntOrNull() ?: 0) > 0
    val passwordValid = password.length >= 6

    val formValid =
        nameValid && descValid && startValid &&
                endValid && peopleValid && passwordValid

    /* ---------- BACKGROUND ---------- */
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF020024), Color(0xFF090979), Color(0xFF1B1464))
    )

    Box(modifier = Modifier.fillMaxSize().background(bg)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .animateContentSize()
        ) {

            /* ---------- HEADER ---------- */
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Text("CREATE ROOM", fontFamily = poppins, fontSize = 24.sp, color = Color.White)
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            /* ---------- ROOM NAME ---------- */
            ValidatedInput("Room Name", Icons.Default.MeetingRoom, name, poppins) {
                name = it
                nameTouched = true
            }
            if ((nameTouched || submitAttempted) && !nameValid)
                ErrorText("Minimum 3 characters")

            /* ---------- DESCRIPTION ---------- */
            ValidatedInput("Description", Icons.Default.Description, desc, poppins, 4) {
                desc = it
                descTouched = true
            }
            if ((descTouched || submitAttempted) && !descValid)
                ErrorText("Minimum 10 characters")

            /* ---------- START DATE ---------- */
            DateFieldFixed("Start Date", startDate, Icons.Default.DateRange) {
                openDatePicker {
                    startDate = it
                    startTouched = true
                    if (endDate.isNotEmpty() && endDate < startDate) endDate = ""
                }
            }
            if ((startTouched || submitAttempted) && !startValid)
                ErrorText("Select start date")

            /* ---------- END DATE ---------- */
            DateFieldFixed("End Date", endDate, Icons.Default.Event) {
                openDatePicker {
                    endDate = it
                    endTouched = true
                }
            }
            if ((endTouched || submitAttempted) && !endValid)
                ErrorText("End date must be after start date")

            /* ---------- PEOPLE ---------- */
            ValidatedInput("Number of People", Icons.Default.People, people, poppins) {
                people = it.filter(Char::isDigit)
                peopleTouched = true
            }
            if ((peopleTouched || submitAttempted) && !peopleValid)
                ErrorText("At least 1 person required")

            /* ---------- PASSWORD ---------- */
            ValidatedInput(
                "Room Password",
                Icons.Default.Lock,
                password,
                poppins,
                isPassword = true
            ) {
                password = it
                passwordTouched = true
            }
            if ((passwordTouched || submitAttempted) && !passwordValid)
                ErrorText("Minimum 6 characters")

            Spacer(Modifier.height(30.dp))

            /* ---------- SUBMIT ---------- */
            Button(
                enabled = formValid && !loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                onClick = {
                    submitAttempted = true
                    if (!formValid) return@Button

                    loading = true
                    apiError = ""

                    api.createRoom(
                        name, desc, startDate, endDate,
                        "manual", "manual",
                        people.toInt(), password, userId
                    ).enqueue(object : Callback<CreateRoomResponse> {
                        override fun onResponse(
                            call: Call<CreateRoomResponse>,
                            response: Response<CreateRoomResponse>
                        ) {
                            loading = false
                            val body = response.body()
                            if (body?.status == "success") {
                                navController.navigate(
                                    "task_scheduling/$userId/${Uri.encode(userName)}/${body.room_id}/${body.room_code}/$password/$startDate/$endDate"
                                )
                            } else apiError = body?.message ?: "Creation failed"
                        }

                        override fun onFailure(call: Call<CreateRoomResponse>, t: Throwable) {
                            loading = false
                            apiError = "Network error"
                        }
                    })
                }
            ) {
                Text("CONFIRM & CREATE ROOM", fontFamily = poppins)
            }

            if (apiError.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(apiError, color = Color.Red)
            }
        }

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

/* ---------- DATE FIELD ---------- */
@Composable
fun DateFieldFixed(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            enabled = false,
            label = { Text(label, color = Color.White) },
            leadingIcon = { Icon(icon, null, tint = Color.White) },
            trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = Color.White) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.White.copy(0.4f),
                disabledTextColor = Color.White,
                disabledLabelColor = Color.White.copy(0.7f),
                disabledLeadingIconColor = Color.White,
                disabledTrailingIconColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp)
        )
    }
    Spacer(Modifier.height(8.dp))
}

/* ---------- INPUT ---------- */
@Composable
fun ValidatedInput(
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
        minLines = minLines,
        visualTransformation =
            if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = Color.White, fontFamily = font),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF00E5FF),
            unfocusedBorderColor = Color.White.copy(0.4f),
            cursorColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    )
    Spacer(Modifier.height(8.dp))
}

/* ---------- ERROR TEXT ---------- */
@Composable
fun ErrorText(text: String) {
    Text(
        text,
        color = Color.Red,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
    )
}
