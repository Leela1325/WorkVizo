<?php
header("Content-Type: application/json");
require_once "../../config.php";

$room_id    = $_POST['room_id'] ?? null;
$instruction = $_POST['instruction'] ?? '';

if (!$room_id || !$instruction) {
    echo json_encode(["error" => "room_id and instruction required"]);
    exit;
}

// 1) FETCH ROOM INFO
$stmt = $conn->prepare("SELECT description, schedule_json FROM rooms WHERE id = ?");
$stmt->bind_param("i", $room_id);
$stmt->execute();
$res = $stmt->get_result()->fetch_assoc();

$desc = $res['description'] ?? '';
$old_schedule = $res['schedule_json'] ?? '';

if ($old_schedule === '') {
    echo json_encode(["error" => "No schedule found"]);
    exit;
}

// 2) BUILD STRICT PROMPT (kept strict to avoid extra fields)
$prompt = "
You are an AI that updates project schedules.

RULES:
- Output ONLY ONE JSON object.
- JSON MUST contain ONLY: { \"tasks\": [...] }
- NEVER add fields like project_name, bos_token, metadata, comments, or explanations.
- NEVER add markdown or code blocks.
- Maintain valid JSON formatting.

PROJECT DESCRIPTION:
$desc

OLD SCHEDULE JSON:
$old_schedule

USER INSTRUCTION:
$instruction

Return ONLY the updated JSON:
";

// save prompt file (your existing approach)
$promptPath = "C:\\xampp\\htdocs\\workvizo_backend\\api\\ai\\prompt_update.txt";
file_put_contents($promptPath, $prompt);

// 3) RUN LLaMA CLI (you used --file successfully in your last run)
$cmd = "\"C:\\llama\\llama-cli.exe\" -m \"C:\\llama\\models\\llama32.gguf\" --file \"$promptPath\" -n 500 2>&1";
$response = shell_exec($cmd);

// 4) CLEAN CLI noise but keep structure (remove ANSI escape sequences only)
$clean = preg_replace('/\e\[[\d;]*[A-Za-z]/', '', $response); // strip ANSI
$clean = trim($clean);

// 5) EXTRACT JSON BLOCKS (recursive regex) and try matches from last->first
$matches = [];
if (preg_match_all('/\{(?:[^{}]|(?R))*\}/s', $clean, $m)) {
    $matches = $m[0];
}

$selected_json = null;
$decoded = null;

// Try matches from last to first (AI often outputs JSON near the end)
for ($i = count($matches) - 1; $i >= 0; $i--) {
    $candidate = $matches[$i];
    // try to decode
    $tmp = json_decode($candidate, true);
    if (json_last_error() === JSON_ERROR_NONE) {
        // basic validation: must have "tasks" as array (adjustable)
        if (isset($tmp['tasks']) && is_array($tmp['tasks'])) {
            $selected_json = $candidate;
            $decoded = $tmp;
            break;
        }
        // if not strict, accept any valid JSON — comment previous validation
        // else continue scanning earlier matches
    }
}

// 6) Fallback: progressive search (in case nested braces or matching failed)
if ($selected_json === null) {
    // find last opening brace and attempt to find a closing brace that yields valid JSON
    $lastOpenPos = strrpos($clean, '{');
    if ($lastOpenPos !== false) {
        // try progressively from the last open backward to earlier opens
        $opens = [];
        for ($i = 0; $i < strlen($clean); $i++) {
            if ($clean[$i] === '{') $opens[] = $i;
        }
        // try candidate substrings starting from later opens
        for ($j = count($opens) - 1; $j >= 0; $j--) {
            $start = $opens[$j];
            // attempt to find matching end by searching forward for '}' and decode
            for ($end = $start; $end < strlen($clean); $end++) {
                if ($clean[$end] === '}') {
                    $candidate = substr($clean, $start, $end - $start + 1);
                    $tmp = json_decode($candidate, true);
                    if (json_last_error() === JSON_ERROR_NONE && isset($tmp['tasks']) && is_array($tmp['tasks'])) {
                        $selected_json = $candidate;
                        $decoded = $tmp;
                        break 2;
                    }
                }
            }
        }
    }
}

// 7) If still not found, return raw for debugging
if ($selected_json === null) {
    echo json_encode([
        "updated_schedule" => null,
        "raw" => $clean,
        "error" => "AI did not return valid JSON (extraction failed)"
    ]);
    exit;
}

// 8) SAVE HISTORY
$h = $conn->prepare("INSERT INTO schedule_history (room_id, old_json, new_json) VALUES (?, ?, ?)");
$h->bind_param("iss", $room_id, $old_schedule, $selected_json);
$h->execute();

// 9) UPDATE ROOM SCHEDULE
$u = $conn->prepare("UPDATE rooms SET schedule_json = ? WHERE id = ?");
$u->bind_param("si", $selected_json, $room_id);
$u->execute();

// 10) RETURN VALIDATED JSON
echo json_encode([
    "updated_schedule" => $decoded,
    "raw" => $selected_json
], JSON_UNESCAPED_UNICODE);
