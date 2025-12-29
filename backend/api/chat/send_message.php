<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_POST['room_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';
$message = $_POST['message'] ?? '';

if (empty($room_id) || empty($user_id) || empty($message)) {
    echo json_encode(["status" => "error", "message" => "room_id, user_id, and message are required"]);
    exit;
}

// Check user belongs to room
$check = $conn->query("SELECT * FROM joined_rooms WHERE room_id='$room_id' AND user_id='$user_id'");
if ($check->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "User not part of this room"]);
    exit;
}

// Insert chat message
$q = $conn->query("
    INSERT INTO room_chat (room_id, user_id, message)
    VALUES ('$room_id', '$user_id', '$message')
");

if ($q) {
    echo json_encode([
        "status" => "success",
        "message" => "Message sent"
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to send message"]);
}
?>
