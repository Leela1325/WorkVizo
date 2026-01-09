package com.simats.workvizo.ui.theme.home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.simats.workvizo.R

@Composable
fun EditProfileScreen(
    navController: NavController,
    userId: String
)

 {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF04051C),
            Color(0xFF0C1042),
            Color(0xFF1A176A)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        /* ---------- HEADER ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }

            Text(
                text = "EDIT PROFILE",
                fontFamily = poppinsBold,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterEnd)
            )
        }

        Spacer(Modifier.height(28.dp))

        /* ---------- SECTIONS ---------- */

        EditProfileSection(
            icon = Icons.Default.Email,
            title = "Change Email",
            description = "Update your registered email address",
            gradient = Brush.horizontalGradient(
                listOf(Color(0xFF7C7CFF), Color(0xFF00E5FF))
            ),
            poppinsBold = poppinsBold
        ) {
            if (userId.isNotBlank()) {
                navController.navigate("change_email/$userId")
            }

        }


        EditProfileSection(
            icon = Icons.Default.Lock,
            title = "Change Password",
            description = "Secure your account with a new password",
            gradient = Brush.horizontalGradient(
                listOf(Color(0xFF00FF87), Color(0xFF00C853))
            ),
            poppinsBold = poppinsBold
        ) {
            navController.navigate("change_password/$userId")

        }

        EditProfileSection(
            icon = Icons.Default.Cake,
            title = "Change Date of Birth",
            description = "Update your personal information",
            gradient = Brush.horizontalGradient(
                listOf(Color(0xFFFF8A65), Color(0xFFFFD54F))
            ),
            poppinsBold = poppinsBold
        ) {
            if (userId.isNotBlank()) {
                navController.navigate("change_dob/$userId")
            }
        }

        Spacer(Modifier.height(12.dp))

        /* ---------- DELETE PROFILE ---------- */
        EditProfileSection(
            icon = Icons.Default.Delete,
            title = "Delete Profile",
            description = "Permanently remove your account",
            gradient = Brush.horizontalGradient(
                listOf(Color(0xFFD32F2F), Color(0xFFB71C1C))
            ),
            poppinsBold = poppinsBold,
            isDanger = true
        ) {
            navController.navigate("delete_profile/$userId")
        }

        Spacer(Modifier.height(20.dp))
    }
}
@Composable
fun EditProfileSection(
    icon: ImageVector,
    title: String,
    description: String,
    gradient: Brush,
    poppinsBold: FontFamily,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        label = "section_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()              // ✅ FULL WIDTH
            .heightIn(min = 110.dp)      // ✅ PROPER HEIGHT
            .padding(vertical = 10.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interaction,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()            // ✅ FILL CARD
                .background(gradient)
                .padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(Modifier.width(18.dp))

                Column(
                    modifier = Modifier.weight(1f) // ✅ EXPAND CONTENT
                ) {
                    Text(
                        text = title,
                        fontFamily = poppinsBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = description,
                        fontFamily = poppinsBold,
                        fontSize = 13.sp,
                        color = Color.White.copy(0.9f)
                    )
                }
            }
        }
    }
}
