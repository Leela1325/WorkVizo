<?php
header("Content-Type: application/json");
require_once "../../config.php";

$room_id = $_POST['room_id'] ?? '';
$sender  = $_POST['sender'] ?? '';
$message = $_POST['message'] ?? '';

if (!$room_id || !$sender || !$message) {
    echo json_encode(["error" => "Missing fields"]);
    exit;
}

$stmt = $conn->prepare("INSERT INTO room_chat_messages (room_id, sender, message) VALUES (?, ?, ?)");
$stmt->bind_param("iss", $room_id, $sender, $message);
$stmt->execute();

echo json_encode(["ok" => true, "id" => $stmt->insert_id]);
?>
