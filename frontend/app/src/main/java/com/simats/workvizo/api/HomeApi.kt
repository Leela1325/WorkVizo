package com.simats.workvizo.api

import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApi {

    @GET("rooms/get_active_rooms.php")
    suspend fun getActiveRooms(
        @Query("user_id") userId: String
    ): ActiveRoomsResponse

    @GET("rooms/get_completed_rooms.php")
    suspend fun getCompletedRooms(
        @Query("user_id") userId: String
    ): CompletedRoomsResponse

    @GET("rooms/get_room_progress.php")
    suspend fun getRoomProgress(
        @Query("room_id") roomId: String
    ): RoomProgressResponse
}
