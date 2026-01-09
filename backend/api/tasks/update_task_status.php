<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- INPUT ---------- */
$task_id = isset($_POST['task_id']) ? (int)$_POST['task_id'] : 0;
$user_id = isset($_POST['user_id']) ? (int)$_POST['user_id'] : 0;
$status  = $_POST['status'] ?? '';

if ($task_id === 0 || $user_id === 0 || $status === '') {
    echo json_encode(["status"=>"error","message"=>"Missing fields"]);
    exit;
}

/* ---------- GET TASK ---------- */
$tq = $conn->prepare("
    SELECT room_id, task_name
    FROM room_tasks
    WHERE id = ?
");
$tq->bind_param("i", $task_id);
$tq->execute();
$task = $tq->get_result()->fetch_assoc();

if (!$task) {
    echo json_encode(["status"=>"error","message"=>"Task not found"]);
    exit;
}

$room_id  = (int)$task['room_id'];
$taskName = $task['task_name'];

/* ---------- GET LATEST PROOF ---------- */
$pq = $conn->prepare("
    SELECT id
    FROM proofs
    WHERE task_id = ? AND user_id = ?
    ORDER BY id DESC
    LIMIT 1
");
$pq->bind_param("ii", $task_id, $user_id);
$pq->execute();
$proof = $pq->get_result()->fetch_assoc();

if (!$proof) {
    echo json_encode([
        "status"=>"error",
        "message"=>"No proof found for this user"
    ]);
    exit;
}

/* ---------- UPDATE STATUS ---------- */
$proof_id = (int)$proof['id'];

$u = $conn->prepare("
    UPDATE proofs SET status = ? WHERE id = ?
");
$u->bind_param("si", $status, $proof_id);
$u->execute();

/* ---------- GET USER NAME ---------- */
$uq = $conn->prepare("SELECT name FROM users WHERE id = ?");
$uq->bind_param("i", $user_id);
$uq->execute();
$user = $uq->get_result()->fetch_assoc();
$userName = $user ? $user['name'] : 'Someone';

/* ---------- INSERT NOTIFICATION (CRITICAL PART) ---------- */
$message = "$userName updated task \"$taskName\" to $status";

$nq = $conn->prepare("
    INSERT INTO notifications (room_id, user_id, message)
    VALUES (?, ?, ?)
");

if (!$nq) {
    echo json_encode([
        "status"=>"error",
        "message"=>"Prepare failed",
        "sql_error"=>$conn->error
    ]);
    exit;
}

$nq->bind_param("iis", $room_id, $user_id, $message);
$nq->execute();

if ($nq->affected_rows === 0) {
    echo json_encode([
        "status"=>"error",
        "message"=>"Notification not inserted",
        "sql_error"=>$nq->error
    ]);
    exit;
}

/* ---------- SUCCESS ---------- */
echo json_encode([
    "status" => "success",
    "message" => "Status updated & notification saved"
]);
exit;
