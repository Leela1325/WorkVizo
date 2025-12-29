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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.workvizo.R
import com.workvizo.api.*
import retrofit2.*

@Composable
fun TaskCommentScreen(
    navController: NavController,
    taskId: String,
    userId: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }


    var comment by remember { mutableStateOf<String?>(null) }
    var time by remember { mutableStateOf<String?>(null) }
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
            fontFamily = poppins,
            fontSize = 24.sp,
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator(color = Color.White)
            return@Column
        }

        /* ---------- TASK CARD ---------- */
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


            Spacer(Modifier.height(12.dp))

            if (comment == null) {
                Text(
                    "No comment added yet",
                    color = Color.White.copy(0.6f)
                )
            } else {
                Text(
                    "Creator Comment",
                    color = Color(0xFF38BDF8)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    comment!!,
                    color = Color.White
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Given on: $time",
                    fontSize = 12.sp,
                    color = Color.White.copy(0.5f)
                )
            }

        }
    }
}
