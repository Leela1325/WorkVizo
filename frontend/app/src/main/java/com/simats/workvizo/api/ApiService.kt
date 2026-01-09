package com.simats.workvizo.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.simats.workvizo.api.*
import androidx.compose.ui.graphics.vector.ImageVector




interface ApiService {

    // LOGIN API
//    @FormUrlEncoded
//    @POST("auth/login.php")
//    fun login(
//        @Field("email") email: String,
//        @Field("password") password: String
//    ): Call<LoginResponse>
    @FormUrlEncoded
    @POST("auth/login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("login_type") loginType: String = "password"
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
//    @FormUrlEncoded
//    @POST("rooms/verify_join_room.php")
//    fun verifyJoinRoom(
//        @Field("room_code") roomCode: String,
//        @Field("room_password") roomPassword: String,
//        @Field("user_id") userId: String
//    ): Call<Map<String, Any>>

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

    @GET("rooms/get_schedule_details.php")
    fun getScheduleDetails(
        @Query("room_id") roomId: String
    ): Call<Map<String, Any>>

    @POST("rooms/update_schedule.php")
    fun updateSchedule(
        @Body request: UpdateScheduleRequest
    ): Call<GenericResponse>
    @GET("rooms/get_room_tasks.php")
    fun getRoomTasks(
        @Query("room_id") roomId: String,
        @Query("user_id") userId: String,
        @Query("is_creator") isCreator: String
    ): Call<RoomTasksResponse>








    @FormUrlEncoded
    @POST("tasks/creator_update_status.php")
    fun creatorUpdateStatus(
        @Field("task_id") taskId: String,
        @Field("status") status: String,
        @Field("comment") comment: String
    ): Call<SimpleResponse>



    @FormUrlEncoded
    @POST("tasks/update_status.php")
    fun updateStatus(
        @Field("task_id") taskId: String,
        @Field("status") status: String,
        @Field("user_id") userId: String? = null,
        @Field("comment") comment: String? = null
    ): Call<GenericResponse>
    @FormUrlEncoded
    @POST("tasks/add_comment.php")
    fun addComment(
        @Field("task_id") taskId: String,
        @Field("user_id") userId: String,
        @Field("comment") comment: String
    ): Call<GenericResponse>


    @Multipart
    @POST("tasks/update_proof.php")
    fun updateProof(
        @Part("task_id") taskId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("description") description: RequestBody,
        @Part proof: MultipartBody.Part?     // ✅ NULLABLE
    ): Call<GenericResponse>


    @GET("tasks/get_task_comment.php")
    fun getTaskComment(
        @Query("task_id") taskId: String,
        @Query("user_id") userId: String
    ): Call<TaskCommentResponse>


    @FormUrlEncoded
    @POST("tasks/get_latest_proof.php")
    fun getLatestProof(
        @Field("task_id") taskId: String
    ): Call<LatestProofResponse>

    @FormUrlEncoded
    @POST("rooms/leave_room.php")
    fun leaveRoom(
        @Field("room_id") roomId: String,
        @Field("user_id") userId: String
    ): Call<GenericResponse>

    @FormUrlEncoded
    @POST("rooms/report_room.php")
    fun reportRoom(
        @Field("room_id") roomId: String,
        @Field("user_id") userId: String,
        @Field("issue") issue: String
    ): Call<GenericResponse>


    @FormUrlEncoded
    @POST("rooms/edit_room.php")
    fun editRoom(
        @Field("room_id") roomId: String,
        @Field("requested_by") requestedBy: String,
        @Field("name") name: String?,
        @Field("description") description: String?,
        @Field("start_date") startDate: String?,
        @Field("end_date") endDate: String?,
        @Field("number_of_people") numberOfPeople: String?
    ): Call<GenericResponse>



    @FormUrlEncoded
    @POST("rooms/remove_member.php")
    fun removeMember(
        @Field("room_id") roomId: String,
        @Field("user_id") userId: String
    ): Call<GenericResponse>

    @GET("rooms/get_room_members.php")
    fun getRoomMembers(
        @Query("room_id") roomId: String
    ): Call<Map<String, Any>>

    @FormUrlEncoded
    @POST("tasks/submit_proof_without_file.php")
    fun submitProofWithoutFile(
        @Field("task_id") taskId: String,
        @Field("user_id") userId: String,
        @Field("status") status: String,
        @Field("description") description: String
    ): Call<SimpleResponse>



    @GET("proofs/check_user_proof.php")
    fun checkUserProof(
        @Query("task_id") taskId: String,
        @Query("user_id") userId: String
    ): Call<Map<String, Any>>

    @Multipart
    @POST("tasks/submit_proof.php")
    fun submitProof(
        @Part("task_id") taskId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("status") status: RequestBody,        // 🔥 THIS WAS MISSING
        @Part("description") description: RequestBody,
        @Part proof: MultipartBody.Part
    ): Call<SimpleResponse>

    @GET("rooms/get_all_room_members.php")
    fun getAllRoomMembers(
        @Query("room_id") roomId: String
    ): Call<MembersResponse>



    @FormUrlEncoded
    @POST("rooms/add_room_task.php")
    fun addTask(
        @Field("room_id") roomId: String,
        @Field("task_name") taskName: String,
        @Field("start_date") startDate: String,
        @Field("end_date") endDate: String,
        @Field("assigned_email") assignedEmail: String,
        @Field("user_id") userId: String   // 🔥 ADD THIS
    ): Call<GenericResponse>


    @FormUrlEncoded
    @POST("tasks/delete_task.php")
    fun deleteTask(
        @Field("task_id") taskId: Int,
        @Field("user_id") userId: String
    ): Call<GenericResponse>


    @GET("rooms/get_room_progress.php")
    fun getRoomProgress(
        @Query("room_id") roomId: String
    ): Call<RoomProgressResponse>



    @GET("rooms/get_notifications.php")
    fun getNotifications(
        @Query("user_id") userId: String
    ): Call<NotificationsResponse>





    @POST("ai/room_ai_chat.php")
    fun roomAiChat(
        @Body body: Map<String, String>
    ): Call<Map<String, String>>

    @FormUrlEncoded
    @POST("ai/init_room_ai.php")
    fun initRoomAi(
        @Field("room_id") roomId: String,
        @Field("user_id") userId: String
    ): Call<Map<String, String>>



    @FormUrlEncoded
    @POST("rooms/verify_join_room.php")
    fun verifyJoinRoom(
        @Field("room_code") roomCode: String,
        @Field("room_password") roomPassword: String,
        @Field("user_id") userId: String
    ): Call<GenericResponse>


    @FormUrlEncoded
    @POST("rooms/join_room.php")
    fun joinRoom(
        @Field("room_code") roomCode: String,
        @Field("user_id") userId: String,
        @Field("room_password") roomPassword: String
    ): Call<GenericResponse>







    @POST("ai/ai_generate_schedule.php")
    fun generateAiSchedule(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<AiScheduleResponse>

    @FormUrlEncoded
    @POST("rooms/save_ai_tasks.php")
    fun saveAiTasks(
        @Field("room_id") roomId: String,
        @Field("tasks") tasksJson: String
    ): Call<GenericResponse>


    @GET("rooms/get_room_id.php")
    fun getRoomId(
        @Query("room_code") roomCode: String
    ): Call<RoomIdResponse>


    @FormUrlEncoded
    @POST("chat/send_room_message.php")
    fun sendRoomMessage(
        @Field("room_id") roomId: Int,
        @Field("user_id") userId: Int,
        @Field("user_name") userName: String,
        @Field("message") message: String
    ): Call<Map<String, Any>>

    @GET("chat/get_room_messages.php")
    fun getRoomMessages(
        @Query("room_id") roomId: Int
    ): Call<Map<String, Any>>

    @GET("chat/ping.php")
    fun chatPing(): Call<String>



}



