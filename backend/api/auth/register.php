<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

/* ---------- READ INPUT (FORM URL ENCODED) ---------- */
$name     = trim($_POST['name'] ?? '');
$email    = trim($_POST['email'] ?? '');
$dob      = trim($_POST['dob'] ?? '');
$password = trim($_POST['password'] ?? '');

/*
🔥 IMPORTANT FIX
Android sends: createpassword
Postman sends: confirm_password
We accept BOTH
*/
$confirm_password = trim(
    $_POST['confirm_password']
    ?? $_POST['createpassword']
    ?? ''
);

/* ---------- VALIDATION ---------- */
if ($name === '' || $email === '' || $dob === '' || $password === '' || $confirm_password === '') {
    echo json_encode([
        "status" => "error",
        "message" => "All fields are required"
    ]);
    exit;
}

if ($password !== $confirm_password) {
    echo json_encode([
        "status" => "error",
        "message" => "Passwords do not match"
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

/* ---------- CHECK EMAIL EXISTS ---------- */
$check = $conn->prepare("SELECT id FROM users WHERE email = ?");
$check->bind_param("s", $email);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Email already exists"
    ]);
    exit;
}
$check->close();

/* ---------- INSERT USER ---------- */
$stmt = $conn->prepare("
    INSERT INTO users (name, email, dob, password, confirm_password)
    VALUES (?, ?, ?, ?, ?)
");

$stmt->bind_param(
    "sssss",
    $name,
    $email,
    $dob,
    $password,
    $confirm_password
);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "User registered successfully",
        "user" => [
            "id" => $stmt->insert_id,
            "name" => $name,
            "email" => $email
        ]
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Registration failed",
        "sql_error" => $stmt->error
    ]);
}

$stmt->close();
$conn->close();
exit;
