import sys
import os
import base64
import cv2
import re
import json
import logging
from openai import OpenAI

# OpenRouter API Config
API_KEY = "api key"  # Replace with actual API key
BASE_URL = "https://openrouter.ai/api/v1"

client = OpenAI(
    base_url=BASE_URL,
    api_key=API_KEY
)

# Document Storage Paths
DOCUMENT_STORAGE_PATH = os.path.join("src", "main", "resources", "document_storage")
VALIDATION_STORAGE_PATH = os.path.join(DOCUMENT_STORAGE_PATH, "validation_json")

# Ensure storage directories exist
os.makedirs(VALIDATION_STORAGE_PATH, exist_ok=True)

# Configure Logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

def preprocess_image(image_path):
    """
    Preprocess document image for better OCR accuracy:
    - Convert to grayscale
    - Apply adaptive thresholding
    - Denoise using Gaussian blur
    """
    try:
        img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
        if img is None:
            return None, f"Error: Unable to load image from {image_path}"


        # Apply Gaussian Blur to remove noise
        img = cv2.GaussianBlur(img, (5, 5), 0)

        # Adaptive thresholding to enhance text clarity
        img = cv2.adaptiveThreshold(img, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 31, 2)

        # Save preprocessed image temporarily
        processed_path = f"{os.path.splitext(image_path)[0]}_processed.jpg"
        cv2.imwrite(processed_path, img)
        return processed_path, None
    except Exception as e:
        return None, f"Preprocessing error: {str(e)}"

def extract_text(image_path):
    """Extracts structured text from an Aadhaar or PAN card using OCR & AI."""
    try:
        if not os.path.exists(image_path):
            return {"error": f"File not found: {image_path}"}
        logging.info(f"Processing image: {image_path}")

        # Preprocess the image
        preprocessed_path, preprocess_error = preprocess_image(image_path)
        if preprocess_error:
            return {"error": preprocess_error, "insight": "Image preprocessing failed. OCR may be inaccurate."}

        # Encode image to base64
        with open(preprocessed_path, "rb") as img_file:
            image_base64 = base64.b64encode(img_file.read()).decode("utf-8")

        # Request payload with refined prompt
        completion = client.chat.completions.create(
            model="google/gemini-pro-vision",
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": (
                            "Perform high-accuracy OCR on this document image.\n"
                            "Return ONLY a valid JSON response, without explanations or extra text.\n"
                            "Ensure the JSON is structured as follows:\n"
                            "{\n"
                            '  "document_type": "Aadhaar" | "PAN" | "Unknown",\n'
                            '  "name": "string" | null,\n'
                            '  "date_of_birth": "YYYY-MM-DD" | "YYYY" | null,\n'
                            '  "gender": "Male" | "Female" | "Other" | null,\n'
                            '  "id_number": "string" | null,\n'
                            '  "confidence_score": integer (0-100),\n'
                            '  "insights": "string",\n'
                            '  "issues": ["string", ...] | [],\n'
                            '  "recommendations": "string"\n'
                            "}\n\n"
                            "**OCR Evaluation Rules:**\n"
                            "- Extract text with the highest possible accuracy.\n"
                            "- If a field is missing or unreadable, return null instead of incorrect values.\n"
                            "- Provide insights specifically about the OCR accuracy:\n"
                            "  - Was the text extracted clearly?\n"
                            "  - Were there any misread characters or missing fields?\n"
                            "  - Any unusual variations in font or format?\n"
                            "- Identify potential OCR-related issues (e.g., partial extractions, incorrect character swaps, missing segments).\n"
                            "- Generate **specific** recommendations for improving OCR extraction (e.g., re-scanning with better contrast, cropping the document edges, retrying with higher resolution).\n"
                        )},
                        {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{image_base64}"}}
                    ]
                }
            ]
        )

        # Handle API response safely
        if not completion or not hasattr(completion, "choices") or not completion.choices:
            logging.error("No valid response received from API")
            return {"error": "No valid response received from API"}

        # Extract JSON response directly
        raw_response = completion.choices[0].message.content if completion.choices[0].message else None
        if not raw_response:
            logging.error("Empty response received from API")
            return {"error": "Empty response received from API"}

        raw_response = raw_response.strip()
        logging.info(f"Gemini API Response: {raw_response}")  # Debugging purpose

        # Ensure it's properly formatted JSON
        try:
            # Remove Markdown-style JSON formatting if present
            cleaned_response = re.sub(r"```json\n|\n```", "", raw_response).strip()

            # Try to parse JSON
            extracted_data = json.loads(cleaned_response)
        except json.JSONDecodeError as e:
            logging.error(f"Failed to parse JSON response: {str(e)}")
            return {"error": f"Failed to parse JSON response: {str(e)}", "raw_response": raw_response}

        # Post-process extracted data
        extracted_data = postprocess_extracted_data(extracted_data)

        # Save extracted JSON for validation
        save_validation_json(image_path, extracted_data)

        return generate_ocr_response(extracted_data)  # Return final structured response

    except Exception as e:
        return {"error": str(e)}


def postprocess_extracted_data(data):
    """
    Final cleanup on extracted data:
    - Mask Aadhaar numbers (XXXX-XXXX-1234)
    - Ensure no hallucinations (replace unlikely values with null)
    - Adjust confidence-based validation
    """
    if data.get("document_type") == "Aadhaar":
        if "id_number" in data and len(data["id_number"]) == 12 and data["id_number"].isdigit():
            data["id_number"] = f"XXXX-XXXX-{data['id_number'][-4:]}"  # Mask Aadhaar

    confidence = data.get("confidence_score", 100)

    if 30 <= confidence < 50:
        data.setdefault("issues", []).append("Some fields have low confidence. Manual review recommended.")

    if confidence < 30:
        for key in ["name", "date_of_birth", "gender", "id_number"]:
            data[key] = None  # Set unreliable data to null

    return data

def save_validation_json(image_path, extracted_data):
    """Save extracted fields JSON for validation.py."""
    validation_json = {
        "document_type": extracted_data.get("document_type"),
        "name": extracted_data.get("name"),
        "date_of_birth": extracted_data.get("date_of_birth"),
        "gender": extracted_data.get("gender"),
        "id_number": extracted_data.get("id_number")
    }
    output_path = os.path.join(VALIDATION_STORAGE_PATH, os.path.basename(image_path).replace(".jpg", ".json").replace(".png", ".json"))
    with open(output_path, "w") as json_file:
        json.dump(validation_json, json_file, indent=4)

def generate_ocr_response(data):
    """Builds structured response with Gemini's insights & recommendations."""
    return {
        "extractedText": {
            "document_type": data.get("document_type"),
            "name": data.get("name"),
            "date_of_birth": data.get("date_of_birth"),
            "gender": data.get("gender"),
            "id_number": data.get("id_number"),
            "confidence_score": data.get("confidence_score", 0),
            "insights": data.get("insights", "No insights available."),  # Use Gemini's insights
            "issues": data.get("issues", []),
            "recommendations": data.get("recommendations", "No recommendations provided.")  # Use Gemini's recommendations
        }
    }


if __name__ == "__main__":
    image_filename = sys.argv[1]
    image_path = os.path.join(DOCUMENT_STORAGE_PATH, image_filename)
    result = extract_text(image_path)
    print(json.dumps(result, indent=4))
