<?php
ini_set('display_errors', 1);
error_reporting(E_ALL);

header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

/*
EXPECTED POST:
- room_id
- tasks_json (JSON array)
*/

$room_id    = $_POST['room_id'] ?? '';
$tasks_json = $_POST['tasks_json'] ?? '';

if (empty($room_id) || empty($tasks_json)) {
    echo json_encode([
        "status" => "error",
        "message" => "room_id and tasks_json are required"
    ]);
    exit;
}

$tasks = json_decode($tasks_json, true);

if (!is_array($tasks)) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid tasks_json format"
    ]);
    exit;
}

$conn->begin_transaction();

try {

    $stmt = $conn->prepare("
        INSERT INTO room_tasks 
        (room_id, task_no, task_name, start_date, end_date, assigned_email)
        VALUES (?, ?, ?, ?, ?, ?)
    ");

    foreach ($tasks as $task) {

        $task_no        = $task['task_no'] ?? 0;
        $task_name      = $task['task_name'] ?? '';
        $start_date     = $task['start_date'] ?? null;
        $end_date       = $task['end_date'] ?? null;
        $assigned_email = $task['assigned_email'] ?? '';

        if (empty($task_name) || empty($assigned_email)) {
            throw new Exception("Task name and assigned email are required");
        }

        $stmt->bind_param(
            "iissss",
            $room_id,
            $task_no,
            $task_name,
            $start_date,
            $end_date,
            $assigned_email
        );

        if (!$stmt->execute()) {
            throw new Exception($stmt->error);
        }
    }

    $conn->commit();

    echo json_encode([
        "status" => "success",
        "message" => "Tasks created successfully"
    ]);

} catch (Exception $e) {

    $conn->rollback();

    echo json_encode([
        "status" => "error",
        "message" => $e->getMessage()
    ]);
}
