<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------------------------------
   READ INPUT
--------------------------------- */
$room_id        = $_POST['room_id'] ?? '';
$task_name      = trim($_POST['task_name'] ?? '');
$start_date     = $_POST['start_date'] ?? '';
$end_date       = $_POST['end_date'] ?? '';
$assigned_email = trim($_POST['assigned_email'] ?? '');
$user_id        = $_POST['user_id'] ?? '';

/* ---------------------------------
   BASIC VALIDATION
--------------------------------- */
if (
    empty($room_id) ||
    empty($task_name) ||
    empty($start_date) ||
    empty($end_date) ||
    empty($assigned_email) ||
    empty($user_id)
) {
    echo json_encode([
        "status" => "error",
        "message" => "All fields are required"
    ]);
    exit;
}

/* ---------------------------------
   DATE VALIDATION (YYYY-MM-DD)
--------------------------------- */
function isValidDate($date) {
    $d = DateTime::createFromFormat('Y-m-d', $date);
    return $d && $d->format('Y-m-d') === $date;
}

if (!isValidDate($start_date) || !isValidDate($end_date)) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid date format. Use YYYY-MM-DD"
    ]);
    exit;
}

$taskStart = new DateTime($start_date);
$taskEnd   = new DateTime($end_date);

if ($taskEnd < $taskStart) {
    echo json_encode([
        "status" => "error",
        "message" => "Task end date cannot be before start date"
    ]);
    exit;
}

/* ---------------------------------
   FETCH ROOM INFO
--------------------------------- */
$roomQ = $conn->prepare("
    SELECT name, start_date, end_date 
    FROM rooms 
    WHERE id = ?
    LIMIT 1
");
$roomQ->bind_param("i", $room_id);
$roomQ->execute();
$roomRes = $roomQ->get_result();

if ($roomRes->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid room"
    ]);
    exit;
}

$room = $roomRes->fetch_assoc();
$roomStart = new DateTime($room['start_date']);
$roomEnd   = new DateTime($room['end_date']);

/* ---------------------------------
   ROOM DATE RANGE VALIDATION
--------------------------------- */
if ($taskStart < $roomStart || $taskEnd > $roomEnd) {
    echo json_encode([
        "status" => "error",
        "message" => "Task dates must be within room duration"
    ]);
    exit;
}

/* ---------------------------------
   INSERT TASK
--------------------------------- */
$insert = $conn->prepare("
    INSERT INTO room_tasks
    (room_id, task_name, start_date, end_date, assigned_email, status)
    VALUES (?, ?, ?, ?, ?, 'pending')
");

$insert->bind_param(
    "issss",
    $room_id,
    $task_name,
    $start_date,
    $end_date,
    $assigned_email
);

if (!$insert->execute()) {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to add task"
    ]);
    exit;
}

/* ---------------------------------
   FETCH CREATOR NAME
--------------------------------- */
$userQ = $conn->prepare("
    SELECT name 
    FROM users 
    WHERE id = ?
    LIMIT 1
");
$userQ->bind_param("i", $user_id);
$userQ->execute();
$userRes = $userQ->get_result();
$userRow = $userRes->fetch_assoc();

$userName = $userRow['name'] ?? 'Creator';

/* ---------------------------------
   INSERT NOTIFICATION
--------------------------------- */
$message = $conn->real_escape_string(
    "$userName added a task: $task_name"
);

$notify = $conn->prepare("
    INSERT INTO notifications (room_id, user_id, message, created_at)
    VALUES (?, ?, ?, NOW())
");
$notify->bind_param("iis", $room_id, $user_id, $message);
$notify->execute();

/* ---------------------------------
   SUCCESS
--------------------------------- */
echo json_encode([
    "status" => "success",
    "message" => "Task added successfully"
]);
exit;
