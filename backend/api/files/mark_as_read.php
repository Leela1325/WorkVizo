<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$response = ["success" => false, "message" => ""];

// Input
$notification_id = $_POST['notification_id'] ?? '';

if (empty($notification_id)) {
    $response["message"] = "Please provide notification_id";
    echo json_encode($response);
    exit;
}

$query = "UPDATE notifications SET is_read = 1 WHERE id = ?";
$stmt = $conn->prepare($query);

if (!$stmt) {
    $response["message"] = "DB Error: " . $conn->error;
    echo json_encode($response);
    exit;
}

$stmt->bind_param("i", $notification_id);

if ($stmt->execute()) {
    $response["success"] = true;
    $response["message"] = "Notification marked as read";
} else {
    $response["message"] = "Failed to update notification";
}

echo json_encode($response);
?>
