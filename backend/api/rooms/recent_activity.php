<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_GET['room_id'] ?? '';

if (empty($room_id)) {
    echo json_encode(["status" => "error", "message" => "room_id is required"]);
    exit;
}

// Fetch last 50 activities
$query = $conn->query("
    SELECT al.*, u.name AS user_name 
    FROM activity_logs al
    JOIN users u ON al.user_id = u.id
    WHERE al.room_id = '$room_id'
    ORDER BY al.created_at DESC
    LIMIT 50
");

$results = [];

while ($row = $query->fetch_assoc()) {
    $results[] = [
        "action_type"   => $row["action_type"],
        "description"   => $row["description"],
        "user_name"     => $row["user_name"],
        "timestamp"     => date("Y-m-d H:i:s", strtotime($row["created_at"])),
        "time_display"  => date("h:i A", strtotime($row["created_at"]))
    ];
}

echo json_encode([
    "status"        => "success",
    "room_id"       => $room_id,
    "activities"    => $results
]);
?>
