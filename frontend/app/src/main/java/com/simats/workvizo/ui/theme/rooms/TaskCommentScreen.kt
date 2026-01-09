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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*

@Composable
fun TaskCommentScreen(
    navController: NavController,
    taskId: String,
    userId: String
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var comment by remember { mutableStateOf<String?>(null) }
    var time by remember { mutableStateOf<String?>(null) }
    var taskName by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        api.getTaskComment(taskId, userId)
            .enqueue(object : Callback<TaskCommentResponse> {
                override fun onResponse(
                    call: Call<TaskCommentResponse>,
                    response: Response<TaskCommentResponse>
                ) {
                    val body = response.body()
                    if (body?.status == "success") {
                        comment = body.comment
                        time = body.comment_time
                        taskName = body.task_name
                    }
                    loading = false
                }

                override fun onFailure(call: Call<TaskCommentResponse>, t: Throwable) {
                    loading = false
                }
            })
    }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF020617))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
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
            Icon(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Task Feedback",
            fontFamily = poppinsBold,
            fontSize = 24.sp,
            color = Color.White
        )

        Text(
            "Below is the feedback shared by the task creator after reviewing your submission.",
            fontFamily = poppinsBold,
            fontSize = 13.sp,
            color = Color.White.copy(0.7f)
        )

        Spacer(Modifier.height(20.dp))

        if (loading) {
            CircularProgressIndicator(color = Color.White)
            return@Column
        }

        /* ---------- TASK INFO CARD ---------- */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF020617))
                    )
                )
                .border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {

            /* ---- TASK NAME ---- */
            Text(
                "Task Name",
                fontFamily = poppinsBold,
                fontSize = 12.sp,
                color = Color.White.copy(0.6f)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                taskName ?: "Unnamed Task",
                fontFamily = poppinsBold,
                fontSize = 16.sp,
                color = Color.White
            )

            Spacer(Modifier.height(20.dp))

            /* ---- FEEDBACK DESCRIPTION ---- */
            Text(
                "Creator Feedback",
                fontFamily = poppinsBold,
                fontSize = 14.sp,
                color = Color(0xFF38BDF8)
            )

            Text(
                "This feedback reflects the creator’s review of your task update or proof submission.",
                fontFamily = poppinsBold,
                fontSize = 12.sp,
                color = Color.White.copy(0.6f)
            )

            Spacer(Modifier.height(14.dp))

            /* ---- ACTUAL COMMENT (HIGHLIGHTED) ---- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(0.35f))
                    .border(
                        1.dp,
                        Color(0xFF38BDF8).copy(0.4f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                if (comment == null) {
                    Text(
                        "No comment has been added for this task yet.",
                        fontFamily = poppinsBold,
                        fontSize = 13.sp,
                        color = Color.White.copy(0.6f)
                    )
                } else {
                    Text(
                        comment!!,
                        fontFamily = poppinsBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (comment != null) {
                Text(
                    "Given on: $time",
                    fontFamily = poppinsBold,
                    fontSize = 12.sp,
                    color = Color.White.copy(0.5f)
                )
            }
        }
    }
}
