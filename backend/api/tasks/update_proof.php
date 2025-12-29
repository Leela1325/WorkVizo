<?php
header("Content-Type: application/json");

ini_set('display_errors', 0);
ini_set('log_errors', 1);
error_reporting(E_ALL);

require_once __DIR__ . "/../../config.php";

/* ---------- STRICT VALIDATION ---------- */
if (
    empty($_POST['task_id']) ||
    empty($_POST['user_id']) ||
    !isset($_POST['description']) ||
    trim($_POST['description']) === '' ||
    !isset($_FILES['proof']) ||
    $_FILES['proof']['error'] !== 0
) {
    echo json_encode([
        "status" => "error",
        "message" => "Description and proof file are required"
    ]);
    exit;
}

$task_id     = (int) $_POST['task_id'];
$user_id     = (int) $_POST['user_id'];
$description = trim($_POST['description']);

/* ---------- FETCH TASK + ROOM ---------- */
$tq = $conn->prepare("
    SELECT 
        rt.task_name,
        rt.room_id,
        r.name AS room_name
    FROM room_tasks rt
    JOIN rooms r ON r.id = rt.room_id
    WHERE rt.id = ?
    LIMIT 1
");
$tq->bind_param("i", $task_id);
$tq->execute();
$task = $tq->get_result()->fetch_assoc();

if (!$task) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid task"
    ]);
    exit;
}

$taskName = $task['task_name'];
$room_id  = (int)$task['room_id'];
$roomName = $task['room_name'];

/* ---------- FIND EXISTING PROOF ---------- */
$q = $conn->prepare("
    SELECT id
    FROM proofs
    WHERE task_id = ? AND user_id = ?
    ORDER BY id DESC
    LIMIT 1
");
$q->bind_param("ii", $task_id, $user_id);
$q->execute();
$res = $q->get_result();

if ($res->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "No existing proof"
    ]);
    exit;
}

$proof_id = (int)$res->fetch_assoc()['id'];

/* ---------- FILE UPLOAD ---------- */
$uploadDir = "C:/xampp/htdocs/workvizo_backend/uploads/";

if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0777, true);
}

$tmpPath  = $_FILES['proof']['tmp_name'];
$origName = $_FILES['proof']['name'];

$ext = pathinfo($origName, PATHINFO_EXTENSION);
if ($ext === '') {
    $ext = 'bin';
}

$newName  = "proof_" . bin2hex(random_bytes(6)) . "." . $ext;
$fullPath = $uploadDir . $newName;

if (!move_uploaded_file($tmpPath, $fullPath)) {
    echo json_encode([
        "status" => "error",
        "message" => "File upload failed"
    ]);
    exit;
}

$filePath = "uploads/" . $newName;

/* ---------- UPDATE PROOF ---------- */
$u = $conn->prepare("
    UPDATE proofs
    SET description = ?, file_path = ?
    WHERE id = ?
");
$u->bind_param("ssi", $description, $filePath, $proof_id);
$u->execute();

/* ---------- FETCH USER NAME ---------- */
$uq = $conn->prepare("SELECT name FROM users WHERE id = ?");
$uq->bind_param("i", $user_id);
$uq->execute();
$user = $uq->get_result()->fetch_assoc();
$userName = $user ? $user['name'] : 'Someone';

/* ---------- INSERT NOTIFICATION ---------- */
$message = "$userName updated proof for task \"$taskName\" in room $roomName";

$nq = $conn->prepare("
    INSERT INTO notifications (room_id, user_id, message)
    VALUES (?, ?, ?)
");
$nq->bind_param("iis", $room_id, $user_id, $message);
$nq->execute();

/* ---------- SUCCESS ---------- */
echo json_encode([
    "status" => "success",
    "message" => "Proof updated successfully"
]);
exit;
