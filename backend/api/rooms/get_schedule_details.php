<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_GET['room_id'] ?? '';

if (empty($room_id)) {
    echo json_encode([
        "status" => "error",
        "message" => "room_id is required"
    ]);
    exit;
}

/* ---------- ROOM SCHEDULE ---------- */
$roomQ = $conn->query("
    SELECT start_date, end_date, name 
    FROM rooms 
    WHERE id = '$room_id'
    LIMIT 1
");

if (!$roomQ || $roomQ->num_rows == 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Room not found"
    ]);
    exit;
}

$room = $roomQ->fetch_assoc();

/* ---------- TASKS ---------- */
/* ---------- TASKS (STATUS FROM PROOFS) ---------- */
$tasks = [];

$taskQ = $conn->query("
    SELECT 
        t.task_no,
        t.task_name,
        t.start_date,
        t.end_date,
        t.assigned_email,

        CASE
            WHEN p.status = 'completed' THEN 'completed'
            WHEN p.status = 'in_progress' THEN 'in_progress'
            ELSE 'pending'
        END AS status

    FROM room_tasks t
    LEFT JOIN proofs p 
        ON p.task_id = t.id   -- proofs.task_id → room_tasks.id
    WHERE t.room_id = '$room_id'
    ORDER BY t.task_no ASC
");

while ($row = $taskQ->fetch_assoc()) {
    $tasks[] = $row;
}

/* ---------- RESPONSE ---------- */
echo json_encode([
    "status" => "success",
    "project" => [
        "name" => $room['name'],
        "start_date" => $room['start_date'],
        "end_date" => $room['end_date']
    ],
    "tasks" => $tasks
]);
