<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$task_id = $_GET['task_id'] ?? '';

if (empty($task_id)) {
    echo json_encode(["status" => "error", "message" => "task_id is required"]);
    exit;
}

// Check task exists
$check = $conn->query("SELECT id FROM tasks WHERE id='$task_id'");
if ($check->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Task does not exist"]);
    exit;
}

// Fetch proofs
$sql = "
SELECT p.id, p.file_path, p.status, p.submitted_at,
       u.id AS user_id, u.name AS user_name, u.email
FROM proofs p
JOIN users u ON p.user_id = u.id
WHERE p.task_id = '$task_id'
ORDER BY p.submitted_at DESC
";

$result = $conn->query($sql);

$proofs = [];

while ($row = $result->fetch_assoc()) {
    $proofs[] = $row;
}

echo json_encode([
    "status" => "success",
    "proofs" => $proofs
]);
?>
