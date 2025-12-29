<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$response = ["success" => false, "message" => ""];

// Input values
$user_id = $_POST['user_id'] ?? '';
$title = $_POST['title'] ?? '';
$message = $_POST['message'] ?? '';
$type = $_POST['type'] ?? 'info';

// Validate
if (empty($user_id) || empty($title) || empty($message)) {
    $response["message"] = "Please provide user_id, title, and message";
    echo json_encode($response);
    exit;
}

$query = "INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, ?)";
$stmt = $conn->prepare($query);

if (!$stmt) {
    $response["message"] = "DB Error: " . $conn->error;
    echo json_encode($response);
    exit;
}

$stmt->bind_param("isss", $user_id, $title, $message, $type);

if ($stmt->execute()) {
    $response["success"] = true;
    $response["message"] = "Notification sent successfully";
    $response["notification_id"] = $stmt->insert_id;
} else {
    $response["message"] = "Failed to send notification";
}

echo json_encode($response);
?>
