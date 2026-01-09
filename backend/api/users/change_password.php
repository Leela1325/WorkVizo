<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

$user_id      = trim($_POST['user_id'] ?? '');
$old_password = trim($_POST['old_password'] ?? '');
$new_password = trim($_POST['new_password'] ?? '');

/* -------- VALIDATION -------- */
if ($user_id === '' || $old_password === '' || $new_password === '') {
    echo json_encode([
        "status" => "error",
        "message" => "user_id, old_password and new_password are required"
    ]);
    exit;
}

/* -------- FETCH USER -------- */
$stmt = $conn->prepare(
    "SELECT password FROM users WHERE id = ?"
);
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

/* -------- VERIFY HASHED OLD PASSWORD -------- */
if (!password_verify($old_password, $user['password'])) {
    echo json_encode([
        "status" => "error",
        "message" => "Incorrect old password"
    ]);
    exit;
}

/* -------- HASH NEW PASSWORD -------- */
$hashed_new_password = password_hash($new_password, PASSWORD_DEFAULT);

/* -------- UPDATE PASSWORD -------- */
$update = $conn->prepare(
    "UPDATE users SET password = ? WHERE id = ?"
);
$update->bind_param("si", $hashed_new_password, $user_id);

if ($update->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Password changed successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to change password"
    ]);
}

$update->close();
$conn->close();
exit;
