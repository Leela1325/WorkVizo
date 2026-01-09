package com.simats.workvizo.api

data class GenericResponse(
    val status: String,
    val message: String?,
    val room_id: String? = null,
    val room_code: String? = null,
    val affected_rows: Int? = null
)
