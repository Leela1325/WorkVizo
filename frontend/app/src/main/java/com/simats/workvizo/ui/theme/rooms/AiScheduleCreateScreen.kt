package com.simats.workvizo.ui.theme.rooms

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import kotlinx.coroutines.delay
import retrofit2.*

@Composable
fun AiScheduleCreateScreen(
    navController: NavController,
    userId: String,
    userName: String,
    roomId: String,
    roomCode: String,
    roomPassword: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val poppinsMedium = FontFamily(Font(R.font.poppins_bold))
    val api = RetrofitClient.instance.create(ApiService::class.java)

    var loading by remember { mutableStateOf(true) }
    var tasks by remember { mutableStateOf<List<AiTask>>(emptyList()) }

    val desc =
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<String>("project_description") ?: ""

    val startDate =
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<String>("start_date") ?: ""

    val endDate =
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<String>("end_date") ?: ""

    val people =
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<Int>("people_count") ?: 1

    LaunchedEffect(Unit) {

        api.generateAiSchedule(
            mapOf(
                "description" to desc,
                "start_date" to startDate,
                "end_date" to endDate,
                "people_count" to people
            )
        ).enqueue(object : Callback<AiScheduleResponse> {

            override fun onResponse(
                call: Call<AiScheduleResponse>,
                response: Response<AiScheduleResponse>
            ) {
                tasks = response.body()?.tasks ?: emptyList()
            }

            override fun onFailure(call: Call<AiScheduleResponse>, t: Throwable) {
                tasks = emptyList()
            }
        })

        delay(5000) // keep animation time
        loading = false
    }

    Box(
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
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(22.dp)
        ) {

            /* ---------- TITLE ---------- */
            Text(
                "AI Schedule Generation",
                fontFamily = poppins,
                fontSize = 24.sp,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Creating an optimized task plan for your room",
                fontFamily = poppinsMedium,
                fontSize = 14.sp,
                color = Color.White.copy(0.75f)
            )

            Spacer(Modifier.height(28.dp))

            /* ---------- ANIMATION ---------- */
            LottieAnimation(
                composition = rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.aicreate)
                ).value,
                iterations = if (loading) LottieConstants.IterateForever else 1,
                modifier = Modifier.size(220.dp)
            )

            Spacer(Modifier.height(24.dp))

            /* ---------- STATUS TEXT ---------- */
            Text(
                if (loading)
                    "Analyzing project scope and distributing tasks..."
                else
                    "Your AI-generated schedule is ready",
                fontFamily = poppins,
                fontSize = 16.sp,
                color = Color.White
            )

            Spacer(Modifier.height(26.dp))

            /* ---------- INFO CARD ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
            ) {

                Column {
                    Text(
                        "Room Summary",
                        fontFamily = poppins,
                        fontSize = 15.sp,
                        color = Color.White
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "• Duration: $startDate → $endDate",
                        fontFamily = poppinsMedium,
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    Text(
                        "• Team Members: $people",
                        fontFamily = poppinsMedium,
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    Text(
                        "• Tasks intelligently distributed across timeline",
                        fontFamily = poppinsMedium,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(34.dp))

            /* ---------- ACTION ---------- */
            if (!loading) {
                Button(
                    onClick = {
                        // 1️⃣ Navigate first
                        navController.navigate(
                            "ai_schedule_preview/$userId/${Uri.encode(userName)}/$roomId/$roomCode/$roomPassword"
                        )

// 2️⃣ Get the PREVIEW screen backStackEntry
                        navController.getBackStackEntry(
                            "ai_schedule_preview/{userId}/{userName}/{roomId}/{roomCode}/{roomPassword}"
                        ).savedStateHandle.set("ai_tasks", tasks)

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    )
                ) {
                    Text(
                        "Review & Edit Schedule",
                        fontFamily = poppins,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}