package com.simats.workvizo.ui.theme.rooms

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*
import kotlinx.coroutines.delay

@Composable
fun AddTaskScreen(
    navController: NavController,
    roomId: String,
    userId: String   // 🔥 ADD THIS
){

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    /* ---------- STATE ---------- */
    var taskName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var assignedEmail by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var popupMsg by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    /* ---------- BACKGROUND (OLD STYLE) ---------- */
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF020617), Color(0xFF0F172A))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {

        /* ---------- HEADER ---------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Add Task",
                fontFamily = poppins,
                fontSize = 22.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.height(26.dp))

        /* ---------- INPUTS (OLD DESIGN FEEL) ---------- */

        TaskInputOld("Task Name", taskName, poppins) {
            taskName = it
        }

        Spacer(Modifier.height(14.dp))

        TaskInputOld("Start Date (YYYY-MM-DD)", startDate, poppins) {
            startDate = it
        }

        Spacer(Modifier.height(14.dp))

        TaskInputOld("End Date (YYYY-MM-DD)", endDate, poppins) {
            endDate = it
        }

        Spacer(Modifier.height(14.dp))

        TaskInputOld("Assigned Email", assignedEmail, poppins) {
            assignedEmail = it
        }

        Spacer(Modifier.height(28.dp))

        /* ---------- SAVE BUTTON ---------- */
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22C55E)
            ),
            enabled = !loading,
            onClick = {

                /* ---------- VALIDATIONS ---------- */
                when {
                    taskName.isBlank() -> {
                        popupMsg = "Task name is required"
                        showPopup = true
                        return@Button
                    }
                    startDate.isBlank() -> {
                        popupMsg = "Start date is required"
                        showPopup = true
                        return@Button
                    }
                    endDate.isBlank() -> {
                        popupMsg = "End date is required"
                        showPopup = true
                        return@Button
                    }
                    assignedEmail.isBlank() -> {
                        popupMsg = "Assigned email is required"
                        showPopup = true
                        return@Button
                    }
                    !assignedEmail.contains("@") -> {
                        popupMsg = "Enter a valid email address"
                        showPopup = true
                        return@Button
                    }
                }

                loading = true

                api.addTask(
                    roomId = roomId,
                    taskName = taskName,
                    startDate = startDate,
                    endDate = endDate,
                    assignedEmail = assignedEmail,
                    userId = userId
                ).enqueue(object : Callback<GenericResponse> {

                    override fun onResponse(
                        call: Call<GenericResponse>,
                        response: Response<GenericResponse>
                    ) {
                        loading = false

                        if (!response.isSuccessful) {
                            popupMsg = "HTTP ${response.code()}"
                            showPopup = true
                            return
                        }

                        val body = response.body()

                        if (body == null) {
                            popupMsg = "Empty response from server"
                            showPopup = true
                            return
                        }

                        popupMsg = body.message ?: "No message from server"
                        success = body.status == "success"
                        showPopup = true
                    }

                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        loading = false
                        popupMsg = "Network error"
                        showPopup = true
                    }
                })

            }
        ) {
            Text(
                "Save Task",
                fontFamily = poppins,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }

    /* ---------- POPUP ---------- */
    if (showPopup) {
        AlertDialog(
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF020617),
            onDismissRequest = { showPopup = false },
            confirmButton = {
                TextButton(onClick = { showPopup = false }) {
                    Text("OK", fontFamily = poppins, color = Color(0xFF22C55E))
                }
            },
            title = {
                Text(
                    if (success) "Success" else "Error",
                    fontFamily = poppins,
                    color = Color.White
                )
            },
            text = {
                Text(
                    popupMsg,
                    fontFamily = poppins,
                    color = Color.White.copy(0.85f),
                    textAlign = TextAlign.Center
                )
            }
        )
    }

    /* ---------- AUTO POP BACK ON SUCCESS ---------- */
    LaunchedEffect(success) {
        if (success) {
            delay(900)
            navController.popBackStack()
        }
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
}

/* ---------- OLD STYLE INPUT ---------- */

@Composable
fun TaskInputOld(
    label: String,
    value: String,
    font: FontFamily,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontFamily = font) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        textStyle = LocalTextStyle.current.copy(fontFamily = font),
        singleLine = true
    )
}
