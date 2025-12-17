<?php
header("Content-Type: application/json");

require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../helpers/log_activity.php";

// Collect data
$room_code   = $_POST['room_code']   ?? '';
$title       = $_POST['title']       ?? '';
$description = $_POST['description'] ?? '';
$due_date    = $_POST['due_date']    ?? null;
$assigned_to = $_POST['assigned_to'] ?? null;  // optional
$created_by  = $_POST['created_by']  ?? '';

// Validate required fields
if (empty($room_code) || empty($title) || empty($created_by)) {
    echo json_encode([
        "status" => "error",
        "message" => "room_code, title, and created_by are required"
    ]);
    exit;
}

// Escape inputs
$room_code   = $conn->real_escape_string($room_code);
$title       = $conn->real_escape_string($title);
$description = $conn->real_escape_string($description);
$due_date    = $due_date ? $conn->real_escape_string($due_date) : null;

// STEP 1: Convert room_code → room_id
$roomQuery = $conn->query("SELECT id, name FROM rooms WHERE room_code = '$room_code'");

if ($roomQuery->num_rows == 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid room code"
    ]);
    exit;
}

$roomData = $roomQuery->fetch_assoc();
$room_id = $roomData['id'];
$room_name = $roomData['name'];

// STEP 2: Insert task
$sql = "
INSERT INTO tasks (room_id, title, description, due_date, assigned_to, created_by)
VALUES (
    '$room_id',
    '$title',
    '$description',
    " . ($due_date ? "'$due_date'" : "NULL") . ",
    " . ($assigned_to ? "'$assigned_to'" : "NULL") . ",
    '$created_by'
)
";

if ($conn->query($sql)) {

    $task_id = $conn->insert_id;

    // Fetch creator name
    $creatorQuery = $conn->query("SELECT name FROM users WHERE id='$created_by'");
    $creatorName = ($creatorQuery->num_rows > 0)
        ? $creatorQuery->fetch_assoc()['name']
        : "User";

    // Fetch assigned user's name (optional)
    $assignedName = null;
    if (!empty($assigned_to)) {
        $assignedQuery = $conn->query("SELECT name FROM users WHERE id='$assigned_to'");
        if ($assignedQuery->num_rows > 0) {
            $assignedName = $assignedQuery->fetch_assoc()['name'];
        }
    }

    // STEP 3: Log activity
    if ($assignedName) {
        $descriptionLog = "$creatorName created task '$title' and assigned it to $assignedName";
    } else {
        $descriptionLog = "$creatorName created a new task '$title'";
    }

    logActivity($conn, $room_id, $created_by, "task_created", $descriptionLog);

    // Response
    echo json_encode([
        "status" => "success",
        "task_id" => $task_id,
        "message" => "Task created successfully"
    ]);

} else {
    echo json_encode([
        "status" => "error",
        "message" => "Database error: " . $conn->error
    ]);
}

?>
