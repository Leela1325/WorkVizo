<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- READ INPUT (JSON OR FORM) ---------- */
$raw = file_get_contents("php://input");
$input = json_decode($raw, true);

$name             = $input['name'] ?? $_POST['name'] ?? '';
$description      = $input['description'] ?? $_POST['description'] ?? '';
$start_date       = $input['start_date'] ?? $_POST['start_date'] ?? '';
$end_date         = $input['end_date'] ?? $_POST['end_date'] ?? '';
$schedule_type    = $input['schedule_type'] ?? $_POST['schedule_type'] ?? 'manual';
$room_type        = $input['room_type'] ?? $_POST['room_type'] ?? 'manual';
$number_of_people = $input['number_of_people'] ?? $_POST['number_of_people'] ?? 1;
$room_password    = $input['room_password'] ?? $_POST['room_password'] ?? '';
$created_by       = $input['created_by'] ?? $_POST['created_by'] ?? '';

/* ---------- VALIDATION ---------- */
if (
    empty($name) ||
    empty($description) ||
    empty($start_date) ||
    empty($end_date) ||
    empty($created_by)
) {
    echo json_encode([
        "status" => "error",
        "message" => "All required fields must be filled",
        "debug" => compact(
            "name",
            "description",
            "start_date",
            "end_date",
            "created_by"
        )
    ]);
    exit;
}

/* ---------- DATE CHECK ---------- */
if (strtotime($start_date) > strtotime($end_date)) {
    echo json_encode([
        "status" => "error",
        "message" => "End date must be after start date"
    ]);
    exit;
}

/* ---------- PASSWORD (HASH FOR DB) ---------- */
$hashed_password = !empty($room_password)
    ? password_hash($room_password, PASSWORD_DEFAULT)
    : null;

/* ---------- ROOM CODE ---------- */
$room_code = str_pad(rand(0, 999999), 6, '0', STR_PAD_LEFT);

/* ---------- TRANSACTION START ---------- */
$conn->begin_transaction();

try {

    /* ---------- INSERT ROOM ---------- */
    $stmt = $conn->prepare("
        INSERT INTO rooms
        (room_code, name, description, start_date, end_date, schedule_type, room_type, number_of_people, room_password, created_by)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ");

    $stmt->bind_param(
        "sssssssiss",
        $room_code,
        $name,
        $description,
        $start_date,
        $end_date,
        $schedule_type,
        $room_type,
        $number_of_people,
        $hashed_password,
        $created_by
    );

    if (!$stmt->execute()) {
        throw new Exception("Room insert failed: " . $stmt->error);
    }

    $room_id = $stmt->insert_id;

    /* ---------- AUTO JOIN CREATOR ---------- */
    $join = $conn->prepare("
        INSERT INTO joined_rooms (room_id, user_id, role)
        VALUES (?, ?, 'creator')
    ");
    $join->bind_param("ii", $room_id, $created_by);

    if (!$join->execute()) {
        throw new Exception("Join insert failed: " . $join->error);
    }

    /* ---------- COMMIT ---------- */
    $conn->commit();

    /* ---------- RESPONSE ---------- */
    echo json_encode([
        "status" => "success",
        "room_id" => $room_id,
        "room_code" => $room_code,
        "message" => "Room created successfully"
    ]);
    exit;

} catch (Exception $e) {

    /* ---------- ROLLBACK ---------- */
    $conn->rollback();

    echo json_encode([
        "status" => "error",
        "message" => "Room creation failed",
        "error" => $e->getMessage()
    ]);
    exit;
}
