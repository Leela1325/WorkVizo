package com.simats.workvizo.ui.theme.rooms

import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.workvizo.R
import com.simats.workvizo.api.*
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

@Composable
fun ChangeTaskStatusScreen(
    navController: NavController,
    taskId: String,
    userId: String,
    isCreator: Boolean
) {

    val context = LocalContext.current
    val api = remember { RetrofitClient.instance.create(ApiService::class.java) }
    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    var status by remember { mutableStateOf("pending") }
    var dropdownOpen by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    var proofUri by remember { mutableStateOf<Uri?>(null) }
    var proofPath by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val statusOptions = listOf("pending", "in_progress", "completed")

    LaunchedEffect(taskId) {
        status = "pending"
        description = ""
        comment = ""
        proofUri = null
        proofPath = ""
    }

    LaunchedEffect(taskId) {
        if (taskId.isBlank()) return@LaunchedEffect
        api.getLatestProof(taskId).enqueue(object : Callback<LatestProofResponse> {
            override fun onResponse(
                call: Call<LatestProofResponse>,
                response: Response<LatestProofResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val proof = response.body()!!.proof
                    proofPath = proof.file_path
                    status = proof.status
                }
            }
            override fun onFailure(call: Call<LatestProofResponse>, t: Throwable) {}
        })
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> proofUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020617), Color(0xFF0F172A))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {

        /* ---------- HEADER ---------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Text(
                "Change Task Status",
                fontFamily = poppinsBold,
                fontSize = 24.sp,
                color = Color.White
            )
        }

        Text(
            "Update the current progress of this task and submit the required information.",
            fontSize = 13.sp,
            color = Color.White.copy(0.7f)
        )

        Spacer(Modifier.height(24.dp))

        /* ---------- STATUS ---------- */
        Text("Task Status", fontFamily = poppinsBold, color = Color.White)
        Text(
            "Choose the latest status to reflect the task progress accurately.",
            fontSize = 12.sp,
            color = Color.White.copy(0.65f)
        )

        Spacer(Modifier.height(8.dp))

        Box {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { dropdownOpen = true },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.2f)),
                color = Color(0xFF020617)
            ) {
                Row(
                    Modifier.padding(14.dp),
                    Arrangement.SpaceBetween
                ) {
                    Text(
                        status.replace("_", " ").uppercase(),
                        fontFamily = poppinsBold,
                        color = statusColor(status)
                    )
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                }
            }

            DropdownMenu(
                expanded = dropdownOpen,
                onDismissRequest = { dropdownOpen = false }
            ) {
                statusOptions.forEach {
                    DropdownMenuItem(
                        text = { Text(it.uppercase(), fontFamily = poppinsBold) },
                        onClick = {
                            status = it
                            dropdownOpen = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        /* ---------- CREATOR VIEW ---------- */
        if (isCreator) {

            Text(
                "Review the submitted proof and provide feedback if required.",
                fontSize = 12.sp,
                color = Color.White.copy(0.7f)
            )

            Spacer(Modifier.height(12.dp))

            Button(
                enabled = proofPath.isNotEmpty(),
                onClick = { openProofFile(context, proofPath) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Submitted Proof", fontFamily = poppinsBold)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Creator Comment") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        /* ---------- USER VIEW ---------- */
        if (!isCreator) {

            Text(
                "Upload proof to support the selected task status.",
                fontSize = 12.sp,
                color = Color.White.copy(0.7f)
            )

            Spacer(Modifier.height(12.dp))

            Button(onClick = { filePicker.launch("*/*") }) {
                Text(
                    if (proofUri == null) "Choose Proof File" else "File Selected",
                    fontFamily = poppinsBold
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Notes") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Make sure all details are correct before submitting the update.",
            fontSize = 12.sp,
            color = Color.White.copy(0.7f)
        )

        Spacer(Modifier.height(12.dp))

        Button(
            modifier = Modifier.fillMaxWidth().height(54.dp),
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
            onClick = {

                if (taskId.isBlank()) {
                    Toast.makeText(context, "Invalid task", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (!isCreator && proofUri == null) {
                    Toast.makeText(context, "Select proof file", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                loading = true

                if (isCreator) {
                    api.creatorUpdateStatus(taskId, status, comment)
                        .enqueue(simpleResponseCallback(context) {
                            loading = false
                            showSuccess = true
                        })
                    return@Button
                }

                val (fileName, mimeType) = getFileMeta(context, proofUri!!)

                val file = uriToFile(context, proofUri!!)
                val requestFile = file.asRequestBody(mimeType.toMediaType())

                val proofPart = MultipartBody.Part.createFormData(
                    "proof",
                    fileName,              // ✅ ORIGINAL NAME
                    requestFile
                )


                api.submitProof(
                    taskId.toRequestBody("text/plain".toMediaType()),
                    userId.toRequestBody("text/plain".toMediaType()),
                    status.toRequestBody("text/plain".toMediaType()),
                    description.toRequestBody("text/plain".toMediaType()),
                    proofPart
                ).enqueue(simpleResponseCallback(context) {
                    loading = false
                    showSuccess = true
                })
            }
        ) {
            Text(if (isCreator) "Update Status" else "Submit Proof", fontFamily = poppinsBold)
        }
    }

    if (showSuccess) {
        StatusSuccessPopup()
        LaunchedEffect(showSuccess) {
            delay(1000)
            navController.popBackStack()
        }
    }
}

/* ---------------- HELPERS ---------------- */

fun statusColor(status: String): Color =
    when (status) {
        "completed" -> Color(0xFF22C55E)
        "in_progress" -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

fun uriToFile(context: Context, uri: Uri): File {
    val file = File(context.cacheDir, "proof_${System.currentTimeMillis()}")
    context.contentResolver.openInputStream(uri)!!.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file
}

fun openProofFile(context: Context, path: String) {

    val url =
        if (path.startsWith("http")) path
        else "http://10.163.250.141/workvizo_backend/$path"


    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)          // ✅ ONLY set data
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(
        Intent.createChooser(intent, "Open proof")
    )
}


fun simpleResponseCallback(
    context: Context,
    onSuccess: () -> Unit
) = object : Callback<SimpleResponse> {

    override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
        val body = response.body()
        if (response.isSuccessful && body?.status == "success") {
            onSuccess()
        } else {
            Toast.makeText(context, body?.message ?: "Error", Toast.LENGTH_LONG).show()
        }
    }

    override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
        Toast.makeText(context, t.message ?: "Network error", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun StatusSuccessPopup() {

    val poppinsBold = FontFamily(Font(R.font.poppins_bold))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF16A34A), Color(0xFF15803D))
                    )
                )
                .padding(vertical = 26.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Status Updated",
                fontFamily = poppinsBold,
                fontSize = 18.sp,
                color = Color.White
            )

            Text(
                text = "Your changes have been saved successfully.",
                fontSize = 13.sp,
                color = Color.White.copy(0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}
fun getFileMeta(context: Context, uri: Uri): Pair<String, String> {
    val contentResolver = context.contentResolver

    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

    var name = "proof_file"
    val cursor = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex != -1) {
            name = it.getString(nameIndex)
        }
    }
    return Pair(name, mimeType)
}
