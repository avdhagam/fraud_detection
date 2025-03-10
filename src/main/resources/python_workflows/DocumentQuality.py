import base64
import json
import logging
import sys
from openai import OpenAI

# Configure logging
logging.basicConfig(filename="quality_assessment.log", level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

# OpenRouter API Setup
API_KEY = "api key"  # Replace with your OpenRouter API key
BASE_URL = "https://openrouter.ai/api/v1"
client = OpenAI(base_url=BASE_URL, api_key=API_KEY)

def assess_quality(image_path):
    """Assess document quality including resolution, clarity, and distortions."""
    try:
        # Convert image to base64
        with open(image_path, "rb") as img_file:
            image_base64 = base64.b64encode(img_file.read()).decode("utf-8")

        # API Request
        completion = client.chat.completions.create(
            extra_headers={
                "Authorization": f"Bearer {API_KEY}",
                "Content-Type": "application/json"
            },
            extra_body={},
            model="google/gemini-pro-vision",
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "text",
                            "text": (
                                "Evaluate the quality of this document image.\n"
                                "Provide structured JSON results with:\n"
                                "- Resolution & Clarity Score (0-100)\n"
                                "- Skew & Orientation Score (0-100)\n"
                                "- Cropping & Edges Score (0-100)\n"
                                "- Blur & Glare Detection Score (0-100)\n"
                                "- Overall Quality Score (0-100) with insights."
                            )
                        },
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:image/jpeg;base64,{image_base64}"
                            }
                        }
                    ]
                }
            ]
        )

        # Extract response
        if hasattr(completion, "choices") and completion.choices:
            response_json = completion.choices[0].message.content
            return json.loads(response_json)
        else:
            return {"error": "No valid response from API"}

    except Exception as e:
        logging.error(f"Quality assessment error: {str(e)}")
        return {"error": f"Unexpected error: {str(e)}"}

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python DocumentQuality.py <image_path>"}))
        sys.exit(1)

    image_path = sys.argv[1]
    result = assess_quality(image_path)
    print(json.dumps(result, indent=4))
