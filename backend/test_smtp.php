<?php
use PHPMailer\PHPMailer\PHPMailer;

require_once __DIR__ . "/vendor/PHPMailer/src/PHPMailer.php";
require_once __DIR__ . "/vendor/PHPMailer/src/SMTP.php";
require_once __DIR__ . "/vendor/PHPMailer/src/Exception.php";
require_once __DIR__ . "/.env.php";

$mail = new PHPMailer(true);

try {
    $mail->isSMTP();
    $mail->Host = 'smtp.gmail.com';
    $mail->SMTPAuth = true;
    $mail->Username = SMTP_EMAIL;
    $mail->Password = SMTP_APP_PASSWORD;
    $mail->SMTPSecure = 'tls';
    $mail->Port = 587;

    $mail->setFrom(SMTP_EMAIL, 'Test');
    $mail->addAddress(SMTP_EMAIL);

    $mail->Subject = 'SMTP Test';
    $mail->Body = 'SMTP is working';

    $mail->send();
    echo "SMTP SUCCESS";

} catch (Exception $e) {
    echo "SMTP ERROR: " . $mail->ErrorInfo;
}
