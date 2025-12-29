<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- INPUT ---------- */
$room_id = $_POST['room_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';   // member being removed

if (empty($room_id) || empty($user_id)) {
    echo json_encode([
        "status" => "error",
        "message" => "room_id and user_id are required"
    ]);
    exit;
}

/* ---------- CHECK ROOM ---------- */
$roomQ = $conn->query("
    SELECT id, created_by 
    FROM rooms 
    WHERE id = '$room_id'
    LIMIT 1
");

if ($roomQ->num_rows == 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Room not found"
    ]);
    exit;
}

$room = $roomQ->fetch_assoc();
$creator_id = $room['created_by'];

/* ---------- FETCH CREATOR NAME ---------- */
$creatorQ = $conn->query("
    SELECT name 
    FROM users 
    WHERE id = '$creator_id'
    LIMIT 1
");

$creatorRow  = $creatorQ->fetch_assoc();
$creatorName = $creatorRow['name'] ?? 'Creator';

/* ---------- FETCH REMOVED USER NAME ---------- */
$userQ = $conn->query("
    SELECT name 
    FROM users 
    WHERE id = '$user_id'
    LIMIT 1
");

$userRow  = $userQ->fetch_assoc();
$removedUserName = $userRow['name'] ?? 'Member';

/* ---------- DELETE MEMBER ---------- */
$stmt = $conn->prepare("
    DELETE FROM joined_rooms 
    WHERE room_id = ? AND user_id = ?
");
$stmt->bind_param("ii", $room_id, $user_id);

if ($stmt->execute()) {

    /* ---------- INSERT NOTIFICATION ---------- */
    $message = $conn->real_escape_string(
        "$creatorName removed $removedUserName from the room"
    );

    $conn->query("
        INSERT INTO notifications (room_id, user_id, message, created_at)
        VALUES ('$room_id', '$creator_id', '$message', NOW())
    ");

    echo json_encode([
        "status"  => "success",
        "message" => "Member removed successfully"
    ]);

} else {

    echo json_encode([
        "status"  => "error",
        "message" => "Failed to remove member"
    ]);
}
?>
