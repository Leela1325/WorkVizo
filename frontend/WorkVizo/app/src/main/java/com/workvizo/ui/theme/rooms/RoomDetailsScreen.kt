package com.workvizo.ui.theme.rooms

import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.workvizo.R
import com.workvizo.api.*
import retrofit2.*

@Composable
fun RoomDetailsScreen(
    navController: NavController,
    roomCode: String,
    userId: String,
    userName: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold, FontWeight.Bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var room by remember { mutableStateOf<Map<String, Any>?>(null) }
    var stats by remember { mutableStateOf<Map<String, Any>?>(null) }
    var progress by remember { mutableStateOf<RoomProgressResponse?>(null) }
    var loading by remember { mutableStateOf(true) }

    /* ---------------- API ---------------- */
    LaunchedEffect(roomCode) {
        api.getRoomDetails(roomCode).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                val body = response.body() ?: return
                if (body["status"] != "success") return

                room = body["room"] as Map<String, Any>
                stats = body["stats"] as Map<String, Any>

                val roomId = room?.get("id")?.toString() ?: return

                api.getRoomProgress(roomId)
                    .enqueue(object : Callback<RoomProgressResponse> {
                        override fun onResponse(
                            call: Call<RoomProgressResponse>,
                            response: Response<RoomProgressResponse>
                        ) {
                            progress = response.body()
                            loading = false
                        }

                        override fun onFailure(call: Call<RoomProgressResponse>, t: Throwable) {
                            loading = false
                        }
                    })
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                loading = false
            }
        })
    }

    /* ---------------- SAFE VALUES ---------------- */
    val roomId = room?.get("id")?.toString() ?: ""
    val creatorId = room?.get("created_by")?.toString() ?: ""

    val members = (stats?.get("total_members") as? Number)?.toInt() ?: 0
    val tasks = (stats?.get("total_tasks") as? Number)?.toInt() ?: 0
    val schedule = room?.get("schedule_type")?.toString()?.uppercase() ?: "AI"
    val percent = progress?.task_progress?.toInt() ?: 0

    /* ---------------- BG ---------------- */
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF0B0F1E), Color(0xFF141A33), Color(0xFF080B18))
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
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    room?.get("name")?.toString() ?: "",
                    fontFamily = poppins,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Text(
                    room?.get("description")?.toString() ?: "",
                    fontFamily = poppins,
                    fontSize = 12.sp,
                    color = Color.White.copy(0.7f)
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        /* ---------- PROGRESS ---------- */
        GlassCard {
            Text("Progress", fontFamily = poppins, color = Color.White)
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percent / 100f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF7C7CFF),
                                    Color(0xFF00E5FF),
                                    Color(0xFF38F9D7)
                                )
                            )
                        )
                )
            }

            Spacer(Modifier.height(10.dp))
            Text("$percent% Complete", fontFamily = poppins, color = Color.White)
        }

        Spacer(Modifier.height(20.dp))

        /* ---------- GRID ROW 1 ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            FeatureCard(
                modifier = Modifier.weight(1f),
                title = "Members",
                value = members.toString(),
                desc = "People in room",
                gradient = Brush.verticalGradient(
                    listOf(Color(0xFF6A5AE0), Color(0xFF4E4ACB))
                )
            ) {
                navController.navigate("room_members/$roomId/$userId")
            }

            FeatureCard(
                modifier = Modifier.weight(1f),
                title = "Tasks",
                value = tasks.toString(),
                desc = "Assigned tasks",
                gradient = Brush.verticalGradient(
                    listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
                )
            ) {
                navController.navigate("room_tasks/$roomId")
            }
        }

        Spacer(Modifier.height(14.dp))

        /* ---------- GRID ROW 2 ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            FeatureCard(
                modifier = Modifier.weight(1f),
                title = "Schedule",
                value = schedule,
                desc = "Planning mode",
                gradient = Brush.verticalGradient(
                    listOf(Color(0xFFFF8A65), Color(0xFFFF7043))
                )
            ) {
                navController.navigate("room_schedule/$roomId")
            }

            FeatureCard(
                modifier = Modifier.weight(1f),
                title = "AI Chat",
                value = "Assistant",
                desc = "Smart help",
                gradient = Brush.verticalGradient(
                    listOf(Color(0xFF43E97B), Color(0xFF38F9D7))
                )
            ) {
                navController.navigate("room_ai_chat/$roomId/$userId")
            }
        }

        Spacer(Modifier.height(24.dp))

        /* ---------- ACTIONS ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            ActionPill("Timeline", Color(0xFF7C7CFF)) {
                navController.navigate("room_timeline/$roomId/$roomCode/$userId")
            }

            ActionPill("Settings", Color(0xFF00E5FF)) {
                navController.navigate("room_settings/$roomId/$roomCode/$userId/$creatorId")
            }
        }
    }
}

/* ================= COMPONENTS ================= */

@Composable
fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(0.08f))
            .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .animateContentSize(),
        content = content
    )
}

@Composable
fun FeatureCard(
    modifier: Modifier,
    title: String,
    value: String,
    desc: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(gradient)
            .border(1.2.dp, Color.White.copy(0.25f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .animateContentSize()
    ) {
        Text(title, fontFamily = FontFamily(Font(R.font.poppins_bold)), color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(value, fontFamily = FontFamily(Font(R.font.poppins_bold)), fontSize = 26.sp, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Text(desc, fontFamily = FontFamily(Font(R.font.poppins_bold)), fontSize = 11.sp, color = Color.White.copy(0.85f))
    }
}

@Composable
fun ActionPill(text: String, accent: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.15f))
            .border(1.dp, accent, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 22.dp, vertical = 10.dp)
    ) {
        Text(text, fontFamily = FontFamily(Font(R.font.poppins_bold)), color = Color.White)
    }
}
