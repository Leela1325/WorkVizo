<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$user_id = $_GET['user_id'] ?? '';

if (empty($user_id)) {
    echo json_encode(["status" => "error", "message" => "user_id is required"]);
    exit;
}

$query = $conn->query("
SELECT r.*
FROM joined_rooms j
JOIN rooms r ON j.room_id = r.id
WHERE j.user_id = '$user_id'
AND r.room_status = 'active'
ORDER BY r.start_date ASC
");

$rooms = [];

while ($row = $query->fetch_assoc()) {
    $rooms[] = $row;
}

echo json_encode([
    "status" => "success",
    "active_rooms" => $rooms
]);
?>
