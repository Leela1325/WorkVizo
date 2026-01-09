<?php
header("Content-Type: application/json");
require_once "../../config.php";

$room_id = intval($_POST['room_id'] ?? 0);
$user_message = trim($_POST['message'] ?? '');

if (!$room_id || !$user_message) {
    echo json_encode(["error" => "Missing room_id or message"]);
    exit;
}

// Fetch project data
$stmt = $conn->prepare("SELECT description, schedule_json FROM rooms WHERE id = ?");
$stmt->bind_param("i", $room_id);
$stmt->execute();
$data = $stmt->get_result()->fetch_assoc();

$context  = $data['description'] ?? 'No description available.';
$schedule = $data['schedule_json'] ?? 'No schedule available.';

// IMPORTANT: LLAMA 3.2 CHAT TEMPLATE
$prompt = "
<|start_header_id|>user<|end_header_id|>

You are the assistant for Project Room #$room_id.

PROJECT:
$context

SCHEDULE:
$schedule

QUESTION:
$user_message

<|eot_id|><|start_header_id|>assistant<|end_header_id|>
";

// Save to file
$promptPath = "C:\\xampp\\htdocs\\workvizo_backend\\api\\rooms\\prompt_chat_$room_id.txt";
file_put_contents($promptPath, $prompt);

// Run llama
$cmd =
"\"C:\\llama\\llama-cli.exe\" ".
"-m \"C:\\llama\\models\\llama32.gguf\" ".
"--file \"$promptPath\" ".
"-n 800 2>&1";

$response = shell_exec($cmd);

// Extract answer AFTER assistant header
$reply = $response;

$start = strpos($reply, "<|start_header_id|>assistant<|end_header_id|>");
if ($start !== false) {
    $reply = substr($reply, $start + strlen("<|start_header_id|>assistant<|end_header_id|>"));
}

// Remove EOF
$end = strpos($reply, "> EOF");
if ($end !== false) {
    $reply = substr($reply, 0, $end);
}

$reply = trim($reply);

echo json_encode(["reply" => $reply]);
?>
