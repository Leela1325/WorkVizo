<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$task_id = $_GET['task_id'] ?? '';

if (!$task_id) {
    echo json_encode([
        "status" => "error",
        "message" => "task_id required"
    ]);
    exit;
}

$stmt = $conn->prepare("
    SELECT task_name
    FROM room_tasks
    WHERE id = ?
    LIMIT 1
");
$stmt->bind_param("i", $task_id);
$stmt->execute();
$res = $stmt->get_result();

if ($res->num_rows === 0) {
    echo json_encode([
        "status" => "success",
        "task_name" => null
    ]);
    exit;
}

$row = $res->fetch_assoc();

echo json_encode([
    "status" => "success",
    "task_name" => $row['task_name']
]);
exit;
