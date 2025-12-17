<?php
header("Content-Type: application/json");

require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php"; // For activity tracking

// Collect input
$name = $_POST['name'] ?? '';
$description = $_POST['description'] ?? '';
$start_date = $_POST['start_date'] ?? '';
$end_date = $_POST['end_date'] ?? '';
$schedule_type = $_POST['schedule_type'] ?? '';
$room_type = $_POST['room_type'] ?? 'manual';
$number_of_people = $_POST['number_of_people'] ?? 1;
$room_password = $_POST['room_password'] ?? null;
$created_by = $_POST['created_by'] ?? '';

// Validate required fields
if (
    empty($name) || empty($description) || empty($start_date) ||
    empty($end_date) || empty($schedule_type) || empty($created_by)
) {
    echo json_encode([
        "status" => "error",
        "message" => "All required fields must be filled"
    ]);
    exit;
}

// Hash password if provided
if (!empty($room_password)) {
    $room_password = password_hash($room_password, PASSWORD_DEFAULT);
}

// Generate 6-digit room code
function generateRoomCode() {
    return str_pad(rand(0, 999999), 6, '0', STR_PAD_LEFT);
}

$room_code = generateRoomCode();

// Insert room
$query = "
INSERT INTO rooms 
(room_code, name, description, start_date, end_date, schedule_type, room_type, number_of_people, room_password, created_by)
VALUES 
('$room_code', '$name', '$description', '$start_date', '$end_date', '$schedule_type', '$room_type', '$number_of_people', '$room_password', '$created_by')
";

if ($conn->query($query)) {

    $room_id = $conn->insert_id;

    // Add creator to joined_rooms with role = creator
    $conn->query("
        INSERT INTO joined_rooms (room_id, user_id, role)
        VALUES ('$room_id', '$created_by', 'creator')
    ");

    // Log activity
    logActivity($conn, $room_id, $created_by, "room_created", "Room '$name' created");

    echo json_encode([
        "status" => "success",
        "room_id" => $room_id,
        "room_code" => $room_code,
        "message" => "Room created successfully"
    ]);

} else {
    echo json_encode([
        "status" => "error",
        "message" => "Database error: " . $conn->error
    ]);
}
?>
