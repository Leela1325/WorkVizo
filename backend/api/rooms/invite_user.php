<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_code = $_POST['room_code'] ?? '';
$email = $_POST['email'] ?? '';

if (empty($room_code) || empty($email)) {
    echo json_encode(["status" => "error", "message" => "room_code and email are required"]);
    exit;
}

// Convert room_code → room_id
$roomQuery = $conn->query("SELECT id FROM rooms WHERE room_code='$room_code'");
if ($roomQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Invalid room code"]);
    exit;
}

$room_id = $roomQuery->fetch_assoc()['id'];

// Check user exists
$userQuery = $conn->query("SELECT id FROM users WHERE email='$email'");
if ($userQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "User not registered"]);
    exit;
}

$user_id = $userQuery->fetch_assoc()['id'];

// Check if already joined
$checkJoined = $conn->query("SELECT * FROM joined_rooms WHERE user_id='$user_id' AND room_id='$room_id'");
if ($checkJoined->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "User already in room"]);
    exit;
}

// Add user to room
$sql = "
INSERT INTO joined_rooms (room_id, user_id)
VALUES ('$room_id', '$user_id')
";

if ($conn->query($sql)) {
    echo json_encode([
        "status" => "success",
        "message" => "User added to room successfully",
        "user_id" => $user_id
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Database error"]);
}
?>
