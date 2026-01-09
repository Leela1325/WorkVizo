<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

/* ---------- READ INPUT ---------- */
$user_id = intval($_POST['user_id'] ?? 0);
$name    = trim($_POST['name'] ?? '');
$email   = trim($_POST['email'] ?? '');
$dob     = trim($_POST['dob'] ?? '');

/* ---------- VALIDATION ---------- */
if ($user_id <= 0 || $name === '' || $email === '' || $dob === '') {
    echo json_encode([
        "status" => "error",
        "message" => "user_id, name, email and dob are required"
    ]);
    exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid email format"
    ]);
    exit;
}

/* ---------- CHECK USER ---------- */
$userCheck = $conn->prepare("SELECT id FROM users WHERE id = ?");
$userCheck->bind_param("i", $user_id);
$userCheck->execute();
$userCheck->store_result();

if ($userCheck->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "User not found"
    ]);
    exit;
}
$userCheck->close();

/* ---------- EMAIL DUPLICATE CHECK ---------- */
$emailCheck = $conn->prepare(
    "SELECT id FROM users WHERE email = ? AND id != ?"
);
$emailCheck->bind_param("si", $email, $user_id);
$emailCheck->execute();
$emailCheck->store_result();

if ($emailCheck->num_rows > 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Email already in use"
    ]);
    exit;
}
$emailCheck->close();

/* ---------- UPDATE USER ---------- */
$stmt = $conn->prepare(
    "UPDATE users
     SET name = ?, email = ?, dob = ?
     WHERE id = ?"
);
$stmt->bind_param("sssi", $name, $email, $dob, $user_id);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Profile updated successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Update failed"
    ]);
}

$stmt->close();
$conn->close();
exit;
