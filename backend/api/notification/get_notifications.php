<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$response = ["success" => false, "message" => ""];

$user_id = $_POST['user_id'] ?? '';

if (empty($user_id)) {
    $response["message"] = "Please provide user_id";
    echo json_encode($response);
    exit;
}

$query = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
$stmt = $conn->prepare($query);

if (!$stmt) {
    $response["message"] = "DB Error: " . $conn->error;
    echo json_encode($response);
    exit;
}

$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$notifications = [];

while ($row = $result->fetch_assoc()) {
    $notifications[] = $row;
}

$response["success"] = true;
$response["notifications"] = $notifications;

echo json_encode($response);
?>
