<?php
session_start();
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ---------- INPUT ---------- */
$input = json_decode(file_get_contents("php://input"), true);
$message = trim($input['message'] ?? '');

if ($message === '') {
    echo json_encode([
        "reply" => "Please ask a question related to this room."
    ]);
    exit;
}

/* ---------- LOAD ROOM CONTEXT ---------- */
if (!isset($_SESSION['ai_room_context'])) {
    echo json_encode([
        "reply" => "AI session not initialized for this room."
    ]);
    exit;
}

$ctx = $_SESSION['ai_room_context'];

$room    = $ctx['room'];
$user    = $ctx['user'];
$members = $ctx['members'];
$tasks   = $ctx['tasks'];

/* ---------- BUILD FACT-LOCKED PROMPT ---------- */
$prompt  = "You are an AI assistant for a project management room.\n\n";
$prompt .= "RULES:\n";
$prompt .= "- Use ONLY the data below.\n";
$prompt .= "- You MAY analyze, summarize, compare, and give suggestions.\n";
$prompt .= "- Do NOT invent tasks, members, roles, or dates.\n";
$prompt .= "- Do NOT dump raw data.\n";
$prompt .= "- If asked to dump data, reply EXACTLY:\n";
$prompt .= "  \"I can summarize or analyze the room, but I cannot display raw room data.\"\n";
$prompt .= "- If the question is unrelated to this room, reply EXACTLY:\n";
$prompt .= "- If user greets, greet him/her politely.\n";
$prompt .= "- If user says bye, thank you end the chat politely.\n";
$prompt .= "  \"I don't have enough information to answer that based on this room.\"\n\n";

/* ---------- ROOM ---------- */
$prompt .= "ROOM:\n";
$prompt .= "name={$room['name']}\n";
$prompt .= "start={$room['start_date']}\n";
$prompt .= "end={$room['end_date']}\n";
$prompt .= "creator={$room['creator_name']}\n\n";

/* ---------- CURRENT USER ---------- */
$prompt .= "CURRENT USER:\n";
$prompt .= "name={$user['name']}\n";
$prompt .= "email={$user['email']}\n\n";

/* ---------- MEMBERS ---------- */
$prompt .= "MEMBERS:\n";
foreach ($members as $m) {
    $prompt .= "- name={$m['name']} role={$m['role']} joined={$m['joined_at']}\n";
}
$prompt .= "\n";

/* ---------- TASKS ---------- */
$prompt .= "TASKS:\n";
if (empty($tasks)) {
    $prompt .= "- none\n";
} else {
    foreach ($tasks as $t) {
        $prompt .= "- name={$t['task_name']} status={$t['status']} start={$t['start_date']} end={$t['end_date']}\n";
    }
}
$prompt .= "\n";

/* ---------- QUESTION ---------- */
$prompt .= "User question:\n{$message}\n\n";
$prompt .= "FINAL_ANSWER:\n";

/* ---------- RUN LLAMA ---------- */
$tmp = tempnam(sys_get_temp_dir(), 'llama_');
file_put_contents($tmp, $prompt);

$cmd = 'C:\\llama\\llama-cli.exe '
     . '-m C:\\llama\\models\\llama32.gguf '
     . '--file ' . escapeshellarg($tmp)
     . ' -n 300 --temp 0.2';

$output = shell_exec($cmd);
unlink($tmp);

/* ---------- CLEAN OUTPUT ---------- */
$output = preg_replace('/\e\[[\d;]*m/', '', $output);

/* Keep only text after FINAL_ANSWER */
if (strpos($output, 'FINAL_ANSWER:') !== false) {
    $output = explode('FINAL_ANSWER:', $output, 2)[1];
}

/* Remove model noise */
$output = preg_replace('/assistant|> EOF.*|Interrupted by user/i', '', $output);
$output = trim($output);

/* ---------- SAFETY FALLBACK ---------- */
if ($output === '') {
    $output = "I don't have enough information to answer that based on this room.";
}

/* ---------- RESPONSE ---------- */
echo json_encode([
    "reply" => $output
]);
exit;
