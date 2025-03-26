import sys
import os
import base64
import json
import logging
import re
import requests
from pathlib import Path


# Configure paths
script_path = Path(__file__).resolve()
root_dir = script_path.parents[4]
sys.path.append(str(root_dir))
import config

# Configure logging
log_dir = os.path.join("src", "main", "resources", "document_storage", "output")
os.makedirs(log_dir, exist_ok=True)
logging.basicConfig(
    filename=os.path.join(log_dir, "document_quality.log"),
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

# Google Gemini API Config
GEMINI_API_KEY = config.GEMINI_API_KEY
GEMINI_OCR_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

# Document Storage Path
DOCUMENT_STORAGE_PATH = os.path.join("src", "main", "resources", "document_storage")
OUTPUT_PATH = os.path.join(DOCUMENT_STORAGE_PATH, "output")
os.makedirs(OUTPUT_PATH, exist_ok=True)

def assess_quality(image_path, quality_prompt):
    """Assess document quality using Google Gemini 1.5 Flash API."""
    try:
        if not os.path.exists(image_path):
            error_msg = f"File not found: {image_path}"
            logging.error(error_msg)
            return {"error": error_msg, "success": False, "finalQualityScore": 0.0}

        # Encode image in Base64
        with open(image_path, "rb") as img_file:
            image_base64 = base64.b64encode(img_file.read()).decode("utf-8")

        # Prepare API Request Payload
        payload = {
            "contents": [
                {"role": "user", "parts": [{"text": quality_prompt}]},
                {"role": "user", "parts": [{"inline_data": {"mime_type": "image/jpeg", "data": image_base64}}]}
            ]
        }

        headers = {"Content-Type": "application/json"}
        params = {"key": GEMINI_API_KEY}

        # Call Google Gemini 1.5 Flash API
        response = requests.post(GEMINI_OCR_URL, headers=headers, params=params, json=payload)

        # Check API response
        if response.status_code != 200:
            logging.error(f"API Error: {response.text}")
            return {"error": f"API Error: {response.text}", "success": False, "finalQualityScore": 0.0}

        ai_response = response.json()

        # Extract AI Response
        if "candidates" not in ai_response or not ai_response["candidates"]:
            logging.error("AI response is empty.")
            return {"error": "AI response is empty", "success": False, "finalQualityScore": 0.0}

        raw_response = ai_response["candidates"][0].get("content", {}).get("parts", [{}])[0].get("text", "")
        logging.info(f"Quality API Raw Response: {raw_response}")

        # Clean & Parse AI Response
        cleaned_response = re.sub(r"```json\n|\n```", "", raw_response).strip()
        try:
            ai_results = json.loads(cleaned_response)
        except json.JSONDecodeError as e:
            logging.error(f"Failed to parse JSON: {e}")
            return {"error": "Invalid AI response format", "success": False, "finalQualityScore": 0.0}

        # Construct Final Quality Response
        combined_results = {
            "qualityAnalysis": {
                key: {
                    "score": ai_results.get(f"{key}_score", 0.0),
                    "detailedInsight": ai_results.get("insights", {}).get(key, f"No insights available for {key}"),
                    "recommendations": ai_results.get("recommendations", {}).get(key, f"No recommendations available for {key}")
                }
                for key in [
                    "readability", "completeness", "blur", "lighting", "color_accuracy", "alignment", "noise_artifacts"
                ]
            },
            "finalQualityScore": ai_results.get("overall_quality_score", 0.0),
            "decision": ai_results.get("final_decision", "Review needed"),
            "success": True
        }
        return combined_results

    except Exception as e:
        logging.error(f"Quality assessment error: {str(e)}")
        return {"error": f"Quality assessment error: {str(e)}", "finalQualityScore": 0.0, "success": False}

if __name__ == "__main__":
    image_path = sys.argv[1]
    quality_prompt = sys.argv[2] if len(sys.argv) > 2 else ""
    result = assess_quality(image_path, quality_prompt)
    print(json.dumps(result, indent=4))