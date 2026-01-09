<?php
header("Content-Type: application/json");

require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php";

$proof_id = $_POST['proof_id'] ?? '';
$status   = $_POST['status'] ?? '';    // approved / rejected
$admin_id = $_POST['admin_id'] ?? ''; // user who approves/rejects

// Validate
if (empty($proof_id) || empty($status) || empty($admin_id)) {
    echo json_encode(["status" => "error", "message" => "proof_id, status, admin_id are required"]);
    exit;
}

$status = strtolower($status);
if (!in_array($status, ['approved', 'rejected'])) {
    echo json_encode(["status" => "error", "message" => "Invalid status. Use 'approved' or 'rejected'"]);
    exit;
}

// Escape inputs
$proof_id_esc = $conn->real_escape_string($proof_id);
$admin_id_esc = $conn->real_escape_string($admin_id);
$status_esc   = $conn->real_escape_string($status);

// Fetch proof + related task + submitter info
$sql = "
    SELECT p.*, t.id AS task_id, t.title AS task_title, t.room_id AS room_id, 
           u.id AS submitter_id, u.name AS submitter_name
    FROM proofs p
    LEFT JOIN tasks t ON p.task_id = t.id
    LEFT JOIN users u  ON p.user_id = u.id
    WHERE p.id = '$proof_id_esc'
    LIMIT 1
";

$result = $conn->query($sql);
if (!$result || $result->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Proof not found"]);
    exit;
}

$row = $result->fetch_assoc();
$task_id     = $row['task_id'];
$task_title  = $row['task_title'] ?? 'Unknown Task';
$room_id     = $row['room_id'];
$submitter_id   = $row['submitter_id'];
$submitter_name = $row['submitter_name'] ?? 'Unknown User';

// Update proofs table status (and optionally store reviewer info if columns exist)
$updateProofSql = "
    UPDATE proofs
    SET status = '$status_esc'
    WHERE id = '$proof_id_esc'
";

if (!$conn->query($updateProofSql)) {
    echo json_encode(["status" => "error", "message" => "Failed to update proof status"]);
    exit;
}

// Update task status:
// - if approved => set to 'verified' (you may choose 'completed' if you prefer)
// - if rejected => set to 'in_progress' (so user can rework)
if (!empty($task_id)) {
    $task_id_esc = $conn->real_escape_string($task_id);
    if ($status === 'approved') {
        $conn->query("UPDATE tasks SET status='verified' WHERE id='$task_id_esc'");
    } else {
        // rejected
        $conn->query("UPDATE tasks SET status='in_progress' WHERE id='$task_id_esc'");
    }
}

// Fetch admin name for logging (optional)
$adminRes = $conn->query("SELECT name FROM users WHERE id = '$admin_id_esc' LIMIT 1");
$admin_name = ($adminRes && $adminRes->num_rows > 0) ? $adminRes->fetch_assoc()['name'] : 'Admin';

// Log activity
if (!empty($room_id)) {
    if ($status === 'approved') {
        $desc = "$admin_name approved proof for task '$task_title' submitted by $submitter_name";
        logActivity($conn, $room_id, $admin_id, "proof_approved", $desc);
    } else {
        $desc = "$admin_name rejected proof for task '$task_title' submitted by $submitter_name";
        logActivity($conn, $room_id, $admin_id, "proof_rejected", $desc);
    }
}

// Success response
echo json_encode([
    "status" => "success",
    "message" => "Proof $status successfully",
    "proof_id" => $proof_id,
    "task_id" => $task_id,
    "task_title" => $task_title,
    "submitter_id" => $submitter_id,
    "submitter_name" => $submitter_name
]);
