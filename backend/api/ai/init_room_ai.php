<?php
session_start();
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = (int)($_POST['room_id'] ?? 0);
$user_id = (int)($_POST['user_id'] ?? 0);

if (!$room_id || !$user_id) {
    echo json_encode(["status" => "error", "message" => "Missing data"]);
    exit;
}

/* ---------- FETCH ROOM ---------- */
$roomQ = $conn->prepare("
    SELECT r.name, r.start_date, r.end_date,
           u.name AS creator_name, u.email AS creator_email
    FROM rooms r
    JOIN users u ON u.id = r.created_by
    WHERE r.id = ?
");
$roomQ->bind_param("i", $room_id);
$roomQ->execute();
$room = $roomQ->get_result()->fetch_assoc();

/* ---------- FETCH USER ---------- */
$uQ = $conn->prepare("SELECT name, email FROM users WHERE id=?");
$uQ->bind_param("i", $user_id);
$uQ->execute();
$user = $uQ->get_result()->fetch_assoc();

/* ---------- FETCH MEMBERS (WITH joined_at) ---------- */
$members = [];
$mQ = $conn->prepare("
    SELECT u.name, u.email, j.role, j.joined_at
    FROM joined_rooms j
    JOIN users u ON u.id = j.user_id
    WHERE j.room_id=?
    ORDER BY j.joined_at DESC
");
$mQ->bind_param("i", $room_id);
$mQ->execute();
$mRes = $mQ->get_result();
while ($m = $mRes->fetch_assoc()) {
    $members[] = $m;
}

/* ---------- FETCH TASKS ---------- */
$tasks = [];
$taskIds = [];

$tQ = $conn->prepare("
    SELECT id, task_name, start_date, end_date, status, assigned_email
    FROM room_tasks
    WHERE room_id=?
");
$tQ->bind_param("i", $room_id);
$tQ->execute();
$tRes = $tQ->get_result();
while ($t = $tRes->fetch_assoc()) {
    $taskIds[] = $t['id'];
    $tasks[] = $t;
}

/* ---------- FETCH PROOFS ---------- */
$proofs = [];
if ($taskIds) {
    $ids = implode(',', $taskIds);
    $pQ = $conn->query("SELECT task_id, status FROM proofs WHERE task_id IN ($ids)");
    while ($p = $pQ->fetch_assoc()) {
        $proofs[] = $p;
    }
}

/* ---------- STORE SESSION ---------- */
$_SESSION['ai_room_context'] = [
    "room"    => $room,
    "user"    => $user,
    "members" => $members,
    "tasks"   => $tasks,
    "proofs"  => $proofs
];

echo json_encode([
    "status" => "success",
    "message" => "AI session initialized"
]);
exit;
