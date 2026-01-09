<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config/db.php";

$email = trim($_POST['email'] ?? '');
$password = trim($_POST['password'] ?? '');
$loginType = trim($_POST['login_type'] ?? 'password');

if ($email === '') {
    echo json_encode(["status"=>"error","message"=>"Email required"]);
    exit;
}

$stmt = $conn->prepare(
    "SELECT id, name, email, dob, password FROM users WHERE email=? LIMIT 1"
);
$stmt->bind_param("s", $email);
$stmt->execute();
$res = $stmt->get_result();

if ($res->num_rows === 0) {
    echo json_encode(["status"=>"error","message"=>"Account not exists in backend"]);
    exit;
}

$user = $res->fetch_assoc();

/* -------- GOOGLE LOGIN -------- */
if ($loginType === "google") {

    echo json_encode([
        "status"=>"success",
        "message"=>"Google login success",
        "user"=>[
            "id"=>$user['id'],
            "name"=>$user['name'],
            "email"=>$user['email'],
            "dob"=>$user['dob']
        ]
    ]);
    exit;
}

/* -------- PASSWORD LOGIN -------- */

if ($password === '') {
    echo json_encode(["status"=>"error","message"=>"Password required"]);
    exit;
}

$dbPassword = $user['password'];

if (str_starts_with($dbPassword, '$2y$')) {

    if (!password_verify($password, $dbPassword)) {
        echo json_encode(["status"=>"error","message"=>"Incorrect password"]);
        exit;
    }

} else {

    if ($password !== $dbPassword) {
        echo json_encode(["status"=>"error","message"=>"Incorrect password"]);
        exit;
    }

    $newHash = password_hash($password, PASSWORD_DEFAULT);
    $upd = $conn->prepare("UPDATE users SET password=? WHERE id=?");
    $upd->bind_param("si", $newHash, $user['id']);
    $upd->execute();
    $upd->close();
}

echo json_encode([
    "status"=>"success",
    "message"=>"Login successful",
    "user"=>[
        "id"=>$user['id'],
        "name"=>$user['name'],
        "email"=>$user['email'],
        "dob"=>$user['dob']
    ]
]);

$stmt->close();
$conn->close();
