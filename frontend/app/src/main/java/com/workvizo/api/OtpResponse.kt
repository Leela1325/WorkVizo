package com.workvizo.api

data class OtpResponse(
    val status: String?,
    val message: String?,
    val otp: String? = null
)
