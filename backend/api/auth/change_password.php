<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$user_id = $_POST['user_id'] ?? '';
$old_password = $_POST['old_password'] ?? '';
$new_password = $_POST['new_password'] ?? '';

// Validate input
if (empty($user_id) || empty($old_password) || empty($new_password)) {
    echo json_encode(["status" => "error", "message" => "All fields are required"]);
    exit;
}

// Fetch user
$query = $conn->query("SELECT password FROM users WHERE id='$user_id'");

if ($query->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "User not found"]);
    exit;
}

$user = $query->fetch_assoc();
$stored_hash = $user['password'];

// Verify old password
if (!password_verify($old_password, $stored_hash)) {
    echo json_encode(["status" => "error", "message" => "Old password is incorrect"]);
    exit;
}

// Hash new password
$new_hash = password_hash($new_password, PASSWORD_DEFAULT);

// Update password
$update = $conn->query("UPDATE users SET password='$new_hash' WHERE id='$user_id'");

if ($update) {
    echo json_encode([
        "status" => "success",
        "message" => "Password updated successfully"
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to update password"]);
}
?>
