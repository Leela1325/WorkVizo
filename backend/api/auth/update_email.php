<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$email     = trim($_POST['email'] ?? '');
$new_email = trim($_POST['new_email'] ?? '');
$password  = trim($_POST['password'] ?? '');

if ($email === '' || $new_email === '' || $password === '') {
    echo json_encode([
        "status" => "error",
        "message" => "email, new_email and password are required"
    ]);
    exit;
}

/* Check if new email already exists (except current user) */
$stmt = $conn->prepare(
    "SELECT id FROM users WHERE email = ? AND email != ?"
);
$stmt->bind_param("ss", $new_email, $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Email already taken"
    ]);
    exit;
}

/* Fetch user with hashed password */
$stmt = $conn->prepare(
    "SELECT password FROM users WHERE email = ?"
);
$stmt->bind_param("s", $email);
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

/* Verify hashed password */
if (!password_verify($password, $user['password'])) {
    echo json_encode([
        "status" => "error",
        "message" => "Incorrect password"
    ]);
    exit;
}

/* Update email */
$stmt = $conn->prepare(
    "UPDATE users SET email = ? WHERE email = ?"
);
$stmt->bind_param("ss", $new_email, $email);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Email updated successfully",
        "new_email" => $new_email
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to update email"
    ]);
}
