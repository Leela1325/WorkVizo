package com.simats.workvizo.ui.theme.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import android.content.Context



@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String,
    userName: String
) {

    /* ---------------- STATE ---------------- */

    var showLogoutDialog by remember { mutableStateOf(false) }

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    /* ✅ LOCAL FALLBACK (REGISTER FIX) */
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences("workvizo_prefs", Context.MODE_PRIVATE)
    }


    var name by remember { mutableStateOf(userName) }
    var email by remember {
        mutableStateOf(prefs.getString("email", "Not available") ?: "Not available")
    }
    var dob by remember {
        mutableStateOf(prefs.getString("dob", "Not available") ?: "Not available")
    }
    val displayName = name.ifBlank { "USER" }.uppercase()


    /* ---------------- FETCH USER PROFILE ---------------- */

    LaunchedEffect(userId) {
        api.getUserProfile(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(
                call: Call<UserProfileResponse>,
                response: Response<UserProfileResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    response.body()?.user?.let {

                        if (it.name.isNotBlank()) {
                            name = it.name
                        }

                        if (it.email.isNotBlank()) {
                            email = it.email
                            prefs.edit().putString("email", it.email).apply()
                        }

                        if (!it.dob.isNullOrBlank()) {
                            dob = it.dob
                        }

                    }
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                // fallback already handled
            }
        })
    }

    /* ---------------- BACKGROUND ---------------- */

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF05061E),
            Color(0xFF0B0F3B),
            Color(0xFF1B1464)
        )
    )

    /* ---------------- LOGOUT POPUP ---------------- */

    if (showLogoutDialog) {
        BeautifulLogoutPopup(
            onConfirm = {
                showLogoutDialog = false
                clearUserSession(navController)
            },
            onCancel = {
                showLogoutDialog = false
            }
        )
    }

    /* ---------------- SCREEN ---------------- */

    Scaffold(
        bottomBar = {
            BottomNavBarProfile(navController, userId, userName)
        },
        containerColor = Color.Transparent
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            /* ---------------- HEADER ---------------- */

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PROFILE",
                    fontFamily = poppinsBold,
                    fontSize = 24.sp,
                    color = Color.White
                )
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            /* ---------------- CENTERED ANIMATION ---------------- */

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.profile)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "WELCOME $displayName",
                        fontFamily = poppinsBold,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
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
            }

            Spacer(Modifier.height(28.dp))

            /* ---------------- USER INFO ---------------- */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFF7C7CFF), Color(0xFF00E5FF))
                    )
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(0.08f)
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    InfoRow("EMAIL", email, poppinsBold)
                    DividerGlow()
                    InfoRow("DATE OF BIRTH", dob, poppinsBold)
                }
            }

            Spacer(Modifier.height(32.dp))

            /* ---------------- ACTIONS ---------------- */

            SectionTitle("ACCOUNT & APP")

            ActionCard(Icons.Default.Edit, "Edit Profile", poppinsBold) {
                navController.navigate("edit_profile/$userId")
            }

            ActionCard(Icons.Default.PrivacyTip, "Privacy Policy", poppinsBold) {
                navController.navigate("privacy_policy")
            }

            ActionCard(Icons.Default.Description, "Terms & Conditions", poppinsBold) {
                navController.navigate("terms_conditions")
            }

            ActionCard(Icons.Default.Info, "About Us", poppinsBold) {
                navController.navigate("about_workvizo")
            }

            ActionCard(Icons.Default.Feedback, "Feedback", poppinsBold) {
                navController.navigate("feedback/$userId")
            }

            Spacer(Modifier.height(30.dp))

            /* ---------------- LOGOUT ---------------- */

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFB71C1C).copy(0.35f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Logout, null, tint = Color.Red)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "Logout",
                        fontFamily = poppinsBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

/* ===================================================== */
/* =================== LOGOUT POPUP ==================== */
/* ===================================================== */

@Composable
fun BeautifulLogoutPopup(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {

        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0B0F3B)
            )
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(56.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Logout",
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 20.sp,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Are you sure you want to logout from WorkVizo?",
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 13.sp,
                    color = Color.White.copy(0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.4f))
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Text("Logout", color = Color.White)
                    }
                }
            }
        }
    }
}

/* ===================================================== */
/* =================== HELPERS ========================== */
/* ===================================================== */

fun clearUserSession(navController: NavController) {
    val context = navController.context
    context.getSharedPreferences("workvizo_prefs", 0)
        .edit()
        .clear()
        .apply()

    navController.navigate("login") {
        popUpTo(0) { inclusive = true }
    }
}

@Composable
fun InfoRow(label: String, value: String, font: FontFamily) {
    Column {
        Text(label, fontFamily = font, fontSize = 12.sp, color = Color(0xFF8EC5FF))
        Spacer(Modifier.height(4.dp))
        Text(value, fontFamily = font, fontSize = 15.sp, color = Color.White)
    }
}

@Composable
fun DividerGlow() {
    Spacer(Modifier.height(12.dp))
    Divider(color = Color.White.copy(0.15f))
    Spacer(Modifier.height(12.dp))
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        fontFamily = FontFamily(Font(R.font.poppins_bold)),
        fontSize = 14.sp,
        color = Color(0xFF7C7CFF),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    font: FontFamily,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White)
            Spacer(Modifier.width(14.dp))
            Text(title, fontFamily = font, fontSize = 15.sp, color = Color.White)
        }
    }
}

@Composable
fun BottomNavBarProfile(
    navController: NavController,
    userId: String,
    userName: String
) {
    NavigationBar(containerColor = Color(0xFF0B0F3B)) {

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home/$userId/$userName") },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("create_room_manual/$userId/$userName") },
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
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Profile") }
        )
    }
}
