<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_POST['room_id'] ?? '';
$requested_by = $_POST['requested_by'] ?? '';

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

// Only creator can delete room
if ($room['created_by'] != $requested_by) {
    echo json_encode(["status" => "error", "message" => "Not authorized"]);
    exit;
}

// Delete room (cascade removes tasks, proofs, members)
$delete = $conn->query("DELETE FROM rooms WHERE id='$room_id'");

if ($delete) {
    echo json_encode([
        "status" => "success",
        "message" => "Room deleted successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Database error"
    ]);
}
?>
