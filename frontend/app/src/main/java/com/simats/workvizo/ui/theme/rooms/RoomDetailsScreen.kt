package com.simats.workvizo.ui.theme.rooms

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
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*
import com.simats.workvizo.api.RoomProgressResponse

@Composable
fun RoomDetailsScreen(
    navController: NavController,
    roomCode: String,
    userId: String,
    userName: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var room by remember { mutableStateOf<Map<String, Any>?>(null) }
    var stats by remember { mutableStateOf<Map<String, Any>?>(null) }

    /* 🔥 PROGRESS STATE (NEW but SAFE) */
    var progress by remember { mutableStateOf<RoomProgressResponse?>(null) }


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

                /* 🔥 FETCH REAL PROGRESS */
                api.getRoomProgress(roomId).enqueue(object : Callback<RoomProgressResponse> {
                    override fun onResponse(
                        call: Call<RoomProgressResponse>,
                        response: Response<RoomProgressResponse>
                    ) {
                        progress = response.body()
                    }

                    override fun onFailure(call: Call<RoomProgressResponse>, t: Throwable) {}
                })
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {}
        })
    }

    val roomId = room?.get("id")?.toString() ?: ""
    val creatorId = room?.get("created_by")?.toString() ?: ""
    val isCreator = userId == creatorId

    val members = (stats?.get("total_members") as? Number)?.toInt() ?: 0
    val tasks = (stats?.get("total_tasks") as? Number)?.toInt() ?: 0
    val schedule = room?.get("schedule_type")?.toString()?.uppercase() ?: "MANUAL"

    /* 🔥 REAL DYNAMIC PERCENT */
    val percent = progress?.taskProgress?.toInt() ?: 0





    val bg = Brush.verticalGradient(
        listOf(Color(0xFF0B0F1E), Color(0xFF141A33), Color(0xFF080B18))
    )

    val lightFeatureGradient = Brush.verticalGradient(
        listOf(Color(0xFF1F2937), Color(0xFF374151))
    )

    val featureBorder = Brush.horizontalGradient(
        listOf(Color(0xFF60A5FA), Color(0xFF38BDF8))
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

        /* ---------- PROGRESS (ONLY THIS SECTION CHANGED) ---------- */
        GlassCard {
            Text("Progress", fontFamily = poppins, color = Color.White)
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percent.toFloat() / 100f)

                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF22C55E),
                                    Color(0xFF38BDF8),
                                    Color(0xFF7C7CFF)
                                )
                            )
                        )
                )
            }

            Spacer(Modifier.height(10.dp))
            Text("$percent% Complete", fontFamily = poppins, color = Color.White)
        }

        Spacer(Modifier.height(22.dp))

        /* ---------- MAIN GRID ---------- */
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FeatureCardLight(
                Modifier.weight(1f),
                "Members",
                members.toString(),
                "People in room",
                lightFeatureGradient,
                featureBorder
            ) { navController.navigate("room_members/$roomId") }

            FeatureCardLight(
                Modifier.weight(1f),
                "Tasks",
                tasks.toString(),
                "Your tasks",
                lightFeatureGradient,
                featureBorder
            ) { navController.navigate("room_tasks/$roomId/$userId/$creatorId") }
        }

        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FeatureCardLight(
                Modifier.weight(1f),
                "Schedule",
                schedule,
                "Planning mode",
                lightFeatureGradient,
                featureBorder
            ) { navController.navigate("schedule_details/$roomId/$roomCode/$userId/$creatorId") }

            FeatureCardLight(
                Modifier.weight(1f),
                "AI Chat",
                "Assistant",
                "Smart help",
                lightFeatureGradient,
                featureBorder
            ) { navController.navigate("room_ai_chat/$roomId/$userId") }
        }

        Spacer(Modifier.height(26.dp))

        /* ---------- BOTTOM ACTIONS ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            if (isCreator) {
                ActionPill("Edit Tasks", Color(0xFF7C7CFF)) {
                    navController.navigate("edit_tasks/$roomId/$userId")
                }
            }
            ActionPill("Chat", Color(0xFF22C55E)) {
                navController.navigate(
                    "room_chat/$roomId/$userId/$userName/$creatorId"
                )

            }

            ActionPill("Settings", Color(0xFF00E5FF)) {
                navController.navigate("room_settings/$roomId/$roomCode/$userId/$creatorId")
            }
        }
    }
}

/* ================= HELPER COMPOSABLES (UNCHANGED) ================= */

@Composable
fun FeatureCardLight(
    modifier: Modifier,
    title: String,
    value: String,
    desc: String,
    backgroundGradient: Brush,
    borderGradient: Brush,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundGradient)
            .border(1.4.dp, borderGradient, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .animateContentSize()
    ) {

        Text(title, fontFamily = FontFamily(Font(R.font.poppins_bold)), fontSize = 14.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(value, fontFamily = FontFamily(Font(R.font.poppins_bold)), fontSize = 26.sp, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Text(desc, fontFamily = FontFamily(Font(R.font.poppins_bold)), fontSize = 11.sp, color = Color.White.copy(0.85f))
    }
}

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
