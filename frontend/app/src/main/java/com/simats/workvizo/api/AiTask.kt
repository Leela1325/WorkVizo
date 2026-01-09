package com.simats.workvizo.api
import com.google.gson.annotations.SerializedName
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


data class AiTask(
    @SerializedName("task_name")
    val task_name: String,

    @SerializedName("start_date")
    val start_date: String,

    @SerializedName("end_date")
    val end_date: String,

    @SerializedName("assigned_email")
    val assigned_email: String? = ""
)

class MutableAiTask(
    taskName: String,
    startDate: String,
    endDate: String,
    assignedEmail: String? = ""
) {
    var task_name by mutableStateOf(taskName)
    var start_date by mutableStateOf(startDate)
    var end_date by mutableStateOf(endDate)
    var assigned_email by mutableStateOf(assignedEmail ?: "")
}

