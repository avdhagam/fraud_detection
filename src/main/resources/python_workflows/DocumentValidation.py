import json
import sys
import os
from difflib import SequenceMatcher
import codecs
import requests


# Set output encoding to UTF-8
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.buffer, "strict")

# Define the base directory for resources (adjust as needed)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REFERENCE_FOLDER = os.path.join(BASE_DIR, "reference_json")

# Define the paths to the reference data and test cases JSON files
REFERENCE_JSON_PATH = os.path.join(REFERENCE_FOLDER, "reference.json")
TEST_CASES_JSON_PATH = os.path.join(BASE_DIR, "test_cases.json")


API_BASE_URL = "http://localhost:8080/leads"

def fetch_ground_truth(lead_id, doc_type):
    """Fetches ground truth from API based on lead ID and document type."""
    if doc_type.lower() == "aadhaar":
        url = f"{API_BASE_URL}/{lead_id}/aadhaar"
    elif doc_type.lower() == "pan":
        url = f"{API_BASE_URL}/{lead_id}/pan"
    else:
        print(json.dumps({"error": f"Unsupported document type: {doc_type}"}, ensure_ascii=False, indent=4))
        sys.exit(1)

    try:
        response = requests.get(url)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(json.dumps({"error": f"Failed to fetch ground truth: {str(e)}"}, ensure_ascii=False, indent=4))
        sys.exit(1)

# Loading of all test cases to JSON files
def load_test_cases():
    """Loads the test cases from a JSON file and returns a list of test case dictionaries."""
    try:
        with open(TEST_CASES_JSON_PATH, "r", encoding="utf-8") as json_file:
            test_cases = json.load(json_file)
            return test_cases  # Returns JSON object
    except FileNotFoundError:
        print(json.dumps({"error": f"Test file not found: {TEST_CASES_JSON_PATH}"}, ensure_ascii=False, indent=4))
        sys.exit(1)
    except json.JSONDecodeError:
        print(json.dumps({"error": "Invalid JSON format in test file"}, ensure_ascii=False, indent=4))
        sys.exit(1)

# Get similarity score between strings
def similarity_score(str1, str2):
    """Calculates similarity score between two strings using SequenceMatcher."""
    if not str1 or not str2:  # Check for empty strings or None values
        return 0.0
    return SequenceMatcher(None, str1.lower(), str2.lower()).ratio()

# JSON document validator
def validate_document(ocr_data, reference_record):
    """Validates extracted OCR data against reference values and reports missing fields."""

    validation_results = {}
    total_score = 0
    max_score = len(reference_record)
    missing_fields = []

    for key, expected_value in reference_record.items():
        extracted_value = ocr_data.get(key)
        if extracted_value is None:
            extracted_value = "N/A"
            missing_fields.append(key)

        match_score = similarity_score(expected_value, extracted_value) if extracted_value != "N/A" else 0

        validation_results[key] = {
            "expected": expected_value,
            "extracted": extracted_value,
            "match_score": round(match_score * 100, 2)
        }

        total_score += match_score

    overall_score = round((total_score / max_score) * 100, 2) if max_score > 0 else 0
    status = "PASS" if overall_score > 80 else "FAIL"

    # Build output object so there are specific errors for JSON parsing
    output = {
        "validation_results": validation_results,
        "overall_validation_score": overall_score,
        "status": status
    }

    if missing_fields:
        output["missing_fields"] = missing_fields

    return output

