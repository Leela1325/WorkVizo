<?php

// Global function to insert activity logs
function logActivity($conn, $room_id, $user_id, $action_type, $description)
{
    // Escape values for safety
    $room_id = $conn->real_escape_string($room_id);
    $user_id = $conn->real_escape_string($user_id);
    $action_type = $conn->real_escape_string($action_type);
    $description = $conn->real_escape_string($description);

    // Insert into DB
    $conn->query("
        INSERT INTO activity_logs (room_id, user_id, action_type, description)
        VALUES ('$room_id', '$user_id', '$action_type', '$description')
    ");
}

?>
