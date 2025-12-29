<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_code = $_GET['room_code'] ?? '';

if (empty($room_code)) {
    echo json_encode(["status" => "error", "message" => "room_code is required"]);
    exit;
}

// Convert room_code → room_id
$roomQuery = $conn->query("SELECT id FROM rooms WHERE room_code='$room_code'");
if ($roomQuery->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Invalid room code"]);
    exit;
}

$room_id = $roomQuery->fetch_assoc()['id'];

// Fetch all users in room
$users = $conn->query("SELECT user_id FROM joined_rooms WHERE room_id='$room_id'");

$response = [];

while ($u = $users->fetch_assoc()) {
    $user_id = $u['user_id'];

    // Calculate score
    $score = 0;

    // 1. Proof Approved = +10
    $approved = $conn->query("
        SELECT COUNT(*) as c FROM proofs p 
        JOIN tasks t ON p.task_id = t.id
        WHERE p.user_id='$user_id' AND p.status='approved' AND t.room_id='$room_id'
    ")->fetch_assoc()['c'];

    $score += $approved * 10;

    // 2. Early submission (submitted before due date) = +5
    $early = $conn->query("
        SELECT COUNT(*) as c
        FROM proofs p
        JOIN tasks t ON p.task_id = t.id
        WHERE p.user_id='$user_id'
          AND p.status='approved'
          AND t.room_id='$room_id'
          AND DATE(p.submitted_at) <= t.due_date
    ")->fetch_assoc()['c'];

    $score += $early * 5;

    // 3. Task completed (status = completed) = +5
    $completed = $conn->query("
        SELECT COUNT(*) as c
        FROM tasks
        WHERE assigned_to = '$user_id'
        AND room_id = '$room_id'
        AND status = 'completed'
    ")->fetch_assoc()['c'];

    $score += $completed * 5;

    // Store or update score table
    $check = $conn->query("SELECT id FROM scores WHERE user_id='$user_id' AND room_id='$room_id'");
    if ($check->num_rows > 0) {
        $conn->query("UPDATE scores SET total_score='$score' WHERE user_id='$user_id' AND room_id='$room_id'");
    } else {
        $conn->query("INSERT INTO scores (user_id, room_id, total_score) VALUES ('$user_id', '$room_id', '$score')");
    }

    // Fetch user name
    $userData = $conn->query("SELECT name FROM users WHERE id='$user_id'")->fetch_assoc();

    $response[] = [
        "user_id" => $user_id,
        "user_name" => $userData['name'],
        "score" => $score
    ];
}

echo json_encode([
    "status" => "success",
    "scores" => $response
]);
?>
