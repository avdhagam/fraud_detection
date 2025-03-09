import base64
import json
import logging
import sys
import re
import io
import requests

# Configure stdout/stderr encoding
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# Configure logging
logging.basicConfig(filename="quality_assessment.log", level=logging.INFO,
                    format="%(asctime)s - %(levelname)s - %(message)s")

# Google Gemini API Key (Replace with your Gemini API key)
GEMINI_API_KEY = "AIzaSyBoCMnuBY55guf4dxKj0cHwQiq4Mfyrn7w"  # Replace this
API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"


def assess_quality(image_path):
    """Assess document quality using Google Gemini Vision API."""
    try:
        # Convert image to base64
        with open(image_path, "rb") as img_file:
            image_base64 = base64.b64encode(img_file.read()).decode("utf-8")

        # API Payload
        headers = {
            "Content-Type": "application/json",
            "x-goog-api-key": GEMINI_API_KEY
        }

        payload = {
            "contents": [
                {
                    "parts": [
                        {"text": (
                            "Evaluate the quality of this document image.\n"
                            "Provide structured JSON results with:\n"
                            "- resolution_clarity_score (0-100)\n"
                            "- skew_orientation_score (0-100)\n"
                            "- cropping_edges_score (0-100)\n"
                            "- blur_glare_score (0-100)\n"
                            "- overall_quality_score (0-100)\n"
                            "Return only a valid JSON response."
                        )},
                        {
                            "inlineData": {
                                "mimeType": "image/jpeg",
                                "data": image_base64
                            }
                        }
                    ]
                }
            ]
        }

        # Send API request
        response = requests.post(API_URL, headers=headers, json=payload)

        if response.status_code == 200:
            raw_response = response.json()
            logging.info(f"API Raw Response: {json.dumps(raw_response, indent=2)}")

            # Try to extract JSON from the response
            if "candidates" in raw_response:
                content = raw_response["candidates"][0]["content"]["parts"][0]["text"]

                json_match = re.search(r"```(?:json)?\s*([\s\S]*?)\s*```", content, re.DOTALL)
                if json_match:
                    json_string = json_match.group(1).strip()
                    return json.loads(json_string)

                try:
                    return json.loads(content)
                except json.JSONDecodeError:
                    return {"error": "Failed to parse JSON", "raw_response": content}

        else:
            logging.error(f"API Error: {response.status_code}, {response.text}")
            return {"error": f"API request failed: {response.text}"}

    except Exception as e:
        logging.error(f"Quality assessment error: {str(e)}")
        return {"error": f"Unexpected error: {str(e)}"}

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python DocumentQuality.py \"<image_path>\""}, ensure_ascii=False))
        sys.exit(1)

    image_path = sys.argv[1]
    result = assess_quality(image_path)
    print(json.dumps(result, indent=4, ensure_ascii=False))
