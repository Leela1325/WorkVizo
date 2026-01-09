package com.simats.workvizo.api

data class UpdateScheduleRequest(
    val room_id: String,
    val start_date: String,
    val end_date: String,
    val tasks: List<UpdateTaskRequest>
)

data class UpdateTaskRequest(
    val task_no: String,
    val task_name: String,
    val start_date: String,
    val end_date: String,
    val assigned_email: String
)
