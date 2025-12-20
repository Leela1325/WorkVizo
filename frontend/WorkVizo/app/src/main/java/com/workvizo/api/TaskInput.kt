package com.workvizo.api

data class TaskInput(
    val taskNo: Int,
    var taskName: String = "",
    var startDate: String = "",
    var endDate: String = ""
)