import sys
import os
import base64
import json
import cv2
import numpy as np
import logging
from openai import OpenAI
import uuid

# Configure logging
log_dir = os.path.join("src", "main", "resources", "document_storage", "output")
os.makedirs(log_dir, exist_ok=True)
logging.basicConfig(
    filename=os.path.join(log_dir, "document_quality.log"),
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

# OpenRouter API Config
API_KEY = "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"
BASE_URL = "https://openrouter.ai/api/v1"

client = OpenAI(base_url=BASE_URL, api_key=API_KEY)

# Document Storage Path
DOCUMENT_STORAGE_PATH = os.path.join("src", "main", "resources", "document_storage")
OUTPUT_PATH = os.path.join(DOCUMENT_STORAGE_PATH, "output")

# Ensure output directory exists
os.makedirs(OUTPUT_PATH, exist_ok=True)

def analyze_image_quality(image_path):
    """Analyze image quality using traditional computer vision methods."""
    try:
        img = cv2.imread(image_path)
        if img is None:
            logging.error(f"Error loading image: {image_path}")
            return {
                "error": f"Unable to load image from {image_path}",
                "quality_score": 0.0
            }

        # Convert to grayscale
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

        # Analyze resolution
        height, width = img.shape[:2]
        resolution_score = min(1.0, (width * height) / (1000 * 1000))

        # Analyze brightness and contrast
        brightness = np.mean(gray)
        contrast = np.std(gray)

        brightness_score = 1.0 - abs((brightness - 127.5) / 127.5)
        contrast_score = min(1.0, contrast / 80)

        # Analyze blur
        laplacian_var = cv2.Laplacian(gray, cv2.CV_64F).var()
        blur_score = min(1.0, laplacian_var / 500)

        # Analyze noise
        # Simple method: use color variation in areas that should be uniform
        # For a more accurate assessment, a dedicated denoising algorithm would be used
        noise_score = min(1.0, 1.0 - (np.std(gray) / 128))

        # Combine scores with weights
        quality_score = (
                resolution_score * 0.3 +
                brightness_score * 0.2 +
                contrast_score * 0.2 +
                blur_score * 0.2 +
                noise_score * 0.1
        )

        return {
            "resolution": {
                "width": width,
                "height": height,
                "score": float(resolution_score)
            },
            "brightness": {
                "value": float(brightness),
                "score": float(brightness_score)
            },
            "contrast": {
                "value": float(contrast),
                "score": float(contrast_score)
            },
            "blur": {
                "laplacian_var": float(laplacian_var),
                "score": float(blur_score)
            },
            "noise": {
                "score": float(noise_score)
            },
            "overall_quality_score": float(quality_score)
        }
    except Exception as e:
        logging.error(f"Error in image quality analysis: {str(e)}")
        return {
            "error": f"Error in quality analysis: {str(e)}",
            "quality_score": 0.0
        }

def assess_quality(image_path):
    """Assess document quality using both CV techniques and AI analysis."""
    try:
        if not os.path.exists(image_path):
            error_msg = f"File not found: {image_path}"
            logging.error(error_msg)
            return {"error": error_msg, "success": False, "score": 0.0}

        # Run CV-based quality analysis
        cv_results = analyze_image_quality(image_path)

        # Encode image for Vision API
        with open(image_path, "rb") as img_file:
            image_base64 = base64.b64encode(img_file.read()).decode("utf-8")

        # Prompt for AI quality analysis
        quality_prompt = (
            "Analyze this document image for quality issues. "
            "Assess readability, clarity, and whether all important fields are visible. "
            "Return a detailed analysis as JSON with the following fields:\n"
            "- readability: Assessment of how easily text can be read (0-100)\n"
            "- completeness: Whether all document parts are visible (0-100)\n"
            "- issues: Array of specific quality problems found\n"
            "- acceptable: Boolean indicating if document meets minimum quality standards\n"
            "- quality_score: Overall quality score from 0.0-1.0\n"
            "- recommendations: Suggestions for improving document quality\n"
            "IMPORTANT: Format as valid JSON only, nothing else."
        )

        # Call Vision API for advanced analysis
        completion = client.chat.completions.create(
            model="google/gemini-pro-vision",
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": quality_prompt},
                        {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{image_base64}"}}
                    ]
                }
            ]
        )

        # Process response
        raw_response = completion.choices[0].message.content
        logging.info(f"Quality API Raw Response: {raw_response}")

        # Extract JSON from response
        try:
            # Try to find JSON inside code blocks
            import re
            json_match = re.search(r"```(?:json)?\s*([\s\S]*?)\s*```", raw_response)
            if json_match:
                json_str = json_match.group(1).strip()
                ai_results = json.loads(json_str)
            else:
                # Try to find JSON with curly braces if no code block
                json_match = re.search(r"({[\s\S]*})", raw_response)
                if json_match:
                    json_str = json_match.group(1).strip()
                    ai_results = json.loads(json_str)
                else:
                    # If all else fails, try using the whole response
                    ai_results = json.loads(raw_response.strip())
        except json.JSONDecodeError as e:
            logging.error(f"Failed to parse JSON from API response: {e}")
            ai_results = {
                "error": "Failed to parse API response",
                "raw_response": raw_response,
                "quality_score": 0.5,  # Default to medium quality on parsing error
                "acceptable": False
            }

        # Combine CV and AI results for final assessment
        final_quality_score = 0.0
        if "overall_quality_score" in cv_results:
            cv_score = cv_results["overall_quality_score"]
            ai_score = ai_results.get("quality_score", 0.5)
            # Weight AI analysis higher as it considers document-specific factors
            final_quality_score = (cv_score * 0.4) + (ai_score * 0.6)
        else:
            final_quality_score = ai_results.get("quality_score", 0.0)

        combined_results = {
            "technical_analysis": {
                "resolution": cv_results.get("resolution", {}),
                "brightness": cv_results.get("brightness", {}),
                "contrast": cv_results.get("contrast", {}),
                "blur": cv_results.get("blur", {}),
                "noise": cv_results.get("noise", {})
            },
            "content_analysis": {
                "readability": ai_results.get("readability", 0),
                "completeness": ai_results.get("completeness", 0),
                "issues": ai_results.get("issues", [])
            },
            "acceptable": ai_results.get("acceptable", False),
            "recommendations": ai_results.get("recommendations", []),
            "score": final_quality_score,
            "success": True
        }

        # Classify quality
        if final_quality_score >= 0.8:
            combined_results["quality_classification"] = "High Quality"
        elif final_quality_score >= 0.5:
            combined_results["quality_classification"] = "Acceptable Quality"
        else:
            combined_results["quality_classification"] = "Poor Quality"

        # Save results to JSON file
        output_filename = f"quality_analysis_{os.path.basename(image_path).split('.')[0]}_{uuid.uuid4().hex[:8]}.json"
        output_path = os.path.join(OUTPUT_PATH, output_filename)
        with open(output_path, "w", encoding="utf-8") as json_file:
            json.dump(combined_results, json_file, indent=4)

        logging.info(f"Quality assessment results saved to: {output_path}")
        return combined_results

    except Exception as e:
        logging.error(f"Quality assessment error: {str(e)}")
        return {
            "error": f"Quality assessment error: {str(e)}",
            "score": 0.0,
            "success": False
        }

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({
            "error": "Usage: python DocumentQuality.py <image_path>",
            "success": False,
            "score": 0.0
        }))
        sys.exit(1)

    image_path = sys.argv[1]
    result = assess_quality(image_path)
    print(json.dumps(result, indent=4))