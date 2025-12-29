<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- VALIDATION ---------- */
if (
    empty($_POST['task_id']) ||
    empty($_POST['user_id']) ||
    empty($_POST['status'])
) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing required fields"
    ]);
    exit;
}

$task_id = (int)$_POST['task_id'];
$user_id = (int)$_POST['user_id'];
$status  = $_POST['status'];
$description = $_POST['description'] ?? "";

/* ---------- CHECK EXISTING PROOF ---------- */
$check = $conn->prepare("
    SELECT id 
    FROM proofs 
    WHERE task_id = ? AND user_id = ?
    LIMIT 1
");
$check->bind_param("ii", $task_id, $user_id);
$check->execute();
$res = $check->get_result();

if ($res->num_rows == 0) {
    echo json_encode([
        "status" => "error",
        "message" => "No proof found. Upload proof first."
    ]);
    exit;
}

/* ---------- UPDATE PROOF ---------- */
$u = $conn->prepare("
    UPDATE proofs 
    SET status = ?, description = ?
    WHERE task_id = ? AND user_id = ?
");
$u->bind_param("ssii", $status, $description, $task_id, $user_id);
$u->execute();

/* ---------- UPDATE TASK STATUS ---------- */
$conn->query("
    UPDATE room_tasks
    SET status = '$status'
    WHERE id = '$task_id'
");

echo json_encode([
    "status" => "success",
    "message" => "Status updated successfully"
]);
exit;
