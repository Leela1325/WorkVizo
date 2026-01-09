<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$user_id = (int)($_GET['user_id'] ?? 0);

if ($user_id <= 0) {
    echo json_encode([
        "status" => "error",
        "message" => "user_id required"
    ]);
    exit;
}

/*
 LOGIC:
 1) Creator: see all notifications of their rooms
 2) Member: see notifications only AFTER joined_at
*/

$sql = "
SELECT
    n.id,
    n.message,
    n.created_at,
    u.name AS actor_name
FROM notifications n
JOIN users u 
    ON u.id = n.user_id

LEFT JOIN joined_rooms jr
    ON jr.room_id = n.room_id
   AND jr.user_id = ?

LEFT JOIN rooms r
    ON r.id = n.room_id

WHERE
(
    /* Creator: full history */
    r.created_by = ?

    OR

    /* Member: only after joining */
    (
        jr.user_id = ?
        AND n.created_at >= jr.joined_at
    )
)

ORDER BY n.created_at DESC
LIMIT 50
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("iii", $user_id, $user_id, $user_id);
$stmt->execute();
$res = $stmt->get_result();

$list = [];
while ($row = $res->fetch_assoc()) {
    $list[] = [
        "message"    => $row['message'],
        "created_at" => $row['created_at'],
        "actor_name" => $row['actor_name']
    ];
}

echo json_encode([
    "status" => "success",
    "notifications" => $list
]);
exit;
