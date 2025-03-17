import os
os.environ["OMP_NUM_THREADS"] = "1"

import json
import re
import requests
from pathlib import Path
import Transcription # Import transcript.py for processing
import prompts
import sys

script_path = Path(__file__).resolve() # finds absolute path of script
root_dir = script_path.parents[4]  # Calculate root directory by moving up four levels
sys.path.append(str(root_dir)) # Add the project's root directory to the Python path
import config


# Define base path for stored audio files
root_path = Path(__file__).resolve().parent.parent  # Moves up two levels
base_path = root_path / "audio_storage"

# def get_uuid():
#     if len(sys.argv)>1:
#         return sys.argv[1]
#     else:
#         print("Error: UUID not provided")
#         sys.exit(1)
#
# uuid = get_uuid()

def get_audio_file_path(uuid):
    """Reconstruct the full path of the audio file using UUID."""
    file_name = f"{uuid}.mp3"  # Assuming files are stored as UUID.mp3
    file_path = base_path / file_name

    if not file_path.exists():
        print(f"Error: File {file_name} not found in {base_path}")
        sys.exit(1)

    return str(file_path)

def parse_transcript_to_structured_format(transcript_text):
    """
    Parse the raw transcript text into a structured array of transcript segments.

    Args:
        transcript_text (str): Raw transcript text

    Returns:
        list: List of dictionaries with structured transcript data
    """
    structured_transcript = []

    # Regular expression to match transcript segments
    # Format: <start_time> <end_time> <speaker> <text>
    pattern = r'(\d+\.\d+)\s+(\d+\.\d+)\s+(SPEAKER_\d+)\s+(.+?)(?=\d+\.\d+\s+\d+\.\d+\s+SPEAKER_|\Z)'

    matches = re.finditer(pattern, transcript_text, re.DOTALL)

    for match in matches:
        start_time = float(match.group(1))
        end_time = float(match.group(2))
        speaker = match.group(3)
        text = match.group(4).strip()

        structured_transcript.append({
            "start_time": start_time,
            "end_time": end_time,
            "speaker": speaker,
            "text": text
        })

    return structured_transcript

def extract_transcript_information(transcript, ground_truth):
    """
    Extract key information and score against ground truth in a single API call.

    Args:
        transcript (str): The call transcript text.
        ground_truth (dict): The ground truth information.

    Returns:
        dict: Extracted information and scoring results.
    """
    # OpenRouter API key
    api_key = config.OPENROUTER_API_KEY
    if not api_key:
        sys.exit(1)

    # API URL
    url = "https://openrouter.ai/api/v1/chat/completions"

    # Headers
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
        "HTTP-Referer": "https://your-app-domain.com",
        "X-Title": "Transcript Analysis App",
    }

    # Convert ground truth to string
    ground_truth_str = json.dumps(ground_truth)

    # Combined prompt for extraction and scoring
    combined_prompt_template = prompts.PROMPTS["EXTRACTION_SCORING_PROMPT"]
    combined_prompt = combined_prompt_template.format(
        transcript=transcript,
        ground_truth_str=ground_truth_str
    )

# Request payload
    payload = {
        "model": "google/gemini-2.0-flash-lite-001",
        "messages": [
            {"role": "user", "content": combined_prompt}
        ]
    }

    # Make the request
    response = requests.post(url, headers=headers, data=json.dumps(payload))

    # Parse response
    if response.status_code == 200:
        try:
            result = response.json()
            content = result.get("choices", [{}])[0].get("message", {}).get("content", "")

            # Clean and parse the JSON response
            content = re.sub(r'^```json\s*|\s*```$', '', content, flags=re.MULTILINE).strip()
            parsed_result = json.loads(content)

            # Determine status based on overall score
            overall_score = parsed_result.get("overall_score", 0)
            parsed_result["status"] = "accept" if overall_score >= 0.7 else "reject"

            return parsed_result
        except json.JSONDecodeError as e:

            return {
                "error": "Failed to parse response",
                "status": "reject"
            }
        except Exception as e:
            print(f"Unexpected error: {e}")
            return {
                "error": str(e),
                "status": "reject"
            }
    else:

        return {
            "error": f"API request failed with status code {response.status_code}",
            "status": "reject"
        }

def process_transcript(transcript, ground_truth):
    """
    Process the transcript by extracting information and scoring against ground truth in a single step.

    Args:
        transcript (str): The call transcript
        ground_truth (dict): The ground truth information

    Returns:
        dict: Comprehensive results including extraction and scoring
    """
    # Parse transcript into structured format
    structured_transcript = parse_transcript_to_structured_format(transcript)

    # Extract and score in one step
    results = extract_transcript_information(transcript, ground_truth)

    # Construct the final output
    return {
        "transcript": structured_transcript,
        "extracted_result": results.get("extracted_result", {}),
        "scoring_results": {
            "field_by_field_scores": results.get("field_by_field_scores", {}),
            "overall_score": results.get("overall_score", 0),
            "explanation": results.get("explanation", {})
        },
        "status": results.get("status", "reject")
    }


# Example usage
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python llmextractor.py <UUID>")
        sys.exit(1)

    uuid = sys.argv[1]  # UUID received from API
    audio_path = get_audio_file_path(uuid)

    # Call the transcription function from transcript.py
    transcript = Transcription.get_transcripts(audio_path)



    # print("\nFinal Transcription Output:\n")
    # print(type(transcript))
    # print(transcript)

    # Print final output (or return to API)

    #print(transcript)

    # Sample transcript
    # transcript = """
    # 2.191  2.574 SPEAKER_01                                                        Hello? 4.213  4.597 SPEAKER_00                                                        Hello? 5.014  7.257 SPEAKER_00                                             Hi, is it Ashish? 7.257  7.581 SPEAKER_01                                                        Hello? 8.499 10.182 SPEAKER_00                                           Sorry, is it Arjun?10.181 11.864 SPEAKER_01                                                          Yes.11.863 14.066 SPEAKER_00                                Hi, Arjun, Shilpa from Car 24.15.307 15.730 SPEAKER_01                                                         Okay.16.769 18.611 SPEAKER_00                               It's a verification called C.Q.18.611 20.174 SPEAKER_00                                Matheo has given your address.21.415 21.897 SPEAKER_01                                                     Ah, okay.23.217 26.461 SPEAKER_00 Actually, he has taken a loan from us, so that is the reason.26.481 27.223 SPEAKER_00                                          How do you know him?28.944 29.806 SPEAKER_01                                          Ah, I'm a colleague.31.334 33.698 SPEAKER_00              Okay, is he doing a job or a business right now?35.100 35.703 SPEAKER_01                                                   No, no job.36.883 38.006 SPEAKER_00                                      And where does he stays?38.005 40.229 SPEAKER_00                                                  His address?40.289 43.674 SPEAKER_01                          He is now in Pattimathur, Ernakulam.45.217 46.420 SPEAKER_00                                 Sorry, sorry, can you repeat?47.080 48.483 SPEAKER_00                                       Pattimathur, Ernakulam.49.224 50.146 SPEAKER_00                                              Okay, thank you.51.167 51.267 SPEAKER_00                                                         Okay.
    # """


    # Ground truth
    ground_truth = {
        "reference_name": "Arjun",
        "subject_name": "Matthew",
        "subject_address": "45,sunshine blaze apartments, pattimathur ,ernakulam",
        "relation_to_subject": "work together",
        "subject_occupation": "unemployed"
    }

    # Process the transcript
    results = process_transcript(transcript, ground_truth)

    # Print the results in JSON format
    print(json.dumps(results))