import json
import os
import sys
from difflib import SequenceMatcher
from openai import OpenAI

# OpenRouter API Config
API_KEY = "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"  # Replace with actual API key
BASE_URL = "https://openrouter.ai/api/v1"

client = OpenAI(
    base_url=BASE_URL,
    api_key=API_KEY
)


# Define correct storage paths
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # Gets 'src/main/resources'
OCR_FOLDER = os.path.join(BASE_DIR, "document_storage", "validation_json")
VALIDATION_FOLDER = os.path.join(BASE_DIR, "validation_results")

# Ensure storage directories exist
os.makedirs(VALIDATION_FOLDER, exist_ok=True)

def load_ocr_data(document_path):
    """Loads the OCR JSON file corresponding to the given document's filename."""
    document_name = os.path.basename(document_path)
    document_json_name = os.path.splitext(document_name)[0] + ".json"
    ocr_json_path = os.path.join(OCR_FOLDER, document_json_name)

    try:
        with open(ocr_json_path, "r") as file:
            return json.load(file), ocr_json_path
    except FileNotFoundError:
        print(f"Error: OCR file not found: {ocr_json_path}")
    except json.JSONDecodeError:
        print(f"Error: Failed to parse JSON in {ocr_json_path}")
    return None, None

def similarity_score(str1, str2):
    """Returns similarity score (0-1) between two strings."""
    return SequenceMatcher(None, str(str1).lower(), str(str2).lower()).ratio() if str1 and str2 else 0.0

def validate_fields(ocr_data, reference_data):
    """Validates extracted fields and assigns similarity scores."""
    extracted_name = ocr_data.get("name", "").strip()
    extracted_dob = ocr_data.get("date_of_birth", "").strip()
    extracted_id = ocr_data.get("id_number", "").strip()

    if not extracted_name and not extracted_dob and not extracted_id:
        return {"error": "OCR data is missing required fields."}

    scores = {
        "nameMatchScore": round(similarity_score(extracted_name, reference_data.get("name", "")), 2),
        "dobMatchScore": round(similarity_score(extracted_dob, reference_data.get("dob", "")), 2),
        "idMatchScore": round(similarity_score(extracted_id, reference_data.get("id_number", "")), 2)
    }

    final_score = round(0.4 * scores["nameMatchScore"] + 0.3 * scores["dobMatchScore"] + 0.3 * scores["idMatchScore"], 2)
    insights, recommendations = [], []

    if scores["nameMatchScore"] < 0.9:
        insights.append("Name mismatch detected.")
        recommendations.append("Verify the name spelling and case sensitivity.")
    if scores["dobMatchScore"] < 0.9:
        insights.append("Date of Birth mismatch detected.")
        recommendations.append("Ensure the date format is correct (YYYY-MM-DD).")
    if scores["idMatchScore"] < 0.8:
        insights.append("ID format mismatch detected.")
        recommendations.append("Check if the ID number follows the correct format.")

    return {
        "validationResults": {
            **scores,
            "finalValidationScore": final_score,
            "detailedInsight": " | ".join(insights) if insights else "All details match perfectly.",
            "recommendations": " | ".join(recommendations) if recommendations else "No issues detected."
        }
    }

def call_openrouter_for_validation(validation_results):
    """Calls OpenRouter's Gemini API for additional validation insights."""
    try:
        response = client.chat.completions.create(
            model="google/gemini-pro",
            messages=[
                {"role": "user", "content": [{"type": "text", "text": (
                    f"Given the following document validation results:\n"
                    f"{json.dumps(validation_results, indent=2)}\n\n"
                    "Provide insights into whether the document appears authentic.\n"
                    "Suggest any next steps for verification and highlight potential concerns."
                )}]
                 }
            ]
        )
        return response.choices[0].message.content.strip() or "No additional insights provided."
    except Exception as e:
        return f"Error calling OpenRouter API: {str(e)}"

def main():
    """Main function for document validation."""
    if len(sys.argv) < 2:
        print("Error: Missing document filename argument.")
        sys.exit(1)

    document_name = sys.argv[1]
    ocr_data, ocr_json_path = load_ocr_data(document_name)

    if not ocr_data:
        print(f"Error: OCR results not found for {document_name}")
        sys.exit(1)

    reference_data = {
        "document_type": "Aadhaar",
        "name": "Sapna Singh",
        "date_of_birth": "1980-01-01",
        "gender": "Female",
        "id_number": "XXXX-XXXX-6666"
    }

    validation_results = validate_fields(ocr_data, reference_data)

    if "error" in validation_results:
        print(validation_results["error"])
        sys.exit(1)

    validation_results["validationResults"]["geminiInsight"] = call_openrouter_for_validation(validation_results)
    os.makedirs(VALIDATION_FOLDER, exist_ok=True)
    document_filename = os.path.basename(document_name)  # Extract just the filename
    validation_json_path = os.path.join(VALIDATION_FOLDER, f"validation_{document_filename}.json")

    with open(validation_json_path, "w") as file:
        json.dump(validation_results, file, indent=2)

    print(json.dumps(validation_results, indent=2))

if __name__ == "__main__":
    main()