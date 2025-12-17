<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_GET['room_id'] ?? '';

if (empty($room_id)) {
    echo json_encode(["status" => "error", "message" => "room_id is required"]);
    exit;
}

$query = $conn->query("
SELECT c.id, c.message, c.created_at,
       u.id as user_id, u.name as user_name
FROM room_chat c
JOIN users u ON c.user_id = u.id
WHERE c.room_id = '$room_id'
ORDER BY c.created_at ASC
");

$messages = [];

while ($row = $query->fetch_assoc()) {
    $messages[] = $row;
}

echo json_encode([
    "status" => "success",
    "total_messages" => count($messages),
    "messages" => $messages
]);
?>
