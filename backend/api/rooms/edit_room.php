<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- INPUT ---------- */
$room_id      = $_POST['room_id'] ?? '';
$requested_by = $_POST['requested_by'] ?? ''; // only creator allowed

if (empty($room_id) || empty($requested_by)) {
    echo json_encode([
        "status" => "error",
        "message" => "room_id and requested_by are required"
    ]);
    exit;
}

/* ---------- CHECK ROOM ---------- */
$roomCheck = $conn->query(
    "SELECT created_by FROM rooms WHERE id = '$room_id' LIMIT 1"
);

if ($roomCheck->num_rows == 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Room not found"
    ]);
    exit;
}

$room = $roomCheck->fetch_assoc();

/* ---------- AUTHORIZATION ---------- */
if ($room['created_by'] != $requested_by) {
    echo json_encode([
        "status" => "error",
        "message" => "Not authorized"
    ]);
    exit;
}

/* ---------- FETCH CREATOR NAME ---------- */
$userQ = $conn->query(
    "SELECT name FROM users WHERE id = '$requested_by' LIMIT 1"
);

$userRow = $userQ->fetch_assoc();
$creator_name = $userRow['name'] ?? 'Creator';

/* ---------- COLLECT UPDATES ---------- */
$updates = [];

if (!empty($_POST['name'])) {
    $updates[] = "name = '" . $conn->real_escape_string($_POST['name']) . "'";
}

if (!empty($_POST['description'])) {
    $updates[] = "description = '" . $conn->real_escape_string($_POST['description']) . "'";
}

if (!empty($_POST['start_date'])) {
    $updates[] = "start_date = '" . $conn->real_escape_string($_POST['start_date']) . "'";
}

if (!empty($_POST['end_date'])) {
    $updates[] = "end_date = '" . $conn->real_escape_string($_POST['end_date']) . "'";
}

if (!empty($_POST['schedule_type'])) {
    $updates[] = "schedule_type = '" . $conn->real_escape_string($_POST['schedule_type']) . "'";
}

if (!empty($_POST['room_type'])) {
    $updates[] = "room_type = '" . $conn->real_escape_string($_POST['room_type']) . "'";
}

if (!empty($_POST['number_of_people'])) {
    $updates[] = "number_of_people = '" . $conn->real_escape_string($_POST['number_of_people']) . "'";
}

if (count($updates) == 0) {
    echo json_encode([
        "status" => "error",
        "message" => "No fields to update"
    ]);
    exit;
}

/* ---------- UPDATE ROOM ---------- */
$updateSQL = implode(", ", $updates);
$sql = "UPDATE rooms SET $updateSQL WHERE id = '$room_id'";

/* ---------- EXECUTE ---------- */
if ($conn->query($sql)) {

    /* ---------- INSERT NOTIFICATION ---------- */
    $message = $conn->real_escape_string(
        "$creator_name updated the room details"
    );

    $conn->query("
        INSERT INTO notifications (room_id, user_id, message, created_at)
        VALUES ('$room_id', '$requested_by', '$message', NOW())
    ");

    echo json_encode([
        "status" => "success",
        "message" => "Room updated successfully"
    ]);

} else {

    echo json_encode([
        "status" => "error",
        "message" => "Database error"
    ]);
}
?>
