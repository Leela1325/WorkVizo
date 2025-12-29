<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$user_id = $_GET['user_id'] ?? '';

if (empty($user_id)) {
    echo json_encode(["status" => "error", "message" => "user_id is required"]);
    exit;
}

// Fetch rooms user joined
$sql = "
SELECT r.id, r.room_code, r.name, r.description, r.start_date, r.end_date,  r.room_status, 
       r.schedule_type, r.room_type, r.number_of_people, r.created_by,
       (SELECT COUNT(*) FROM joined_rooms WHERE room_id = r.id) AS total_members
FROM rooms r
JOIN joined_rooms j ON r.id = j.room_id
WHERE j.user_id = '$user_id'
";

$result = $conn->query($sql);

$rooms = [];

while ($row = $result->fetch_assoc()) {
    $rooms[] = $row;
}

echo json_encode([
    "status" => "success",
    "rooms" => $rooms
]);
?>
