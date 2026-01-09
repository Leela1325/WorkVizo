package com.simats.workvizo.ui.theme.rooms
import androidx.compose.material.icons.filled.Person

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import kotlinx.coroutines.delay
import retrofit2.*

/* ================= MODEL ================= */

data class ChatMessage(
    val user_id: String,
    val user_name: String,
    val message: String,
    val time: String
)

/* ================= SCREEN ================= */

@Composable
fun RoomChatScreen(
    navController: NavController,
    roomId: String,
    userId: String,          // logged-in user
    userName: String,
    creatorId: String        // 🔥 room creator id
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var input by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    /* ---------- FETCH ---------- */
    fun fetchMessages() {
        api.getRoomMessages(roomId.toInt()).enqueue(object :
            Callback<Map<String, Any>> {

            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                val list = response.body()?.get("messages")
                        as? List<Map<String, String>> ?: emptyList()

                messages = list.map {
                    ChatMessage(
                        user_id = it["user_id"] ?: "",
                        user_name = it["user_name"] ?: "",
                        message = it["message"] ?: "",
                        time = it["time"] ?: ""
                    )
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Log.e("CHAT", "Fetch failed", t)
            }
        })
    }

    /* ---------- LOAD + POLLING ---------- */
    LaunchedEffect(roomId) {
        fetchMessages()
        while (true) {
            delay(3000)
            fetchMessages()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    /* ---------- BACKGROUND ---------- */
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF020617), Color(0xFF0F172A))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {

        /* ================= HEADER ================= */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "Room Chat",
            fontFamily = poppins,
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
        )

        /* ================= GREETING ================= */
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Welcome to Room Chat",
                        fontFamily = poppins,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Start collaborating with your team.\nAll messages will appear here.",
                        fontFamily = poppins,
                        fontSize = 14.sp,
                        color = Color.White.copy(0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {

            /* ================= CHAT LIST ================= */
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                items(messages) { msg ->
                    WhatsAppBubble(
                        msg = msg,
                        isMine = msg.user_id == userId,
                        isCreator = msg.user_id == creatorId,
                        poppins = poppins
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        /* ================= INPUT ================= */
        Row(
            modifier = Modifier
                .padding(12.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(0.06f))
                .border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Type a message", fontFamily = poppins) },
                textStyle = TextStyle(fontFamily = poppins, color = Color.White),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
                    .clickable {
                        if (input.isNotBlank()) {
                            val text = input
                            input = ""

                            api.sendRoomMessage(
                                roomId.toInt(),
                                userId.toInt(),
                                userName,
                                text
                            ).enqueue(object : Callback<Map<String, Any>> {
                                override fun onResponse(
                                    call: Call<Map<String, Any>>,
                                    response: Response<Map<String, Any>>
                                ) {
                                    fetchMessages()
                                }

                                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                    Log.e("CHAT", "Send failed", t)
                                }
                            })
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(">", fontFamily = poppins, fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

/* ================= MESSAGE BUBBLE ================= */

@Composable
fun WhatsAppBubble(
    msg: ChatMessage,
    isMine: Boolean,
    isCreator: Boolean,
    poppins: FontFamily
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {

        if (!isMine) {
            UserAvatar()
            Spacer(Modifier.width(6.dp))
        }

        if (isMine) Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .widthIn(min = 100.dp, max = 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isCreator) 1.5.dp else 0.dp,
                    brush = if (isCreator)
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        )
                    else SolidColor(Color.Transparent),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    if (isMine)
                        Brush.horizontalGradient(
                            listOf(Color(0xFF22C55E), Color(0xFF16A34A))
                        )
                    else
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF334155))
                        )
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {

            if (!isMine) {
                Text(
                    msg.user_name,
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    color = if (isCreator) Color(0xFFFFD700) else Color(0xFF60A5FA)
                )
                Spacer(Modifier.height(2.dp))
            }

            Text(
                msg.message,
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.White
            )

            Spacer(Modifier.height(4.dp))

            Text(
                msg.time,
                fontFamily = poppins,
                fontSize = 10.sp,
                color = Color.White.copy(0.6f),
                modifier = Modifier.align(Alignment.End)
            )
        }

        if (!isMine) Spacer(Modifier.weight(1f))

        if (isMine) {
            Spacer(Modifier.width(6.dp))
            UserAvatar()
        }
    }
}

/* ================= USER AVATAR ================= */

@Composable
fun UserAvatar() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFF334155)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Person,   // 👤 BUILT-IN ICON
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

