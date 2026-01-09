<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_POST['room_id'] ?? '';
$new_password = $_POST['new_password'] ?? '';
$requested_by = $_POST['requested_by'] ?? ''; // user who wants to change room password

if (empty($room_id) || empty($requested_by)) {
    echo json_encode([
        "status" => "error",
        "message" => "room_id and requested_by are required"
    ]);
    exit;
}

// Check if room exists
$roomCheck = $conn->query("SELECT created_by FROM rooms WHERE id='$room_id'");
if ($roomCheck->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Room not found"]);
    exit;
}

$room = $roomCheck->fetch_assoc();

// Only creator can update room password
if ($room['created_by'] != $requested_by) {
    echo json_encode(["status" => "error", "message" => "Not authorized"]);
    exit;
}

$hashedPassword = !empty($new_password) 
    ? password_hash($new_password, PASSWORD_DEFAULT) 
    : null;

// Update room password
$update = $conn->query("
UPDATE rooms 
SET room_password = " . ($hashedPassword ? "'$hashedPassword'" : "NULL") . "
WHERE id = '$room_id'
");

if ($update) {
    echo json_encode([
        "status" => "success",
        "message" => "Room password updated successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Database error"
    ]);
}
?>