# Document test code, checks all properties and if can be extracted as JSON.
def run_tests():
    """Runs a set of test cases to validate the document validation script."""
    print("Starting test run...\n")
    try:
        test_cases = load_test_cases()
        reference_record = fetch_ground_truth(lead_id, doc_type)

        # Set initial status
        all_tests_passed = True

        # Run through each test case in the test
        for i, test_case in enumerate(test_cases):
            try:
                print(f"Running test case #{i + 1}: {test_case['test_name']}")
                ocr_data = test_case.get("input_ocr_data", {})  # Safely get OCR data

                # If it's invalid skip and print
                if not isinstance(ocr_data, dict):
                    print(f"Test case #{i + 1} is INVALID: 'input_ocr_data' is missing or not a dictionary")
                    all_tests_passed = False
                    continue

                # Run validate_document with a JSON object
                result = validate_document(ocr_data, reference_record)

                # Check if status passes expected
                status_pass = (result["status"] == test_case.get("expected_status"))

                # Check overall score as a number, and safe check
                try:
                    expected_overall_validation_score = float(test_case.get("expected_overall_validation_score", 0))
                    overall_score_pass = abs(result["overall_validation_score"] - expected_overall_validation_score) < 0.01
                except ValueError:
                    overall_score_pass = False
                    print(f"Test case #{i + 1} is INVALID: bad overall score in JSON format")
                    test_passed = False

                # Test is passed if all test conditions met
                test_passed = status_pass and overall_score_pass

                if not test_passed:
                    all_tests_passed = False
                    print(f"Test case #{i + 1} FAILED:")
                    if not status_pass:
                        print(f"\t- Status check failed: Expected {test_case.get('expected_status')}, got {result['status']}")
                    if not overall_score_pass:
                        print(f"\t- Overall score check failed: Expected {expected_overall_validation_score}, got {result['overall_validation_score']}")
                else:
                    print(f"Test case #{i + 1} PASSED")

                print()
            except Exception as e:
                all_tests_passed = False
                print(f"CRITICAL ERROR: Test case threw exception:\n{e}")
                print(
                    "Ensure JSON can be read, no values are incorrect, file permissions and dependencies were properly setup and you are using a proper Python interpreter. More information is described in other solutions.")

        if all_tests_passed:
            print("All test cases PASSED!\n")
        else:
            print("Some test cases FAILED!\n")

    except Exception as e:
        print(
            f"An critical failure has happened:\nEnsure that the paths are loaded and all dependcies of your framework (python + values) can run properly, more description is available in other solutions. {e}")

if __name__ == "__main__":
    # Check if the correct number of command-line arguments is provided
    if len(sys.argv) != 4:
        print(json.dumps({"error": "Usage: python DocumentValidation.py <ocr_json_path> <lead_id> <doc_type>"}, ensure_ascii=False, indent=4))
        sys.exit(1)

    # Get the path to the OCR JSON file from the command-line arguments
    ocr_json_path = sys.argv[1]
    lead_id = sys.argv[2]
    doc_type = sys.argv[3]

    # Check if the OCR JSON file exists
    if not os.path.exists(ocr_json_path):
        print(json.dumps({"error": f"File not found: {ocr_json_path}"}, ensure_ascii=False, indent=4))
        sys.exit(1)

    try:
        with open(ocr_json_path, "r", encoding="utf-8") as json_file:
            ocr_data = json.load(json_file)

        ground_truth = fetch_ground_truth(lead_id, doc_type)
        result = validate_document(ocr_data, ground_truth)
        print(json.dumps(result, indent=4, ensure_ascii=False))
    except Exception as e:
        print(json.dumps({"error": f"Unexpected error: {str(e)}"}, ensure_ascii=False, indent=4))
        sys.exit(1)



    #Add error JSON to be provided for output
    except ValueError as e:
        error_message = f"JSON file value error occurred: {str(e)}"
        error_response = {"error": error_message, "validation_results": {}, "overall_validation_score": 0,
                          "status": "ERROR"}
        print(json.dumps(error_response, indent=4, ensure_ascii=False))
    except json.JSONDecodeError:
        print(json.dumps({"error": "Invalid JSON format"}, ensure_ascii=False, indent=4))

    except Exception as e:
        error_message = f"An unexpected error occurred: {str(e)}"
        error_response = {"error": error_message, "validation_results": {}, "overall_validation_score": 0,
                          "status": "ERROR"}
        print(json.dumps(error_response, indent=4, ensure_ascii=False))