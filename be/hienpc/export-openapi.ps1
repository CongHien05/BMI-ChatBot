# PowerShell Script to Export OpenAPI Specification
# Run this script to download OpenAPI spec from running backend server

$baseUrl = "http://localhost:8080"
$outputDir = "."
$jsonFile = "openapi.json"
$yamlFile = "openapi.yaml"

Write-Host "Exporting OpenAPI Specification..." -ForegroundColor Green
Write-Host "Base URL: $baseUrl" -ForegroundColor Cyan

# Check if server is running
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/v3/api-docs" -Method Get -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✓ Server is running" -ForegroundColor Green
} catch {
    Write-Host "✗ Error: Cannot connect to server at $baseUrl" -ForegroundColor Red
    Write-Host "  Make sure the backend server is running!" -ForegroundColor Yellow
    exit 1
}

# Download JSON format
Write-Host "`nDownloading OpenAPI JSON..." -ForegroundColor Cyan
try {
    Invoke-WebRequest -Uri "$baseUrl/v3/api-docs" -OutFile "$outputDir\$jsonFile" -ErrorAction Stop
    Write-Host "✓ Saved: $jsonFile" -ForegroundColor Green
} catch {
    Write-Host "✗ Error downloading JSON: $_" -ForegroundColor Red
}

# Download YAML format
Write-Host "`nDownloading OpenAPI YAML..." -ForegroundColor Cyan
try {
    Invoke-WebRequest -Uri "$baseUrl/v3/api-docs.yaml" -OutFile "$outputDir\$yamlFile" -ErrorAction Stop
    Write-Host "✓ Saved: $yamlFile" -ForegroundColor Green
} catch {
    Write-Host "✗ Error downloading YAML: $_" -ForegroundColor Red
}

Write-Host "`n✓ Export completed!" -ForegroundColor Green
Write-Host "`nFiles saved:" -ForegroundColor Cyan
Write-Host "  - $jsonFile" -ForegroundColor White
Write-Host "  - $yamlFile" -ForegroundColor White
Write-Host "`nYou can now:" -ForegroundColor Cyan
Write-Host "  1. Share these files with Frontend team" -ForegroundColor White
Write-Host "  2. Use them to generate client code" -ForegroundColor White
Write-Host "  3. Import into API testing tools (Postman, Insomnia)" -ForegroundColor White
