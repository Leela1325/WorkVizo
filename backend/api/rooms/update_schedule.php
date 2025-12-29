<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- READ JSON INPUT ---------- */
$input = json_decode(file_get_contents("php://input"), true);

$room_id = $input['room_id'] ?? '';
$start   = $input['start_date'] ?? '';
$end     = $input['end_date'] ?? '';
$tasks   = $input['tasks'] ?? [];

if (empty($room_id) || empty($start) || empty($end)) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid input"
    ]);
    exit;
}

/* ---------- FETCH ROOM + CREATOR ---------- */
$roomQ = $conn->query("
    SELECT r.id, r.created_by, u.name AS creator_name
    FROM rooms r
    JOIN users u ON u.id = r.created_by
    WHERE r.id = '$room_id'
    LIMIT 1
");

if ($roomQ->num_rows == 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Room not found"
    ]);
    exit;
}

$room = $roomQ->fetch_assoc();
$creator_id   = $room['created_by'];
$creator_name = $room['creator_name'] ?? 'Creator';

/* ---------- TRANSACTION ---------- */
$conn->begin_transaction();

try {

    /* ---------- UPDATE ROOM DATES ---------- */
    $stmtRoom = $conn->prepare("
        UPDATE rooms 
        SET start_date = ?, end_date = ?
        WHERE id = ?
    ");
    $stmtRoom->bind_param("ssi", $start, $end, $room_id);
    $stmtRoom->execute();

    /* ---------- UPDATE TASKS ---------- */
    foreach ($tasks as $t) {

        $task_no = $t['task_no'];
        $name    = $t['task_name'];
        $s       = $t['start_date'];
        $e       = $t['end_date'];
        $email   = $t['assigned_email'];

        $stmtTask = $conn->prepare("
            UPDATE room_tasks
            SET task_name = ?, start_date = ?, end_date = ?, assigned_email = ?
            WHERE room_id = ? AND task_no = ?
        ");
        $stmtTask->bind_param(
            "ssssii",
            $name,
            $s,
            $e,
            $email,
            $room_id,
            $task_no
        );
        $stmtTask->execute();

        /* ---------- RESET TASK STATUS (REMOVE PROOFS) ---------- */
        $conn->query("
            DELETE FROM proofs 
            WHERE task_id = (
                SELECT id FROM room_tasks 
                WHERE room_id = '$room_id' AND task_no = '$task_no'
                LIMIT 1
            )
        ");
    }

    /* ---------- INSERT NOTIFICATION ---------- */
    $message = $conn->real_escape_string(
        "$creator_name updated the project schedule"
    );

    $conn->query("
        INSERT INTO notifications (room_id, user_id, message, created_at)
        VALUES ('$room_id', '$creator_id', '$message', NOW())
    ");

    $conn->commit();

    echo json_encode([
        "status" => "success",
        "message" => "Schedule updated successfully"
    ]);

} catch (Exception $e) {

    $conn->rollback();

    echo json_encode([
        "status" => "error",
        "message" => "Schedule update failed"
    ]);
}
?>
