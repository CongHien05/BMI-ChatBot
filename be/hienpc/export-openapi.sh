#!/bin/bash
# Bash Script to Export OpenAPI Specification
# Run this script to download OpenAPI spec from running backend server

BASE_URL="http://localhost:8080"
OUTPUT_DIR="."
JSON_FILE="openapi.json"
YAML_FILE="openapi.yaml"

echo "Exporting OpenAPI Specification..."
echo "Base URL: $BASE_URL"

# Check if server is running
if curl -s --head --request GET "$BASE_URL/v3/api-docs" | grep "200 OK" > /dev/null; then
    echo "✓ Server is running"
else
    echo "✗ Error: Cannot connect to server at $BASE_URL"
    echo "  Make sure the backend server is running!"
    exit 1
fi

# Download JSON format
echo ""
echo "Downloading OpenAPI JSON..."
if curl -s "$BASE_URL/v3/api-docs" -o "$OUTPUT_DIR/$JSON_FILE"; then
    echo "✓ Saved: $JSON_FILE"
else
    echo "✗ Error downloading JSON"
fi

# Download YAML format
echo ""
echo "Downloading OpenAPI YAML..."
if curl -s "$BASE_URL/v3/api-docs.yaml" -o "$OUTPUT_DIR/$YAML_FILE"; then
    echo "✓ Saved: $YAML_FILE"
else
    echo "✗ Error downloading YAML"
fi

echo ""
echo "✓ Export completed!"
echo ""
echo "Files saved:"
echo "  - $JSON_FILE"
echo "  - $YAML_FILE"
echo ""
echo "You can now:"
echo "  1. Share these files with Frontend team"
echo "  2. Use them to generate client code"
echo "  3. Import into API testing tools (Postman, Insomnia)"
