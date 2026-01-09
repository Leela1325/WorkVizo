package com.simats.workvizo.ui.theme.rooms

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
fun RemoveMembersScreen(
    navController: NavController,
    roomId: String
) {

    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var members by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var selectedUserId by remember { mutableStateOf("") }
    var selectedUserName by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }

    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF2C3E50),
            Color(0xFF4CA1AF),
            Color(0xFF2C3E50)
        )
    )

    /* ---------- FETCH MEMBERS ---------- */
    fun loadMembers() {
        loading = true
        api.getRoomMembers(roomId)
            .enqueue(object : Callback<Map<String, Any>> {

                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    val body = response.body()
                    val list = body?.get("members") as? List<*>
                    members = list?.filterIsInstance<Map<String, Any>>() ?: emptyList()
                    loading = false
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    loading = false
                }
            })
    }

    LaunchedEffect(Unit) { loadMembers() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
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
            "REMOVE MEMBERS",
            fontFamily = poppins,
            fontSize = 26.sp,
            color = Color.White
        )

        Text(
            "Manage participants in this room",
            fontFamily = poppins,
            fontSize = 14.sp,
            color = Color.White.copy(0.8f)
        )

        Spacer(Modifier.height(20.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                members.forEach { member ->

                    val userId = member["id"].toString()
                    val name = member["name"].toString()
                    val email = member["email"].toString()

                    MemberCard(
                        name = name,
                        email = email,
                        font = poppins
                    ) {
                        selectedUserId = userId
                        selectedUserName = name
                        showConfirm = true
                    }
                }
            }
        }
    }

    /* ---------- CONFIRM POPUP ---------- */
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = Color(0xFF1C2541),
            title = {
                Text(
                    "REMOVE MEMBER?",
                    fontFamily = poppins,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove $selectedUserName from this room?",
                    fontFamily = poppins,
                    color = Color.White.copy(0.8f)
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(Color(0xFFD32F2F)),
                    onClick = {

                        api.removeMember(
                            roomId = roomId,
                            userId = selectedUserId
                        ).enqueue(object : Callback<GenericResponse> {

                            override fun onResponse(
                                call: Call<GenericResponse>,
                                response: Response<GenericResponse>
                            ) {
                                showConfirm = false
                                loadMembers() // refresh list
                            }

                            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                showConfirm = false
                            }
                        })
                    }
                ) {
                    Text("REMOVE", fontFamily = poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("CANCEL", fontFamily = poppins, color = Color.White)
                }
            }
        )
    }
}

/* ---------- MEMBER CARD ---------- */

@Composable
fun MemberCard(
    name: String,
    email: String,
    font: FontFamily,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFff512f), Color(0xFFdd2476))
                )
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column {
            Text(name, fontFamily = font, color = Color.White, fontSize = 16.sp)
            Text(
                email,
                fontFamily = font,
                color = Color.White.copy(0.85f),
                fontSize = 13.sp
            )
        }

        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, null, tint = Color.White)
        }
    }
}
