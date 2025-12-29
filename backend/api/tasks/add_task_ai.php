<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

set_time_limit(300); // Prevent timeout

// Inputs
$room_id = $_POST["room_id"] ?? "";
$new_tasks = $_POST["new_tasks"] ?? "";

if (!$room_id || !$new_tasks) {
    echo json_encode(["error" => "room_id and new_tasks required"]);
    exit;
}

// Database connection
$mysqli = new mysqli($host, $user, $pass, $db);
if ($mysqli->connect_errno) {
    echo json_encode(["error" => "DB connection failed"]);
    exit;
}

// Fetch existing schedule
$res = $mysqli->query("SELECT schedule_json FROM rooms WHERE id='$room_id' LIMIT 1");
if (!$res || $res->num_rows == 0) {
    echo json_encode(["error" => "Room not found"]);
    exit;
}

$row = $res->fetch_assoc();
$current_json = $row["schedule_json"] ?: '{"tasks":[]}';

// Build AI prompt
$prompt =
"ONLY OUTPUT JSON.\n".
"CURRENT_SCHEDULE = $current_json\n".
"NEW_TASKS = $new_tasks\n\n".
"Add NEW_TASKS into CURRENT_SCHEDULE.tasks.\n".
"Preserve existing tasks.\n".
"Generate unique ids.\n".
"Output strictly:\n".
"{ \"tasks\": [ ... ] }";

// Ensure /api/ai folder exists
$ai_folder = __DIR__ . "/../../ai";
if (!is_dir($ai_folder)) mkdir($ai_folder, 0777, true);

// Save prompt file
$prompt_file = $ai_folder . "/add_task_prompt.txt";
file_put_contents($prompt_file, $prompt);

// Paths
$llama = "C:\\llama\\llama-cli.exe";
$model = "C:\\llama\\models\\llama32.gguf";

// Run model (FINAL WORKING CMD)
$cmd = "\"$llama\" -m \"$model\" --file \"$prompt_file\" -n 350 --no-chat --no-interactive";

// Execute
$output = shell_exec($cmd);

// Clean output
$output = preg_replace('/\e\[[0-9;]*m/', '', $output);
$output = str_replace(["```json", "```"], "", $output);

// Extract JSON
preg_match_all('/\{[\s\S]*?\}/', $output, $matches);

if (!isset($matches[0]) || empty($matches[0])) {
    echo json_encode(["error" => "Model returned no JSON", "raw" => $output]);
    exit;
}

// Pick largest JSON block
$largest = "";
foreach ($matches[0] as $m) {
    if (strlen($m) > strlen($largest)) $largest = $m;
}

$final_json = trim($largest);

// Decode JSON
$decoded = json_decode($final_json, true);

if (!$decoded || !isset($decoded["tasks"])) {
    echo json_encode([
        "error" => "Invalid JSON from model",
        "raw" => $output,
        "json" => $final_json
    ]);
    exit;
}

// Clean result
$clean_json = json_encode(["tasks" => $decoded["tasks"]]);

// Save history
$mysqli->query("
    INSERT INTO schedule_history (room_id, old_json, new_json, created_at)
    VALUES (
        '$room_id',
        '".$mysqli->real_escape_string($current_json)."',
        '".$mysqli->real_escape_string($clean_json)."',
        NOW()
    )
");

// Update schedule
$mysqli->query("
    UPDATE rooms SET schedule_json='".$mysqli->real_escape_string($clean_json)."'
    WHERE id='$room_id'
");

// Return result
echo $clean_json;

?>
