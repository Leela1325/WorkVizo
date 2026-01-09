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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import android.net.Uri
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*

@Composable
fun CreatedRoomsScreen(
    navController: NavController,
    userId: String,
    userName: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var rooms by remember { mutableStateOf<List<RoomItem>>(emptyList()) }

    LaunchedEffect(userId) {
        api.getCreatedRooms(userId).enqueue(object : Callback<RoomsResponse> {

            override fun onResponse(
                call: Call<RoomsResponse>,
                response: Response<RoomsResponse>
            ) {
                rooms = response.body()?.createdRooms ?: emptyList()
            }

            override fun onFailure(call: Call<RoomsResponse>, t: Throwable) {
                rooms = emptyList()
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF05061E), Color(0xFF1B1464), Color(0xFF05061E))
                )
            )
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
            text = "CREATED ROOMS",
            fontFamily = poppins,
            fontSize = 26.sp,
            color = Color.White
        )

        Spacer(Modifier.height(20.dp))

        if (rooms.isEmpty()) {
            Text(
                text = "No rooms created yet",
                fontFamily = poppins,
                color = Color.White.copy(0.6f)
            )
        } else {
            rooms.forEach { room ->
                CreatedRoomCard(
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
fun CreatedRoomCard(
    room: RoomItem,
    font: FontFamily,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(0.08f))
            .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(20.dp))
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
            color = Color(0xFF8EC5FF)
        )

        Spacer(Modifier.height(8.dp))

        if (!room.description.isNullOrBlank()) {
            Text(
                text = room.description!!,
                fontFamily = font,
                fontSize = 13.sp,
                color = Color.White.copy(0.7f),
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
                color = Color(0xFF00E5FF)
            )
            Text(
                text = "End: ${room.end_date ?: "--"}",
                fontFamily = font,
                fontSize = 12.sp,
                color = Color(0xFF00FF87)
            )
        }
    }
}
