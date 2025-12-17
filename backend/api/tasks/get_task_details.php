<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$task_id = $_GET['task_id'] ?? '';

if (empty($task_id)) {
    echo json_encode(["status" => "error", "message" => "task_id is required"]);
    exit;
}

// STEP 1 — Check Task Exists
$taskQuery = $conn->query("
SELECT t.*, u.name AS assigned_user_name
FROM tasks t
LEFT JOIN users u ON t.assigned_to = u.id
WHERE t.id = '$task_id'
");

if ($taskQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Task not found"]);
    exit;
}

$task = $taskQuery->fetch_assoc();

// STEP 2 — Fetch Proofs
$proofQuery = $conn->query("
SELECT p.*, u.name AS submitted_by_name
FROM proofs p
LEFT JOIN users u ON p.user_id = u.id
WHERE p.task_id = '$task_id'
ORDER BY p.created_at DESC
");

$proofs = [];
while ($row = $proofQuery->fetch_assoc()) {
    $proofs[] = [
        "proof_id" => $row['id'],
        "file_url" => $row['file_path'],
        "status" => $row['status'], // pending, approved, rejected
        "submitted_by" => [
            "id" => $row['user_id'],
            "name" => $row['submitted_by_name']
        ],
        "created_at" => $row['created_at']
    ];
}

// FINAL RESPONSE
$response = [
    "status" => "success",
    "task" => [
        "id" => $task['id'],
        "title" => $task['title'],
        "description" => $task['description'],
        "due_date" => $task['due_date'],
        "status" => $task['status'],
        "assigned_to" => $task['assigned_to'],
        "assigned_user_name" => $task['assigned_user_name']
    ],
    "proofs" => $proofs
];

echo json_encode($response);
?>
