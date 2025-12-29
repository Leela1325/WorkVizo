<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- VALIDATION ---------- */
$room_id = $_GET['room_id'] ?? '';

if (!$room_id) {
    echo json_encode([
        "status" => "error",
        "message" => "room_id required"
    ]);
    exit;
}

/* ---------- TOTAL TASKS ---------- */
$totalQ = $conn->prepare("
    SELECT COUNT(*) AS total
    FROM room_tasks
    WHERE room_id = ?
");
$totalQ->bind_param("i", $room_id);
$totalQ->execute();
$totalRes = $totalQ->get_result()->fetch_assoc();

$totalTasks = (int)$totalRes['total'];

if ($totalTasks === 0) {
    echo json_encode([
        "task_progress" => 0,
        "time_progress" => 0,
        "completed_tasks" => 0,
        "total_tasks" => 0,
        "progress_status" => "NO_TASKS"
    ]);
    exit;
}

/* ---------- LATEST STATUS PER TASK ---------- */
$sql = "
    SELECT
        rt.id AS task_id,
        COALESCE(p.status, 'pending') AS status
    FROM room_tasks rt
    LEFT JOIN proofs p
        ON p.id = (
            SELECT p2.id
            FROM proofs p2
            WHERE p2.task_id = rt.id
            ORDER BY p2.id DESC
            LIMIT 1
        )
    WHERE rt.room_id = ?
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $room_id);
$stmt->execute();
$res = $stmt->get_result();

/* ---------- CALCULATE PROGRESS ---------- */
$completedTasks = 0;
$totalScore = 0;

while ($row = $res->fetch_assoc()) {

    switch ($row['status']) {
        case 'completed':
            $completedTasks++;
            $totalScore += 100;
            break;

        case 'in_progress':
            $totalScore += 50;
            break;

        default:
            $totalScore += 0;
    }
}

/* ---------- FINAL PERCENT ---------- */
$taskProgress = round(($totalScore / ($totalTasks * 100)) * 100);

/* ---------- RESPONSE (MATCHES ANDROID MODEL) ---------- */
echo json_encode([
    "task_progress"   => $taskProgress,
    "time_progress"   => 0,
    "completed_tasks" => $completedTasks,
    "total_tasks"     => $totalTasks,
    "progress_status" => ($taskProgress >= 80 ? "ON_TRACK" : "IN_PROGRESS")
]);
