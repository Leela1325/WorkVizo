<?php
header("Content-Type: application/json");

require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php";  // IMPORTANT

$task_id = $_POST['task_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';
$status = $_POST['status'] ?? '';

// Allowed status values
$allowed_status = ["pending", "in_progress", "submitted", "completed"];

// Validation
if (empty($task_id) || empty($user_id) || empty($status)) {
    echo json_encode(["status" => "error", "message" => "task_id, user_id and status are required"]);
    exit;
}

if (!in_array($status, $allowed_status)) {
    echo json_encode(["status" => "error", "message" => "Invalid status value"]);
    exit;
}

// Check if task exists
$taskCheck = $conn->query("SELECT * FROM tasks WHERE id='$task_id'");
if ($taskCheck->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Task not found"]);
    exit;
}

$taskData = $taskCheck->fetch_assoc();
$room_id = $taskData["room_id"];
$task_title = $taskData["title"];

// Check if user is part of this room
$checkJoin = $conn->query("SELECT * FROM joined_rooms WHERE room_id='$room_id' AND user_id='$user_id'");
if ($checkJoin->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "User not part of this room"]);
    exit;
}

// Get user name for logs
$userResult = $conn->query("SELECT name FROM users WHERE id='$user_id'");
$user_name = ($userResult->num_rows > 0) ? $userResult->fetch_assoc()["name"] : "Unknown User";

// Update task status
$update = $conn->query("UPDATE tasks SET status='$status' WHERE id='$task_id'");

if ($update) {

    // ---------- INSERT ACTIVITY LOG ----------
    logActivity(
        $conn,
        $room_id,
        $user_id,
        "task_updated",
        "$user_name updated task '$task_title' to '$status'"
    );
    // ------------------------------------------

    echo json_encode([
        "status" => "success",
        "message" => "Task status updated",
        "task_id" => $task_id,
        "new_status" => $status
    ]);

} else {
    echo json_encode(["status" => "error", "message" => "Failed to update status"]);
}
?>
