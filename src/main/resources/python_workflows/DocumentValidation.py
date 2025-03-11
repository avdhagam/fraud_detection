import json
import sys
import os
from difflib import SequenceMatcher
import codecs

# Set output encoding to UTF-8
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.buffer, "strict")

# Define the base directory for resources (adjust as needed)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__))) # location of script
REFERENCE_FOLDER = os.path.join(BASE_DIR, "reference_json")

# Define the path to the reference data JSON file
REFERENCE_JSON_PATH = os.path.join(REFERENCE_FOLDER, "reference.json")

def load_reference_record():
    """Loads the reference record from the reference.json file. Handles file not found and JSON errors."""
    try:
        with open(REFERENCE_JSON_PATH, "r", encoding="utf-8") as json_file:
            return json.load(json_file) # Returns JSON object
    except FileNotFoundError:
        print(json.dumps({"error": f"Reference file not found: {REFERENCE_JSON_PATH}"}, ensure_ascii=False, indent=4))
        sys.exit(1)
    except json.JSONDecodeError:
        print(json.dumps({"error": "Invalid JSON format in reference file"}, ensure_ascii=False, indent=4))
        sys.exit(1)

def similarity_score(str1, str2):
    """Calculates similarity score between two strings using SequenceMatcher."""
    return SequenceMatcher(None, str1.lower(), str2.lower()).ratio()

def validate_document(ocr_data, reference_record):
    """
    Validates extracted OCR data against reference values.

    Args:
        ocr_data (dict): A dictionary containing the extracted OCR data (name, dob, address, id_number).
                         Example: {"name": "John Doe", "dob": "01-01-1990", "address": "...", "id_number": "..."}
        reference_record (dict): A dictionary containing the reference data (name, dob, address, id_number).
                                  Example: {"name": "John Doe", "dob": "01-01-1990", "address": "...", "id_number": "..."}

    Returns:
        dict: A dictionary containing the validation results.
              Example: {"validation_results": {"name": {...}, "dob": {...}, ...}, "overall_validation_score": 95.0, "status": "PASS"}
    """

    validation_results = {}
    total_score = 0
    max_score = len(reference_record)

    # Iterate through the keys in the reference record
    for key, expected_value in reference_record.items():
        # Get the extracted value from the OCR data (or "N/A" if not found)
        extracted_value = ocr_data.get(key, "N/A")

        # Calculate the similarity score between the expected and extracted values
        match_score = similarity_score(expected_value, extracted_value) if extracted_value != "N/A" else 0

        # Store the validation results for the current key
        validation_results[key] = {
            "expected": expected_value,
            "extracted": extracted_value,
            "match_score": round(match_score * 100, 2)  # Convert to percentage
        }

        # Add the match score to the total score
        total_score += match_score

    # Compute the overall validation score
    overall_score = round((total_score / max_score) * 100, 2) if max_score > 0 else 0

    # Determine the validation status (PASS or FAIL) based on the overall score
    status = "PASS" if overall_score > 80 else "FAIL"

    # Return the validation results
    return {
        "validation_results": validation_results,
        "overall_validation_score": overall_score,
        "status": status
    }

if __name__ == "__main__":
    # Check if the correct number of command-line arguments is provided
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python DocumentValidation.py <ocr_json_path>"}, ensure_ascii=False, indent=4))
        sys.exit(1)

    # Get the path to the OCR JSON file from the command-line arguments
    ocr_json_path = sys.argv[1]

    # Check if the OCR JSON file exists
    if not os.path.exists(ocr_json_path):
        print(json.dumps({"error": f"File not found: {ocr_json_path}"}, ensure_ascii=False, indent=4))
        sys.exit(1)

    try:
        # Open the OCR JSON file and load the OCR data
        with open(ocr_json_path, "r", encoding="utf-8") as json_file:
            ocr_data = json.load(json_file) #Returns JSON object
            #print(f"Data successfully loaded from {ocr_json_path}: {ocr_data}") #This is for testing REMOVE THIS
        # Load reference record from JSON file
        reference_record = load_reference_record()

        # Validate the document
        result = validate_document(ocr_data, reference_record)

        # Print the validation results as JSON
        print(json.dumps(result, indent=4, ensure_ascii=False))

    except json.JSONDecodeError:
        print(json.dumps({"error": "Invalid JSON format"}, ensure_ascii=False, indent=4))
    except Exception as e:
        print(json.dumps({"error": f"Unexpected error: {str(e)}"}, ensure_ascii=False, indent=4))