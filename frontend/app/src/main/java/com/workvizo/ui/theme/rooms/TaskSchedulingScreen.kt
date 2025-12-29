package com.workvizo.ui.theme.rooms

import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.workvizo.R
import com.workvizo.api.*
import retrofit2.*

@Composable
fun TaskSchedulingScreen(
    navController: NavController,
    userId: String,
    userName: String,
    roomId: String,
    roomCode: String,
    roomPassword: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold, FontWeight.Bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val tasks = remember { mutableStateListOf(TaskDraft()) }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        /* ---------- HEADER ---------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text(
                "TASK SCHEDULING",
                fontFamily = poppins,
                fontSize = 22.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.height(24.dp))

        /* ---------- TASK LIST ---------- */
        tasks.forEachIndexed { index, task ->
            TaskCard(index + 1, task, poppins)
            Spacer(Modifier.height(18.dp))
        }

        /* ---------- ADD TASK ---------- */
        OutlinedButton(
            onClick = { tasks.add(TaskDraft()) },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("ADD NEXT TASK", fontFamily = poppins, color = Color.White)
        }

        Spacer(Modifier.height(28.dp))

        /* ---------- SAVE ---------- */
        Button(
            enabled = !loading,
            onClick = {

                error = ""

                if (tasks.any {
                        it.taskName.value.isBlank() ||
                                it.assignedEmail.value.isBlank()
                    }) {
                    error = "Task name and assigned email are required"
                    return@Button
                }

                val roomIdInt = roomId.toIntOrNull()
                if (roomIdInt == null) {
                    error = "Invalid room ID"
                    return@Button
                }

                loading = true

                api.createRoomTasks(
                    roomId = roomIdInt,
                    tasksJson = buildTasksJson(tasks)
                ).enqueue(object : Callback<GenericResponse> {

                    override fun onResponse(
                        call: Call<GenericResponse>,
                        response: Response<GenericResponse>
                    ) {
                        loading = false
                        if (response.body()?.status == "success") {

                            navController.navigate(
                                "room_created_success/$userId/$userName/$roomCode/$roomPassword"
                            ) {
                                popUpTo(
                                    "task_scheduling/$userId/$userName/$roomId/$roomCode/$roomPassword"
                                ) { inclusive = true }
                            }

                        } else {
                            error = response.body()?.message ?: "Failed to save tasks"
                        }
                    }

                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        loading = false
                        error = "Network error"
                    }
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87))
        ) {
            Text(
                "SAVE & CONTINUE",
                fontFamily = poppins,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = Color.Red, fontFamily = poppins)
        }

        Spacer(Modifier.height(40.dp))
    }

    /* ---------- LOADING ---------- */
    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

/* ================= MODEL ================= */

class TaskDraft {
    val taskName = mutableStateOf("")
    val startDate = mutableStateOf("")
    val endDate = mutableStateOf("")
    val assignedEmail = mutableStateOf("")   // ✅ SINGLE USER
}

/* ================= CARD ================= */

@Composable
fun TaskCard(taskNo: Int, task: TaskDraft, poppins: FontFamily) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(Modifier.padding(18.dp)) {

            Text("TASK $taskNo", fontFamily = poppins, color = Color.White)
            Spacer(Modifier.height(10.dp))

            VisibleInput("Task Name", "UI Design", task.taskName.value, poppins) {
                task.taskName.value = it
            }

            Spacer(Modifier.height(10.dp))

            VisibleInput("Start Date", "YYYY-MM-DD", task.startDate.value, poppins) {
                task.startDate.value = it
            }

            Spacer(Modifier.height(10.dp))

            VisibleInput("End Date", "YYYY-MM-DD", task.endDate.value, poppins) {
                task.endDate.value = it
            }

            Spacer(Modifier.height(10.dp))

            VisibleInput(
                "Assigned Email",
                "user@email.com",
                task.assignedEmail.value,
                poppins
            ) {
                task.assignedEmail.value = it
            }
        }
    }
}

/* ================= INPUT ================= */

@Composable
fun VisibleInput(
    label: String,
    placeholder: String,
    value: String,
    poppins: FontFamily,
    onChange: (String) -> Unit
) {
    Column {
        Text(label, fontFamily = poppins, color = Color.White)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.White, fontFamily = poppins),
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
                focusedIndicatorColor = Color(0xFF00FF87),
                unfocusedIndicatorColor = Color.Gray,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

/* ================= JSON ================= */

fun buildTasksJson(tasks: List<TaskDraft>): String {
    val arr = JSONArray()
    tasks.forEachIndexed { i, t ->
        val o = JSONObject()
        o.put("task_no", i + 1)
        o.put("task_name", t.taskName.value)
        o.put("start_date", t.startDate.value)
        o.put("end_date", t.endDate.value)
        o.put("assigned_email", t.assignedEmail.value)
        arr.put(o)
    }
    return arr.toString()
}
