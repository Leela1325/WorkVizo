package com.simats.workvizo.api

data class TaskInput(
    val taskNo: Int,
    var taskName: String = "",
    var startDate: String = "",
    var endDate: String = ""
)
data class TaskCommentResponse(
    val status: String,
    val task_name: String?,
    val comment: String?,
    val comment_time: String?
)


data class SimpleResponse(
    val status: String,
    val message: String? = null
)
