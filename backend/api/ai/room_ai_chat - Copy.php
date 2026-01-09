<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../../config.php";

/* ===========================
   INPUT
=========================== */
$room_id = (int)($_POST['room_id'] ?? 0);
$user_id = (int)($_POST['user_id'] ?? 0);
$query   = trim($_POST['query'] ?? '');

if (!$room_id || !$user_id || $query === '') {
    echo json_encode(["reply" => "Missing required input."]);
    exit;
}

/* ===========================
   FETCH ROOM CREATOR
=========================== */
$roomQ = $conn->prepare("
    SELECT r.name, r.start_date, r.end_date, u.name AS creator_name
    FROM rooms r
    JOIN users u ON u.id = r.created_by
    WHERE r.id = ?
    LIMIT 1
");
$roomQ->bind_param("i", $room_id);
$roomQ->execute();
$room = $roomQ->get_result()->fetch_assoc();

if (!$room) {
    echo json_encode(["reply" => "Invalid room."]);
    exit;
}

/* ===========================
   FETCH MEMBERS (joined_rooms)
=========================== */
$members = [];
$mQ = $conn->prepare("
    SELECT u.name, j.role, j.joined_at
    FROM joined_rooms j
    JOIN users u ON u.id = j.user_id
    WHERE j.room_id = ?
    ORDER BY j.joined_at DESC
");
$mQ->bind_param("i", $room_id);
$mQ->execute();
$mRes = $mQ->get_result();

while ($m = $mRes->fetch_assoc()) {
    $members[] = [
        "name" => $m['name'],
        "role" => $m['role'],
        "joined_at" => $m['joined_at']
    ];
}

/* ===========================
   FETCH TASKS (room_tasks)
=========================== */
$tasks = [];
$taskIds = [];

$tQ = $conn->prepare("
    SELECT id, task_name, start_date, end_date, status, assigned_email
    FROM room_tasks
    WHERE room_id = ?
");
$tQ->bind_param("i", $room_id);
$tQ->execute();
$tRes = $tQ->get_result();

while ($t = $tRes->fetch_assoc()) {
    $taskIds[] = (int)$t['id'];
    $tasks[] = $t;
}
$myTasks = [];

foreach ($tasks as $t) {
    if (!empty($userEmail) && $t['assigned_email'] === $userEmail) {
        $myTasks[] = $t;
    }
}


/* ===========================
   FETCH PROOFS (task status)
=========================== */
$proofs = [];
if (!empty($taskIds)) {
    $ids = implode(',', $taskIds);
    $pQ = $conn->query("
        SELECT task_id, user_id, status, created_at
        FROM proofs
        WHERE task_id IN ($ids)
        ORDER BY created_at DESC
    ");
    while ($p = $pQ->fetch_assoc()) {
        $proofs[] = $p;
    }
}

/* ===========================
   STRUCTURED DATA FOR AI
=========================== */
$data = [
    "project" => [
        "name" => $room['name'],
        "duration" => $room['start_date'] . " to " . $room['end_date'],
        "creator" => $room['creator_name']
    ],
    "members" => $members,
    "tasks"   => $tasks,
    "proofs"  => $proofs
];

$jsonData = json_encode($data, JSON_PRETTY_PRINT);

/* ===========================
   AI PROMPT (STRICT + FAST)
=========================== */
$prompt = <<<PROMPT
You are an AI assistant inside a project management application.

You already know all the project information below.
Answer the user's question directly and naturally.

Rules:
- creator = head = owner = admin
- "recently joined" means the member with the latest joined_at
- Use task dates and proof status to understand progress
- Give suggestions when asked by analyzing tasks and progress (do not exceed more than 120 letters)
- If information is missing, say: "That information is not available."
- If the question is unrelated, say:
  "I can help only with this project, its tasks, members, or progress."
- NEVER mention data sources, JSON, or how you know the answer.
-If user asks room details, analyze the below information and give it to user.
PROJECT INFORMATION:
{$jsonData}

USER QUESTION:
{$query}

###FINAL_ANSWER###

PROMPT;


/* ===========================
   RUN LLAMA (OPTIMIZED)
=========================== */
$tmp = tempnam(sys_get_temp_dir(), 'llama_');
file_put_contents($tmp, $prompt);

$cmd = 'C:\\llama\\llama-cli.exe -m C:\\llama\\models\\llama32.gguf --file '
     . escapeshellarg($tmp)
     . ' -n 120 --temp 0.2';

$output = shell_exec($cmd);
unlink($tmp);

/* ===========================
   CLEAN OUTPUT
=========================== */
/* ===========================
   CLEAN OUTPUT (STRICT)
=========================== */

/* ===========================
   CLEAN OUTPUT (FINAL & STRICT)
=========================== */

// remove ANSI color codes
$output = preg_replace('/\e\[[\d;]*m/', '', $output);

// normalize newlines
$output = str_replace("\r\n", "\n", $output);

// extract ONLY text after FINAL marker
$pos = strpos($output, '###FINAL_ANSWER###');
if ($pos !== false) {
    $output = substr($output, $pos + strlen('###FINAL_ANSWER###'));
}

// remove role prefixes like "assistant" or "user"
$output = preg_replace('/^\s*(assistant|user)\s*/i', '', $output);

// remove llama-cli termination junk
$output = preg_replace('/> EOF by user.*$/is', '', $output);
$output = preg_replace('/Interrupted by user.*$/is', '', $output);

// trim whitespace
$reply = trim($output);

// safety fallback
if ($reply === '') {
    $reply = "I couldn't generate a clear response right now.";
}

echo json_encode(["reply" => $reply]);
exit;
