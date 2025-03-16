import sys
import os
import base64
import json
import cv2
import numpy as np
import logging
from openai import OpenAI
import uuid
import prompts

from pathlib import Path
script_path = Path(__file__).resolve() # finds absolute path of script
root_dir = script_path.parents[4]  # Calculate root directory by moving up four levels
sys.path.append(str(root_dir))

import config

# Configure logging
log_dir = os.path.join("src", "main", "resources", "document_storage", "output")
os.makedirs(log_dir, exist_ok=True)
logging.basicConfig(
    filename=os.path.join(log_dir, "document_forgery.log"),
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

# OpenRouter API Config
API_KEY = config.OPENROUTER_API_KEY
BASE_URL = "https://openrouter.ai/api/v1"
client = OpenAI(base_url=BASE_URL, api_key=API_KEY)

# Document Storage Path
DOCUMENT_STORAGE_PATH = os.path.join("src", "main", "resources", "document_storage")
OUTPUT_PATH = os.path.join(DOCUMENT_STORAGE_PATH, "output")
os.makedirs(OUTPUT_PATH, exist_ok=True)

def detect_image_manipulation(image_path):
    """Detect potential image manipulation using traditional CV methods."""
    try:
        img = cv2.imread(image_path)
        if img is None:
            raise ValueError(f"Unable to load image from {image_path}")

        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        noise_level = np.std(gray)

        temp_path = os.path.join(OUTPUT_PATH, f"temp_{uuid.uuid4().hex[:8]}.jpg")
        cv2.imwrite(temp_path, img, [int(cv2.IMWRITE_JPEG_QUALITY), 90])
        compressed_img = cv2.imread(temp_path)
        os.remove(temp_path)

        if compressed_img is None:
            raise ValueError("Error during compression analysis")

        diff = cv2.absdiff(img, compressed_img)
        diff_gray = cv2.cvtColor(diff, cv2.COLOR_BGR2GRAY)
        ela_score = np.mean(diff_gray)

        tampering_score = min(1.0, max(0.0, (ela_score / 10) * 0.5 + (noise_level > 50) * 0.5))

        return {
            "tamperingScore": float(tampering_score),
            "metadataAnomalyScore": 0.0,
            "formatConsistencyScore": 0.0,
            "securityFeatureScore": 0.0,
            "backgroundConsistencyScore": 0.0
        }
    except Exception as e:
        logging.error(f"Error in image manipulation detection: {str(e)}")
        return {"tamperingScore": 0.5, "metadataAnomalyScore": 0.5, "formatConsistencyScore": 0.5,
                "securityFeatureScore": 0.5, "backgroundConsistencyScore": 0.5}

def analyze_forgery(image_path):
    """Analyze document for potential forgery indicators."""
    try:
        if not os.path.exists(image_path):
            raise FileNotFoundError(f"File not found: {image_path}")

        cv_results = detect_image_manipulation(image_path)
        try:
            with open(image_path, "rb") as img_file:
                image_base64 = base64.b64encode(img_file.read()).decode("utf-8")
        except Exception as e:
            logging.error(f"Error encoding image: {str(e)}")
            return {"error": f"Failed to encode image: {str(e)}", "success": False}

        forgery_prompt = prompts.PROMPTS["DOCUMENT_FORGERY_PROMPT"]

        completion = client.chat.completions.create(
            model="google/gemini-pro-vision",
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": forgery_prompt},
                        {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{image_base64}"}}
                    ]
                }
            ]
        )

        raw_response = completion.choices[0].message.content
        logging.info(f"Forgery API Raw Response: {raw_response}")
        # Properly clean the response for JSON parsing
        if "```json" in raw_response:
            # Remove the ```json prefix and trailing ``` suffix
            json_start = raw_response.find("```json") + len("```json")
            json_end = raw_response.rfind("```")
            if json_end > json_start:
                cleaned_response = raw_response[json_start:json_end].strip()
            else:
                cleaned_response = raw_response[json_start:].strip()
        else:
            cleaned_response = raw_response.strip()

        # Log the cleaned response to ensure it's not empty
        logging.info(f"Cleaned API Response: {cleaned_response}")

        if not cleaned_response:
            logging.error("Error: Cleaned response is empty.")
            return {"error": "Empty response from the forgery analysis API", "success": False}

        try:
            # First try direct parsing
            ai_results = json.loads(cleaned_response)
        except json.JSONDecodeError:
            # If that fails, try additional cleanup
            try:
                # Remove any non-JSON characters and try again
                import re
                json_pattern = re.search(r'(\{.*\})', cleaned_response, re.DOTALL)
                if json_pattern:
                    cleaned_json = json_pattern.group(1)
                    ai_results = json.loads(cleaned_json)
                else:
                    raise ValueError("Could not extract JSON pattern")
            except Exception as e:
                logging.error(f"Failed to parse JSON after cleanup: {str(e)}")
                return {"error": "Invalid JSON response", "success": False}

        combined_results = {
            "forgeryAnalysis": {
                "tamperingAnalysis": {
                    "tamperingScore": ai_results.get("Tampering Analysis", {}).get("Score", 0),
                    "detailedInsight": ai_results.get("Tampering Analysis", {}).get("Insights", "No tampering insights available.")
                },
                "metadataAnalysis": {
                    "metadataAnomalyScore": ai_results.get("Metadata Analysis", {}).get("Score", 0),
                    "detailedInsight": ai_results.get("Metadata Analysis", {}).get("Insights", "No metadata insights available."),
                },
                "formatConsistencyAnalysis": {
                    "formatConsistencyScore": ai_results.get("Format Consistency", {}).get("Score", 0),
                    "detailedInsight": ai_results.get("Format Consistency", {}).get("Insights", "No format inconsistencies detected.")
                },
                "securityFeatureAnalysis": {
                    "securityFeatureScore": ai_results.get("Security Features", {}).get("Score", 0),
                    "detailedInsight": ai_results.get("Security Features", {}).get("Insights", "Security features analysis unavailable.")
                },
                "backgroundIntegrityAnalysis": {
                    "backgroundConsistencyScore": ai_results.get("Background Integrity", {}).get("Score", 0),
                    "detailedInsight": ai_results.get("Background Integrity", {}).get("Insights", "No background inconsistencies found.")
                },
                "overallForgeryAssessment": {
                    "finalForgeryRiskScore": ai_results.get("Forgery Risk Score", 0.0),
                    "decision": ai_results.get("Final Decision", "Manual Review Recommended"),
                }
            },
            "success": True
        }


        output_filename = f"forgery_analysis_{uuid.uuid4().hex[:8]}.json"
        output_path = os.path.join(OUTPUT_PATH, output_filename)
        with open(output_path, "w", encoding="utf-8") as json_file:
            json.dump(combined_results, json_file, indent=4)

        logging.info(f"Forgery analysis results saved to: {output_path}")
        return combined_results

    except Exception as e:
        logging.error(f"Forgery analysis error: {str(e)}")
        return {"error": str(e), "finalForgeryRiskScore": 0.7, "success": False}

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python DocumentForgery.py <image_path>", "success": False}))
        sys.exit(1)

    image_path = sys.argv[1]
    result = analyze_forgery(image_path)
    print(json.dumps(result, indent=4))
