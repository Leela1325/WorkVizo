package com.simats.workvizo.api
import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
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

    @SerializedName("task_progress")
    val taskProgress: Float,

    @SerializedName("time_progress")
    val timeProgress: Float,

    @SerializedName("completed_tasks")
    val completedTasks: Int,

    @SerializedName("total_tasks")
    val totalTasks: Int,

    @SerializedName("progress_status")
    val progressStatus: String
)


data class NotificationItem(
    val message: String,
    val created_at: String
)

data class NotificationsResponse(
    val status: String,
    val notifications: List<NotificationItem>
)


fun buildTasksJson(tasks: List<MutableAiTask>): String {
    return Gson().toJson(
        tasks.mapIndexed { index, task ->
            mapOf(
                "task_no" to index + 1,
                "task_name" to task.task_name,
                "start_date" to task.start_date,
                "end_date" to task.end_date,
                "assigned_email" to task.assigned_email
            )
        }
    )
}
