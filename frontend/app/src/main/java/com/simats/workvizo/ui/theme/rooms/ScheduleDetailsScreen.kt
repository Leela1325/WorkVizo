package com.simats.workvizo.ui.theme.rooms

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*

@Composable
fun ScheduleDetailsScreen(
    navController: NavController,
    roomId: String,
    roomCode: String,
    userId: String,
    creatorId: String
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold, FontWeight.Bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var project by remember { mutableStateOf<Map<String, Any>?>(null) }
    var tasks by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val isCreator = userId == creatorId

    /* ---------------- API ---------------- */
    LaunchedEffect(roomId) {
        api.getScheduleDetails(roomId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                val body = response.body() ?: return
                project = body["project"] as Map<String, Any>
                tasks = body["tasks"] as List<Map<String, Any>>
                loading = false
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                loading = false
            }
        })
    }

    val projectStart = project?.get("start_date")?.toString() ?: ""
    val projectEnd = project?.get("end_date")?.toString() ?: ""

    /* ---------------- BG ---------------- */
    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF05070F),
            Color(0xFF0C1024),
            Color(0xFF05070F)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            /* ================= HEADER ================= */
            Row(verticalAlignment = Alignment.CenterVertically) {

                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }

                Text(
                    "Project Schedule",
                    fontFamily = poppinsBold,
                    fontSize = 26.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                if (isCreator) {
                    IconButton(onClick = {
                        navController.navigate(
                            "edit_schedule/$roomId/$roomCode/$userId/$creatorId"
                        )
                    }) {
                        Icon(Icons.Default.Edit, null, tint = Color(0xFF00E5FF))
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            /* ================= PROJECT TIMELINE ================= */
            Text(
                "PROJECT TIMELINE",
                fontFamily = poppinsBold,
                fontSize = 13.sp,
                color = Color.White.copy(0.55f)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(0.05f))
                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(22.dp))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimelineBlock("START DATE", projectStart)
                TimelineBlock("END DATE", projectEnd)
            }

            Spacer(Modifier.height(36.dp))

            /* ================= TASKS ================= */
            Text(
                "TASK PLAN",
                fontFamily = poppinsBold,
                fontSize = 13.sp,
                color = Color.White.copy(0.55f)
            )

            Spacer(Modifier.height(18.dp))

            tasks.forEachIndexed { index, task ->

                val taskStart = task["start_date"].toString()
                val taskEnd = task["end_date"].toString()

                val invalidDates =
                    projectStart.isNotBlank() &&
                            projectEnd.isNotBlank() &&
                            (taskStart < projectStart ||
                                    taskEnd > projectEnd ||
                                    taskStart > taskEnd)

                PremiumTaskCard(
                    index = index + 1,
                    taskName = task["task_name"].toString(),
                    startDate = taskStart,
                    endDate = taskEnd,
                    assignedEmail = task["assigned_email"].toString(),
                    status = task["status"].toString(),
                    showDateError = invalidDates
                )

                Spacer(Modifier.height(22.dp))
            }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

/* ================= COMPONENTS ================= */

@Composable
fun TimelineBlock(label: String, value: String) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    Column {
        Text(label, fontFamily = poppinsBold, fontSize = 11.sp, color = Color.White.copy(0.6f))
        Spacer(Modifier.height(6.dp))
        Text(value, fontFamily = poppinsBold, fontSize = 16.sp, color = Color.White)
    }
}

@Composable
fun PremiumTaskCard(
    index: Int,
    taskName: String,
    startDate: String,
    endDate: String,
    assignedEmail: String,
    status: String,
    showDateError: Boolean
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    Row(modifier = Modifier.fillMaxWidth()) {

        /* ---------- ACCENT BAR ---------- */
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF6C63FF), Color(0xFF00E5FF))
                    )
                )
        )

        Spacer(Modifier.width(16.dp))

        /* ---------- CARD ---------- */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF020617))
                    )
                )
                .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(26.dp))
                .padding(22.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "TASK $index",
                    fontFamily = poppinsBold,
                    fontSize = 12.sp,
                    color = Color(0xFF00E5FF)
                )
                Spacer(Modifier.weight(1f))
                StatusPill(status)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                taskName,
                fontFamily = poppinsBold,
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetaBlock("START DATE", startDate)
                MetaBlock("END DATE", endDate)
            }

            if (showDateError) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Task dates must be within project timeline",
                    fontFamily = poppinsBold,
                    fontSize = 11.sp,
                    color = Color(0xFFEF4444)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "ASSIGNED TO",
                fontFamily = poppinsBold,
                fontSize = 11.sp,
                color = Color.White.copy(0.55f)
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(0.08f))
                    .border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    assignedEmail,
                    fontFamily = poppinsBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MetaBlock(label: String, value: String) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    Column {
        Text(label, fontFamily = poppinsBold, fontSize = 11.sp, color = Color.White.copy(0.6f))
        Spacer(Modifier.height(6.dp))
        Text(value, fontFamily = poppinsBold, fontSize = 14.sp, color = Color.White)
    }
}

@Composable
fun StatusPill(status: String) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    val (bg, color) = when (status.lowercase()) {
        "completed" -> Color(0xFF22C55E) to Color.Black
        "in_progress" -> Color(0xFF3B82F6) to Color.White
        else -> Color(0xFFFFC107) to Color.Black
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            status.uppercase(),
            fontFamily = poppinsBold,
            fontSize = 11.sp,
            color = color
        )
    }
}
