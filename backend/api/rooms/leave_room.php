<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- INPUT ---------- */
$user_id = $_POST['user_id'] ?? '';
$room_id = $_POST['room_id'] ?? '';

if (!$user_id || !$room_id) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing user_id or room_id"
    ]);
    exit;
}

/* ---------- CHECK IF USER IS IN ROOM ---------- */
$check = $conn->prepare("
    SELECT id FROM joined_rooms 
    WHERE room_id = ? AND user_id = ?
");
$check->bind_param("ii", $room_id, $user_id);
$check->execute();
$result = $check->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "User not found in room"
    ]);
    exit;
}

/* ---------- DELETE ---------- */
$delete = $conn->prepare("
    DELETE FROM joined_rooms 
    WHERE room_id = ? AND user_id = ?
");
$delete->bind_param("ii", $room_id, $user_id);

if ($delete->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "User left room successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to leave room"
    ]);
}
