package com.simats.workvizo.ui.theme.rooms
import androidx.compose.ui.text.TextStyle

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
fun ReportRoomScreen(
    navController: NavController,
    roomId: String,
    userId: String
) {
    val poppins = FontFamily(Font(R.font.poppins_bold))
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }

    var issue by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF2B0000),   // deep danger red
            Color(0xFF4A0000),
            Color(0xFF1A0000)
        )
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

        Spacer(Modifier.height(24.dp))

        Text(
            "REPORT ROOM",
            fontFamily = poppins,
            fontSize = 26.sp,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "If you believe this room violates guidelines or contains inappropriate content, please let us know. Your report will be reviewed carefully.",
            fontFamily = poppins,
            fontSize = 14.sp,
            color = Color.White.copy(0.8f)
        )

        Spacer(Modifier.height(28.dp))

        Text(
            "Describe the issue",
            fontFamily = poppins,
            fontSize = 16.sp,
            color = Color.White
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = issue,
            onValueChange = { issue = it },
            placeholder = {
                Text(
                    "Describe the issue clearly…",
                    fontFamily = poppins,
                    color = Color.White.copy(0.6f)
                )
            },
            textStyle = TextStyle(
                fontFamily = poppins,
                color = Color.White,
                fontSize = 14.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(18.dp),
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF5252),
                unfocusedBorderColor = Color.White.copy(0.4f),
                cursorColor = Color(0xFFFF5252),
                focusedContainerColor = Color(0xFF1A0000),
                unfocusedContainerColor = Color(0xFF1A0000)
            )
        )


        Spacer(Modifier.height(30.dp))

        Button(
            onClick = {
                showDialog = true
            },
            enabled = issue.isNotBlank() && !submitting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(Color(0xFFD32F2F)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                if (submitting) "SUBMITTING..." else "SUBMIT REPORT",
                fontFamily = poppins,
                fontSize = 15.sp
            )
        }
    }

    /* ---------- CONFIRM POPUP ---------- */
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF1C2541),
            title = {
                Text("SUBMIT REPORT?", fontFamily = poppins, color = Color.White)
            },
            text = {
                Text(
                    "Are you sure you want to report this room? This action cannot be undone.",
                    fontFamily = poppins,
                    color = Color.White.copy(0.8f)
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(Color(0xFFD32F2F)),
                    onClick = {

                        submitting = true

                        api.reportRoom(
                            roomId = roomId,
                            userId = userId,
                            issue = issue
                        ).enqueue(object : Callback<GenericResponse> {

                            override fun onResponse(
                                call: Call<GenericResponse>,
                                response: Response<GenericResponse>
                            ) {
                                submitting = false
                                showDialog = false
                                navController.popBackStack()
                            }

                            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                submitting = false
                                showDialog = false
                            }
                        })
                    }
                ) {
                    Text("CONFIRM", fontFamily = poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("CANCEL", fontFamily = poppins, color = Color.White)
                }
            }
        )
    }
}
