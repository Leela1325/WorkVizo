<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$room_id    = $_GET['room_id'] ?? '';
$user_id    = $_GET['user_id'] ?? '';
$is_creator = $_GET['is_creator'] ?? '0';

if (!$room_id || !$user_id) {
    echo json_encode([
        "status" => "error",
        "message" => "room_id and user_id required"
    ]);
    exit;
}

/* ---------- GET USER EMAIL ---------- */
$userQ = $conn->prepare("SELECT email FROM users WHERE id = ? LIMIT 1");
$userQ->bind_param("i", $user_id);
$userQ->execute();
$userRes = $userQ->get_result();

if ($userRes->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid user"
    ]);
    exit;
}

$user_email = $userRes->fetch_assoc()['email'];

/* ==========================================================
   CREATOR → ALL TASKS (UNCHANGED)
   ========================================================== */
if ($is_creator === "1") {

    $sql = "
        SELECT
            rt.id             AS id,
            rt.task_name      AS title,
            ''                AS description,
            COALESCE(p.status, 'pending') AS status,
            rt.assigned_email AS assigned_to,
            1 AS is_assigned_to_me
        FROM room_tasks rt
        LEFT JOIN proofs p
          ON p.id = (
              SELECT p2.id
              FROM proofs p2
              WHERE p2.task_id = rt.id
              ORDER BY p2.id DESC
              LIMIT 1
          )
        WHERE rt.room_id = ?
        ORDER BY rt.id ASC
    ";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $room_id);
}

/* ==========================================================
   MEMBER → ALL TASKS + FLAG IF ASSIGNED
   ========================================================== */
else {

    $sql = "
        SELECT
            rt.id             AS id,
            rt.task_name      AS title,
            ''                AS description,
            COALESCE(p.status, 'pending') AS status,
            rt.assigned_email AS assigned_to,
            CASE 
                WHEN rt.assigned_email = ? THEN 1
                ELSE 0
            END AS is_assigned_to_me
        FROM room_tasks rt
        LEFT JOIN proofs p
          ON p.id = (
              SELECT p2.id
              FROM proofs p2
              WHERE p2.task_id = rt.id
                AND p2.user_id = ?
              ORDER BY p2.id DESC
              LIMIT 1
          )
        WHERE rt.room_id = ?
        ORDER BY rt.id ASC
    ";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sii", $user_email, $user_id, $room_id);
}

/* ---------- EXECUTE ---------- */
$stmt->execute();
$result = $stmt->get_result();

$tasks = [];
while ($row = $result->fetch_assoc()) {
    $tasks[] = [
        "id"          => (string)$row["id"],
        "title"       => $row["title"],
        "description" => "",
        "status"      => $row["status"],
        "assigned_to" => $row["assigned_to"],
        "is_assigned" => (int)$row["is_assigned_to_me"]   // 🔥 NEW FIELD
    ];
}

echo json_encode([
    "status" => "success",
    "tasks"  => $tasks
]);
