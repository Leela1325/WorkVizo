<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

/* -------- INPUT -------- */
$user_id  = trim($_POST['user_id'] ?? '');
$password = trim($_POST['password'] ?? '');
$dob_raw  = trim($_POST['dob'] ?? '');

/* -------- VALIDATION -------- */
if ($user_id === '' || $password === '' || $dob_raw === '') {
    echo json_encode([
        "status" => "error",
        "message" => "user_id, password and dob are required"
    ]);
    exit;
}

/* -------- DOB NORMALIZE -------- */
$date = DateTime::createFromFormat('Y-m-d', $dob_raw);
if (!$date || $date->format('Y-m-d') !== $dob_raw) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid DOB format (use YYYY-MM-DD)"
    ]);
    exit;
}
$dob = $date->format('Y-m-d');

/* -------- FETCH USER -------- */
$stmt = $conn->prepare("SELECT password FROM users WHERE id = ?");
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

/* -------- VERIFY HASHED PASSWORD -------- */
if (!password_verify($password, $user['password'])) {
    echo json_encode([
        "status" => "error",
        "message" => "Incorrect password"
    ]);
    exit;
}

/* -------- UPDATE DOB -------- */
$update = $conn->prepare(
    "UPDATE users SET dob = ? WHERE id = ?"
);
$update->bind_param("si", $dob, $user_id);
$update->execute();

/* -------- CHECK AFFECTED ROWS -------- */
if ($update->affected_rows > 0) {
    echo json_encode([
        "status" => "success",
        "message" => "DOB updated successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "DOB not changed (same value or update failed)"
    ]);
}

$update->close();
$conn->close();
exit;
