<?php
ini_set('display_errors', 0);
error_reporting(0);

header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------------------------
   READ INPUT
--------------------------- */
$room_code = $_GET['room_code'] ?? '';

if (empty($room_code)) {
    echo json_encode([
        "status" => "error",
        "message" => "room_code is required"
    ]);
    exit;
}

/* ---------------------------
   ROOM DETAILS
--------------------------- */
$roomQuery = $conn->query("
    SELECT r.*, u.name AS created_by_name
    FROM rooms r
    JOIN users u ON r.created_by = u.id
    WHERE r.room_code = '$room_code'
    LIMIT 1
");

if (!$roomQuery || $roomQuery->num_rows == 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid room code"
    ]);
    exit;
}

$room = $roomQuery->fetch_assoc();
$room_id = $room['id'];

/* ---------------------------
   MEMBERS COUNT
--------------------------- */
$total_members = 0;

$memberResult = $conn->query("
    SELECT COUNT(*) AS total_members
    FROM joined_rooms
    WHERE room_id = '$room_id'
");

if ($memberResult) {
    $row = $memberResult->fetch_assoc();
    $total_members = (int)($row['total_members'] ?? 0);
}

/* ---------------------------
   TASKS COUNT (FIXED)
   ✔ Uses room_tasks table
--------------------------- */
$total_tasks = 0;
$completed_tasks = 0;

$taskResult = $conn->query("
    SELECT 
        COUNT(*) AS total_tasks,
        SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) AS completed_tasks
    FROM room_tasks
    WHERE room_id = '$room_id'
");

if ($taskResult) {
    $taskRow = $taskResult->fetch_assoc();
    $total_tasks = (int)($taskRow['total_tasks'] ?? 0);
    $completed_tasks = (int)($taskRow['completed_tasks'] ?? 0);
}

/* ---------------------------
   RESPONSE
--------------------------- */
echo json_encode([
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
        "created_at" => $room['created_at']
    ],
    "stats" => [
        "total_members" => $total_members,
        "total_tasks" => $total_tasks,
        "completed_tasks" => $completed_tasks
    ]
]);
