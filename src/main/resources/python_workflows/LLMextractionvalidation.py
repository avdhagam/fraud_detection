import os
import json
import requests

def extract_transcript_information(transcript):
    """
    Extract key information from a call transcript using OpenRouter API.

    Args:
        transcript (str): The call transcript text.

    Returns:
        str: Extracted information in JSON format.
    """
    # OpenRouter API key
    api_key = os.environ.get("OPENROUTER_API_KEY", "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")

    # API URL
    url = "https://openrouter.ai/api/v1/chat/completions"

    # Headers
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
        "HTTP-Referer": "https://your-app-domain.com",  # Replace with your actual domain
        "X-Title": "Transcript Analysis App",  # Replace with your app name
    }

    # Prompt for extraction
    extraction_prompt = f"""
    Extract the following information from this transcript:
    1. Name of reference (person being called)
    2. Name of subject (person who took the loan)
    3. Address of subject
    4. Relation between reference and subject
    5. Occupation of subject

    Respond ONLY in JSON format with these keys: reference_name, subject_name, subject_address, relation_to_subject, subject_occupation

    Transcript:
    {transcript}
    """

    # Request payload
    payload = {
        "model": "google/gemini-2.0-flash-lite-001",  # You can try "anthropic/claude-3-haiku" as well
        "messages": [
            {"role": "user", "content": extraction_prompt}
        ]
    }

    # Make the request
    response = requests.post(url, headers=headers, data=json.dumps(payload))

    # Parse response
    if response.status_code == 200:
        try:
            result = response.json()
            return result.get("choices", [{}])[0].get("message", {}).get("content", "Error: No content found")
        except json.JSONDecodeError:
            return "Error: Failed to parse JSON response"
    else:
        return f"Error: API request failed with status code {response.status_code}, Response: {response.text}"

# Example usage
if __name__ == "__main__":
    # Sample transcript
    transcript = """
    2.191  2.574 SPEAKER_01                                                        Hello? 4.213  4.597 SPEAKER_00                                                        Hello? 5.014  7.257 SPEAKER_00                                             Hi, is it Ashish? 7.257  7.581 SPEAKER_01                                                        Hello? 8.499 10.182 SPEAKER_00                                           Sorry, is it Arjun?10.181 11.864 SPEAKER_01                                                          Yes.11.863 14.066 SPEAKER_00                                Hi, Arjun, Shilpa from Car 24.15.307 15.730 SPEAKER_01                                                         Okay.16.769 18.611 SPEAKER_00                               It's a verification called C.Q.18.611 20.174 SPEAKER_00                                Matheo has given your address.21.415 21.897 SPEAKER_01                                                     Ah, okay.23.217 26.461 SPEAKER_00 Actually, he has taken a loan from us, so that is the reason.26.481 27.223 SPEAKER_00                                          How do you know him?28.944 29.806 SPEAKER_01                                          Ah, I'm a colleague.31.334 33.698 SPEAKER_00              Okay, is he doing a job or a business right now?35.100 35.703 SPEAKER_01                                                   No, no job.36.883 38.006 SPEAKER_00                                      And where does he stays?38.005 40.229 SPEAKER_00                                                  His address?40.289 43.674 SPEAKER_01                          He is now in Pattimathur, Ernakulam.45.217 46.420 SPEAKER_00                                 Sorry, sorry, can you repeat?47.080 48.483 SPEAKER_00                                       Pattimathur, Ernakulam.49.224 50.146 SPEAKER_00                                              Okay, thank you.51.167 51.267 SPEAKER_00                                                         Okay.
    """

    # Extract information
    result = extract_transcript_information(transcript)
    print("Extracted Information:")
    print(result)
