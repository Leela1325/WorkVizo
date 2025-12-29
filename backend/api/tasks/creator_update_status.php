<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- INPUT ---------- */
$task_id = $_POST['task_id'] ?? '';
$status  = $_POST['status'] ?? '';
$comment = $_POST['comment'] ?? '';

if (!$task_id || !$status) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing data"
    ]);
    exit;
}

/* ---------- GET TASK + ROOM + ASSIGNED USER ---------- */
$tq = $conn->prepare("
    SELECT rt.task_name, rt.room_id, u.id AS assigned_user_id, u.name AS assigned_user_name
    FROM room_tasks rt
    JOIN users u ON u.email = rt.assigned_email
    WHERE rt.id = ?
    LIMIT 1
");
$tq->bind_param("i", $task_id);
$tq->execute();
$task = $tq->get_result()->fetch_assoc();

if (!$task) {
    echo json_encode([
        "status" => "error",
        "message" => "Task not found"
    ]);
    exit;
}

$taskName       = $task['task_name'];
$room_id        = (int)$task['room_id'];
$assignedUserId = (int)$task['assigned_user_id'];

/* ---------- GET LATEST PROOF ---------- */
$q = $conn->prepare("
    SELECT id, user_id
    FROM proofs
    WHERE task_id = ?
    ORDER BY id DESC
    LIMIT 1
");
$q->bind_param("i", $task_id);
$q->execute();
$res = $q->get_result();

if ($res->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "No proof found"
    ]);
    exit;
}

$row = $res->fetch_assoc();
$proof_id = (int)$row['id'];

/* ---------- UPDATE STATUS ---------- */
$u = $conn->prepare("
    UPDATE proofs SET status = ? WHERE id = ?
");
$u->bind_param("si", $status, $proof_id);
$u->execute();

/* ---------- INSERT COMMENT (OPTIONAL) ---------- */
if (!empty($comment)) {
    $c = $conn->prepare("
        INSERT INTO proof_comments (proof_id, task_id, user_id, comment)
        VALUES (?, ?, ?, ?)
    ");
    $c->bind_param("iiis", $proof_id, $task_id, $assignedUserId, $comment);
    $c->execute();
}

/* ---------- GET CREATOR NAME ---------- */
$uq = $conn->prepare("SELECT name FROM users WHERE id = ?");
$uq->bind_param("i", $row['user_id']); // creator id from proof
$uq->execute();
$user = $uq->get_result()->fetch_assoc();
$creatorName = $user ? $user['name'] : 'Someone';

/* ---------- INSERT NOTIFICATION (ONLY TO ASSIGNED USER) ---------- */
$message = "$creatorName added a comment on task \"$taskName\"";


$nq = $conn->prepare("
    INSERT INTO notifications (room_id, user_id, message)
    VALUES (?, ?, ?)
");
$nq->bind_param("iis", $room_id, $assignedUserId, $message);
$nq->execute();

/* ---------- SUCCESS ---------- */
echo json_encode([
    "status" => "success"
]);
exit;
