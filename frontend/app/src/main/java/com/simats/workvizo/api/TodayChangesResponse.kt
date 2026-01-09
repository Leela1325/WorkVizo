package com.simats.workvizo.api

data class TodayChangesResponse(
    val status: String,
    val room_id: String,
    val date: String,
    val total_changes: Int,
    val changes: List<TodayChange>
)
