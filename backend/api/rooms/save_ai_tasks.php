<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

$room_id = $_POST['room_id'] ?? '';
$tasks_json = $_POST['tasks'] ?? '';

if (empty($room_id) || empty($tasks_json)) {
    echo json_encode([
        "status" => "error",
        "message" => "room_id and tasks are required"
    ]);
    exit;
}

$tasks = json_decode($tasks_json, true);

if (!is_array($tasks)) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid tasks format"
    ]);
    exit;
}

$conn->begin_transaction();

try {

    // 🔥 DELETE OLD TASKS (AI regenerated)
    $del = $conn->prepare("DELETE FROM room_tasks WHERE room_id = ?");
    $del->bind_param("i", $room_id);
    $del->execute();

    // 🔥 INSERT NEW TASKS
    $stmt = $conn->prepare("
        INSERT INTO room_tasks 
        (room_id, task_no, task_name, start_date, end_date, status, people, assigned_email)
        VALUES (?, ?, ?, ?, ?, 'pending', 0, ?)
    ");

    foreach ($tasks as $index => $task) {

        $task_no = $index + 1;
        $task_name = trim($task['task_name'] ?? '');
        $start_date = $task['start_date'] ?? null;
        $end_date = $task['end_date'] ?? null;
        $assigned_email = trim($task['assigned_email'] ?? '');

        if ($task_name === '' || $assigned_email === '') {
            throw new Exception("Task name and assigned email required");
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

        $stmt->execute();
    }

    $conn->commit();

    echo json_encode([
        "status" => "success",
        "message" => "AI tasks saved successfully"
    ]);

} catch (Exception $e) {

    $conn->rollback();

    echo json_encode([
        "status" => "error",
        "message" => $e->getMessage()
    ]);
}
