<?php
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";
require_once __DIR__ . "/../../.env.php";

require_once __DIR__ . "/../../vendor/PHPMailer/src/Exception.php";
require_once __DIR__ . "/../../vendor/PHPMailer/src/PHPMailer.php";
require_once __DIR__ . "/../../vendor/PHPMailer/src/SMTP.php";
date_default_timezone_set("Asia/Kolkata");

$email = trim($_POST['email'] ?? '');

if ($email === '') {
    echo json_encode(["status" => "error", "message" => "Email is required"]);
    exit;
}

/* ---------- CHECK USER ---------- */
$stmt = $conn->prepare("SELECT id, name FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$user = $stmt->get_result()->fetch_assoc();

if (!$user) {
    echo json_encode(["status" => "error", "message" => "Email not found"]);
    exit;
}

$user_id = $user['id'];
$name = $user['name'];

/* ---------- REMOVE OLD OTPs ---------- */
$del = $conn->prepare("DELETE FROM password_resets WHERE email = ?");
$del->bind_param("s", $email);
$del->execute();

/* ---------- CREATE OTP ---------- */
$otp = random_int(100000, 999999);
$expires_at = date("Y-m-d H:i:s", strtotime("+10 minutes"));

$ins = $conn->prepare(
    "INSERT INTO password_resets (user_id, email, otp, expires_at)
     VALUES (?, ?, ?, ?)"
);
$ins->bind_param("isis", $user_id, $email, $otp, $expires_at);
$ins->execute();

/* ---------- SEND MAIL ---------- */
$mail = new PHPMailer(true);

try {
    $mail->isSMTP();
    $mail->Host = 'smtp.gmail.com';
    $mail->SMTPAuth = true;
    $mail->Username = SMTP_EMAIL;
    $mail->Password = SMTP_APP_PASSWORD;
    $mail->SMTPSecure = 'tls';
    $mail->Port = 587;

    $mail->setFrom(SMTP_EMAIL, 'WorkVizo');
    $mail->addAddress($email, $name);
    $mail->isHTML(true);
    $mail->Subject = 'WorkVizo - Password Reset OTP';

    $mail->Body = "
        <h2>WorkVizo Password Reset</h2>
        <p>Hello <b>$name</b>,</p>
        <h1>$otp</h1>
        <p>Valid for 10 minutes</p>
    ";

    $mail->send();

    echo json_encode([
        "status" => "success",
        "message" => "OTP sent"
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Mail failed"
    ]);
}
