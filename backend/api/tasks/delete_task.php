<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$task_id = $_POST['task_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';

if (!$task_id || !$user_id) {
    echo json_encode([
        "status" => "error",
        "message" => "task_id and user_id required"
    ]);
    exit;
}

/* ---------- FETCH TASK + ROOM + CREATOR (BEFORE DELETE) ---------- */
$q = $conn->prepare("
    SELECT 
        rt.task_name,
        rt.room_id,
        r.name AS room_name,
        r.created_by
    FROM room_tasks rt
    JOIN rooms r ON r.id = rt.room_id
    WHERE rt.id = ?
    LIMIT 1
");
$q->bind_param("i", $task_id);
$q->execute();
$res = $q->get_result();

if ($res->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Invalid task"]);
    exit;
}

$data = $res->fetch_assoc();

if ((int)$data['created_by'] !== (int)$user_id) {
    echo json_encode(["status" => "error", "message" => "Unauthorized"]);
    exit;
}

$taskName = $data['task_name'];
$room_id  = (int)$data['room_id'];
$roomName = $data['room_name'];

/* ---------- FETCH USER NAME ---------- */
$uq = $conn->prepare("SELECT name FROM users WHERE id = ?");
$uq->bind_param("i", $user_id);
$uq->execute();
$user = $uq->get_result()->fetch_assoc();
$userName = $user ? $user['name'] : 'Someone';

/* ---------- INSERT NOTIFICATION FIRST ---------- */
$message = "$userName deleted task \"$taskName\" from room $roomName";

$nq = $conn->prepare("
    INSERT INTO notifications (room_id, user_id, message)
    VALUES (?, ?, ?)
");

if (!$nq) {
    echo json_encode([
        "status" => "error",
        "message" => "Notification prepare failed",
        "sql_error" => $conn->error
    ]);
    exit;
}

$nq->bind_param("iis", $room_id, $user_id, $message);
$nq->execute();

if ($nq->affected_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Notification insert failed",
        "sql_error" => $nq->error
    ]);
    exit;
}

/* ---------- DELETE TASK AFTER NOTIFICATION ---------- */
$del = $conn->prepare("DELETE FROM room_tasks WHERE id = ?");
$del->bind_param("i", $task_id);
$del->execute();

/* ---------- SUCCESS ---------- */
echo json_encode([
    "status" => "success",
    "message" => "Task deleted and notification saved"
]);
exit;
