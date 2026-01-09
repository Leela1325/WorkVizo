<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_GET['room_id'] ?? '';

if (empty($room_id)) {
    echo json_encode(["status" => "error", "message" => "room_id is required"]);
    exit;
}

// Join users with joined_rooms table
$sql = "
SELECT u.id, u.name, u.email
FROM users u
JOIN joined_rooms j ON u.id = j.user_id
JOIN rooms r ON r.id = j.room_id
WHERE j.room_id = '$room_id'
AND u.id != r.created_by
";


$result = $conn->query($sql);

$members = [];

while ($row = $result->fetch_assoc()) {
    $members[] = $row;
}

// Response
echo json_encode([
    "status" => "success",
    "members" => $members
]);
?>
