<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$user_id = $_GET['user_id'] ?? '';

if (!$user_id) {
    echo json_encode(["status" => "error", "message" => "user_id required"]);
    exit;
}

$query = $conn->query("
    SELECT *
    FROM rooms
    WHERE created_by = '$user_id'
    ORDER BY created_at DESC
");

$rooms = [];
while ($row = $query->fetch_assoc()) {
    $rooms[] = $row;
}

echo json_encode([
    "status" => "success",
    "created_rooms" => $rooms
]);
