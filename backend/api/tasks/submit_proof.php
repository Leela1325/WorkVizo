<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* -------------------------------------------------
   BASIC VALIDATION
------------------------------------------------- */
if (
    empty($_POST['task_id']) ||
    empty($_POST['user_id']) ||
    empty($_POST['status'])
) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing required fields"
    ]);
    exit;
}

$task_id = (int) $_POST['task_id'];
$user_id = (int) $_POST['user_id'];
$status  = $_POST['status'];
$description = $_POST['description'] ?? "";

/* -------------------------------------------------
   VERIFY USER
------------------------------------------------- */
$userQ = $conn->prepare("SELECT name, email FROM users WHERE id = ?");
$userQ->bind_param("i", $user_id);
$userQ->execute();
$userRes = $userQ->get_result();

if ($userRes->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "User not found"
    ]);
    exit;
}

$userRow   = $userRes->fetch_assoc();
$userName  = $userRow['name'];
$userEmail = $userRow['email'];

/* -------------------------------------------------
   VERIFY TASK ASSIGNMENT + GET ROOM INFO
------------------------------------------------- */
$taskQ = $conn->prepare("
    SELECT id, room_id, task_name
    FROM room_tasks
    WHERE id = ? AND assigned_email = ?
    LIMIT 1
");
$taskQ->bind_param("is", $task_id, $userEmail);
$taskQ->execute();
$task = $taskQ->get_result()->fetch_assoc();

if (!$task) {
    echo json_encode([
        "status" => "error",
        "message" => "You are not assigned to this task"
    ]);
    exit;
}

$room_id  = (int)$task['room_id'];
$taskName = $task['task_name'];

/* -------------------------------------------------
   FETCH ROOM NAME
------------------------------------------------- */
$rq = $conn->prepare("SELECT name FROM rooms WHERE id = ?");
$rq->bind_param("i", $room_id);
$rq->execute();
$roomRow  = $rq->get_result()->fetch_assoc();
$roomName = $roomRow ? $roomRow['name'] : 'Unknown Room';

/* -------------------------------------------------
   CHECK EXISTING PROOF
------------------------------------------------- */
$check = $conn->prepare("
    SELECT id, file_path
    FROM proofs
    WHERE task_id = ? AND user_id = ?
    LIMIT 1
");
$check->bind_param("ii", $task_id, $user_id);
$check->execute();
$res = $check->get_result();

/* -------------------------------------------------
   CASE 1: PROOF EXISTS, NO FILE (STATUS UPDATE ONLY)
------------------------------------------------- */
if ($res->num_rows > 0 && empty($_FILES['proof'])) {

    $row = $res->fetch_assoc();
    $proof_id = $row['id'];

    $u = $conn->prepare("
        UPDATE proofs
        SET status = ?, description = ?
        WHERE id = ?
    ");
    $u->bind_param("ssi", $status, $description, $proof_id);
    $u->execute();

}
/* -------------------------------------------------
   CASE 2: FILE UPLOAD REQUIRED
------------------------------------------------- */
else {

    if (!isset($_FILES['proof'])) {
        echo json_encode([
            "status" => "error",
            "message" => "Proof file required"
        ]);
        exit;
    }

    $uploadDir = __DIR__ . "/../../uploads/";
    if (!is_dir($uploadDir)) {
        mkdir($uploadDir, 0777, true);
    }

    $ext = pathinfo($_FILES['proof']['name'], PATHINFO_EXTENSION);
    if (!$ext) $ext = "bin";

    $fileName = "proof_" . time() . "_" . rand(1000,9999) . "." . $ext;
    $fullPath = $uploadDir . $fileName;
    $dbPath   = "uploads/" . $fileName;

    if (!move_uploaded_file($_FILES['proof']['tmp_name'], $fullPath)) {
        echo json_encode([
            "status" => "error",
            "message" => "File upload failed"
        ]);
        exit;
    }

    if ($res->num_rows > 0) {

        // UPDATE EXISTING PROOF WITH NEW FILE
        $row = $res->fetch_assoc();
        $proof_id = $row['id'];

        $u = $conn->prepare("
            UPDATE proofs
            SET file_path = ?, status = ?, description = ?
            WHERE id = ?
        ");
        $u->bind_param("sssi", $dbPath, $status, $description, $proof_id);
        $u->execute();

    } else {

        // FIRST TIME INSERT
        $i = $conn->prepare("
            INSERT INTO proofs (task_id, user_id, file_path, description, status)
            VALUES (?, ?, ?, ?, ?)
        ");
        $i->bind_param(
            "iisss",
            $task_id,
            $user_id,
            $dbPath,
            $description,
            $status
        );
        $i->execute();
    }
}

/* -------------------------------------------------
   INSERT NOTIFICATION (ENRICHED)
------------------------------------------------- */
$message = "$userName submitted proof for task \"$taskName\" "
         . "in room \"$roomName\" "
         . "(status: $status)";

$nq = $conn->prepare("
    INSERT INTO notifications (room_id, user_id, message)
    VALUES (?, ?, ?)
");
$nq->bind_param("iis", $room_id, $user_id, $message);
$nq->execute();

/* -------------------------------------------------
   RESPONSE
------------------------------------------------- */
echo json_encode([
    "status" => "success",
    "message" => "Proof submitted successfully"
]);
exit;
