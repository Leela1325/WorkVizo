<?php
header("Content-Type: application/json");

require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php";

// Collect input
$task_id = $_POST['task_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';
$comment = $_POST['comment'] ?? '';

// Validate required fields
if (empty($task_id) || empty($user_id) || empty($comment)) {
    echo json_encode([
        "status" => "error",
        "message" => "task_id, user_id and comment are required"
    ]);
    exit;
}

// Escape comment for safety
$comment_escaped = $conn->real_escape_string($comment);

// STEP 1: Check task exists
$taskCheck = $conn->query("
    SELECT id, room_id, title 
    FROM tasks 
    WHERE id='$task_id'
");

if ($taskCheck->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Task not found"]);
    exit;
}

$task = $taskCheck->fetch_assoc();
$room_id = $task["room_id"];
$task_title = $task["title"];

// STEP 2: Check user belongs to the room
$userCheck = $conn->query("
    SELECT u.name 
    FROM joined_rooms jr
    JOIN users u ON jr.user_id = u.id
    WHERE jr.user_id='$user_id' AND jr.room_id='$room_id'
");

if ($userCheck->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "User not part of this room"]);
    exit;
}

$userData = $userCheck->fetch_assoc();
$user_name = $userData['name'];

// STEP 3: Insert the comment
$insert = $conn->query("
    INSERT INTO task_comments (task_id, user_id, comment) 
    VALUES ('$task_id', '$user_id', '$comment_escaped')
");

if ($insert) {

    // STEP 4: Log activity
    $logMessage = "$user_name commented on task '$task_title': \"$comment\"";
    logActivity($conn, $room_id, $user_id, "comment_added", $logMessage);

    echo json_encode([
        "status" => "success",
        "message" => "Comment added successfully"
    ]);

} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to add comment"
    ]);
}

?>
