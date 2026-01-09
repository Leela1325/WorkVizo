package com.simats.workvizo.api

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("auth/login.php")
    fun login(
        @Query("email") email: String,
        @Query("password") password: String
    ): Call<LoginResponse>

    @POST("auth/send_otp.php")
    fun sendOtp(
        @Query("email") email: String
    ): Call<OtpResponse>

    @POST("auth/verify_otp.php")
    fun verifyOtp(
        @Query("email") email: String,
        @Query("otp") otp: String
    ): Call<OtpVerifyResponse>
}
