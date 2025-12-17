<?php
ini_set('display_errors', 0);
error_reporting(0);

header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

// -------------------------------
// READ POST DATA
// -------------------------------
$name           = trim($_POST['name'] ?? "");
$email          = trim($_POST['email'] ?? "");
$dob            = trim($_POST['dob'] ?? "");
$password       = trim($_POST['password'] ?? "");
$createpassword = trim($_POST['createpassword'] ?? "");

// -------------------------------
// NO POST RECEIVED
// -------------------------------
if (empty($_POST)) {
    echo json_encode([
        "status" => "error",
        "message" => "NO POST RECEIVED"
    ]);
    exit;
}

// -------------------------------
// VALIDATION
// -------------------------------
if ($name === "" || $email === "" || $dob === "" || $password === "" || $createpassword === "") {
    echo json_encode([
        "status" => "error",
        "message" => "All fields are required"
    ]);
    exit;
}

if ($password !== $createpassword) {
    echo json_encode([
        "status" => "error",
        "message" => "Passwords do not match"
    ]);
    exit;
}

// -------------------------------
// CHECK IF EMAIL ALREADY EXISTS
// (THIS IS THE KEY FIX)
// -------------------------------
$check = $conn->prepare("SELECT id FROM users WHERE email = ?");
if (!$check) {
    echo json_encode([
        "status" => "error",
        "message" => "Email check failed"
    ]);
    exit;
}

$check->bind_param("s", $email);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Email already exists"
    ]);
    $check->close();
    $conn->close();
    exit;
}
$check->close();

// -------------------------------
// INSERT USER
// -------------------------------
$sql = "INSERT INTO users (name, email, dob, password) VALUES (?, ?, ?, ?)";
$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode([
        "status" => "error",
        "message" => "SQL PREPARE FAILED"
    ]);
    exit;
}

// ⚠️ NOTE: password hashing can be added later
$stmt->bind_param("ssss", $name, $email, $dob, $password);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "User registered!"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Registration failed",
        "mysql_error" => $stmt->error
    ]);
}

$stmt->close();
$conn->close();
exit;
