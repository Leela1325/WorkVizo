package com.workvizo.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    // LOGIN API
    @FormUrlEncoded
    @POST("auth/login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>


    // REGISTER API
    @FormUrlEncoded
    @POST("auth/register.php")
    fun register(
        @Field("name") name: String,                 // MUST be "name"
        @Field("email") email: String,               // MUST be "email"
        @Field("dob") dob: String,                   // MUST be "dob"
        @Field("password") password: String,         // MUST be "password"
        @Field("createpassword") createPassword: String // MUST be "createpassword"
    ): Call<RegisterResponse>


    @FormUrlEncoded
    @POST("auth/send_otp.php")
    fun sendOtp(
        @Field("email") email: String
    ): Call<OtpResponse>

    @FormUrlEncoded
    @POST("auth/verify_otp.php")
    fun verifyOtp(
        @Field("email") email: String,
        @Field("otp") otp: String
    ): Call<OtpVerifyResponse>


    @FormUrlEncoded
    @POST("auth/reset_password.php")
    fun resetPassword(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<Any>



}
