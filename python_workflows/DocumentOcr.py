import requests
import base64
import json
import sys
import os
import codecs

# Load API Key from environment variable
GEMINI_API_KEY = "AIzaSyBoCMnuBY55guf4dxKj0cHwQiq4Mfyrn7w"  # Replace with your actual key or better, use environment variables
GEMINI_OCR_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

# Ensure UTF-8 output encoding
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.buffer, "strict")

if not GEMINI_API_KEY or GEMINI_API_KEY == "YOUR_API_KEY_HERE":
    print(json.dumps({"error": "Missing or invalid API Key. Set GEMINI_API_KEY as an environment variable."}, indent=4))
    sys.exit(1)

def encode_image(image_path):
    """Encodes an image file as base64."""
    try:
        with open(image_path, "rb") as image_file:
            return base64.b64encode(image_file.read()).decode("utf-8")
    except Exception as e:
        print(json.dumps({"error": f"Failed to read image: {str(e)}"}, indent=4))
        sys.exit(1)

def extract_text_from_image(image_path):
    """Extracts text from an image using Gemini API."""
    if not os.path.exists(image_path):
        print(json.dumps({"error": f"File not found: {image_path}"}, indent=4))
        sys.exit(1)

    image_data = encode_image(image_path)
    headers = {"Content-Type": "application/json"}

    payload = {
        "contents": [
            {
                "parts": [
                    {"text": "Extract the text from this document image."},
                    {"inline_data": {"mime_type": "image/jpeg", "data": image_data}}
                ]
            }
        ]
    }

    try:
        response = requests.post(f"{GEMINI_OCR_URL}?key={GEMINI_API_KEY}", headers=headers, json=payload)
        response.raise_for_status()  # Raise an exception for HTTP errors
        response_data = response.json()

        # Extracting text correctly from the response
        extracted_text = ""
        candidates = response_data.get("candidates", [])
        if candidates and "content" in candidates[0]:
            extracted_text = candidates[0]["content"]["parts"][0]["text"].strip()

        if not extracted_text:
            error_message = "No text extracted from image."
            print(json.dumps({"error": error_message}, indent=4, ensure_ascii=False))
            sys.exit(1)

        # Save OCR output as JSON
        ocr_output = {"text": extracted_text}  # Only include the extracted text
        ocr_json_path = os.path.splitext(image_path)[0] + "_ocr.json"

        with open(ocr_json_path, "w", encoding="utf-8") as json_file:
            json.dump(ocr_output, json_file, ensure_ascii=False, indent=4)

        return {"ocr_json_path": ocr_json_path, "text": extracted_text}

    except requests.exceptions.RequestException as e:
        error_message = f"API request failed: {str(e)}"
        print(json.dumps({"error": error_message}, indent=4, ensure_ascii=False))
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python DocumentOcr.py <image_path>"}, indent=4, ensure_ascii=False))
        sys.exit(1)

    image_path = sys.argv[1]
    result = extract_text_from_image(image_path)
    print(json.dumps(result, indent=4, ensure_ascii=False))