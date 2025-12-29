<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- INPUT ---------- */
$room_id = $_POST['room_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';
$issue   = trim($_POST['issue'] ?? '');

if (!$room_id || !$user_id || !$issue) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing required fields"
    ]);
    exit;
}

/* ---------- INSERT REPORT ---------- */
$stmt = $conn->prepare("
    INSERT INTO reported_rooms (room_id, user_id, issue)
    VALUES (?, ?, ?)
");
$stmt->bind_param("iis", $room_id, $user_id, $issue);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Room reported successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to submit report"
    ]);
}
