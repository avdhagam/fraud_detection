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


def get_ground_truth(lead_id):
    api_url = f"http://localhost:8080/leads/{lead_id}/audio"
    try:
        response = requests.get(api_url)
        if response.status_code == 200:
            return response.json()
        else:
            print(f"Error: Failed to fetch ground truth for lead ID {lead_id}")
            sys.exit(1)
    except Exception as e:
        print(f"Error fetching ground truth: {e}")
        sys.exit(1)


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
        print("Error: OPENROUTER_API_KEY is not set.")
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
            print(f"Error parsing API response: {e}")
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
        print(f"Error: API request failed with status code {response.status_code}")
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

def output_json(data):
    """
    Outputs the given data as a JSON string to standard output.

    Args:
        data (dict): The data to output as JSON.
    """
    try:
        print(json.dumps(data))
    except Exception as e:
        print(json.dumps({"error": f"Failed to encode data as JSON: {e}"}))
        sys.exit(1)

# Example usage
if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python llmextractor.py <UUID> <LEAD_ID>")
        sys.exit(1)

    uuid = sys.argv[1]
    lead_id = sys.argv[2]
    audio_path = get_audio_file_path(uuid)

    ground_truth = get_ground_truth(lead_id)

    # transcript = Transcription.get_transcripts(audio_path)
    transcript = """start   end    speaker                                                              utterance
    0.000  0.500 SPEAKER_01                                                            Hello?
    1.560  2.180 SPEAKER_01                                                            Hello? Hi,
    2.620  4.020 SPEAKER_00                                                            is it Arjun? Hello?
    4.844  5.344 SPEAKER_00                                                            Sorry,
    6.168  6.668 SPEAKER_00                                                            is
    7.492  7.992 SPEAKER_00                                                            it
    8.816  9.316 SPEAKER_01                                                            Arjun?
    10.140  10.640 SPEAKER_00                                                            Yes.
    11.660  13.860 SPEAKER_01                                                            Hi, Arjun, Shilpa from Kast 24.
    15.220  15.720 SPEAKER_00                                                            Okay.
    16.580  17.480 SPEAKER_00                                                            It's a verification
    17.780  20.240 SPEAKER_00                                                            called C.J. Mathew has given your address.
    21.280  21.780 SPEAKER_01                                                            Okay.
    23.140  27.540 SPEAKER_00                                                            Actually he has taken a loan from us so that is the reason. How do you know him?
    29.060  29.840 SPEAKER_01                                                            I'm a colleague.
    31.300  33.940 SPEAKER_01                                                            Okay. Is he doing a job or a business right now?
    34.900  35.780 SPEAKER_00                                                            No, no job.
    36.760  38.800 SPEAKER_00                                                            And where does he stay? Is it a address?
    40.120  41.180 SPEAKER_00                                                            He is now in
    41.600  42.100 SPEAKER_00                                                            Pattimatham,
    43.160  43.660 SPEAKER_01                                                            Ernakulam.
    45.060  46.520 SPEAKER_00                                                            Sorry, sorry, can you repeat?
    47.020  48.280 SPEAKER_01                                                            Patimatham, Kerala.
    49.120  50.320 SPEAKER_00                                                            Okay, thank you.
    50.860  51.360 SPEAKER_00                                                            Okay."""


    results = process_transcript(transcript, ground_truth)

    output_json(results)