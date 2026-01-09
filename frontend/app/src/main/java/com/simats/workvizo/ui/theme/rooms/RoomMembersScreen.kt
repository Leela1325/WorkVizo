package com.simats.workvizo.ui.theme.rooms

/* ---------------- IMPORTS ---------------- */

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.simats.workvizo.R
import com.simats.workvizo.api.ApiService
import com.simats.workvizo.api.RetrofitClient
import com.simats.workvizo.api.MembersResponse
import com.simats.workvizo.api.MemberItem

/* ---------------- SCREEN ---------------- */

@Composable
fun RoomMembersScreen(
    navController: NavController,
    roomId: String
) {

    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    var creator by remember { mutableStateOf<MemberItem?>(null) }
    var members by remember { mutableStateOf<List<MemberItem>>(emptyList()) }

    /* ---------- FETCH MEMBERS ---------- */
    LaunchedEffect(Unit) {
        api.getAllRoomMembers(roomId)
            .enqueue(object : Callback<MembersResponse> {

                override fun onResponse(
                    call: Call<MembersResponse>,
                    response: Response<MembersResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val data = response.body()!!
                        creator = data.creator
                        members = data.members
                    }
                }

                override fun onFailure(
                    call: Call<MembersResponse>,
                    t: Throwable
                ) {
                    // optional log
                }
            })
    }

    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF020617),
            Color(0xFF0F172A),
            Color(0xFF020617)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        /* ---------- HEADER ---------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }

            Text(
                "ROOM MEMBERS",
                fontFamily = poppinsBold,
                fontSize = 20.sp,
                color = Color.White
            )

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        /* ---------- CREATOR SECTION ---------- */
        creator?.let {
            RoleSection(
                title = "CREATOR",
                accent = Color(0xFFFFD700),
                font = poppinsBold
            ) {
                CreatorCard(it, poppinsBold)
            }
        }

        Spacer(Modifier.height(22.dp))

        /* ---------- MEMBERS SECTION ---------- */
        RoleSection(
            title = "MEMBERS",
            accent = Color(0xFF00E5FF),
            font = poppinsBold
        ) {
            if (members.isEmpty()) {
                Text(
                    "No members joined yet",
                    fontFamily = poppinsBold,
                    fontSize = 13.sp,
                    color = Color.White.copy(0.6f)
                )
            } else {
                members.forEach {
                    MemberCard(it, poppinsBold)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

/* ---------------- SECTIONS ---------------- */

@Composable
fun RoleSection(
    title: String,
    accent: Color,
    font: FontFamily,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(0.06f))
            .border(1.dp, accent.copy(0.6f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {

        Text(title, fontFamily = font, fontSize = 18.sp, color = accent)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

/* ---------------- CREATOR CARD ---------------- */

@Composable
fun CreatorCard(
    member: MemberItem,
    font: FontFamily
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFD700).copy(0.35f),
                        Color(0xFFFFD700).copy(0.15f)
                    )
                )
            )
            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {

        Text("👑 ${member.name}", fontFamily = font, fontSize = 16.sp, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Text(member.email, fontFamily = font, fontSize = 12.sp, color = Color.White.copy(0.85f))
        Spacer(Modifier.height(6.dp))
        Text("Joined: ${member.joined_at ?: "--"}", fontFamily = font, fontSize = 11.sp, color = Color.White.copy(0.7f))
    }
}

/* ---------------- MEMBER CARD ---------------- */

@Composable
fun MemberCard(
    member: MemberItem,
    font: FontFamily
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(0.05f))
            .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {

        Text(member.name, fontFamily = font, fontSize = 15.sp, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(member.email, fontFamily = font, fontSize = 12.sp, color = Color.White.copy(0.8f))

        if (!member.assigned_task.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Task: ${member.assigned_task}",
                fontFamily = font,
                fontSize = 12.sp,
                color = Color(0xFF00E5FF)
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "Joined: ${member.joined_at ?: "--"}",
            fontFamily = font,
            fontSize = 11.sp,
            color = Color.White.copy(0.6f)
        )
    }
}
