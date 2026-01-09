<?php
header("Content-Type: application/json");
require_once dirname(__DIR__, 2) . "/config.php";

$room_id = intval($_GET['room_id'] ?? 0);

if ($room_id <= 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Room ID missing"
    ]);
    exit;
}

$stmt = $conn->prepare("
    SELECT
        user_id,
        user_name,
        message,
        DATE_FORMAT(created_at, '%h:%i %p') AS time
    FROM room_chats
    WHERE room_id = ?
    ORDER BY id ASC
");

$stmt->bind_param("i", $room_id);
$stmt->execute();
$result = $stmt->get_result();

$messages = [];

while ($row = $result->fetch_assoc()) {
    $messages[] = [
        "user_id"   => (string)$row["user_id"],
        "user_name" => $row["user_name"],
        "message"   => $row["message"],
        "time"      => $row["time"]
    ];
}

echo json_encode([
    "status" => "success",
    "messages" => $messages
]);
