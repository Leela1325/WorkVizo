<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

$user_id = trim($_POST['user_id'] ?? '');

/* -------- VALIDATION -------- */
if ($user_id === '') {
    echo json_encode([
        "status" => "error",
        "message" => "user_id is required"
    ]);
    exit;
}

/* -------- DELETE USER -------- */
$stmt = $conn->prepare(
    "DELETE FROM users WHERE id = ?"
);
$stmt->bind_param("i", $user_id);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Profile deleted successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to delete profile"
    ]);
}

$stmt->close();
$conn->close();
exit;
