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

$stmt = $conn->prepare("
    SELECT comment, created_at
    FROM proof_comments
    WHERE task_id = ?
      AND user_id = ?
    ORDER BY id DESC
    LIMIT 1
");
$stmt->bind_param("ii", $task_id, $user_id);
$stmt->execute();
$res = $stmt->get_result();

if ($res->num_rows === 0) {
    echo json_encode([
        "status" => "success",
        "comment" => null,
        "comment_time" => null
    ]);
    exit;
}

$row = $res->fetch_assoc();

echo json_encode([
    "status" => "success",
    "comment" => $row['comment'],
    "comment_time" => $row['created_at']
]);
exit;
