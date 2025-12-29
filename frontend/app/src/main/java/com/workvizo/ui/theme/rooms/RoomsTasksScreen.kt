package com.workvizo.ui.theme.rooms

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.workvizo.R
import com.workvizo.api.ApiService
import com.workvizo.api.RetrofitClient
import com.workvizo.api.RoomTasksResponse
import com.workvizo.api.TaskItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.flow.collectLatest
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext


@Composable
fun RoomTasksScreen(
    navController: NavController,
    roomId: String,
    userId: String,
    creatorId: String
) {
    val context = LocalContext.current

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    val isCreator = userId == creatorId
    var loading by remember { mutableStateOf(true) }
    var tasks by remember { mutableStateOf<List<TaskItem>>(emptyList()) }

    /* -------- OBSERVE REFRESH FLAG (IMPORTANT FIX) -------- */
    val refreshFlag by navController.currentBackStackEntry!!
        .savedStateHandle
        .getStateFlow("TASK_UPDATED", false)
        .collectAsState()

    /* ---------------- FETCH TASKS ---------------- */
    LaunchedEffect(roomId, refreshFlag) {

        loading = true

        api.getRoomTasks(
            roomId = roomId,
            userId = userId,
            isCreator = if (isCreator) "1" else "0"
        ).enqueue(object : Callback<RoomTasksResponse> {

            override fun onResponse(
                call: Call<RoomTasksResponse>,
                response: Response<RoomTasksResponse>
            ) {
                if (response.body()?.status == "success") {
                    tasks = response.body()?.tasks ?: emptyList()
                }
                loading = false

                // clear flag so it won't loop
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<Boolean>("TASK_UPDATED")
            }

            override fun onFailure(call: Call<RoomTasksResponse>, t: Throwable) {
                loading = false
            }
        })
    }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF05070F), Color(0xFF0C1024), Color(0xFF05070F))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        /* ---------- HEADER ---------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text(
                "Tasks",
                fontFamily = poppinsBold,
                fontSize = 26.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.height(24.dp))

        if (loading) {
            CircularProgressIndicator(color = Color.White)
            return@Column
        }

        if (tasks.isEmpty()) {
            Text(
                "No tasks available",
                fontFamily = poppinsBold,
                color = Color.White.copy(0.6f)
            )
            return@Column
        }

        tasks.forEach { task ->
            TaskPremiumCard(
                title = task.title,
                description = task.description,
                status = task.status,
                assignedTo = task.assigned_to,
                isCreator = isCreator,
                isAssignedToMe = task.is_assigned == 1,
                onChangeStatus = {

                    if (task.id > 0) {
                        navController.navigate(
                            "change_task_status/${task.id}/$userId/${if (isCreator) 1 else 0}"
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Invalid task selected. Please reopen tasks.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onEditProof = {
                    navController.navigate("edit_proof/${task.id}/$userId")
                },
                onViewComment = {
                    navController.navigate("task_comment/${task.id}/$userId")
                }
            )
            Spacer(Modifier.height(20.dp))
        }

    }
}


/* ================= TASK CARD ================= */

@Composable
fun TaskPremiumCard(
    title: String,
    description: String,
    status: String,
    assignedTo: String,
    isCreator: Boolean,
    isAssignedToMe: Boolean,
    onChangeStatus: () -> Unit,
    onEditProof: () -> Unit,
    onViewComment: () -> Unit
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E1627), Color(0xFF020617))
                )
            )
            .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontFamily = poppinsBold, fontSize = 18.sp, color = Color.White)
            StatusChip(status)
        }

        Spacer(Modifier.height(8.dp))

        DetailRow("Assigned To", assignedTo)

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // 🔥 Creator OR assigned member
            if (isCreator || isAssignedToMe) {
                GradientButton(
                    text = "Change Status",
                    colors = listOf(Color(0xFF6366F1), Color(0xFF22D3EE)),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onChangeStatus
                )
            }

            // 🔥 ONLY assigned member
            if (!isCreator && isAssignedToMe) {

                GradientButton(
                    text = "Edit Proof",
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onEditProof
                )

                GradientButton(
                    text = "View Comment",
                    colors = listOf(Color(0xFF22C55E), Color(0xFF4ADE80)),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onViewComment
                )
            }
        }
    }
}

/* ================= HELPERS ================= */

@Composable
fun StatusChip(status: String) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    val (bg, text) = when (status.lowercase()) {
        "completed" -> Color(0xFF22C55E) to "COMPLETED"
        "in_progress" -> Color(0xFFF59E0B) to "IN PROGRESS"
        else -> Color(0xFF64748B) to "PENDING"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg.copy(alpha = 0.15f))
            .border(1.dp, bg, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text, fontFamily = poppinsBold, fontSize = 11.sp, color = bg)
    }
}

@Composable
fun GradientButton(
    text: String,
    colors: List<Color>,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(colors))
            .background(Brush.horizontalGradient(colors))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontFamily = poppinsBold, fontSize = 13.sp, color = Color.Black)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontFamily = poppinsBold, fontSize = 12.sp, color = Color.White.copy(0.6f))
        Text(value, fontFamily = poppinsBold, fontSize = 12.sp, color = Color.White)
    }
}
