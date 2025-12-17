<?php
header("Content-Type: application/json");
include '../../config/db.php';

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

// 1. Empty check
if (empty($email) || empty($password)) {
    echo json_encode([
        "status" => "error",
        "message" => "Email and password are required"
    ]);
    exit;
}

// 2. Email format
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid email format"
    ]);
    exit;
}

// 3. Check user
$sql = "SELECT id, name, email, dob, password FROM users WHERE email='$email' LIMIT 1";
$result = $conn->query($sql);

if ($result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Email not registered"
    ]);
    exit;
}

$user = $result->fetch_assoc();

// 4. Plain password check
if ($password !== $user['password']) {
    echo json_encode([
        "status" => "error",
        "message" => "Incorrect password"
    ]);
    exit;
}

// 5. Success
echo json_encode([
    "status" => "success",
    "message" => "Login successful",
    "user" => [
        "id" => $user['id'],
        "name" => $user['name'],
        "email" => $user['email'],
        "dob" => $user['dob']
    ]
]);
