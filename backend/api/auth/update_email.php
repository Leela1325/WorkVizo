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

/* Check email already taken */
$check = $conn->query(
    "SELECT id FROM users WHERE email='$new_email' AND email!='$email'"
);
if ($check->num_rows > 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Email already taken"
    ]);
    exit;
}

/* Fetch user */
$userQuery = $conn->query(
    "SELECT password FROM users WHERE email='$email'"
);

if ($userQuery->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "User not found"
    ]);
    exit;
}

$user = $userQuery->fetch_assoc();

/* Plain password check (matches your login logic) */
if ($password !== $user['password']) {
    echo json_encode([
        "status" => "error",
        "message" => "Incorrect password"
    ]);
    exit;
}

/* Update email */
$update = $conn->query(
    "UPDATE users SET email='$new_email' WHERE email='$email'"
);

if ($update) {
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
