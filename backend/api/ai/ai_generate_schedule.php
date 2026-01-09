<?php
header("Content-Type: application/json");

/* ---------- CONFIG ---------- */
$GROQ_API_KEY = "";
$GROQ_URL = "";

/* ---------- INPUT ---------- */
$raw = file_get_contents("php://input");
$input = json_decode($raw, true);

$description  = trim($input['description'] ?? '');
$start_date   = trim($input['start_date'] ?? '');
$end_date     = trim($input['end_date'] ?? '');
$people_count = (int)($input['people_count'] ?? 0);

if (!$description || !$start_date || !$end_date || $people_count <= 0) {
    echo json_encode(["status"=>"error","message"=>"Invalid input"]);
    exit;
}

/* ---------- DATE LOGIC ---------- */
$startTs = strtotime($start_date);
$endTs   = strtotime($end_date);
$totalDays = max(1, ceil(($endTs - $startTs) / 86400));
$chunkDays = max(1, floor($totalDays / $people_count));

/* ---------- AI PROMPT ---------- */
$prompt = <<<PROMPT
Analyze the project description and generate {$people_count} distinct task names.

Rules:
- Output ONLY task names
- One task per line
- Short clear task names
- No numbering
- No explanations
- No extra text

Project description:
{$description}
PROMPT;

/* ---------- GROQ API CALL ---------- */
$payload = [
    "model" => "llama-3.1-8b-instant",
    "messages" => [
        ["role" => "system", "content" => "You generate software task names only."],
        ["role" => "user", "content" => $prompt]
    ],
    "temperature" => 0.2
];

$ch = curl_init($GROQ_URL);
curl_setopt_array($ch, [
    CURLOPT_POST => true,
    CURLOPT_HTTPHEADER => [
        "Authorization: Bearer $GROQ_API_KEY",
        "Content-Type: application/json"
    ],
    CURLOPT_POSTFIELDS => json_encode($payload),
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_TIMEOUT => 30
]);

$response = curl_exec($ch);
curl_close($ch);

$data = json_decode($response, true);

if (!$data) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid JSON from Groq",
        "raw" => $response
    ]);
    exit;
}

if (isset($data['error'])) {
    echo json_encode([
        "status" => "error",
        "message" => "Groq error",
        "details" => $data['error']
    ]);
    exit;
}

if (!isset($data['choices'][0]['message']['content'])) {
    echo json_encode([
        "status" => "error",
        "message" => "Unexpected Groq response",
        "raw" => $data
    ]);
    exit;
}


$output = trim($data['choices'][0]['message']['content']);

/* ---------- PARSE TASK NAMES ---------- */
$lines = array_map('trim', explode("\n", $output));
$tasks_ai = [];

foreach ($lines as $line) {
    if ($line === '') continue;
    if (strlen($line) < 3) continue;
    $tasks_ai[] = ucwords($line);
}

$tasks_ai = array_slice(array_unique($tasks_ai), 0, $people_count);

/* ---------- FALLBACK (SAFETY) ---------- */
if (count($tasks_ai) < $people_count) {
    $defaults = ["Frontend Development","Backend Development","Database Design","API Development","Testing"];
    $tasks_ai = array_slice(array_unique(array_merge($tasks_ai, $defaults)), 0, $people_count);
}

/* ---------- BUILD FINAL SCHEDULE ---------- */
$tasks = [];
$currentTs = $startTs;

foreach ($tasks_ai as $taskName) {
    $taskStart = date('Y-m-d', $currentTs);
    $taskEndTs = min($currentTs + ($chunkDays * 86400), $endTs);
    $taskEnd   = date('Y-m-d', $taskEndTs);

    $tasks[] = [
        "task_name"  => $taskName,
        "start_date" => $taskStart,
        "end_date"   => $taskEnd
    ];

    $currentTs = $taskEndTs + 86400;
}

/* ---------- RESPONSE ---------- */
echo json_encode(["tasks"=>$tasks]);
exit;
