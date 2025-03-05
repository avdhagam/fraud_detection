import base64
import json
import logging
import sys
from openai import OpenAI

# Configure logging
logging.basicConfig(filename="forgery_detection.log", level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

# OpenRouter API Setup
API_KEY = "api key"  # Replace with your OpenRouter API key
BASE_URL = "https://openrouter.ai/api/v1"
client = OpenAI(base_url=BASE_URL, api_key=API_KEY)

def analyze_forgery(image_path):
    """Analyze document image for forgery detection."""
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
        logging.error(f"Forgery detection error: {str(e)}")
        return {"error": f"Unexpected error: {str(e)}"}

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python DocumentForgery.py <image_path>"}))
        sys.exit(1)

    image_path = sys.argv[1]
    result = analyze_forgery(image_path)
    print(json.dumps(result, indent=4))
