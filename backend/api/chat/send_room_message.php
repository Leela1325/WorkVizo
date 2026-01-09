<?php
header("Content-Type: application/json");
require_once dirname(__DIR__, 2) . "/config.php";

/* ---------- INPUT ---------- */
$room_id   = intval($_POST['room_id'] ?? 0);
$user_id   = intval($_POST['user_id'] ?? 0);
$user_name = trim($_POST['user_name'] ?? '');
$message   = trim($_POST['message'] ?? '');

/* ---------- LOG PAYLOAD ---------- */
file_put_contents(
    __DIR__ . "/payload.log",
    json_encode($_POST) . PHP_EOL,
    FILE_APPEND
);

/* ---------- VALIDATION ---------- */
if ($room_id <= 0 || $user_id <= 0 || $message === '') {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid input"
    ]);
    exit;
}

/* ---------- INSERT CHAT MESSAGE ---------- */
$stmt = $conn->prepare("
    INSERT INTO room_chats (room_id, user_id, user_name, message, created_at)
    VALUES (?, ?, ?, ?, NOW())
");
$stmt->bind_param("iiss", $room_id, $user_id, $user_name, $message);
$stmt->execute();

$chat_id = $stmt->insert_id;

/* ---------- FETCH ROOM NAME ---------- */
$rq = $conn->prepare("SELECT name FROM rooms WHERE id = ? LIMIT 1");
$rq->bind_param("i", $room_id);
$rq->execute();
$room = $rq->get_result()->fetch_assoc();
$room_name = $room['name'] ?? 'Room';

/* ---------- CREATE NOTIFICATION ---------- */
$notify_text = $user_name . " sent a message in room \"" . $room_name . "\"";

$ns = $conn->prepare("
    INSERT INTO notifications (room_id, user_id, message, created_at)
    VALUES (?, ?, ?, NOW())
");
$ns->bind_param("iis", $room_id, $user_id, $notify_text);
$ns->execute();

/* ---------- RESPONSE ---------- */
echo json_encode([
    "status" => "success",
    "chat_id" => $chat_id
]);
