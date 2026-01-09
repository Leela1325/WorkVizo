package com.simats.workvizo.ui.theme.rooms

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

@Composable
fun EditProofScreen(
    navController: NavController,
    taskId: String,
    userId: String
) {

    val context = LocalContext.current
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }
    val poppins = FontFamily(Font(R.font.poppins_bold))

    var description by remember { mutableStateOf("") }
    var proofUri by remember { mutableStateOf<Uri?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { proofUri = it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F2027), Color(0xFF203A43))
                )
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        /* ---------- HEADER ---------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Edit Proof",
                fontFamily = poppins,
                fontSize = 22.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.height(24.dp))

        /* ---------- FILE ---------- */
        Text("Proof File", fontFamily = poppins, color = Color.White)
        Text(
            "Replace existing proof",
            color = Color.White.copy(0.6f),
            fontSize = 12.sp
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { picker.launch("*/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                if (proofUri == null) "Choose File" else "File Selected",
                fontFamily = poppins
            )
        }

        Spacer(Modifier.height(20.dp))

        /* ---------- DESCRIPTION ---------- */
        Text("Description", fontFamily = poppins, color = Color.White)
        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("Describe your proof") }
        )

        Spacer(Modifier.height(30.dp))

        /* ---------- SAVE ---------- */
        Button(
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87)),
            onClick = {

                /* 🔥 VALIDATION (ONLY ADDITION) */
                when {
                    proofUri == null -> {
                        Toast.makeText(
                            context,
                            "Please select a proof file",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    description.trim().isEmpty() -> {
                        Toast.makeText(
                            context,
                            "Please enter description",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                }

                loading = true

                val file = getFileFromUri(context, proofUri!!)
                val proofPart = MultipartBody.Part.createFormData(
                    "proof",
                    file.name,
                    file.asRequestBody("application/octet-stream".toMediaType())
                )

                api.updateProof(
                    taskId.toRequestBody("text/plain".toMediaType()),
                    userId.toRequestBody("text/plain".toMediaType()),
                    description.toRequestBody("text/plain".toMediaType()),
                    proofPart
                ).enqueue(object : Callback<GenericResponse> {

                    override fun onResponse(
                        call: Call<GenericResponse>,
                        response: Response<GenericResponse>
                    ) {
                        loading = false
                        if (response.isSuccessful && response.body()?.status == "success") {
                            showSuccess = true
                        } else {
                            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        loading = false
                        Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                    }
                })
            }
        ) {
            Text("Save Proof", fontFamily = poppins, color = Color.Black)
        }
    }

    /* ---------- SUCCESS POPUP ---------- */
    if (showSuccess) {

        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "Proof Updated",
                        fontFamily = poppins,
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Changes saved successfully",
                        fontFamily = poppins,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("TASK_UPDATED", true)

            navController.popBackStack()
        }
    }
}


/* ---------- FILE UTILITY (ONLY ONE, NO DUPLICATES) ---------- */
fun uriToCacheFile(context: Context, uri: Uri): File {
    val input = context.contentResolver.openInputStream(uri)
        ?: throw IllegalStateException("Cannot open file")

    val file = File(context.cacheDir, "proof_${System.currentTimeMillis()}")
    val output = FileOutputStream(file)

    input.copyTo(output)
    input.close()
    output.close()

    return file
}
fun getFileFromUri(context: Context, uri: Uri): File {

    val contentResolver = context.contentResolver
    val mime = contentResolver.getType(uri) ?: "application/octet-stream"

    val extension = when (mime) {
        "image/jpeg" -> ".jpg"
        "image/png" -> ".png"
        "application/pdf" -> ".pdf"
        else -> ""
    }

    val inputStream = contentResolver.openInputStream(uri)
        ?: throw IllegalStateException("Cannot open URI")

    val file = File(
        context.cacheDir,
        "proof_${System.currentTimeMillis()}$extension"
    )

    val outputStream = FileOutputStream(file)
    inputStream.copyTo(outputStream)

    inputStream.close()
    outputStream.close()

    return file
}
