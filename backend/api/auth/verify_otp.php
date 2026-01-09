<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";
date_default_timezone_set("Asia/Kolkata");

$email = trim($_POST['email'] ?? '');
$otp   = intval($_POST['otp'] ?? 0);

if ($email === '' || $otp === 0) {
    echo json_encode(["status" => "error", "message" => "Email and OTP required"]);
    exit;
}

/* ---------- VERIFY LATEST OTP ---------- */
$stmt = $conn->prepare(
    "SELECT id FROM password_resets
     WHERE email = ?
     AND otp = ?
     AND expires_at > NOW()
     ORDER BY id DESC
     LIMIT 1"
);

$stmt->bind_param("si", $email, $otp);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid or expired OTP"
    ]);
    exit;
}

/* ---------- SUCCESS ---------- */
echo json_encode([
    "status" => "success",
    "message" => "OTP verified"
]);
