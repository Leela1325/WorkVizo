package com.simats.workvizo.ui.theme.rooms

import com.simats.workvizo.api.UpdateScheduleRequest
import com.simats.workvizo.api.UpdateTaskRequest

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import kotlinx.coroutines.delay
import retrofit2.*

@Composable
fun EditScheduleScreen(
    navController: NavController,
    roomId: String,
    roomCode: String,
    userId: String,
    creatorId: String
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var projectStart by remember { mutableStateOf("") }
    var projectEnd by remember { mutableStateOf("") }
    val tasks = remember { mutableStateListOf<EditableTask>() }

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    /* ---------------- LOAD EXISTING SCHEDULE ---------------- */
    LaunchedEffect(roomId) {
        api.getScheduleDetails(roomId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                val body = response.body() ?: return
                val project = body["project"] as Map<String, Any>

                projectStart = project["start_date"].toString()
                projectEnd = project["end_date"].toString()

                tasks.clear()
                val list = body["tasks"] as List<Map<String, Any>>
                list.forEach {
                    tasks.add(
                        EditableTask(
                            id = it["task_no"].toString(),
                            name = it["task_name"].toString(),
                            start = it["start_date"].toString(),
                            end = it["end_date"].toString(),
                            email = it["assigned_email"].toString(),
                            status = it["status"].toString()
                        )
                    )
                }
                loading = false
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                loading = false
            }
        })
    }

    /* ---------------- AUTO NAV AFTER SUCCESS ---------------- */
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(3000)
            navController.popBackStack()
        }
    }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF05070F), Color(0xFF0C1024), Color(0xFF05070F))
    )

    val hasEditableTasks = tasks.any { it.status != "completed" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            /* ================= HEADER ================= */
            Row(verticalAlignment = Alignment.CenterVertically) {

                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }

                Text(
                    "Edit Schedule",
                    fontFamily = poppinsBold,
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    enabled = !saving && hasEditableTasks,
                    onClick = {
                        if (projectStart.isBlank() || projectEnd.isBlank()) return@TextButton

                        saving = true

                        val request = UpdateScheduleRequest(
                            room_id = roomId,
                            start_date = projectStart,
                            end_date = projectEnd,
                            tasks = tasks.map {
                                UpdateTaskRequest(
                                    task_no = it.id,
                                    task_name = it.name,
                                    start_date = it.start,
                                    end_date = it.end,
                                    assigned_email = it.email
                                )
                            }
                        )

                        api.updateSchedule(request)
                            .enqueue(object : Callback<GenericResponse> {

                                override fun onResponse(
                                    call: Call<GenericResponse>,
                                    response: Response<GenericResponse>
                                ) {
                                    saving = false
                                    if (response.body()?.status == "success") {
                                        showSuccess = true
                                    }
                                }

                                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                    saving = false
                                }
                            })
                    }
                ) {
                    Icon(Icons.Default.Save, null, tint = Color(0xFF00E5FF))
                    Spacer(Modifier.width(6.dp))
                    Text("Save", fontFamily = poppinsBold, color = Color(0xFF00E5FF))
                }
            }

            Spacer(Modifier.height(26.dp))

            if (loading) {
                CircularProgressIndicator(color = Color.White)
                return@Column
            }

            /* ================= PROJECT ================= */
            SectionCard("PROJECT TIMELINE") {
                EditField("Start Date", projectStart) { projectStart = it }
                EditField("End Date", projectEnd) { projectEnd = it }
            }

            Spacer(Modifier.height(26.dp))

            /* ================= TASKS ================= */
            tasks.forEach { task ->

                val cardBg =
                    if (task.status == "completed")
                        Color.White.copy(0.04f)
                    else
                        Color.White.copy(0.06f)

                SectionCard("TASK ${task.id}", cardBg) {

                    Text(
                        text = "Status: ${task.status.uppercase()}",
                        fontFamily = poppinsBold,
                        fontSize = 12.sp,
                        color = when (task.status) {
                            "completed" -> Color(0xFF22C55E)
                            "in_progress" -> Color(0xFFFACC15)
                            else -> Color.White.copy(0.6f)
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    val editable = task.status != "completed"

                    if (editable) {
                        EditField("Task Name", task.name) { task.name = it }
                        EditField("Start Date", task.start) { task.start = it }
                        EditField("End Date", task.end) { task.end = it }
                        EditField("Assigned Email", task.email) { task.email = it }
                    } else {
                        Text(
                            "This task is completed and cannot be edited.",
                            fontFamily = poppinsBold,
                            fontSize = 12.sp,
                            color = Color.White.copy(0.6f)
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
            }
        }

        /* ================= SAVING OVERLAY ================= */
        if (saving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.45f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        /* ================= SUCCESS POPUP ================= */
        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn() + fadeIn(),
            exit = fadeOut()
        ) {
            SuccessPopup()
        }
    }
}

/* ================= MODELS ================= */

class EditableTask(
    val id: String,
    name: String,
    start: String,
    end: String,
    email: String,
    status: String
) {
    var name by mutableStateOf(name)
    var start by mutableStateOf(start)
    var end by mutableStateOf(end)
    var email by mutableStateOf(email)
    val status by mutableStateOf(status)
}

/* ================= UI COMPONENTS ================= */

@Composable
fun SectionCard(
    title: String,
    bgColor: Color = Color.White.copy(0.06f),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(0.14f), RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Text(title, fontFamily = FontFamily(Font(R.font.poppins_bold)), color = Color.White)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
fun EditField(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        Text(
            label,
            fontFamily = FontFamily(Font(R.font.poppins_bold)),
            color = Color.White.copy(0.7f)
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

/* ================= SUCCESS POPUP ================= */

@Composable
fun SuccessPopup() {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.55f)),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F172A),
                            Color(0xFF020617)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(0.18f),
                    RoundedCornerShape(28.dp)
                )
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF22C55E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Schedule Updated",
                fontFamily = poppinsBold,
                fontSize = 22.sp,
                color = Color.White
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Your project timeline and task assignments have been saved successfully.",
                fontFamily = poppinsBold,
                fontSize = 13.sp,
                color = Color.White.copy(0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Redirecting…",
                fontFamily = poppinsBold,
                fontSize = 11.sp,
                color = Color.White.copy(0.5f)
            )
        }
    }
}