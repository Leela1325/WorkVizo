package com.simats.workvizo.ui.theme.rooms

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*

@Composable
fun RemoveTasksScreen(
    navController: NavController,
    roomId: String,
    creatorId: String
)
 {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var tasks by remember { mutableStateOf<List<TaskItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var selectedTaskId by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        api.getRoomTasks(
            roomId = roomId,
            userId = creatorId,
            isCreator = "1"
        )
            .enqueue(object : Callback<RoomTasksResponse> {

            override fun onResponse(
                call: Call<RoomTasksResponse>,
                response: Response<RoomTasksResponse>
            ) {
                tasks = response.body()?.tasks ?: emptyList()
                loading = false
            }

            override fun onFailure(call: Call<RoomTasksResponse>, t: Throwable) {
                loading = false
            }
        })
    }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF0F2027))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        /* ---------- HEADER ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "REMOVE TASKS",
            fontFamily = poppins,
            fontSize = 26.sp,
            color = Color.White
        )

        Spacer(Modifier.height(20.dp))

        if (loading) {
            CircularProgressIndicator(color = Color.White)
            return@Column
        }

        tasks.forEach { task ->
            TaskDeleteCard(
                task = task,
                font = poppins,
                onDeleteClick = {
                    selectedTaskId = task.id
                    showDeleteDialog = true
                }
            )
            Spacer(Modifier.height(18.dp))
        }
    }

    /* ---------- CONFIRM DELETE ---------- */
    if (showDeleteDialog && selectedTaskId != null) {

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1C2541),
            title = {
                Text("DELETE TASK?", fontFamily = poppins, color = Color.White)
            },
            text = {
                Text(
                    "This task will be permanently removed.",
                    fontFamily = poppins,
                    color = Color.White.copy(0.8f)
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(Color(0xFFD32F2F)),
                    onClick = {

                        api.deleteTask(
                            taskId = selectedTaskId!!,
                            userId = creatorId   // 🔥 MUST BE CREATOR
                        )
                            .enqueue(object : Callback<GenericResponse> {

                            override fun onResponse(
                                call: Call<GenericResponse>,
                                response: Response<GenericResponse>
                            ) {
                                if (response.body()?.status == "success") {
                                    tasks = tasks.filter { it.id != selectedTaskId }
                                }
                                showDeleteDialog = false
                            }

                            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                showDeleteDialog = false
                            }
                        })
                    }
                ) {
                    Text("CONFIRM", fontFamily = poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", fontFamily = poppins, color = Color.White)
                }
            }
        )
    }
}
@Composable
fun TaskDeleteCard(
    task: TaskItem,
    font: FontFamily,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
                )
            )
            .border(1.dp, Color.White.copy(0.25f), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                task.title,
                fontFamily = font,
                fontSize = 18.sp,
                color = Color.White
            )

            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.clickable { onDeleteClick() }
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            "Assigned To: ${task.assigned_to}",
            fontFamily = font,
            fontSize = 13.sp,
            color = Color.White.copy(0.8f)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Status: ${task.status.uppercase()}",
            fontFamily = font,
            fontSize = 12.sp,
            color = Color.White.copy(0.7f)
        )
    }
}
