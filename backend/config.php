<?php
$host = "localhost:3308";  // Important
$user = "root";
$pass = "";
$db   = "workvizo_db";

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "DB Connection failed: " . $conn->connect_error]));
}

header("Content-Type: application/json");
?>
