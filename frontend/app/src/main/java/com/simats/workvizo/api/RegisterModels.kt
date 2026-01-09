package com.simats.workvizo.api

data class RegisterResponse(
    val status: String,
    val message: String,
    val user: User?
)

data class User(
    val id: String,
    val name: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val dob: String,
    val password: String,
    val confirm_password: String
)
