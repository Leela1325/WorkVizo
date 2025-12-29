<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$email    = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

if (empty($email) || empty($password)) {
    echo json_encode(["status" => "error", "message" => "All fields required"]);
    exit;
}

// ⚠️ PLAIN PASSWORD (as per your requirement)
$conn->query("UPDATE users SET password='$password' WHERE email='$email'");

// Cleanup OTPs
$conn->query("DELETE FROM password_resets WHERE email='$email'");

echo json_encode([
    "status" => "success",
    "message" => "Password reset successful"
]);
