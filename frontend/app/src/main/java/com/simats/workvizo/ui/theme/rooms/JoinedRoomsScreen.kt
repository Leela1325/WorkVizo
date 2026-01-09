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
fun JoinedRoomsScreen(
    navController: NavController,
    userId: String,
    userName: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var rooms by remember { mutableStateOf<List<RoomItem>>(emptyList()) }

    /* ---------- API CALL ---------- */
    LaunchedEffect(userId) {
        api.getJoinedRooms(userId).enqueue(object : Callback<RoomsResponse> {
            override fun onResponse(
                call: Call<RoomsResponse>,
                response: Response<RoomsResponse>
            ) {
                rooms = response.body()?.rooms ?: emptyList()
            }

            override fun onFailure(call: Call<RoomsResponse>, t: Throwable) {
                rooms = emptyList()
            }
        })
    }

    /* ---------- BACKGROUND ---------- */
    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF001F3F),
            Color(0xFF003566),
            Color(0xFF001F3F)
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
            text = "JOINED ROOMS",
            fontFamily = poppins,
            fontSize = 26.sp,
            color = Color.White
        )

        Text(
            text = "Rooms you are part of",
            fontFamily = poppins,
            fontSize = 13.sp,
            color = Color.White.copy(0.7f)
        )

        Spacer(Modifier.height(20.dp))

        if (rooms.isEmpty()) {
            Text(
                text = "You haven't joined any rooms yet",
                fontFamily = poppins,
                color = Color.White.copy(0.6f)
            )
        } else {
            rooms.forEach { room ->
                JoinedRoomCard(
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
fun JoinedRoomCard(
    room: RoomItem,
    font: FontFamily,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(0.10f))
            .border(
                1.dp,
                Color(0xFF4DD0E1).copy(0.5f),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {

        Text(
            text = room.name.ifBlank { "Untitled Room" },
            fontFamily = font,
            fontSize = 18.sp,
            color = Color.White
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Room Code: ${room.room_code ?: "--"}",
            fontFamily = font,
            fontSize = 13.sp,
            color = Color(0xFF4DD0E1)
        )

        Spacer(Modifier.height(8.dp))

        if (!room.description.isNullOrBlank()) {
            Text(
                text = room.description!!,
                fontFamily = font,
                fontSize = 13.sp,
                color = Color.White.copy(0.75f),
                maxLines = 2
            )
            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Start: ${room.start_date ?: "--"}",
                fontFamily = font,
                fontSize = 12.sp,
                color = Color(0xFF81D4FA)
            )

            Text(
                text = "End: ${room.end_date ?: "--"}",
                fontFamily = font,
                fontSize = 12.sp,
                color = Color(0xFF80CBC4)
            )
        }
    }
}
