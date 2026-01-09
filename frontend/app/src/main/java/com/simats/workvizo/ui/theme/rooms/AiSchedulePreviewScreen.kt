package com.simats.workvizo.ui.theme.rooms

import android.net.Uri
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.gson.Gson
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*

@Composable
fun AiSchedulePreviewScreen(
    navController: NavController,
    userId: String,
    userName: String,
    roomId: String,
    roomCode: String,
    roomPassword: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    val tasks = remember { mutableStateListOf<MutableAiTask>() }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    /* -------- LOAD AI TASKS FROM PREVIOUS SCREEN -------- */
    LaunchedEffect(Unit) {
        val aiTasks =
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.get<List<AiTask>>("ai_tasks")

        if (!aiTasks.isNullOrEmpty()) {
            tasks.clear()
            tasks.addAll(
                aiTasks.map {
                    MutableAiTask(
                        taskName = it.task_name,
                        startDate = it.start_date,
                        endDate = it.end_date,
                        assignedEmail = it.assigned_email
                    )
                }
            )
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
        }

        Text(
            text = "AI Generated Schedule",
            fontFamily = poppins,
            fontSize = 22.sp,
            color = Color.White
        )

        Spacer(Modifier.height(24.dp))

        tasks.forEachIndexed { index, task ->
            TaskCardEditable(index + 1, task, poppins)
            Spacer(Modifier.height(18.dp))
        }

        if (error.isNotEmpty()) {
            Text(error, color = Color.Red, fontFamily = poppins)
        }

        Spacer(Modifier.height(30.dp))

        Button(
            enabled = !loading,
            onClick = {

                error = ""

                if (tasks.any {
                        it.task_name.isBlank() ||
                                it.start_date.isBlank() ||
                                it.end_date.isBlank() ||
                                it.assigned_email.isBlank()
                    }
                ) {
                    error = "All task fields must be filled"
                    return@Button
                }

                loading = true

                /* -------- BUILD JSON FOR API -------- */
                val tasksJson = Gson().toJson(
                    tasks.mapIndexed { index, task ->
                        mapOf(
                            "task_no" to index + 1,
                            "task_name" to task.task_name,
                            "start_date" to task.start_date,
                            "end_date" to task.end_date,
                            "assigned_email" to task.assigned_email
                        )
                    }
                )

                /* -------- SAVE TASKS TO DB -------- */
                api.saveAiTasks(
                    roomId = roomId,
                    tasksJson = tasksJson
                ).enqueue(object : Callback<GenericResponse> {

                    override fun onResponse(
                        call: Call<GenericResponse>,
                        response: Response<GenericResponse>
                    ) {
                        loading = false

                        if (!response.isSuccessful || response.body()?.status != "success") {
                            error = response.body()?.message ?: "Failed to save tasks"
                            return
                        }

                        navController.navigate(
                            "room_created_success/$userId/${Uri.encode(userName)}/$roomCode/$roomPassword"
                        )
                    }

                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        loading = false
                        error = "Network error while saving tasks"
                    }
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Create Room", fontFamily = poppins, color = Color.Black)
            }
        }
    }
}

/* ---------------- TASK CARD ---------------- */

@Composable
fun TaskCardEditable(
    index: Int,
    task: MutableAiTask,
    poppins: FontFamily
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                )
            )
            .padding(18.dp)
    ) {

        Text("Task $index", fontFamily = poppins, color = Color.White)

        Spacer(Modifier.height(12.dp))

        EditableField("Task Name", task.task_name, { task.task_name = it }, poppins)

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EditableField(
                "Start Date",
                task.start_date,
                { task.start_date = it },
                poppins,
                Modifier.weight(1f)
            )

            EditableField(
                "End Date",
                task.end_date,
                { task.end_date = it },
                poppins,
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        EditableField(
            "Assigned Email",
            task.assigned_email,
            { task.assigned_email = it },
            poppins
        )
    }
}

/* ---------------- INPUT FIELD ---------------- */

@Composable
fun EditableField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    poppins: FontFamily,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontFamily = poppins) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontFamily = poppins),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF00E5FF),
            unfocusedBorderColor = Color.White.copy(0.7f),
            focusedLabelColor = Color(0xFF00E5FF),
            unfocusedLabelColor = Color.White,
            cursorColor = Color(0xFF00E5FF),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp)
    )
}