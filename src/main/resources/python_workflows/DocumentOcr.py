import sys
import os
import base64
import json
import cv2
import numpy as np
from openai import OpenAI

# OpenRouter API Config
API_KEY = "api key"  # 🔹 Replace with your actual API key
BASE_URL = "https://openrouter.ai/api/v1"

client = OpenAI(
    base_url=BASE_URL,
    api_key=API_KEY
)

# Document Storage Path
DOCUMENT_STORAGE_PATH = os.path.join("src", "main", "resources", "document_storage")

def preprocess_image(image_path):
    """
    Preprocess the document image for better OCR accuracy.
    - Convert to grayscale
    - Apply adaptive thresholding
    - Deskew if needed
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
        processed_path = image_path.replace(".jpg", "_processed.jpg").replace(".png", "_processed.png")
        cv2.imwrite(processed_path, img)
        return processed_path, None

    except Exception as e:
        return None, f"Preprocessing error: {str(e)}"

import re

def extract_text(image_path):
    try:
        # Check if file exists
        if not os.path.exists(image_path):
            return {"error": f"File not found: {image_path}"}

        # Preprocess the image
        preprocessed_path, preprocess_error = preprocess_image(image_path)
        if preprocess_error:
            return {"error": preprocess_error, "insight": "Image preprocessing failed. OCR may be inaccurate."}

        # Encode image to base64
        with open(preprocessed_path, "rb") as img_file:
            image_base64 = base64.b64encode(img_file.read()).decode("utf-8")

        # Request payload with refined prompt
        completion = client.chat.completions.create(
            extra_headers={"Authorization": f"Bearer {API_KEY}", "Content-Type": "application/json"},
            model="google/gemini-pro-vision",
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": (
                            "Perform high-accuracy OCR on this document image.\n"
                            "Classify the document type (Aadhaar or PAN).\n"
                            "Extract accurate text fields in a structured JSON:\n"
                            "1. document_type (Aadhaar, PAN, or Unknown)\n"
                            "2. name\n"
                            "3. date_of_birth\n"
                            "4. gender\n"
                            "5. id_number(Adhaar/PAN number)\n"
                            "6. qr_code_data (if present)\n"
                            "7. confidence_score (0-100, confidence in extraction)\n"
                            "If extraction is unreliable, return 'null' instead of incorrect values.\n"
                            "Ensure support for:\n"
                            "- Aadhaar in 16 languages: Assamese, Bengali, English, Gujarati, Hindi, Kannada, Konkani, Malayalam, Marathi, Manipuri, Nepali, Odia, Punjabi, Tamil, Telugu, Urdu.\n"
                            "- PAN in Hindi & English."
                        )},
                        {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{image_base64}"}}
                    ]
                }
            ]
        )

        # Print API raw response for debugging
        raw_response = completion.choices[0].message.content
        print("🔹 API Raw Response:", raw_response)

        # **🔹 Extract JSON from Markdown formatting**
        json_match = re.search(r"```json\n(.*)\n```", raw_response, re.DOTALL)
        if json_match:
            json_string = json_match.group(1).strip()
        else:
            return {"error": "API response not in expected JSON format", "raw_response": raw_response}

        # Convert string to JSON
        extracted_data = json.loads(json_string)

        # Apply final checks
        extracted_data = postprocess_extracted_data(extracted_data)

        # Save extracted data as JSON for workflow
        save_extraction_results(image_path, extracted_data)

        return extracted_data

    except Exception as e:
        return {"error": str(e)}

def postprocess_extracted_data(data):
    """
    Final cleanup on extracted data:
    - Mask Aadhaar numbers (XXXX-XXXX-1234)
    - Ensure no hallucinations (replace unlikely values with null)
    """
    if data.get("document_type") == "Aadhaar":
        if "id_number" in data and len(data["id_number"]) == 12 and data["id_number"].isdigit():
            data["id_number"] = f"XXXX-XXXX-{data['id_number'][-4:]}"  # Mask Aadhaar

    if data.get("confidence_score", 100) < 50:
        for key in ["name", "date_of_birth", "gender", "id_number"]:
            data[key] = None  # Set unreliable data to null

    return data

def save_extraction_results(image_path, extracted_data):
    """
    Save the extracted JSON data in the document storage folder.
    """
    output_path = image_path.replace(".jpg", ".json").replace(".png", ".json")
    with open(output_path, "w") as json_file:
        json.dump(extracted_data, json_file, indent=4)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python DocumentOcr.py <image_filename>"}))
        sys.exit(1)

    image_filename = sys.argv[1]
    image_path = os.path.join(DOCUMENT_STORAGE_PATH, image_filename)

    result = extract_text(image_path)
    print(json.dumps(result, indent=4))
