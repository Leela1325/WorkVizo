package com.simats.workvizo.api

data class TodayChange(
    val action_type: String,
    val description: String,
    val user_name: String,
    val time: String
)
