<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- INPUT ---------- */
$room_code     = $_POST['room_code'] ?? '';
$user_id       = (int)($_POST['user_id'] ?? 0);
$room_password = $_POST['room_password'] ?? '';

if (!$room_code || !$user_id) {
    echo json_encode([
        "status" => "error",
        "message" => "room_code and user_id are required"
    ]);
    exit;
}

/* ---------- FETCH ROOM ---------- */
$roomQ = $conn->prepare("
    SELECT id, name, room_password, number_of_people
    FROM rooms
    WHERE room_code = ?
    LIMIT 1
");
$roomQ->bind_param("s", $room_code);
$roomQ->execute();
$roomRes = $roomQ->get_result();

if ($roomRes->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid room code"
    ]);
    exit;
}

$room = $roomRes->fetch_assoc();
$room_id   = (int)$room['id'];
$room_name = $room['name'];

/* ---------- PASSWORD CHECK ---------- */
if (!empty($room['room_password'])) {
    if (!$room_password || !password_verify($room_password, $room['room_password'])) {
        echo json_encode([
            "status" => "error",
            "message" => "Incorrect room password"
        ]);
        exit;
    }
}

/* ---------- FETCH USER ---------- */
$userQ = $conn->prepare("
    SELECT name, email
    FROM users
    WHERE id = ?
    LIMIT 1
");
$userQ->bind_param("i", $user_id);
$userQ->execute();
$user = $userQ->get_result()->fetch_assoc();

$user_name  = $user['name'];
$user_email = $user['email'];

/* ---------- CHECK ALREADY JOINED ---------- */
$checkQ = $conn->prepare("
    SELECT id
    FROM joined_rooms
    WHERE room_id = ? AND user_id = ?
    LIMIT 1
");
$checkQ->bind_param("ii", $room_id, $user_id);
$checkQ->execute();

$alreadyJoined = $checkQ->get_result()->num_rows > 0;

/* ---------- JOIN ROOM ---------- */
if (!$alreadyJoined) {

    // 🔥 joined_at is auto-set here
    $joinQ = $conn->prepare("
        INSERT INTO joined_rooms (room_id, user_id)
        VALUES (?, ?)
    ");
    $joinQ->bind_param("ii", $room_id, $user_id);
    $joinQ->execute();

    /* ---------- NOTIFICATION ---------- */
    $message = "$user_name joined the room $room_name";

    $membersQ = $conn->prepare("
        SELECT user_id
        FROM joined_rooms
        WHERE room_id = ?
    ");
    $membersQ->bind_param("i", $room_id);
    $membersQ->execute();
    $membersRes = $membersQ->get_result();

    while ($m = $membersRes->fetch_assoc()) {
        $notifyQ = $conn->prepare("
            INSERT INTO notifications (room_id, user_id, message, created_at)
            VALUES (?, ?, ?, NOW())
        ");
        $notifyQ->bind_param("iis", $room_id, $m['user_id'], $message);
        $notifyQ->execute();
    }
}

/* ---------- RESPONSE ---------- */
echo json_encode([
    "status" => "success",
    "message" => $alreadyJoined ? "Already joined" : "Joined room successfully",
    "room_id" => $room_id
]);
exit;
