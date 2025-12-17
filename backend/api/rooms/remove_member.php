<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php";

$room_id = $_POST['room_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';         // user to be removed
$requested_by = $_POST['requested_by'] ?? ''; // room creator/admin

if (empty($room_id) || empty($user_id) || empty($requested_by)) {
    echo json_encode([
        "status" => "error",
        "message" => "room_id, user_id, and requested_by are required"
    ]);
    exit;
}

// Escape inputs
$room_id = $conn->real_escape_string($room_id);
$user_id = $conn->real_escape_string($user_id);
$requested_by = $conn->real_escape_string($requested_by);

// Check if room exists
$roomCheck = $conn->query("SELECT created_by, name FROM rooms WHERE id='$room_id'");
if ($roomCheck->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Room does not exist"]);
    exit;
}

$roomData = $roomCheck->fetch_assoc();

// Permission: only room creator can remove members (extend later for admins)
if ($roomData['created_by'] != $requested_by) {
    echo json_encode(["status" => "error", "message" => "Not authorized"]);
    exit;
}

// Prevent creator removing themselves
if ($user_id === $requested_by) {
    echo json_encode(["status" => "error", "message" => "Creator cannot remove themselves"]);
    exit;
}

// Check if the user is actually in the room
$memberCheck = $conn->query(
    "SELECT jr.*, u.name as user_name 
     FROM joined_rooms jr
     JOIN users u ON jr.user_id = u.id
     WHERE jr.room_id='$room_id' AND jr.user_id='$user_id'"
);

if ($memberCheck->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "User is not in this room"]);
    exit;
}

$memberRow = $memberCheck->fetch_assoc();
$removed_user_name = $memberRow['user_name'] ?? 'User';

// Get requester name (for friendly log message)
$requesterQuery = $conn->query("SELECT name FROM users WHERE id='$requested_by' LIMIT 1");
$requesterName = ($requesterQuery && $requesterQuery->num_rows) ? $requesterQuery->fetch_assoc()['name'] : 'Admin';

// Remove the member
$delete = $conn->query(
    "DELETE FROM joined_rooms WHERE room_id='$room_id' AND user_id='$user_id'"
);

if ($delete) {
    // Log activity: who removed whom
    $description = "$removed_user_name was removed from room '{$roomData['name']}' by $requesterName";
    logActivity($conn, $room_id, $requested_by, "member_removed", $description);

    echo json_encode([
        "status" => "success",
        "message" => "Member removed successfully",
        "removed_user" => $removed_user_name
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to remove member"
    ]);
}
?>
