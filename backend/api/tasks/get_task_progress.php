<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$task_id = $_GET['task_id'] ?? '';

if (empty($task_id)) {
    echo json_encode(["status" => "error", "message" => "task_id is required"]);
    exit;
}

// Fetch task
$taskQuery = $conn->query("SELECT * FROM tasks WHERE id='$task_id'");

if ($taskQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Task not found"]);
    exit;
}

$task = $taskQuery->fetch_assoc();
$status = $task['status'];

// Assign progress based on status
$progress = 0;

switch ($status) {
    case 'pending':
        $progress = 0;
        break;
    case 'in_progress':
        $progress = 50;
        break;
    case 'proof_submitted':
        $progress = 80;
        break;
    case 'completed':
        $progress = 100;
        break;
    case 'verified':
        $progress = 100;
        break;
    default:
        $progress = 0;
}

echo json_encode([
    "status" => "success",
    "task_id" => $task_id,
    "task_name" => $task['title'],
    "status" => $status,
    "task_progress" => $progress,
]);
?>
