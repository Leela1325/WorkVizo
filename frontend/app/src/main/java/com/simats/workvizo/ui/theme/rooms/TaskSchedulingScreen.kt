package com.simats.workvizo.ui.theme.rooms

import android.app.DatePickerDialog
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

@Composable
fun TaskSchedulingScreen(
    navController: NavController,
    userId: String,
    userName: String,
    roomId: String,
    roomCode: String,
    roomPassword: String,
    projectStart: String,
    projectEnd: String
) {

    val context = LocalContext.current
    val poppins = FontFamily(Font(R.font.poppins_bold, FontWeight.Bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val tasks = remember { mutableStateListOf(TaskDraft()) }

    val dateRegex = remember {
        Pattern.compile("""\d{4}-\d{2}-\d{2}""")
    }

    fun invalid(msg: String) {
        error = msg
    }

    /* ---------- DATE UTILS ---------- */
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val projectStartMillis = remember { sdf.parse(projectStart)!!.time }
    val projectEndMillis = remember { sdf.parse(projectEnd)!!.time }

    fun openDatePicker(
        currentValue: String,
        onPicked: (String) -> Unit
    ) {
        val cal = Calendar.getInstance()
        if (currentValue.matches(dateRegex.toRegex())) {
            cal.timeInMillis = sdf.parse(currentValue)!!.time
        }

        val dialog = DatePickerDialog(
            context,
            { _, y, m, d ->
                val mm = (m + 1).toString().padStart(2, '0')
                val dd = d.toString().padStart(2, '0')
                onPicked("$y-$mm-$dd")
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        // 🔥 STRICT PROJECT RANGE
        dialog.datePicker.minDate = projectStartMillis
        dialog.datePicker.maxDate = projectEndMillis

        dialog.show()
    }

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
            TaskCard(
                taskNo = index + 1,
                task = task,
                poppins = poppins,
                onStartDateClick = {
                    openDatePicker(task.startDate.value) {
                        task.startDate.value = it
                        if (
                            task.endDate.value.isNotEmpty() &&
                            task.endDate.value < task.startDate.value
                        ) {
                            task.endDate.value = ""
                        }
                    }
                },
                onEndDateClick = {
                    openDatePicker(task.endDate.value) {
                        task.endDate.value = it
                    }
                }
            )
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

                for ((i, t) in tasks.withIndex()) {

                    val idx = i + 1

                    when {
                        t.taskName.value.isBlank() ->
                            return@Button invalid("Task $idx: Task name required")

                        t.assignedEmail.value.isBlank() ->
                            return@Button invalid("Task $idx: Assigned email required")

                        !t.assignedEmail.value.contains("@") ->
                            return@Button invalid("Task $idx: Invalid email")

                        !dateRegex.matcher(t.startDate.value).matches() ->
                            return@Button invalid("Task $idx: Invalid start date")

                        !dateRegex.matcher(t.endDate.value).matches() ->
                            return@Button invalid("Task $idx: Invalid end date")

                        t.startDate.value > t.endDate.value ->
                            return@Button invalid("Task $idx: End date before start date")

                        t.startDate.value < projectStart ||
                                t.endDate.value > projectEnd ->
                            return@Button invalid(
                                "Task $idx: Dates must be between\n$projectStart and $projectEnd"
                            )
                    }
                }

                val roomIdInt = roomId.toIntOrNull()
                if (roomIdInt == null) {
                    invalid("Invalid room ID")
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
    val assignedEmail = mutableStateOf("")
}

/* ================= CARD ================= */

@Composable
fun TaskCard(
    taskNo: Int,
    task: TaskDraft,
    poppins: FontFamily,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit
) {
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

            DateInput("Start Date", task.startDate.value, poppins, onStartDateClick)

            Spacer(Modifier.height(10.dp))

            DateInput("End Date", task.endDate.value, poppins, onEndDateClick)

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

/* ================= DATE INPUT ================= */

@Composable
fun DateInput(
    label: String,
    value: String,
    poppins: FontFamily,
    onClick: () -> Unit
) {
    Column {
        Text(label, fontFamily = poppins, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                enabled = false,
                placeholder = { Text("YYYY-MM-DD", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.White, fontFamily = poppins),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color.Black,
                    disabledIndicatorColor = Color.Gray,
                    disabledTextColor = Color.White,
                    disabledPlaceholderColor = Color.Gray
                )
            )
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
