<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$user_id = $_GET['user_id'] ?? '';

if (!$user_id) {
    echo json_encode([
        "status" => "error",
        "message" => "user_id required"
    ]);
    exit;
}

$sql = "
    SELECT 
        r.id,
        r.name,
        r.description,
        r.room_code,
        r.start_date,
        r.end_date,
        r.room_status,
        u.name AS creator_name
    FROM rooms r
    JOIN users u ON u.id = r.created_by
    WHERE r.created_by = ?
    ORDER BY r.created_at DESC
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$rooms = [];
while ($row = $result->fetch_assoc()) {
    $rooms[] = $row;
}

echo json_encode([
    "status" => "success",
    "createdRooms" => $rooms   // 👈 MATCH ANDROID MODEL
]);
