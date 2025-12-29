<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

$user_id   = $_POST['user_id'] ?? '';
$old_email = $_POST['old_email'] ?? '';
$password  = $_POST['password'] ?? '';
$new_email = $_POST['new_email'] ?? '';

if (!$user_id || !$old_email || !$password || !$new_email) {
    echo json_encode([
        "status" => "error",
        "message" => "All fields are required"
    ]);
    exit;
}

/* ---------- FETCH USER ---------- */
$sql = "SELECT email, password FROM users WHERE id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "User not found"
    ]);
    exit;
}

$user = $result->fetch_assoc();

/* ---------- VALIDATIONS ---------- */
if ($user['email'] !== $old_email) {
    echo json_encode([
        "status" => "error",
        "message" => "Old email does not match"
    ]);
    exit;
}

if ($user['password'] !== $password) {
    echo json_encode([
        "status" => "error",
        "message" => "Incorrect password"
    ]);
    exit;
}

/* ---------- UPDATE EMAIL ---------- */
$update = "UPDATE users SET email = ? WHERE id = ?";
$stmt = $conn->prepare($update);
$stmt->bind_param("si", $new_email, $user_id);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Email updated successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to update email"
    ]);
}
