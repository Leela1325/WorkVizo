<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php";

/* ---------------------------
   READ INPUT
--------------------------- */
$room_code     = $conn->real_escape_string($_POST['room_code'] ?? '');
$user_id       = $conn->real_escape_string($_POST['user_id'] ?? '');
$room_password = $_POST['room_password'] ?? '';

if (empty($room_code) || empty($user_id)) {
    echo json_encode([
        "status" => "error",
        "message" => "room_code and user_id are required"
    ]);
    exit;
}

/* ---------------------------
   1) FIND ROOM
--------------------------- */
$roomQ = $conn->query("SELECT * FROM rooms WHERE room_code='$room_code' LIMIT 1");
if ($roomQ->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid room code"
    ]);
    exit;
}

$room = $roomQ->fetch_assoc();
$room_id = $room['id'];

/* ---------------------------
   2) VERIFY PASSWORD
--------------------------- */
if (!empty($room['room_password'])) {
    if (empty($room_password) || !password_verify($room_password, $room['room_password'])) {
        echo json_encode([
            "status" => "error",
            "message" => "Incorrect room password"
        ]);
        exit;
    }
}

/* ---------------------------
   3) FETCH USER
--------------------------- */
$userQ = $conn->query("SELECT email, name FROM users WHERE id='$user_id' LIMIT 1");
if ($userQ->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "User not found"
    ]);
    exit;
}

$user = $userQ->fetch_assoc();
$user_email = $user['email'];
$user_name  = $user['name'];

/* ---------------------------
   4) CHECK ASSIGNED TASKS (🔥 MAIN FIX)
--------------------------- */
$taskQ = $conn->query("
    SELECT COUNT(*) AS cnt
    FROM room_tasks
    WHERE room_id='$room_id'
    AND assigned_email='$user_email'
");

$assignedTaskCount = (int)($taskQ->fetch_assoc()['cnt'] ?? 0);

if ($assignedTaskCount === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "You are not assigned to any task in this room. Unable to join."
    ]);
    exit;
}

/* ---------------------------
   5) CHECK ALREADY JOINED
--------------------------- */
$check = $conn->query("
    SELECT id FROM joined_rooms
    WHERE room_id='$room_id'
    AND user_id='$user_id'
    LIMIT 1
");

if ($check->num_rows > 0) {
    echo json_encode([
        "status" => "success",
        "message" => "Already joined",
        "room_id" => $room_id,
        "assigned_tasks" => $assignedTaskCount
    ]);
    exit;
}

/* ---------------------------
   6) INSERT JOIN
--------------------------- */
$join = $conn->query("
    INSERT INTO joined_rooms (room_id, user_id)
    VALUES ('$room_id', '$user_id')
");

if (!$join) {
    echo json_encode([
        "status" => "error",
        "message" => "Database error while joining room"
    ]);
    exit;
}

/* ---------------------------
   7) LOG ACTIVITY
--------------------------- */
logActivity(
    $conn,
    $room_id,
    $user_id,
    "member_joined",
    "$user_name joined the room"
);

/* ---------------------------
   8) SUCCESS
--------------------------- */
echo json_encode([
    "status" => "success",
    "message" => "Joined room successfully",
    "room_id" => $room_id,
    "assigned_tasks" => $assignedTaskCount
]);
exit;
