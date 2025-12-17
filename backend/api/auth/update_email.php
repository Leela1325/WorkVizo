<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$user_id = $_POST['user_id'] ?? '';
$new_email = $_POST['new_email'] ?? '';
$password = $_POST['password'] ?? '';

// Validate required fields
if (empty($user_id) || empty($new_email) || empty($password)) {
    echo json_encode(["status" => "error", "message" => "user_id, new_email and password are required"]);
    exit;
}

// Check if email already exists for another account
$checkEmail = $conn->query("SELECT id FROM users WHERE email='$new_email' AND id != '$user_id'");
if ($checkEmail->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Email already taken"]);
    exit;
}

// Fetch user data including password
$userQuery = $conn->query("SELECT password FROM users WHERE id='$user_id'");
if ($userQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "User not found"]);
    exit;
}

$user = $userQuery->fetch_assoc();
$stored_hash = $user['password'];

// Verify user's password to allow email change
if (!password_verify($password, $stored_hash)) {
    echo json_encode(["status" => "error", "message" => "Incorrect password"]);
    exit;
}

// Update email
$update = $conn->query("UPDATE users SET email='$new_email' WHERE id='$user_id'");

if ($update) {
    echo json_encode([
        "status" => "success",
        "message" => "Email updated successfully",
        "new_email" => $new_email
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to update email"]);
}
?>
