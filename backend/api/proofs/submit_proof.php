<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php";

$task_id = $_POST['task_id'] ?? '';
$user_id = $_POST['user_id'] ?? '';

// Basic validation
if (empty($task_id) || empty($user_id)) {
    echo json_encode(["status" => "error", "message" => "task_id and user_id are required"]);
    exit;
}

// Check task exists
$taskCheck = $conn->query("SELECT * FROM tasks WHERE id = '" . $conn->real_escape_string($task_id) . "'");
if ($taskCheck->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Task not found"]);
    exit;
}
$taskData = $taskCheck->fetch_assoc();
$room_id = $taskData['room_id'];
$task_title = $taskData['title'] ?? 'Unknown Task';

// Check user belongs to room
$checkJoin = $conn->query("SELECT * FROM joined_rooms WHERE room_id = '" . $conn->real_escape_string($room_id) . "' AND user_id = '" . $conn->real_escape_string($user_id) . "'");
if ($checkJoin->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "User not part of this room"]);
    exit;
}

// Check file uploaded
if (!isset($_FILES['proof']) || $_FILES['proof']['error'] !== UPLOAD_ERR_OK) {
    echo json_encode(["status" => "error", "message" => "No file uploaded or upload error"]);
    exit;
}

$file = $_FILES['proof'];

// Optional: validate file size / type (basic)
$maxSize = 10 * 1024 * 1024; // 10 MB
if ($file['size'] > $maxSize) {
    echo json_encode(["status" => "error", "message" => "File too large (max 10 MB)"]);
    exit;
}

// Allowed extensions (adjust if needed)
$allowedExt = ['png','jpg','jpeg','pdf','doc','docx','mp4','mov','zip'];
$extension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
if (!in_array($extension, $allowedExt)) {
    echo json_encode(["status" => "error", "message" => "File type not allowed"]);
    exit;
}

// Prepare upload directory
$upload_dir = __DIR__ . "/../../uploads/proofs/";
if (!file_exists($upload_dir)) {
    mkdir($upload_dir, 0777, true);
}

// Generate unique filename
$filename = "proof_" . time() . "_" . rand(10000, 99999) . "." . $extension;
$filepath = $upload_dir . $filename;
$relative_path = "uploads/proofs/" . $filename;

// Move uploaded file
if (!move_uploaded_file($file['tmp_name'], $filepath)) {
    echo json_encode(["status" => "error", "message" => "File upload failed"]);
    exit;
}

// Insert into proofs table
$task_id_esc = $conn->real_escape_string($task_id);
$user_id_esc = $conn->real_escape_string($user_id);
$path_esc = $conn->real_escape_string($relative_path);

$insertSql = "
    INSERT INTO proofs (task_id, user_id, file_path, created_at)
    VALUES ('$task_id_esc', '$user_id_esc', '$path_esc', NOW())
";

if (!$conn->query($insertSql)) {
    // rollback file if DB insert fails
    if (file_exists($filepath)) unlink($filepath);
    echo json_encode(["status" => "error", "message" => "Database error while saving proof"]);
    exit;
}

$proof_id = $conn->insert_id;

// Update task status to 'submitted' (only if not already completed/verified)
$allowedUpdate = $conn->real_escape_string($task_id);
$conn->query("UPDATE tasks SET status='submitted' WHERE id='$allowedUpdate'");

// Get user name for log
$userRes = $conn->query("SELECT name FROM users WHERE id = '$user_id_esc' LIMIT 1");
$user_name = ($userRes && $userRes->num_rows > 0) ? $userRes->fetch_assoc()['name'] : 'Unknown User';

// Log activity
logActivity(
    $conn,
    $room_id,
    $user_id,
    "proof_submitted",
    "$user_name submitted proof for task '$task_title'"
);

// Success response
echo json_encode([
    "status" => "success",
    "message" => "Proof uploaded successfully",
    "proof_id" => $proof_id,
    "file_path" => $relative_path
]);
?>
