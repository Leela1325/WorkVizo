package com.simats.workvizo.ui.theme.rooms

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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*

@Composable
fun RecentRoomsScreen(
    navController: NavController,
    userId: String,
    userName: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var rooms by remember { mutableStateOf<List<RoomItem>>(emptyList()) }

    /* ---------- API CALL ---------- */
    LaunchedEffect(userId) {
        api.getRecentRooms(userId).enqueue(object : Callback<RoomsResponse> {

            override fun onResponse(
                call: Call<RoomsResponse>,
                response: Response<RoomsResponse>
            ) {
                rooms = response.body()?.recentRooms ?: emptyList()
            }

            override fun onFailure(call: Call<RoomsResponse>, t: Throwable) {
                rooms = emptyList()
            }
        })
    }

    /* ---------- RECENT GRADIENT ---------- */
    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF311B92),
            Color(0xFF512DA8),
            Color(0xFF311B92)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        /* ---------- TOP BAR ---------- */
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
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "RECENT ROOMS",
            fontFamily = poppins,
            fontSize = 26.sp,
            color = Color.White
        )

        Text(
            text = "Recently accessed rooms",
            fontFamily = poppins,
            fontSize = 13.sp,
            color = Color.White.copy(0.7f)
        )

        Spacer(Modifier.height(20.dp))

        if (rooms.isEmpty()) {
            Text(
                text = "No recent rooms found",
                fontFamily = poppins,
                color = Color.White.copy(0.6f)
            )
        } else {
            rooms.forEach { room ->
                RecentRoomCard(
                    room = room,
                    font = poppins,
                    onClick = {
                        val safeCode = Uri.encode(room.room_code ?: "")
                        val safeUserName = Uri.encode(userName)

                        navController.navigate(
                            "room_details/$safeCode/$userId/$safeUserName"
                        )
                    }
                )
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

/* ===================================================== */
/* ================= ROOM CARD ========================= */
/* ===================================================== */

@Composable
fun RecentRoomCard(
    room: RoomItem,
    font: FontFamily,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE1BEE7).copy(alpha = 0.35f),
                        Color(0xFFBA68C8).copy(alpha = 0.20f)
                    )
                )
            )
            .border(
                1.dp,
                Color(0xFFE1BEE7).copy(0.6f),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {

        Text(
            text = "RECENT",
            fontFamily = font,
            fontSize = 10.sp,
            color = Color.Black,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3E5F5))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = room.name.ifBlank { "Untitled Room" },
            fontFamily = font,
            fontSize = 18.sp,
            color = Color.White
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Code: ${room.room_code ?: "--"}",
            fontFamily = font,
            fontSize = 13.sp,
            color = Color(0xFFF8BBD0)
        )

        if (!room.description.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = room.description!!,
                fontFamily = font,
                fontSize = 13.sp,
                color = Color.White.copy(0.85f),
                maxLines = 2
            )
        }

        Spacer(Modifier.height(10.dp))

        Row {
            Text(
                text = "📅 ${room.start_date ?: "--"}",
                fontFamily = font,
                fontSize = 12.sp,
                color = Color.White.copy(0.9f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "→ ${room.end_date ?: "--"}",
                fontFamily = font,
                fontSize = 12.sp,
                color = Color.White.copy(0.9f)
            )
        }
    }
}
