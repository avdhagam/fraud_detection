import sys
import os
import base64
import json
import logging
import re
from openai import OpenAI

# Configure logging
log_dir = os.path.join("src", "main", "resources", "document_storage", "output")
os.makedirs(log_dir, exist_ok=True)
logging.basicConfig(
    filename=os.path.join(log_dir, "document_quality.log"),
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

# OpenRouter API Config
API_KEY = "api"
BASE_URL = "https://openrouter.ai/api/v1"

client = OpenAI(base_url=BASE_URL, api_key=API_KEY)

# Document Storage Path
DOCUMENT_STORAGE_PATH = os.path.join("src", "main", "resources", "document_storage")
OUTPUT_PATH = os.path.join(DOCUMENT_STORAGE_PATH, "output")
os.makedirs(OUTPUT_PATH, exist_ok=True)


def assess_quality(image_path):
    """Assess document quality using AI-based analysis."""
    try:
        if not os.path.exists(image_path):
            error_msg = f"File not found: {image_path}"
            logging.error(error_msg)
            return {"error": error_msg, "success": False, "finalQualityScore": 0.0}

        # Encode image for AI processing
        with open(image_path, "rb") as img_file:
            image_base64 = base64.b64encode(img_file.read()).decode("utf-8")

        # AI Quality Analysis Prompt
        quality_prompt = (
            "Analyze this document image for quality assessment with a focus on readability, clarity, and completeness. Identify the following aspects:\n\n"
            " **Readability Analysis** - Assess if text is clear, legible, and distortion-free.\n"
            " **Completeness Analysis** - Verify if all document sections are fully visible.\n"
            " **Blur Detection** - Identify if blur affects readability.\n"
            " **Lighting Issues** - Detect overexposure, shadows, or uneven brightness.\n"
            " **Color Accuracy** - Identify distortions or unnatural color shifts.\n"
            " **Document Alignment** - Check for tilt or misalignment impacting clarity.\n"
            " **Noise & Artifacts** - Identify unwanted marks, background noise, or distortions.\n\n"
            "Provide the results in **structured JSON format** with:\n"
            "- **Scores (0-1)** for each category.\n"
            "- **Detailed insights** explaining the score.\n"
            "- **Clear recommendations** for improvement.\n"
            "- **An overall quality score** and a final decision (Good, Acceptable, Poor)."
        )

        # Call AI Model
        completion = client.chat.completions.create(
            model="google/gemini-pro-vision",
            messages=[
                {"role": "user", "content": [
                    {"type": "text", "text": quality_prompt},
                    {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{image_base64}"}}
                ]}
            ]
        )

        # Extract AI Response
        raw_response = completion.choices[0].message.content
        logging.info(f"Quality API Raw Response: {raw_response}")

        # Clean & Parse AI Response
        cleaned_response = re.sub(r"```json\n|\n```", "", raw_response).strip()

        try:
            ai_results = json.loads(cleaned_response)
        except json.JSONDecodeError as e:
            logging.error(f"Failed to parse JSON: {e}")
            ai_results = {}

        # Construct Final Quality Response
        combined_results = {
            "qualityAnalysis": {
                "readability": {
                    "readabilityScore": ai_results.get("readability_score", 0),
                    "detailedInsight": ai_results.get("insights", {}).get("readability", "Readability insights not available."),
                    "recommendations": ai_results.get("recommendations", {}).get("readability", "Ensure document text is clear.")
                },
                "completeness": {
                    "completenessScore": ai_results.get("completeness_score", 0),
                    "detailedInsight": ai_results.get("insights", {}).get("completeness", "Completeness insights unavailable."),
                    "recommendations": ai_results.get("recommendations", {}).get("completeness", "Ensure full document is captured.")
                },
                "blur": {
                    "blurScore": ai_results.get("blur_score", 0),
                    "detailedInsight": ai_results.get("insights", {}).get("blur", "No blur analysis found."),
                    "recommendations": ai_results.get("recommendations", {}).get("blur", "Retake photo with better focus.")
                },
                "lighting": {
                    "lightingScore": ai_results.get("lighting_score", 0),
                    "detailedInsight": ai_results.get("insights", {}).get("lighting", "No lighting insights available."),
                    "recommendations": ai_results.get("recommendations", {}).get("lighting", "Adjust lighting to avoid glare.")
                },
                "colorAccuracy": {
                    "colorAccuracyScore": ai_results.get("color_accuracy_score", 0),
                    "detailedInsight": ai_results.get("insights", {}).get("color_accuracy", "No color accuracy insights available."),
                    "recommendations": ai_results.get("recommendations", {}).get("color_accuracy", "Ensure colors are true to original document.")
                },
                "alignment": {
                    "alignmentScore": ai_results.get("alignment_score", 0),
                    "detailedInsight": ai_results.get("insights", {}).get("alignment_score", "No alignment insights found."),
                    "recommendations": ai_results.get("recommendations", {}).get("alignment_score", "Align document properly before scanning.")
                },
                "noiseArtifacts": {
                    "noiseArtifactsScore": ai_results.get("noise_artifacts_score", 0),
                    "detailedInsight": ai_results.get("insights", {}).get("noise_artifacts", "No noise/artifact insights available."),
                    "recommendations": ai_results.get("recommendations", {}).get("noise_artifacts", "Ensure document is clean before scanning.")
                }
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
    result = assess_quality(image_path)
    print(json.dumps(result, indent=4))
