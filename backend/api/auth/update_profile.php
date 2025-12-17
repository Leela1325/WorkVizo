<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

$user_id = $_POST['user_id'] ?? '';
$name = $_POST['name'] ?? '';
$email = $_POST['email'] ?? '';
$dob = $_POST['dob'] ?? '';

// Validate required fields
if (empty($user_id)) {
    echo json_encode(["status" => "error", "message" => "user_id is required"]);
    exit;
}

if (empty($name) || empty($email)) {
    echo json_encode(["status" => "error", "message" => "Name and email cannot be empty"]);
    exit;
}

// Check if email already exists for another user
$emailCheck = $conn->query("SELECT id FROM users WHERE email = '$email' AND id != '$user_id'");
if ($emailCheck->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Email already in use"]);
    exit;
}

// Handle profile image upload (optional)
$profile_image_path = "";

// If file exists in request
if (!empty($_FILES['profile_image']['name'])) {

    $uploadDir = "../../uploads/profile_images/";
    if (!file_exists($uploadDir)) {
        mkdir($uploadDir, 0777, true);
    }

    $fileName = time() . "_" . basename($_FILES["profile_image"]["name"]);
    $targetFile = $uploadDir . $fileName;

    if (move_uploaded_file($_FILES["profile_image"]["tmp_name"], $targetFile)) {
        $profile_image_path = "uploads/profile_images/" . $fileName;
    }
}

// Update query
$query = "UPDATE users SET 
            name='$name',
            email='$email',
            dob='$dob'";

if (!empty($profile_image_path)) {
    $query .= ", profile_image='$profile_image_path'";
}

$query .= " WHERE id='$user_id'";

if ($conn->query($query)) {
    echo json_encode([
        "status" => "success",
        "message" => "Profile updated successfully",
        "profile_image" => $profile_image_path
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to update profile"]);
}
?>
