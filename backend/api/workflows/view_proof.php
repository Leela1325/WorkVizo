<?php
require_once __DIR__ . "/../../config.php";

/*
 Example:
 /api/files/view_proof.php?path=uploads/proof_123.png
*/

$path = $_GET['path'] ?? '';

if (!$path) {
    http_response_code(400);
    exit("Invalid file");
}

/* 🔐 SECURITY: prevent directory traversal */
$path = str_replace(['..', './', '\\'], '', $path);

$fullPath = __DIR__ . "/../../" . $path;

if (!file_exists($fullPath)) {
    http_response_code(404);
    exit("File not found");
}

/* ---------- MIME TYPE ---------- */
$mime = mime_content_type($fullPath);
header("Content-Type: $mime");
header("Content-Length: " . filesize($fullPath));
header("Content-Disposition: inline"); // view in browser

readfile($fullPath);
exit;
