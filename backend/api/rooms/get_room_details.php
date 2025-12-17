<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_code = $_GET['room_code'] ?? '';

if (empty($room_code)) {
    echo json_encode(["status" => "error", "message" => "room_code is required"]);
    exit;
}

// STEP 1 — Fetch room details
$roomQuery = $conn->query("
SELECT r.*, u.name AS created_by_name 
FROM rooms r
JOIN users u ON r.created_by = u.id
WHERE r.room_code = '$room_code'
");

if ($roomQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Invalid room code"]);
    exit;
}

$room = $roomQuery->fetch_assoc();
$room_id = $room['id'];

// STEP 2 — Count members
$memberQuery = $conn->query("
SELECT COUNT(*) AS total_members
FROM joined_rooms
WHERE room_id = '$room_id'
");
$total_members = $memberQuery->fetch_assoc()['total_members'];

// STEP 3 — Count tasks
$taskQuery = $conn->query("
SELECT 
    COUNT(*) AS total_tasks,
    SUM(CASE WHEN status='completed' THEN 1 ELSE 0 END) AS completed_tasks
FROM tasks
WHERE room_id = '$room_id'
");

$taskData = $taskQuery->fetch_assoc();

// STEP 4 — Build response
$response = [
    "status" => "success",
    "room" => [
        "id" => $room['id'],
        "room_code" => $room['room_code'],
        "name" => $room['name'],
        "description" => $room['description'],
        "start_date" => $room['start_date'],
        "end_date" => $room['end_date'],
        "schedule_type" => $room['schedule_type'],
        "room_type" => $room['room_type'],
        "number_of_people" => $room['number_of_people'],
        "created_by" => $room['created_by'],
        "created_by_name" => $room['created_by_name'],
    ],
    "stats" => [
        "total_members" => $total_members,
        "total_tasks" => $taskData['total_tasks'],
        "completed_tasks" => $taskData['completed_tasks']
    ]
];

echo json_encode($response);
?>
