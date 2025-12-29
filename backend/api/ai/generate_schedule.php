<?php
header("Content-Type: application/json");

// CONNECT TO DATABASE
require_once "../../config.php";

$room_id = $_POST['room_id'] ?? '';
$desc    = $_POST['description'] ?? '';
$start   = $_POST['start_date'] ?? '';
$end     = $_POST['end_date'] ?? '';
$people  = $_POST['people_count'] ?? 1;

// Validate
if (!$room_id || !$desc || !$start || !$end) {
    echo json_encode(["error" => "Missing required fields"]);
    exit;
}

// AI Prompt
$prompt = "
Generate a project schedule in STRICT JSON.

DESCRIPTION: $desc
START: $start
END: $end
PEOPLE: $people

Only output JSON.

Example:
{
  \"tasks\": [
     {\"task\":\"...\", \"start\":\"YYYY-MM-DD\", \"end\":\"YYYY-MM-DD\", \"assigned_to\":\"Person 1\"}
  ]
}

Give final schedule now:
";

// Save prompt to file
$promptPath = "C:\\xampp\\htdocs\\workvizo_backend\\api\\ai\\prompt.txt";
file_put_contents($promptPath, $prompt);

// Run llama
$cmd =
"\"C:\\llama\\llama-cli.exe\" ".
"-m \"C:\\llama\\models\\llama32.gguf\" ".
"--file \"$promptPath\" ".
"-n 800";

$response = shell_exec($cmd);

// Clean markdown ```
$clean = preg_replace('/```(json)?/i', '', $response);
$clean = str_replace('```', '', $clean);

// Extract LAST JSON block
preg_match_all('/\{(?:[^{}]|(?R))*\}/s', $clean, $allMatches);
$json = !empty($allMatches[0]) ? end($allMatches[0]) : null;

// SAVE SCHEDULE JSON TO DB
$stmt = $conn->prepare("UPDATE rooms SET schedule_json = ? WHERE id = ?");
$stmt->bind_param("si", $json, $room_id);
$stmt->execute();

// Return schedule
echo json_encode(["schedule" => $json]);
?>
