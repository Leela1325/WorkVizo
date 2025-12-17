<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_code = $_GET['room_code'] ?? '';

if (empty($room_code)) {
    echo json_encode(["status" => "error", "message" => "room_code is required"]);
    exit;
}

// STEP 1 — Convert room_code → room_id
$roomQuery = $conn->query("SELECT id FROM rooms WHERE room_code = '$room_code'");

if ($roomQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Invalid room code"]);
    exit;
}

$roomData = $roomQuery->fetch_assoc();
$room_id = $roomData['id'];

// STEP 2 — Fetch all tasks under this room
$sql = "
SELECT t.id, t.title, t.description, t.due_date, t.assigned_to, t.created_by,
       t.status, t.created_at,
       u1.name AS assigned_user,
       u2.name AS created_user
FROM tasks t
LEFT JOIN users u1 ON t.assigned_to = u1.id
LEFT JOIN users u2 ON t.created_by = u2.id
WHERE t.room_id = '$room_id'
ORDER BY t.created_at DESC
";

$result = $conn->query($sql);

$tasks = [];

while ($row = $result->fetch_assoc()) {
    $tasks[] = $row;
}

echo json_encode([
    "status" => "success",
    "tasks" => $tasks
]);
?>
