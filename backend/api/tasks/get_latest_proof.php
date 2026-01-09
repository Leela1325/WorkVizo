<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- VALIDATION ---------- */
if (empty($_POST['task_id'])) {
    echo json_encode([
        "status" => "error",
        "message" => "task_id required"
    ]);
    exit;
}

$task_id = (int) $_POST['task_id'];

/* ---------- FETCH LATEST PROOF ---------- */
$q = $conn->prepare("
    SELECT file_path, status, description
    FROM proofs
    WHERE task_id = ?
    ORDER BY id DESC
    LIMIT 1
");
$q->bind_param("i", $task_id);
$q->execute();
$res = $q->get_result();

if ($res->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "No proof found"
    ]);
    exit;
}

$proof = $res->fetch_assoc();

/* ---------- RESPONSE ---------- */
echo json_encode([
    "status" => "success",
    "proof" => [
        "file_path" => $proof['file_path'],
        "status" => $proof['status'],
        "description" => $proof['description']
    ]
]);
exit;
