<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../helpers/log_activity.php";

require_once __DIR__ . "/../../config.php";

$task_id = $_POST['task_id'] ?? '';

if (empty($task_id)) {
    echo json_encode(["status" => "error", "message" => "task_id is required"]);
    exit;
}

// Collect editable fields
$updates = [];

if (!empty($_POST['title'])) {
    $updates[] = "title = '" . $conn->real_escape_string($_POST['title']) . "'";
}

if (!empty($_POST['description'])) {
    $updates[] = "description = '" . $conn->real_escape_string($_POST['description']) . "'";
}

if (!empty($_POST['due_date'])) {
    $updates[] = "due_date = '" . $_POST['due_date'] . "'";
}

if (!empty($_POST['assigned_to'])) {
    $updates[] = "assigned_to = '" . $_POST['assigned_to'] . "'";
}

if (!empty($_POST['status'])) {
    $status = $_POST['status'];
    if (!in_array($status, ['pending', 'in_progress', 'submitted', 'completed'])) {
        echo json_encode(["status" => "error", "message" => "Invalid status"]);
        exit;
    }
    $updates[] = "status = '$status'";
}

if (count($updates) == 0) {
    echo json_encode(["status" => "error", "message" => "No fields to update"]);
    exit;
}

$updateSQL = implode(", ", $updates);

// Run update
$sql = "UPDATE tasks SET $updateSQL WHERE id = '$task_id'";

if ($conn->query($sql)) {
    echo json_encode(["status" => "success", "message" => "Task updated successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Database error"]);
}
?>
