package com.simats.workvizo.ui.theme.rooms
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.LottieConstants

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomAiAssistantScreen(
    navController: NavController,
    roomId: String,
    userId: String
) {
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    /* ---------- INIT AI SESSION (RUN ONCE) ---------- */
    LaunchedEffect(Unit) {
        api.initRoomAi(roomId, userId)
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {
                    // session initialized successfully
                }

                override fun onFailure(
                    call: Call<Map<String, String>>,
                    t: Throwable
                ) {
                    // optional: log or show error
                }
            })
    }

    val poppins = FontFamily(Font(R.font.poppins_bold))


    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val messages = remember { mutableStateListOf<ChatMsg>() }
    val listState = rememberLazyListState()

    /* ---------- AUTO SCROLL ---------- */
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val background = Brush.verticalGradient(
        listOf(Color(0xFF030712), Color(0xFF0F172A), Color(0xFF020617))
    )

    val borderGradient = Brush.horizontalGradient(
        listOf(Color(0xFF22C55E), Color(0xFF38BDF8), Color(0xFF7C7CFF))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp)
    ) {

        /* ---------- HEADER (UNCHANGED) ---------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text("AI Assistant", fontFamily = poppins, color = Color.White, fontSize = 20.sp)
        }

        Spacer(Modifier.height(18.dp))

        /* ---------- LOTTIE CARD ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(1.6.dp, borderGradient, RoundedCornerShape(28.dp))
                .background(Color.White.copy(0.06f)),
            contentAlignment = Alignment.Center
        ) {
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.ai)
            )
            LottieAnimation(
                composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(180.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        /* ---------- CHAT LIST ---------- */
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(messages) { msg ->

                when (msg.type) {

                    /* ---------- USER MESSAGE ---------- */
                    MsgType.USER -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF38BDF8))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    msg.text,
                                    fontFamily = poppins,
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    /* ---------- AI THINKING ---------- */
                    MsgType.AI_THINKING -> {
                        Row {
                            Image(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(0.12f))
                                    .padding(12.dp)
                            ) {
                                AiThinkingDots()
                            }
                        }
                    }

                    /* ---------- AI RESPONSE ---------- */
                    MsgType.AI_REPLY -> {
                        Row {
                            Image(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(0.12f))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    msg.text,
                                    fontFamily = poppins,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        /* ---------- INPUT BAR ---------- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .clip(RoundedCornerShape(22.dp))
                .border(1.6.dp, borderGradient, RoundedCornerShape(22.dp))
                .background(Color.White.copy(0.08f))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = poppins,
                    fontSize = 14.sp
                ),
                placeholder = {
                    Text(
                        "Ask about tasks, deadlines, progress...",
                        fontFamily = poppins,
                        color = Color.White.copy(0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 3
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                enabled = !loading,
                onClick = {
                    if (query.isBlank()) return@IconButton

                    val userMsg = query
                    query = ""
                    loading = true

                    messages.add(ChatMsg(userMsg, MsgType.USER))
                    messages.add(ChatMsg(type = MsgType.AI_THINKING))

                    api.roomAiChat(
                        mapOf(
                            "message" to userMsg
                        )
                    )
                        .enqueue(object : Callback<Map<String, String>> {

                            override fun onResponse(
                                call: Call<Map<String, String>>,
                                response: Response<Map<String, String>>
                            ) {
                                loading = false
                                messages.removeAt(messages.lastIndex) // remove thinking
                                messages.add(
                                    ChatMsg(
                                        response.body()?.get("reply")
                                            ?: "No response from AI",
                                        MsgType.AI_REPLY
                                    )
                                )
                            }

                            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                                loading = false
                                if (messages.isNotEmpty()) {
                                    messages.removeAt(messages.lastIndex) // remove thinking bubble
                                }

                                val msg = when {
                                    t is java.net.SocketTimeoutException ->
                                        "AI is taking longer than usual. Please wait and try again."

                                    t is java.io.IOException ->
                                        "Network error. Check your connection."

                                    else ->
                                        "Something went wrong. Please try again."
                                }

                                messages.add(ChatMsg(msg, MsgType.AI_REPLY))
                            }

                        })
                }
            ) {
                Icon(Icons.Default.Send, null, tint = Color(0xFF38BDF8))
            }
        }
    }
}
@Composable
fun AiThinkingDots() {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        0.3f, 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        )
    )

    Text("•••", color = Color.White.copy(alpha), fontSize = 18.sp)
}
