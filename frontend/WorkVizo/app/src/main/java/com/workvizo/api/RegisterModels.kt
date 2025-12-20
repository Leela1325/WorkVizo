package com.workvizo.api

data class RegisterResponse(
    val status: String,
    val message: String,
    val user: User?
)

data class User(
    val id: String,
    val name: String
)

