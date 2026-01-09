<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

$user_id = $_POST['user_id'] ?? '';
$feedback = trim($_POST['feedback'] ?? '');

if (!$user_id || !$feedback) {
    echo json_encode([
        "status" => "error",
        "message" => "Feedback cannot be empty"
    ]);
    exit;
}

$query = "INSERT INTO feedback (user_id, feedback_text) VALUES (?, ?)";
$stmt = $conn->prepare($query);
$stmt->bind_param("is", $user_id, $feedback);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Feedback submitted successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to submit feedback"
    ]);
}
