<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_GET['room_id'] ?? '';

if (empty($room_id)) {
    echo json_encode(["status" => "error", "message" => "room_id is required"]);
    exit;
}

// Today's date
$today = date("Y-m-d");

// Fetch today's logs
$query = $conn->query("
    SELECT al.*, u.name AS user_name 
    FROM activity_logs al
    JOIN users u ON al.user_id = u.id
    WHERE al.room_id = '$room_id'
    AND DATE(al.created_at) = '$today'
    ORDER BY al.created_at DESC
");

$logs = [];

while ($row = $query->fetch_assoc()) {
    $logs[] = [
        "action_type" => $row["action_type"],
        "description" => $row["description"],
        "user_name" => $row["user_name"],
        "time" => date("h:i A", strtotime($row["created_at"]))
    ];
}

echo json_encode([
    "status" => "success",
    "room_id" => $room_id,
    "date" => $today,
    "total_changes" => count($logs),
    "changes" => $logs
]);
?>
