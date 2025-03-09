import json
import os
import sys
import logging
import Levenshtein

# Logging configuration
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

# Define storage paths
BASE_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "resources")  # Gets 'src/main/resources'
OCR_FOLDER = os.path.join(BASE_DIR, "document_storage", "validation_json")
REFERENCE_FOLDER = os.path.join(BASE_DIR, "reference_json")  # Folder where reference data is stored
VALIDATION_FOLDER = os.path.join(BASE_DIR, "validation_results")

# Ensure storage directories exist
os.makedirs(OCR_FOLDER, exist_ok=True)
os.makedirs(REFERENCE_FOLDER, exist_ok=True)
os.makedirs(VALIDATION_FOLDER, exist_ok=True)

# Load OCR data from document path
def load_ocr_data(document_path):
    """Loads the OCR JSON file corresponding to the given document's filename."""
    document_name = os.path.basename(document_path)
    document_json_name = os.path.splitext(document_name)[0] + ".json"
    ocr_json_path = os.path.join(OCR_FOLDER, document_json_name)

    try:
        with open(ocr_json_path, "r") as file:
            return json.load(file), ocr_json_path
    except FileNotFoundError:
        logging.error(f"Error: OCR file not found: {ocr_json_path}")
    except json.JSONDecodeError:
        logging.error(f"Error: Failed to parse JSON in {ocr_json_path}")
    return None, None

#Load reference data
def load_reference_data():
    """Loads the reference data from reference.json."""
    reference_json_path = os.path.join(REFERENCE_FOLDER, "reference.json")
    try:
        with open(reference_json_path, "r") as file:
            return json.load(file)
    except FileNotFoundError:
        logging.error(f"Error: Reference file not found at {reference_json_path}")
    except json.JSONDecodeError:
        logging.error(f"Error: Failed to parse JSON in {reference_json_path}")
    return None


def calculate_similarity(expected, extracted):
    """Computes similarity between expected and extracted values using Levenshtein ratio."""
    if not expected or not extracted:
        return 0.0
    return round(Levenshtein.ratio(str(expected), str(extracted)), 2)


def generate_detailed_insight(field, expected, extracted, similarity):
    """
    Generates detailed insights and recommendations based on field importance, similarity, and common document errors.
    """
    if similarity == 1.0:
        return "Perfect match.", "No action needed."

    # Custom logic based on field type
    if field == "id_number":
        if similarity < 0.8:
            return "Mismatch in ID number, which is critical.", "Immediate recheck required."
        return "Minor formatting difference in ID number.", "Verify but likely correct."

    if field == "name":
        if similarity >= 0.9:
            return "Minor name variation.", "Verify manually if needed."
        elif similarity >= 0.7:
            return "Moderate mismatch in name.", "Check against another document."
        else:
            return "Significant name mismatch.", "Possible identity issue; further validation required."

    if field == "date_of_birth":
        if similarity >= 0.8:
            return "Minor date format difference.", "Acceptable if other details match."
        else:
            return "Mismatch in date of birth.", "Verify with another document."

    if field == "document_type":
        if similarity < 1.0:
            return "Document type mismatch.", "Ensure correct document is provided."

    if field == "gender":
        if similarity < 1.0:
            return "Gender mismatch detected.", "Requires manual verification."

    return "Field mismatch detected.", "Verify manually."


def validate_document(ocr_data, reference_data):
    """
    Performs field-by-field validation and generates detailed insights and recommendations.
    """
    validation_results = {"fields": {}, "finalValidationScore": 0.0, "summaryInsight": "", "overallRecommendation": ""}
    total_score = 0
    field_count = 0

    for field, expected_value in reference_data.items():
        extracted_value = ocr_data.get(field, None)
        similarity = calculate_similarity(expected_value, extracted_value)
        insight, recommendation = generate_detailed_insight(field, expected_value, extracted_value, similarity)

        validation_results["fields"][field] = {
            "expected": expected_value,
            "extracted": extracted_value,
            "matchScore": similarity,
            "insight": insight,
            "recommendation": recommendation
        }

        total_score += similarity
        field_count += 1

    # Compute final validation score
    validation_results["finalValidationScore"] = round(total_score / field_count, 2) if field_count else 0.0

    # Generate overall insights
    if validation_results["finalValidationScore"] >= 0.9:
        validation_results["summaryInsight"] = "Document is highly accurate with minor or no mismatches."
        validation_results["overallRecommendation"] = "Approved."
    elif validation_results["finalValidationScore"] >= 0.7:
        validation_results["summaryInsight"] = "Document has some discrepancies but is mostly accurate."
        validation_results["overallRecommendation"] = "Manual review recommended."
    else:
        validation_results["summaryInsight"] = "Document contains significant mismatches."
        validation_results["overallRecommendation"] = "Rejection or further verification required."

    return validation_results


def main():

    if len(sys.argv) < 2:
        print("Error: Missing document filename argument.")
        sys.exit(1)

    document_path = sys.argv[1]
    ocr_data, ocr_json_path = load_ocr_data(document_path)

    if not ocr_data:
        print(f"Error: OCR results not found for {document_path}")
        sys.exit(1)

    # Load reference data from the reference.json file
    reference_data = load_reference_data()

    if not reference_data:
        logging.error("Reference data could not be loaded.")
        return

    # Validate document
    validation_results = validate_document(ocr_data, reference_data)

    if "error" in validation_results:
        print(validation_results["error"])
        sys.exit(1)

    os.makedirs(VALIDATION_FOLDER, exist_ok=True)
    document_basename = os.path.basename(document_path)  # Extract just the filename
    base_filename= os.path.splitext(document_basename)[0]
    validation_json_path = os.path.join(VALIDATION_FOLDER, f"validation_{base_filename}.json")

    with open(validation_json_path, "w") as file:
        json.dump(validation_results, file, indent=2)

    print(json.dumps(validation_results, indent=2))

if __name__ == "__main__":
    main()