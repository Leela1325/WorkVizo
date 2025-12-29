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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.workvizo.R
import com.workvizo.api.*
import retrofit2.*
import android.net.Uri
@Composable
fun RoomsOverviewScreen(
    navController: NavController,
    userId: String,
    userName: String,
    onClick: () -> Unit
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var created by remember { mutableStateOf(emptyList<RoomItem>()) }
    var joined by remember { mutableStateOf(emptyList<RoomItem>()) }
    var active by remember { mutableStateOf(emptyList<RoomItem>()) }
    var completed by remember { mutableStateOf(emptyList<RoomItem>()) }
    var recent by remember { mutableStateOf(emptyList<RoomItem>()) }


    LaunchedEffect(Unit) {
        api.getCreatedRooms(userId).enqueue(simple { created = it.createdRooms ?: emptyList()})
        api.getJoinedRooms(userId).enqueue(simple { joined = it.rooms ?: emptyList() })
        api.getActiveRooms(userId).enqueue(simple { active = it.activeRooms ?: emptyList() })
        api.getCompletedRooms(userId).enqueue(simple { completed = it.completedRooms ?: emptyList() })
        api.getRecentRooms(userId).enqueue(simple { recent = it.recentRooms ?: emptyList() })
    }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF05061E), Color(0xFF1B1464), Color(0xFF05061E))
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
            "YOUR ROOMS",
            fontFamily = poppins,
            fontSize = 26.sp,
            color = Color.White
        )

        Spacer(Modifier.height(20.dp))


        RoomsSection(
            title = "CREATED ROOMS",
            desc = "Rooms you started",
            rooms = created,
            font = poppins,
            accent = Color(0xFF7C7CFF)
        ) {
            val safeUserName = Uri.encode(userName)
            navController.navigate("created_rooms/$userId/$userName")

        }

        RoomsSection(
            "JOINED ROOMS",
            "Rooms you joined",
            joined,
            poppins,
            Color(0xFF00E5FF)
        ) {
            val safeUserName = Uri.encode(userName)
            navController.navigate("joined_rooms/$userId/$userName")
        }

        RoomsSection(
            "ACTIVE ROOMS",
            "Currently running",
            active,
            poppins,
            Color(0xFF00FF87)
        ) {
            val safeUserName = Uri.encode(userName)
            navController.navigate("active_rooms/$userId/$safeUserName")

        }

        RoomsSection(
            "COMPLETED ROOMS",
            "Finished projects",
            completed,
            poppins,
            Color(0xFFFF8A65)
        ) {
            val safeUserName = Uri.encode(userName)
            navController.navigate("completed_rooms/$userId/$safeUserName")

        }

        RoomsSection(
            "RECENT ROOMS",
            "Recently accessed",
            recent,
            poppins,
            Color(0xFFFFD54F)
        ) {
            val safeUserName = Uri.encode(userName)
            navController.navigate("recent_rooms/$userId/$safeUserName")

        }

    }
}

/* ---------- SECTION ---------- */

@Composable
fun RoomsSection(
    title: String,
    desc: String,
    rooms: List<RoomItem>,
    font: FontFamily,
    accent: Color,
    onClick: () -> Unit   // ✅ NOT nullable
)
{
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(0.06f))
            .border(1.dp, accent.copy(0.5f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, fontFamily = font, fontSize = 18.sp, color = accent)
                Text(desc, fontFamily = font, fontSize = 12.sp, color = Color.White.copy(0.7f))
            }

            if (onClick != null) {
                Text(
                    text = "View All",
                    fontFamily = font,
                    fontSize = 12.sp,
                    color = accent,
                    modifier = Modifier.clickable { onClick.invoke() }
                )
            }



        }

        Spacer(Modifier.height(14.dp))

        if (rooms.isEmpty()) {
            Text(
                "No rooms found",
                fontFamily = font,
                fontSize = 13.sp,
                color = Color.White.copy(0.6f)
            )
        } else {
            rooms.take(3).forEach { room ->
                RoomCard(room, font, accent)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}


/* ---------- SIMPLE CALLBACK ---------- */

fun <T> simple(onSuccess: (T) -> Unit) =
    object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            response.body()?.let(onSuccess)
        }
        override fun onFailure(call: Call<T>, t: Throwable) {}
    }
@Composable
fun RoomCard(
    room: RoomItem,
    font: FontFamily,
    accent: Color
) {
    val status = room.room_status?.uppercase() ?: "UNKNOWN"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = 0.35f),
                        accent.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                1.dp,
                accent.copy(alpha = 0.6f),
                RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {

        /* TOP ROW */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CODE: ${room.room_code ?: "--"}",
                fontFamily = font,
                fontSize = 11.sp,
                color = Color.White.copy(0.85f)
            )

            Text(
                text = status,
                fontFamily = font,
                fontSize = 10.sp,
                color = Color.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(0.85f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = room.name,
            fontFamily = font,
            fontSize = 17.sp,
            color = Color.White
        )

        if (!room.description.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = room.description,
                fontFamily = font,
                fontSize = 12.sp,
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
