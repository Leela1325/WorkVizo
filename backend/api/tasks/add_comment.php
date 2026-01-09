<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$task_id = $_POST['task_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';
$comment = $_POST['comment'] ?? '';

if (!$task_id || !$user_id || !$comment) {
    echo json_encode(["status" => "error"]);
    exit;
}

/* Get latest proof */
$q = $conn->prepare("
    SELECT id FROM proofs
    WHERE task_id = ? AND user_id = ?
    ORDER BY id DESC
    LIMIT 1
");
$q->bind_param("ii", $task_id, $user_id);
$q->execute();
$res = $q->get_result();

if ($res->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "No proof"]);
    exit;
}

$proof_id = $res->fetch_assoc()['id'];

/* Insert comment */
$stmt = $conn->prepare("
    INSERT INTO proof_comments (proof_id, task_id, user_id, comment)
    VALUES (?, ?, ?, ?)
");
$stmt->bind_param("iiis", $proof_id, $task_id, $user_id, $comment);
$stmt->execute();

echo json_encode(["status" => "success"]);
