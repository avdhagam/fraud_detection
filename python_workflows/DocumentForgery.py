import base64
import json
import logging
import sys
import io
import requests
import os

# Configure stdout/stderr encoding
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# Configure logging
logging.basicConfig(filename="forgery_detection.log", level=logging.INFO,
                    format="%(asctime)s - %(levelname)s - %(message)s")

# Gemini API Key (Replace with your actual Gemini API key)
GEMINI_API_KEY = "AIzaSyBoCMnuBY55guf4dxKj0cHwQiq4Mfyrn7w"  # Replace this
API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

def analyze_forgery(image_path):
    """Analyze document image for forgery detection using Gemini API."""
    logging.info(f"Analyzing forgery for image: {image_path}")
    try:
        # Convert image to base64
        with open(image_path, "rb") as img_file:
            image_base64 = base64.b64encode(img_file.read()).decode("utf-8")

        # API Request Payload
        headers = {"Content-Type": "application/json"}
        payload = {
            "contents": [
                {
                    "parts": [
                        {
                            "text": (
                                "Analyze this document image for forgery and tampering.\n"
                                "Provide detailed JSON results including:\n"
                                "- Tampering Detection Score (0-100)\n"
                                "- Compression Artifacts Score (0-100)\n"
                                "- Color Distortion Score (0-100)\n"
                                "- QR Code Integrity (if present)\n"
                                "- Detailed insights and confidence levels."
                            )
                        },
                        {
                            "inline_data": {
                                "mime_type": "image/jpeg",
                                "data": image_base64
                            }
                        }
                    ]
                }
            ]
        }

        # Send request to Gemini API
        logging.info("Sending request to Gemini API...")
        response = requests.post(
            f"{API_URL}?key={GEMINI_API_KEY}",
            headers=headers,
            json=payload
        )
        logging.info(f"Gemini API Response: {response.status_code} - {response.text[:100]}...") # Log API response

        # Parse API response
        if response.status_code == 200:
            response_json = response.json()
            logging.info(f"Received response: {json.dumps(response_json)[:100]}...")

            # Extract JSON from response
            text_response = response_json.get("candidates", [{}])[0].get("content", {}).get("parts", [{}])[0].get("text", "")

            # Try to extract JSON if wrapped in code blocks
            import re
            json_match = re.search(r"```(?:json)?\s*([\s\S]*?)\s*```", text_response)
            if json_match:
                try:
                    parsed_json = json.loads(json_match.group(1).strip())
                    logging.info(f"Parsed JSON from code block: {json.dumps(parsed_json)[:100]}...") #Log parsed JSON
                    return parsed_json
                except json.JSONDecodeError as e:
                    logging.error(f"JSON Decode Error in code block: {e}") #Log Decode error
                    pass  # Fall back to direct parsing

            # Try to parse the entire response as JSON
            try:
                parsed_json = json.loads(text_response)
                logging.info(f"Parsed JSON directly: {json.dumps(parsed_json)[:100]}...") #Log parsed JSON
                return parsed_json
            except json.JSONDecodeError as e:
                logging.error(f"JSON Decode Error: {e} - Raw response: {text_response[:300]}") #Log Decode error

                error_message = "API response is not in valid JSON format."
                return {
                    "error": error_message,
                    "forgery_detection_summary": {
                        "tampering_score": 50,
                        "compression_artifacts_score": 50,
                        "color_distortion_score": 50,
                        "overall_authenticity": "Inconclusive - parsing error"
                    },
                    "raw_response": text_response[:300]
                }
        else:
            error_message = f"API request failed with status code {response.status_code} - {response.text}"
            logging.error(error_message)
            return {"error": error_message, "details": response.text}

    except Exception as e:
        error_message = f"Unexpected error: {str(e)}"
        logging.error(error_message)
        return {"error": error_message}

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python DocumentForgery.py \"<image_path>\""}, ensure_ascii=False))
        sys.exit(1)

    image_path = sys.argv[1]
    logging.info(f"Processing image path: {image_path}")
    if not os.path.exists(image_path):
        error_message = f"File not found: {image_path}"
        logging.error(error_message)
        print(json.dumps({"error": error_message}, ensure_ascii=False))
        sys.exit(1)

    result = analyze_forgery(image_path)
    print(json.dumps(result, indent=4, ensure_ascii=False))