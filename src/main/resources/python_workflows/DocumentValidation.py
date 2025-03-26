import json
import sys
import os
import codecs
import requests
import warnings
from difflib import SequenceMatcher
from thefuzz import fuzz
from datetime import datetime

# Suppress warnings
warnings.filterwarnings("ignore")

# Set output encoding to UTF-8
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.buffer, "strict")

API_BASE_URL = "http://localhost:8080/leads"

def fetch_ground_truth(lead_id, doc_type):
    """Fetches ground truth from API based on lead ID and document type."""
    url = f"{API_BASE_URL}/{lead_id}/{doc_type.lower()}"
    try:
        response = requests.get(url)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        sys.exit(json.dumps({"error": f"Failed to fetch ground truth: {str(e)}"}, ensure_ascii=False, indent=4))

def normalize_text(text):
    """Normalizes text by lowercasing and removing extra spaces."""
    return str(text).strip().lower()

def normalize_date(date_str):
    """Attempts to normalize a date string into a standard YYYY-MM-DD format."""
    date_formats = ["%Y-%m-%d", "%d-%m-%Y", "%m/%d/%Y", "%d/%m/%Y", "%Y/%m/%d"]
    for fmt in date_formats:
        try:
            return datetime.strptime(date_str, fmt).strftime("%Y-%m-%d")
        except ValueError:
            continue
    return date_str  # Return original if no match

def similarity_score(str1, str2, key):
    """Calculates similarity score with adjustments for ID, gender, and date fields."""
    if not str1 or not str2:
        return 0.0

    str1, str2 = normalize_text(str1), normalize_text(str2)

    if key == "id_number":
        return 1.0 if str1 == str2 else 0.0

    if key == "date_of_birth":
        return 1.0 if normalize_date(str1) == normalize_date(str2) else 0.0

    if key == "gender":
        return 1.0 if str1[0] == str2[0] else 0.0

    sm_score = SequenceMatcher(None, str1, str2).ratio()
    fuzz_score = fuzz.ratio(str1, str2) / 100
    return (sm_score * 0.5) + (fuzz_score * 0.5)

def validate_document(ocr_data, reference_record):
    """Validates extracted OCR data against reference values."""
    validation_results = {}
    total_score = 0
    max_score = len(reference_record)
    missing_fields = []

    for key, expected_value in reference_record.items():
        extracted_entry = ocr_data.get(key, {})
        if isinstance(extracted_entry, dict) and "value" in extracted_entry:
            extracted_value = extracted_entry["value"]
        else:
            extracted_value = "N/A"

        if extracted_value == "N/A":
            missing_fields.append(key)
            match_score = 0
        else:
            match_score = similarity_score(expected_value, extracted_value, key)

        validation_results[key] = {
            "expected": expected_value,
            "extracted": extracted_value,
            "match_score": round(match_score * 100, 2)
        }
        total_score += match_score

    overall_score = round((total_score / max_score) * 100, 2) if max_score > 0 else 0
    status = "PASS" if overall_score > 85 else "FAIL"

    output = {
        "validation_results": validation_results,
        "overall_validation_score": overall_score,
        "status": status
    }
    if missing_fields:
        output["missing_fields"] = missing_fields

    return output

if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit(json.dumps({"error": "Usage: python DocumentValidation.py <ocr_json_path> <lead_id> <doc_type>"}, ensure_ascii=False, indent=4))

    ocr_json_path, lead_id, doc_type = sys.argv[1:4]
    if not os.path.exists(ocr_json_path):
        sys.exit(json.dumps({"error": f"File not found: {ocr_json_path}"}, ensure_ascii=False, indent=4))

    try:
        with open(ocr_json_path, "r", encoding="utf-8") as json_file:
            ocr_data = json.load(json_file)

        ground_truth = fetch_ground_truth(lead_id, doc_type)
        result = validate_document(ocr_data, ground_truth)

        print(json.dumps(result, indent=4, ensure_ascii=False))
    except Exception as e:
        sys.exit(json.dumps({"error": f"Unexpected error: {str(e)}"}, ensure_ascii=False, indent=4))