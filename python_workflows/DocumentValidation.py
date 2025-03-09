import json
import sys
import os
from difflib import SequenceMatcher

# Define reference data (modify as needed)
REFERENCE_RECORD = {
    "name": "John Doe",
    "dob": "01-01-1990",
    "address": "123 Main Street, New York",
    "id_number": "ABC123456"
}

def similarity_score(str1, str2):
    """Calculates similarity score between two strings."""
    return SequenceMatcher(None, str1.lower(), str2.lower()).ratio()

def validate_document(ocr_data):
    """Validates extracted OCR data against reference values."""
    validation_results = {}
    total_score = 0
    max_score = len(REFERENCE_RECORD)

    for key, expected_value in REFERENCE_RECORD.items():
        extracted_value = ocr_data.get(key, "N/A")
        match_score = similarity_score(expected_value, extracted_value) if extracted_value != "N/A" else 0

        validation_results[key] = {
            "expected": expected_value,
            "extracted": extracted_value,
            "match_score": round(match_score * 100, 2)  # Convert to percentage
        }

        total_score += match_score

    # Compute overall validation score
    overall_score = round((total_score / max_score) * 100, 2) if max_score > 0 else 0

    return {
        "validation_results": validation_results,
        "overall_validation_score": overall_score,
        "status": "PASS" if overall_score > 80 else "FAIL"
    }

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python DocumentValidation.py <ocr_json_path>"}))
        sys.exit(1)

    ocr_json_path = sys.argv[1]

    if not os.path.exists(ocr_json_path):
        print(json.dumps({"error": f"File not found: {ocr_json_path}"}))
        sys.exit(1)

    try:
        with open(ocr_json_path, "r", encoding="utf-8") as json_file:
            ocr_data = json.load(json_file)

        result = validate_document(ocr_data)

        print(json.dumps(result, indent=4, ensure_ascii=False))

    except json.JSONDecodeError:
        print(json.dumps({"error": "Invalid JSON format"}))
    except Exception as e:
        print(json.dumps({"error": f"Unexpected error: {str(e)}"}))
