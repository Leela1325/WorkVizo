<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php";


$task_id = $_POST['task_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';  // optional: check permissions later

if (empty($task_id)) {
    echo json_encode(["status" => "error", "message" => "task_id is required"]);
    exit;
}

// Check task exists
$taskQuery = $conn->query("SELECT * FROM tasks WHERE id='$task_id'");
if ($taskQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Task does not exist"]);
    exit;
}

// Delete task
$sql = "DELETE FROM tasks WHERE id='$task_id'";

if ($conn->query($sql)) {
    echo json_encode(["status" => "success", "message" => "Task deleted successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Database error"]);
}
?>
