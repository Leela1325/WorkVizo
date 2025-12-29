<?php
header("Content-Type: application/json");

require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php";

$room_id = $_POST['room_id'] ?? '';
$target_user = $_POST['target_user'] ?? '';   // user whose role will change
$new_role = $_POST['new_role'] ?? '';
$requested_by = $_POST['requested_by'] ?? ''; // creator or admin

// Validate
if (empty($room_id) || empty($target_user) || empty($new_role) || empty($requested_by)) {
    echo json_encode(["status" => "error", "message" => "All fields are required"]);
    exit;
}

if (!in_array($new_role, ['admin', 'member'])) {
    echo json_encode(["status" => "error", "message" => "Invalid role"]);
    exit;
}

// GET requester's role
$roleCheck = $conn->query("
    SELECT role FROM joined_rooms 
    WHERE room_id='$room_id' AND user_id='$requested_by'
");

if ($roleCheck->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Requester not in room"]);
    exit;
}

$requester_role = $roleCheck->fetch_assoc()['role'];

// Only creator & admin can update roles
if ($requester_role != 'creator' && $requester_role != 'admin') {
    echo json_encode(["status" => "error", "message" => "Not authorized"]);
    exit;
}

// Prevent downgrading creator
$targetCheck = $conn->query("
    SELECT role, user_id FROM joined_rooms
    WHERE room_id='$room_id' AND user_id='$target_user'
");

if ($targetCheck->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Target user not in room"]);
    exit;
}

$targetData = $targetCheck->fetch_assoc();
$target_role = $targetData['role'];

if ($target_role == 'creator') {
    echo json_encode(["status" => "error", "message" => "Cannot change creator role"]);
    exit;
}

// UPDATE ROLE
$update = $conn->query("
    UPDATE joined_rooms 
    SET role='$new_role'
    WHERE room_id='$room_id' AND user_id='$target_user'
");

if ($update) {

    // Log activity
    $logMsg = "Changed role of user $target_user to $new_role";
    logActivity($conn, $room_id, $requested_by, "role_updated", $logMsg);

    echo json_encode([
        "status" => "success",
        "message" => "Role updated successfully"
    ]);

} else {
    echo json_encode([
        "status" => "error",
        "message" => "Database error: " . $conn->error
    ]);
}
?>
