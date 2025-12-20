package com.workvizo.api

data class Room(
    val id: String,
    val name: String,
    val description: String?,
    val start_date: String,
    val end_date: String,
    val room_status: String
)

data class ActiveRoomsResponse(
    val status: String,
    val active_rooms: List<Room>
)

data class CompletedRoomsResponse(
    val status: String,
    val completed_rooms: List<Room>
)

data class RoomProgressResponse(
    val task_progress: Float,
    val time_progress: Float,
    val completed_tasks: Int,
    val total_tasks: Int,
    val progress_status: String
)
