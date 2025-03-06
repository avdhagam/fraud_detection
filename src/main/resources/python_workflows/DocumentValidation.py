import json
import logging
import os
from difflib import SequenceMatcher

# Configure logging
logging.basicConfig(filename="validation.log", level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

# Hardcoded reference record (to be replaced with actual DB lookup later)
REFERENCE_RECORD = {
    "document_type": "Aadhaar",  # Change to "PAN" for PAN validation
    "name": "Sapni Singh",
    "dob": "1900-01-01",
    "gender": "Male",
    "id_number": "XXXX-XXXX-XXXX",  # Masked for Aadhaar
    "qr_code_data": None
}

# Mapping extracted field names to expected names
FIELD_NAME_MAPPING = {
    "date_of_birth": "dob"  # Maps OCR "date_of_birth" field to "dob" in validation
}

def similarity_score(value1, value2):
    """Calculate similarity score between two text values."""
    if not value1 or not value2:
        return 0  # If any value is missing, assume no match
    return SequenceMatcher(None, value1.lower(), value2.lower()).ratio()

def normalize_ocr_data(ocr_data):
    """Map OCR field names to expected validation fields."""
    normalized_data = {}
    for key, value in ocr_data.items():
        mapped_key = FIELD_NAME_MAPPING.get(key, key)  # Map if exists, else keep original
        normalized_data[mapped_key] = value
    return normalized_data

def validate_document(ocr_data):
    """Validate extracted OCR data against the reference record."""
    if not isinstance(ocr_data, dict):
        logging.error("Invalid OCR data format")
        return {"error": "Invalid OCR data format"}

    # Normalize OCR field names before validation
    ocr_data = normalize_ocr_data(ocr_data)

    validation_results = {}
    total_score = 0
    max_score = len(REFERENCE_RECORD)  # Each field contributes equally to score

    for key, expected_value in REFERENCE_RECORD.items():
        extracted_value = ocr_data.get(key, None)
        match_score = similarity_score(str(expected_value), str(extracted_value))

        validation_results[key] = {
            "expected": expected_value,
            "extracted": extracted_value,
            "match_score": round(match_score * 100, 2)  # Convert to percentage
        }

        total_score += match_score

        if match_score < 0.8:  # Threshold for mismatch
            logging.warning(f"Field '{key}' mismatch. Extracted: {extracted_value}, Expected: {expected_value}")

    overall_score = round((total_score / max_score) * 100, 2)

    result = {
        "validation_results": validation_results,
        "overall_validation_score": overall_score,
        "status": "PASS" if overall_score > 80 else "FAIL",
    }

    logging.info(f"Validation complete. Score: {overall_score}% - Status: {result['status']}")
    return result

if __name__ == "__main__":
    # Path to OCR output JSON file
    OCR_JSON_PATH = os.path.join("src", "main", "resources", "document_storage", "test.json")

    try:
        # Load OCR output from the saved JSON file
        with open(OCR_JSON_PATH, "r") as json_file:
            ocr_data = json.load(json_file)

        # Validate OCR data
        validation_output = validate_document(ocr_data)
        print(json.dumps(validation_output, indent=4))

    except FileNotFoundError:
        print(json.dumps({"error": f"File not found: {OCR_JSON_PATH}"}))
    except json.JSONDecodeError:
        print(json.dumps({"error": "Invalid OCR JSON file format"}))
