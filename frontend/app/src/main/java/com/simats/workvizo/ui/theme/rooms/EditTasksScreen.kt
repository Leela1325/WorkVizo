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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R

@Composable
fun EditTasksScreen(
    navController: NavController,
    roomId: String,
    userId: String
)
 {

    val poppins = FontFamily(Font(R.font.poppins_bold))

    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF0B0F1E),
            Color(0xFF141A33),
            Color(0xFF080B18)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
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
                "EDIT TASKS",
                fontFamily = poppins,
                fontSize = 20.sp,
                color = Color.White
            )

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(Modifier.height(30.dp))

        Text(
            text = "Manage tasks for this room",
            fontFamily = poppins,
            fontSize = 14.sp,
            color = Color.White.copy(0.75f)
        )

        Spacer(Modifier.height(26.dp))

        /* ---------- ADD TASK ---------- */
        EditTaskBorderCard(
            title = "Add Task",
            desc = "Create a new task and assign members",
            borderGradient = Brush.horizontalGradient(
                listOf(Color(0xFF7C7CFF), Color(0xFF00E5FF))
            )
        ) {
            navController.navigate("add_task/$roomId/$userId")

        }

        Spacer(Modifier.height(18.dp))

        /* ---------- REMOVE TASK ---------- */
        EditTaskBorderCard(
            title = "Remove Task",
            desc = "Delete existing tasks from this room",
            borderGradient = Brush.horizontalGradient(
                listOf(Color(0xFFFF8A65), Color(0xFFFF5252))
            )
        ) {
            navController.navigate("remove_tasks/$roomId/$userId")
        }
    }
}

/* ================= BORDER STYLE CARD ================= */

@Composable
fun EditTaskBorderCard(
    title: String,
    desc: String,
    borderGradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(0.06f))
            .border(
                width = 1.5.dp,
                brush = borderGradient,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {

        Column {
            Text(
                text = title,
                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                fontSize = 18.sp,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = desc,
                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                fontSize = 13.sp,
                color = Color.White.copy(0.85f)
            )
        }
    }
}
