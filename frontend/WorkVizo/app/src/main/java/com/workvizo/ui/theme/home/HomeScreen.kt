package com.workvizo.ui.theme.home
import android.net.Uri

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.workvizo.R
import com.workvizo.api.*
import retrofit2.*

/* ===================================================== */
/* ================= HOME SCREEN ======================= */
/* ===================================================== */

@Composable
fun HomeScreen(
    navController: NavController,
    userId: String,
    userName: String
) {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val displayName = userName.uppercase()

    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }
    var todayChanges by remember { mutableStateOf<List<TodayChange>>(emptyList()) }

    LaunchedEffect(Unit) {
        api.getTodayChanges("1").enqueue(object : Callback<TodayChangesResponse> {
            override fun onResponse(
                call: Call<TodayChangesResponse>,
                response: Response<TodayChangesResponse>
            ) {
                todayChanges = response.body()?.changes ?: emptyList()
            }

            override fun onFailure(call: Call<TodayChangesResponse>, t: Throwable) {}
        })
    }

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF05061E),
            Color(0xFF0B0F3B),
            Color(0xFF1B1464),
            Color(0xFF05061E)
        )
    )

    Scaffold(
        bottomBar = {
            BottomNavBar(navController, userId, userName)
        },
        containerColor = Color.Transparent
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(16.dp))

            /* ---------- HEADER ---------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "WELCOME BACK",
                        fontFamily = poppinsBold,
                        fontSize = 16.sp,
                        color = Color(0xFF8EC5FF)
                    )

                    Text(
                        displayName,
                        fontFamily = poppinsBold,
                        fontSize = 32.sp,
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF7C7CFF),
                                    Color(0xFF00E5FF),
                                    Color(0xFF00FF87)
                                )
                            )
                        )
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            /* ---------- DASHBOARD CARDS ---------- */

            DashboardVerticalCard(
                title = "CREATE ROOM",
                description = "Manually create tasks",
                lottie = R.raw.createmanual,
                gradient = Brush.horizontalGradient(
                    listOf(Color(0xFF7C7CFF), Color(0xFFB983FF))
                ),
                poppinsBold = poppinsBold
            ) {
                navController.navigate("create_room_manual/$userId/$userName")
            }

            Spacer(Modifier.height(14.dp))

            DashboardVerticalCard(
                title = "CREATE USING AI",
                description = "AI generated workflow",
                lottie = R.raw.createusingai,
                gradient = Brush.horizontalGradient(
                    listOf(Color(0xFF00E5FF), Color(0xFF00FF87))
                ),
                poppinsBold = poppinsBold
            ) {
                navController.navigate("create_room_ai/$userId/$userName")
            }

            Spacer(Modifier.height(14.dp))

            DashboardVerticalCard(
                title = "JOIN ROOM",
                description = "Enter room code",
                lottie = R.raw.join,
                gradient = Brush.horizontalGradient(
                    listOf(Color(0xFFFF8A65), Color(0xFFFFD54F))
                ),
                poppinsBold = poppinsBold
            ) {
                navController.navigate("join_room/$userId/$userName")
            }

            Spacer(Modifier.height(24.dp))

            /* ---------- TODAY’S CHANGES ---------- */
            AnimatedSection("TODAY’S CHANGES") {
                if (todayChanges.isEmpty()) {
                    EmptyState("No updates today")
                } else {
                    todayChanges.forEach {
                        TodayChangeCard(it)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/* ===================================================== */
/* ================= DASHBOARD CARD ==================== */
/* ===================================================== */

@Composable
fun DashboardVerticalCard(
    title: String,
    description: String,
    lottie: Int,
    gradient: Brush,
    poppinsBold: FontFamily,
    onClick: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "dash")
    val float by infiniteTransition.animateFloat(
        -4f, 4f,
        infiniteRepeatable(tween(2800), RepeatMode.Reverse)
    )

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f)

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(lottie)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .graphicsLayer {
                translationY = float
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(26.dp))
            .background(gradient)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(80.dp)
            )

            Spacer(Modifier.width(14.dp))

            Column {
                Text(title, fontFamily = poppinsBold, fontSize = 18.sp, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text(description, fontFamily = poppinsBold, fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

/* ===================================================== */
/* ================= EXTRA COMPOSABLES ================= */
/* ===================================================== */

@Composable
fun AnimatedSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(0.07f))
            .padding(14.dp)
    ) {
        Text(title, fontSize = 17.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun EmptyState(text: String) {
    Text(
        text = text,
        color = Color.White.copy(0.7f),
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    )
}

@Composable
fun TodayChangeCard(change: TodayChange) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(0.08f)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "${change.user_name} • ${change.time}",
                color = Color(0xFF8EC5FF),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(change.description, color = Color.White, fontSize = 14.sp)
        }
    }
}

/* ===================================================== */
/* ================= BOTTOM NAV BAR ==================== */
/* ===================================================== */

@Composable
fun BottomNavBar(
    navController: NavController,
    userId: String,
    userName: String
) {
    NavigationBar(containerColor = Color(0xFF0B0F3B)) {

        NavigationBarItem(
            selected = true, // Home highlighted
            onClick = {},
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("create_room_manual/$userId/$userName")
            },
            icon = { Icon(Icons.Default.AddTask, null) },
            label = { Text("Create") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                val safeUserName = Uri.encode(userName)
                navController.navigate("rooms_overview/$userId/$safeUserName")

            },
            icon = { Icon(Icons.Default.Groups, null) },
            label = { Text("Rooms") }
        )



        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("profile/$userId/$userName")

            },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Profile") }
        )
    }
}

