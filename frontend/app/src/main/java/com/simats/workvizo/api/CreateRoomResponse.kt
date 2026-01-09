package com.simats.workvizo.api

data class CreateRoomResponse(
    val status: String,
    val message: String?,
    val room_id: String?,
    val room_code: String?
)
