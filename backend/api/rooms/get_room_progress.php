<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_GET['room_id'] ?? '';

if (empty($room_id)) {
    echo json_encode(["status" => "error", "message" => "room_id is required"]);
    exit;
}

// Check room
$roomQuery = $conn->query("SELECT * FROM rooms WHERE id='$room_id'");
if ($roomQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Room not found"]);
    exit;
}

$room = $roomQuery->fetch_assoc();
$start_date = $room['start_date'];
$end_date = $room['end_date'];

// Total tasks
$totalTasksQuery = $conn->query("SELECT COUNT(*) AS total FROM tasks WHERE room_id='$room_id'");
$totalTasks = $totalTasksQuery->fetch_assoc()['total'];

// Completed tasks
$completedTasksQuery = $conn->query("SELECT COUNT(*) AS done FROM tasks WHERE room_id='$room_id' AND status='completed'");
$completedTasks = $completedTasksQuery->fetch_assoc()['done'];

// Calculate task progress
$taskProgress = ($totalTasks > 0) ? round(($completedTasks / $totalTasks) * 100, 1) : 0;

// Time progress (based on deadline)
$today = strtotime(date("Y-m-d"));
$start = strtotime($start_date);
$end = strtotime($end_date);

$totalDays = max(1, floor(($end - $start) / 86400));
$daysPassed = max(0, min($totalDays, floor(($today - $start) / 86400)));

$timeProgress = round(($daysPassed / $totalDays) * 100, 1);

// Behind / On Track / Ahead
$status = "on_track";
if ($taskProgress + 10 < $timeProgress) {
    $status = "behind"; 
} else if ($taskProgress > $timeProgress + 10) {
    $status = "ahead";
}

echo json_encode([
    "status" => "success",
    "room_id" => $room_id,

    "task_progress" => $taskProgress,
    "time_progress" => $timeProgress,
    "completed_tasks" => $completedTasks,
    "total_tasks" => $totalTasks,

    "progress_status" => $status,
    "message" => "Room progress calculated successfully"
]);
?>
