package com.workvizo.api

data class GenericResponse(
    val status: String,
    val message: String?,
    val room_id: String? = null,
    val room_code: String? = null
)
