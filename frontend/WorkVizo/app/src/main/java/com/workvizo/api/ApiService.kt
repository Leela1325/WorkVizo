package com.workvizo.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.*

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

// ---------------- ROOMS APIs ----------------



    @FormUrlEncoded
    @POST("rooms/get_room_progress.php")
    fun getRoomProgress(
        @Field("room_id") roomId: String
    ): Call<RoomProgressResponse>




    @GET("rooms/todays_changes.php")
    fun getTodayChanges(
        @Query("room_id") roomId: String
    ): Call<TodayChangesResponse>
    @FormUrlEncoded
    @POST("rooms/create_room.php")
    fun createRoom(
        @Field("name") name: String,
        @Field("description") description: String,
        @Field("start_date") startDate: String,
        @Field("end_date") endDate: String,
        @Field("schedule_type") scheduleType: String,
        @Field("room_type") roomType: String,
        @Field("number_of_people") numberOfPeople: Int,
        @Field("room_password") roomPassword: String,
        @Field("created_by") createdBy: String
    ): Call<CreateRoomResponse>

    @FormUrlEncoded
    @POST("rooms/create_room_tasks.php")
    fun createRoomTasks(
        @Field("room_id") roomId: Int,
        @Field("tasks_json") tasksJson: String
    ): Call<GenericResponse>

    @GET("rooms/get_room_details.php")
    fun getRoomDetails(
        @Query("room_code") roomCode: String
    ): Call<Map<String, Any>>
    @FormUrlEncoded
    @POST("rooms/verify_join_room.php")
    fun verifyJoinRoom(
        @Field("room_code") roomCode: String,
        @Field("room_password") roomPassword: String,
        @Field("user_id") userId: String
    ): Call<Map<String, Any>>
    @GET("users/get_user.php")
    fun getUserProfile(
        @Query("user_id") userId: String
    ): Call<UserProfileResponse>
    @FormUrlEncoded
    @POST("users/submit_feedback.php")
    fun submitFeedback(
        @Field("user_id") userId: String,
        @Field("feedback") feedback: String
    ): Call<GenericResponse>

    @FormUrlEncoded
    @POST("users/change_email.php")
    fun changeEmail(
        @Field("user_id") userId: String,
        @Field("old_email") oldEmail: String,
        @Field("password") password: String,
        @Field("new_email") newEmail: String
    ): Call<GenericResponse>
    @FormUrlEncoded
    @POST("users/change_password.php")
    fun changePassword(
        @Field("user_id") userId: String,
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String,
        @Field("confirm_password") confirmPassword: String
    ): Call<GenericResponse>
    @FormUrlEncoded
    @POST("users/change_dob.php")
    fun changeDob(
        @Field("user_id") userId: String,
        @Field("password") password: String,
        @Field("dob") dob: String
    ): Call<GenericResponse>
    @FormUrlEncoded
    @POST("users/delete_profile.php")
    fun deleteProfile(
        @Field("user_id") userId: String
    ): Call<GenericResponse>
    @GET("rooms/get_created_rooms.php")
    fun getCreatedRooms(@Query("user_id") userId: String): Call<RoomsResponse>

    @GET("rooms/get_joined_rooms.php")
    fun getJoinedRooms(@Query("user_id") userId: String): Call<RoomsResponse>

    @GET("rooms/get_active_rooms.php")
    fun getActiveRooms(@Query("user_id") userId: String): Call<RoomsResponse>

    @GET("rooms/get_completed_rooms.php")
    fun getCompletedRooms(@Query("user_id") userId: String): Call<RoomsResponse>

    @GET("rooms/get_recent_rooms.php")
    fun getRecentRooms(@Query("user_id") userId: String): Call<RoomsResponse>

    @FormUrlEncoded
    @POST("rooms/delete_room.php")
    fun deleteRoom(
        @Field("room_id") roomId: String,
        @Field("requested_by") requestedBy: String
    ): Call<GenericResponse>




}
