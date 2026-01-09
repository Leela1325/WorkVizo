<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- VALIDATION ---------- */
if (
    empty($_POST['task_id']) ||
    empty($_POST['user_id']) ||
    empty($_POST['status']) ||
    !isset($_FILES['proof'])
) {
    echo json_encode(["status" => "error", "message" => "Missing fields"]);
    exit;
}

$task_id = (int)$_POST['task_id'];
$user_id = (int)$_POST['user_id'];
$status  = $_POST['status'];
$description = $_POST['description'] ?? "";

/* ---------- UPLOAD FILE ---------- */
$uploadDir = __DIR__ . "/../../uploads/";
if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0777, true);
}

$tmp = $_FILES['proof']['tmp_name'];
$ext = pathinfo($_FILES['proof']['name'], PATHINFO_EXTENSION);
$fileName = "proof_" . time() . "_" . rand(1000,9999) . "." . $ext;
$fullPath = $uploadDir . $fileName;
$filePathForDB = "uploads/" . $fileName;

if (!move_uploaded_file($tmp, $fullPath)) {
    echo json_encode(["status" => "error", "message" => "File upload failed"]);
    exit;
}

/* ---------- CHECK EXISTING PROOF ---------- */
$check = $conn->prepare("
    SELECT id, file_path
    FROM proofs
    WHERE task_id = ? AND user_id = ?
    LIMIT 1
");
$check->bind_param("ii", $task_id, $user_id);
$check->execute();
$res = $check->get_result();

if ($res->num_rows > 0) {

    /* ---------- UPDATE ---------- */
    $row = $res->fetch_assoc();
    $proof_id = $row['id'];

    // OPTIONAL: delete old file
    if (!empty($row['file_path']) && file_exists(__DIR__ . "/../../" . $row['file_path'])) {
        unlink(__DIR__ . "/../../" . $row['file_path']);
    }

    $u = $conn->prepare("
        UPDATE proofs
        SET file_path = ?, description = ?, status = ?
        WHERE id = ?
    ");
    $u->bind_param("sssi", $filePathForDB, $description, $status, $proof_id);
    $u->execute();

} else {

    /* ---------- INSERT ---------- */
    $i = $conn->prepare("
        INSERT INTO proofs (task_id, user_id, file_path, description, status)
        VALUES (?, ?, ?, ?, ?)
    ");
    $i->bind_param("iisss", $task_id, $user_id, $filePathForDB, $description, $status);
    $i->execute();
}

echo json_encode(["status" => "success"]);
exit;
