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
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import retrofit2.*

@Composable
fun RoomSettingsScreen(
    navController: NavController,
    roomId: String,
    roomCode: String,
    userId: String,
    creatorId: String
) {
    var showLeaveDialog by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    val isCreator = userId == creatorId
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF0B132B), Color(0xFF1C2541), Color(0xFF0B132B))
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

        Spacer(Modifier.height(20.dp))

        Text(
            "ROOM SETTINGS",
            fontFamily = poppins,
            fontSize = 26.sp,
            color = Color.White
        )

        Text(
            "Manage room preferences & controls",
            fontFamily = poppins,
            fontSize = 13.sp,
            color = Color.White.copy(0.7f)
        )

        Spacer(Modifier.height(28.dp))

        /* ================= CREATOR OPTIONS ================= */
        if (isCreator) {

            PremiumSettingCard(
                title = "EDIT ROOM",
                desc = "Modify room name, description & type",
                colors = listOf(Color(0xFF7F00FF), Color(0xFFE100FF)),
                font = poppins
            ) {    navController.navigate("edit_room/$roomCode/$userId") }


            PremiumSettingCard(
                title = "REMOVE MEMBERS",
                desc = "Manage participants in this room",
                colors = listOf(Color(0xFFFF512F), Color(0xFFF09819)),
                font = poppins
            ) { navController.navigate("remove_members/$roomId") }

            PremiumSettingCard(
                title = "DELETE ROOM",
                desc = "This action is permanent",
                colors = listOf(Color(0xFFB00020), Color(0xFFFF5252)),
                font = poppins
            ) {
                showDeleteDialog = true
            }
        }

        /* ================= MEMBER OPTIONS ================= */
        if (!isCreator) {

            PremiumSettingCard(
                title = "LEAVE ROOM",
                desc = "Exit from this room permanently",
                colors = listOf(Color(0xFF355C7D), Color(0xFF6C5B7B)),
                font = poppins
            ) {
                showLeaveDialog = true
            }

            PremiumSettingCard(
                title = "REPORT ROOM",
                desc = "Report inappropriate content",
                colors = listOf(Color(0xFF614385), Color(0xFF516395)),
                font = poppins
            ) {
                navController.navigate("report_room/$roomId/$userId")
            }

        }
    }

    /* ================= DELETE CONFIRMATION ================= */
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1C2541),
            title = {
                Text(
                    "DELETE ROOM?",
                    fontFamily = poppins,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "This action cannot be undone. All data will be removed.",
                    fontFamily = poppins,
                    color = Color.White.copy(0.8f)
                )
            },
            confirmButton = {
                Button(
                    enabled = !deleting,
                    colors = ButtonDefaults.buttonColors(Color(0xFFD32F2F)),
                    onClick = {

                        deleting = true

                        api.deleteRoom(
                            roomId = roomId,
                            requestedBy = userId
                        ).enqueue(object : Callback<GenericResponse> {

                            override fun onResponse(
                                call: Call<GenericResponse>,
                                response: Response<GenericResponse>
                            ) {

                                if (response.body()?.status == "success") {

                                    showDeleteDialog = false
                                    deleting = false

                                    // SAFE EXIT
                                    navController.popBackStack()
                                    navController.popBackStack()

                                } else {
                                    deleting = false
                                    showDeleteDialog = false
                                }
                            }

                            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                deleting = false
                                showDeleteDialog = false
                            }
                        })
                    }
                ) {
                    Text(
                        if (deleting) "DELETING..." else "CONFIRM",
                        fontFamily = poppins
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", fontFamily = poppins, color = Color.White)
                }
            }
        )
    }
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = Color(0xFF1C2541),
            title = {
                Text(
                    "LEAVE ROOM?",
                    fontFamily = poppins,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "You will be removed from this room. Continue?",
                    fontFamily = poppins,
                    color = Color.White.copy(0.8f)
                )
            },
            confirmButton = {
                Button(
                    enabled = !leaving,
                    colors = ButtonDefaults.buttonColors(Color(0xFFD32F2F)),
                    onClick = {

                        leaving = true

                        api.leaveRoom(
                            roomId = roomId,
                            userId = userId
                        ).enqueue(object : Callback<GenericResponse> {

                            override fun onResponse(
                                call: Call<GenericResponse>,
                                response: Response<GenericResponse>
                            ) {
                                leaving = false
                                showLeaveDialog = false

                                if (response.body()?.status == "success") {
                                    // Go back to RoomOverview
                                    navController.popBackStack()
                                    navController.popBackStack()
                                }
                            }

                            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                leaving = false
                                showLeaveDialog = false
                            }
                        })
                    }
                ) {
                    Text(
                        if (leaving) "LEAVING..." else "CONFIRM",
                        fontFamily = poppins
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("CANCEL", fontFamily = poppins, color = Color.White)
                }
            }
        )
    }

}
/* leave room */

/* ================= PREMIUM CARD ================= */

@Composable
fun PremiumSettingCard(
    title: String,
    desc: String,
    colors: List<Color>,
    font: FontFamily,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(colors))
            .border(
                1.dp,
                Color.White.copy(0.4f),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(18.dp)
    ) {

        Text(
            title,
            fontFamily = font,
            fontSize = 17.sp,
            color = Color.White
        )

        Spacer(Modifier.height(6.dp))

        Text(
            desc,
            fontFamily = font,
            fontSize = 13.sp,
            color = Color.White.copy(0.85f)
        )
    }
}
