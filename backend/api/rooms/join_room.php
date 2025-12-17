<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php";

$room_code     = $conn->real_escape_string($_POST['room_code'] ?? '');
$user_id       = $conn->real_escape_string($_POST['user_id'] ?? '');
$room_password = $_POST['room_password'] ?? ''; // keep raw for password_verify

if (empty($room_code) || empty($user_id)) {
    echo json_encode(["status" => "error", "message" => "room_code and user_id are required"]);
    exit;
}

/* ---------------------------
   1) Find room by code
   --------------------------- */
$roomQuery = $conn->query("SELECT * FROM rooms WHERE room_code='$room_code' LIMIT 1");
if ($roomQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Invalid room code"]);
    exit;
}
$room = $roomQuery->fetch_assoc();
$room_id = $room['id'];

/* ---------------------------
   2) Enforce member limit (if set)
   --------------------------- */
$max_people = (int)$room['number_of_people'];
if ($max_people > 0) {
    $cntQ = $conn->query("SELECT COUNT(*) AS cnt FROM joined_rooms WHERE room_id='$room_id'");
    $cnt = $cntQ->fetch_assoc()['cnt'] ?? 0;
    if ($cnt >= $max_people) {
        echo json_encode(["status" => "error", "message" => "Room is full"]);
        exit;
    }
}

/* ---------------------------
   3) Check room password (if exists)
   --------------------------- */
// If room_password column contains hashed password, verify. If empty or NULL -> no password required
if (!empty($room['room_password'])) {
    if (empty($room_password) || !password_verify($room_password, $room['room_password'])) {
        echo json_encode(["status" => "error", "message" => "Incorrect room password"]);
        exit;
    }
}

/* ---------------------------
   4) Check already joined
   --------------------------- */
$check = $conn->query("SELECT id FROM joined_rooms WHERE room_id='$room_id' AND user_id='$user_id' LIMIT 1");
if ($check->num_rows > 0) {
    echo json_encode(["status" => "success", "message" => "Already joined", "room_id" => $room_id]);
    exit;
}

/* ---------------------------
   5) Insert into joined_rooms
   --------------------------- */
$join = $conn->query("INSERT INTO joined_rooms (room_id, user_id) VALUES ('$room_id', '$user_id')");

if ($join) {
    // Fetch user name for activity log
    $uQ = $conn->query("SELECT name FROM users WHERE id='$user_id' LIMIT 1");
    $user_name = ($uQ && $uQ->num_rows > 0) ? $uQ->fetch_assoc()['name'] : "User#$user_id";

    // Log activity
    logActivity(
        $conn,
        $room_id,
        $user_id,
        "member_joined",
        "$user_name joined the room"
    );

    echo json_encode([
        "status" => "success",
        "message" => "Joined room successfully",
        "room_id" => $room_id
    ]);
} else {
    // You can return detailed mysqli error in dev; keep generic in production
    echo json_encode(["status" => "error", "message" => "Database error"]);
}
?>
