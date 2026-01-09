package com.simats.workvizo.ui.theme.home
import androidx.compose.ui.text.style.TextOverflow

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
import com.simats.workvizo.R
import com.simats.workvizo.api.*
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
    val displayName = userName.lowercase()

    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var showAllNotifications by remember { mutableStateOf(false) }

    /* 🔔 FETCH NOTIFICATIONS */
    LaunchedEffect(Unit) {
        api.getNotifications(userId).enqueue(object : Callback<NotificationsResponse> {
            override fun onResponse(
                call: Call<NotificationsResponse>,
                response: Response<NotificationsResponse>
            ) {
                notifications = response.body()?.notifications ?: emptyList()
            }

            override fun onFailure(call: Call<NotificationsResponse>, t: Throwable) {}
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
        bottomBar = { BottomNavBar(navController, userId, userName) },
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
            /* ---------- HEADER ---------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Welcome $displayName",
                    fontFamily = poppinsBold,
                    fontSize = 28.sp,
                    color = Color.White
                )

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

            /* ---------- NOTIFICATIONS ---------- */
            NotificationSection(
                notifications = notifications,
                showAll = showAllNotifications,
                onToggle = { showAllNotifications = !showAllNotifications },
                poppinsBold = poppinsBold
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

/* ===================================================== */
/* ================= NOTIFICATION SECTION ============== */
/* ===================================================== */

@Composable
fun NotificationSection(
    notifications: List<NotificationItem>,
    showAll: Boolean,
    onToggle: () -> Unit,
    poppinsBold: FontFamily
) {

    val visibleNotifications =
        if (showAll) notifications else notifications.take(5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(0.07f))
            .padding(14.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NOTIFICATIONS",
                fontFamily = poppinsBold,
                fontSize = 17.sp,
                color = Color.White
            )

            if (notifications.size > 5) {
                Icon(
                    imageVector = if (showAll)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Notifications",
                    tint = Color(0xFF8EC5FF),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onToggle() }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (visibleNotifications.isEmpty()) {
            EmptyState("No notifications")
        } else {
            visibleNotifications.forEach {
                NotificationCard(it)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/* ===================================================== */
/* ================= NOTIFICATION CARD ================= */
/* ===================================================== */
@Composable
fun NotificationCard(item: NotificationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),   // Same width for all
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(0.08f)
        ),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(Color(0xFF7C7CFF), Color(0xFF00E5FF))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {

            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF8EC5FF),
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.message,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_bold))
                    // ❌ NO maxLines
                    // ❌ NO ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = item.created_at,
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_bold))
                )
            }
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
/* ================= EXTRA ============================= */
/* ===================================================== */

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

/* ===================================================== */
/* ================= BOTTOM NAV ======================== */
/* ===================================================== */

@Composable
fun BottomNavBar(
    navController: NavController,
    userId: String,
    userName: String
) {
    NavigationBar(containerColor = Color(0xFF0B0F3B)) {

        NavigationBarItem(
            selected = true,
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
