<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$task_id = $_GET['task_id'] ?? '';

if (empty($task_id)) {
    echo json_encode(["status" => "error", "message" => "task_id is required"]);
    exit;
}

// Fetch comments with user names
$query = $conn->query("
    SELECT c.id, c.comment, c.created_at,
           u.name AS user_name, u.id AS user_id
    FROM task_comments c
    JOIN users u ON c.user_id = u.id
    WHERE c.task_id = '$task_id'
    ORDER BY c.created_at ASC
");

$comments = [];

while ($row = $query->fetch_assoc()) {
    $comments[] = $row;
}

echo json_encode([
    "status" => "success",
    "total_comments" => count($comments),
    "comments" => $comments
]);
?>
