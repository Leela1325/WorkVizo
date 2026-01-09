<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$task_id = $_GET['task_id'] ?? '';
$user_id = $_GET['user_id'] ?? '';

if (!$task_id || !$user_id) {
    echo json_encode([
        "status" => "error",
        "message" => "task_id and user_id required"
    ]);
    exit;
}

/* ---------------- GET TASK NAME FROM room_tasks ---------------- */
$taskQ = $conn->prepare("
    SELECT task_name 
    FROM room_tasks 
    WHERE id = ?
");
$taskQ->bind_param("i", $task_id);
$taskQ->execute();
$taskRes = $taskQ->get_result();

$task_name = null;
if ($taskRes->num_rows > 0) {
    $task_name = $taskRes->fetch_assoc()['task_name'];
}

/* ---------------- GET LATEST PROOF ID ---------------- */
$proofQ = $conn->prepare("
    SELECT id 
    FROM proofs 
    WHERE task_id = ? AND user_id = ?
    ORDER BY id DESC 
    LIMIT 1
");
$proofQ->bind_param("ii", $task_id, $user_id);
$proofQ->execute();
$proofRes = $proofQ->get_result();

if ($proofRes->num_rows === 0) {
    echo json_encode([
        "status" => "success",
        "task_name" => $task_name,
        "comment" => null,
        "comment_time" => null
    ]);
    exit;
}

$proof_id = $proofRes->fetch_assoc()['id'];

/* ---------------- GET LATEST COMMENT ---------------- */
$commentQ = $conn->prepare("
    SELECT comment, created_at
    FROM proof_comments
    WHERE proof_id = ?
    ORDER BY id DESC
    LIMIT 1
");
$commentQ->bind_param("i", $proof_id);
$commentQ->execute();
$commentRes = $commentQ->get_result();

if ($commentRes->num_rows === 0) {
    echo json_encode([
        "status" => "success",
        "task_name" => $task_name,
        "comment" => null,
        "comment_time" => null
    ]);
    exit;
}

$row = $commentRes->fetch_assoc();

/* ---------------- FINAL RESPONSE ---------------- */
echo json_encode([
    "status" => "success",
    "task_name" => $task_name,
    "comment" => $row['comment'],
    "comment_time" => $row['created_at']
]);
