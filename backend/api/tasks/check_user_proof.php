<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$task_id = $_GET['task_id'] ?? '';
$user_id = $_GET['user_id'] ?? '';

$q = $conn->prepare("
    SELECT id, status, description 
    FROM proofs 
    WHERE task_id=? AND user_id=?
    LIMIT 1
");
$q->bind_param("ii", $task_id, $user_id);
$q->execute();
$res = $q->get_result();

if ($res->num_rows > 0) {
    echo json_encode([
        "status" => "exists",
        "proof" => $res->fetch_assoc()
    ]);
} else {
    echo json_encode(["status" => "none"]);
}
exit;
