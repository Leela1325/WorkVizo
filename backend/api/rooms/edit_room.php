<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_POST['room_id'] ?? '';
$requested_by = $_POST['requested_by'] ?? ''; // only room creator allowed

if (empty($room_id) || empty($requested_by)) {
    echo json_encode(["status" => "error", "message" => "room_id and requested_by are required"]);
    exit;
}

// Check room exists
$roomCheck = $conn->query("SELECT created_by FROM rooms WHERE id='$room_id'");
if ($roomCheck->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Room not found"]);
    exit;
}

$room = $roomCheck->fetch_assoc();

// Only creator can edit room
if ($room['created_by'] != $requested_by) {
    echo json_encode(["status" => "error", "message" => "Not authorized"]);
    exit;
}

// COLLECT UPDATABLE FIELDS
$updates = [];

if (!empty($_POST['name'])) {
    $updates[] = "name = '" . $conn->real_escape_string($_POST['name']) . "'";
}

if (!empty($_POST['description'])) {
    $updates[] = "description = '" . $conn->real_escape_string($_POST['description']) . "'";
}

if (!empty($_POST['start_date'])) {
    $updates[] = "start_date = '" . $_POST['start_date'] . "'";
}

if (!empty($_POST['end_date'])) {
    $updates[] = "end_date = '" . $_POST['end_date'] . "'";
}

if (!empty($_POST['schedule_type'])) {
    $updates[] = "schedule_type = '" . $_POST['schedule_type'] . "'";
}

if (!empty($_POST['room_type'])) {
    $updates[] = "room_type = '" . $_POST['room_type'] . "'";
}

if (!empty($_POST['number_of_people'])) {
    $updates[] = "number_of_people = '" . $_POST['number_of_people'] . "'";
}

if (count($updates) == 0) {
    echo json_encode(["status" => "error", "message" => "No fields to update"]);
    exit;
}

$updateSQL = implode(", ", $updates);

// Execute UPDATE
$sql = "UPDATE rooms SET $updateSQL WHERE id='$room_id'";

if ($conn->query($sql)) {
    echo json_encode(["status" => "success", "message" => "Room updated successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Database error"]);
}
?>
