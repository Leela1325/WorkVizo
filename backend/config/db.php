<?php
$servername = "127.0.0.1:3308";
$username   = "root";
$password   = "";
$dbname     = "workvizo_db"; // your DB name

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    echo json_encode([
        "status" => "error",
        "message" => "DB connection failed: " . $conn->connect_error
    ]);
    exit;
}
?>
