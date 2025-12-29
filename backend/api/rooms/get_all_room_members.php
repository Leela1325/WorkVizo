<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id = $_GET['room_id'] ?? '';

if (!$room_id) {
    echo json_encode([
        "status" => "error",
        "message" => "Room ID missing"
    ]);
    exit;
}

/* -------------------------------------------------
   FETCH CREATOR
------------------------------------------------- */
$creatorQ = $conn->prepare("
    SELECT 
        u.id,
        u.name,
        u.email,
        NULL AS assigned_task,
        r.created_at AS joined_at
    FROM rooms r
    JOIN users u ON u.id = r.created_by
    WHERE r.id = ?
");
$creatorQ->bind_param("i", $room_id);
$creatorQ->execute();
$creator = $creatorQ->get_result()->fetch_assoc();

$creator_id = $creator['id']; // ✅ IMPORTANT

/* -------------------------------------------------
   FETCH MEMBERS (EXCLUDING CREATOR)
------------------------------------------------- */
$membersQ = $conn->prepare("
    SELECT 
        u.id,
        u.name,
        u.email,
        rt.task_name AS assigned_task,
        jr.joined_at
    FROM joined_rooms jr
    JOIN users u 
        ON u.id = jr.user_id
    LEFT JOIN room_tasks rt 
        ON rt.room_id = jr.room_id
       AND rt.assigned_email = u.email
    WHERE jr.room_id = ?
      AND jr.user_id != ?
");
$membersQ->bind_param("ii", $room_id, $creator_id);
$membersQ->execute();

$members = [];
$res = $membersQ->get_result();
while ($row = $res->fetch_assoc()) {
    $members[] = $row;
}

/* -------------------------------------------------
   RESPONSE
------------------------------------------------- */
echo json_encode([
    "status" => "success",
    "creator" => $creator,
    "members" => $members
]);
exit;
