<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$response = ["success" => false, "message" => ""];

// Input
$user_id = $_POST['user_id'] ?? '';

if (empty($user_id)) {
    $response["message"] = "Please provide user_id";
    echo json_encode($response);
    exit;
}

$query = "SELECT COUNT(*) AS unread_count FROM notifications WHERE user_id = ? AND is_read = 0";
$stmt = $conn->prepare($query);

if (!$stmt) {
    $response["message"] = "DB Error: " . $conn->error;
    echo json_encode($response);
    exit;
}

$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();
$row = $result->fetch_assoc();

$response["success"] = true;
$response["unread_count"] = $row["unread_count"] ?? 0;

echo json_encode($response);
?>
