package com.workvizo.api

import com.google.gson.annotations.SerializedName

data class RoomsResponse(

    @SerializedName("created_rooms")
    val createdRooms: List<RoomItem>? = null,

    @SerializedName("rooms")
    val rooms: List<RoomItem>? = null,

    @SerializedName("active_rooms")
    val activeRooms: List<RoomItem>? = null,

    @SerializedName("completed_rooms")
    val completedRooms: List<RoomItem>? = null,

    @SerializedName("recent_rooms")
    val recentRooms: List<RoomItem>? = null
)

data class RoomItem(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val room_code: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val room_status: String? = null,
    val creator_name: String? = null
)
data class RoomDetails(
    val id: String,
    val room_code: String,
    val name: String,
    val description: String?,
    val start_date: String?,
    val end_date: String?,
    val schedule_type: String?,
    val room_type: String?,
    val number_of_people: String?,
    val created_by: String,
    val created_by_name: String,
    val created_at: String?
)
data class RoomStats(
    val total_members: Int,
    val total_tasks: Int,
    val completed_tasks: Int
)

data class RoomProgressStats(
    val status: String,
    val task_progress: Float,
    val time_progress: Float,
    val progress_status: String
)

data class RoomDetailsResponse(
    val status: String,
    val room: RoomDetails,
    val stats: RoomStats
)
