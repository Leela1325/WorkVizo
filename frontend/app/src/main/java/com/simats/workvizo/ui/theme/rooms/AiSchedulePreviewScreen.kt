package com.simats.workvizo.ui.theme.rooms

import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.gson.Gson
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AiSchedulePreviewScreen(
    navController: NavController,
    userId: String,
    userName: String,
    roomId: String,
    roomCode: String,
    roomPassword: String
) {

    val context = LocalContext.current
    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    val tasks = remember { mutableStateListOf<MutableAiTask>() }
    val taskErrors = remember { mutableStateMapOf<Int, String>() }

    var loading by remember { mutableStateOf(false) }

    /* ---------- PROJECT END DATE ---------- */
    val projectEnd =
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<String>("end_date") ?: ""

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val projectEndMillis =
        projectEnd.takeIf { it.isNotBlank() }?.let { sdf.parse(it)!!.time }

    /* ---------- DATE PICKER ---------- */
    fun openDatePicker(
        currentValue: String,
        onPicked: (String) -> Unit
    ) {
        val cal = Calendar.getInstance()

        if (currentValue.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
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

        dialog.datePicker.minDate = System.currentTimeMillis()
        projectEndMillis?.let { dialog.datePicker.maxDate = it }

        dialog.show()
    }

    /* ---------- LOAD AI TASKS ---------- */
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
                        it.task_name,
                        it.start_date,
                        it.end_date,
                        it.assigned_email
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
            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
        }

        Text("AI Generated Schedule", fontFamily = poppins, fontSize = 22.sp, color = Color.White)

        Spacer(Modifier.height(24.dp))

        tasks.forEachIndexed { index, task ->
            TaskCardEditable(
                index = index + 1,
                task = task,
                error = taskErrors[index],
                poppins = poppins,
                onStartDateClick = {
                    openDatePicker(task.start_date) {
                        task.start_date = it
                        if (task.end_date.isNotEmpty() && task.end_date < task.start_date) {
                            taskErrors[index] = "End date cannot be before start date"
                        } else {
                            taskErrors.remove(index)
                        }
                    }
                },
                onEndDateClick = {
                    openDatePicker(task.end_date) {
                        task.end_date = it
                        if (task.end_date < task.start_date) {
                            taskErrors[index] = "End date cannot be before start date"
                        } else {
                            taskErrors.remove(index)
                        }
                    }
                }
            )
            Spacer(Modifier.height(18.dp))
        }

        Spacer(Modifier.height(30.dp))

        Button(
            enabled = !loading,
            onClick = {

                taskErrors.clear()

                tasks.forEachIndexed { index, task ->
                    when {
                        task.task_name.isBlank() ->
                            taskErrors[index] = "Task name required"

                        task.start_date.isBlank() ->
                            taskErrors[index] = "Start date required"

                        task.end_date.isBlank() ->
                            taskErrors[index] = "End date required"

                        task.end_date < task.start_date ->
                            taskErrors[index] = "End date cannot be before start date"

                        task.assigned_email.isBlank() ->
                            taskErrors[index] = "Assigned email required"
                    }
                }

                if (taskErrors.isNotEmpty()) return@Button

                loading = true

                val tasksJson = Gson().toJson(
                    tasks.mapIndexed { i, t ->
                        mapOf(
                            "task_no" to i + 1,
                            "task_name" to t.task_name,
                            "start_date" to t.start_date,
                            "end_date" to t.end_date,
                            "assigned_email" to t.assigned_email
                        )
                    }
                )

                api.saveAiTasks(roomId, tasksJson)
                    .enqueue(object : Callback<GenericResponse> {

                        override fun onResponse(
                            call: Call<GenericResponse>,
                            response: Response<GenericResponse>
                        ) {
                            loading = false
                            if (response.body()?.status == "success") {
                                navController.navigate(
                                    "room_created_success/$userId/${Uri.encode(userName)}/$roomCode/$roomPassword"
                                )
                            }
                        }

                        override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                            loading = false
                        }
                    })
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp))
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
    error: String?,
    poppins: FontFamily,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit
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

            DateEditableField(
                label = "Start Date",
                value = task.start_date,
                poppins = poppins,
                onClick = onStartDateClick,
                modifier = Modifier.weight(1f)
            )

            DateEditableField(
                label = "End Date",
                value = task.end_date,
                poppins = poppins,
                onClick = onEndDateClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        EditableField(
            "Assigned Email",
            task.assigned_email,
            { task.assigned_email = it },
            poppins
        )

        if (!error.isNullOrEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(error, color = Color.Red, fontSize = 12.sp)
        }
    }
}

/* ---------------- DATE FIELD ---------------- */

@Composable
fun DateEditableField(
    label: String,
    value: String,
    poppins: FontFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().clickable { onClick() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            enabled = false,
            label = { Text(label, fontFamily = poppins) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontFamily = poppins),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.White.copy(0.7f),
                disabledLabelColor = Color.White,
                disabledTextColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
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

