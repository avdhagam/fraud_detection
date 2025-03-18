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

<<<<<<< HEAD
def extract_and_score_transcript(transcript, ground_truth):
    """
    Combined function to extract information from transcript and score against ground truth
    using a single API call.

    Args:
        transcript (str): The call transcript
        ground_truth (dict): The ground truth information

    Returns:
        dict: Comprehensive results including extraction and scoring
=======
def extract_transcript_information(transcript, ground_truth):
    """
    Extract key information and score against ground truth in a single API call.

    Args:
        transcript (str): The call transcript text.
        ground_truth (dict): The ground truth information.

    Returns:
        dict: Extracted information and scoring results.
>>>>>>> 358758ac0303a57dca92f554bd388f2d3c19c1b4
    """
    # Parse transcript into structured format
    structured_transcript = parse_transcript_to_structured_format(transcript)

    # OpenRouter API key
<<<<<<< HEAD
    api_key = os.environ.get("OPENROUTER_API_KEY", "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")
=======
    api_key = config.OPENROUTER_API_KEY
    if not api_key:
        print("Error: OPENROUTER_API_KEY is not set.")
        sys.exit(1)

>>>>>>> 358758ac0303a57dca92f554bd388f2d3c19c1b4
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

<<<<<<< HEAD
    # Combined prompt for extraction and scoring in a single API call
    combined_prompt = f"""
    You have two tasks:
    
    TASK 1: Extract the following information from this transcript:
    - reference_name: Name of the person being called
    - subject_name: Name of the person who took the loan
    - subject_address: Full address of the subject
    - relation_to_subject: Relationship between reference and subject
    - subject_occupation: Current occupation of the subject
    
    TASK 2: Score the extracted information against this ground truth:
    {ground_truth_str}
    
    Scoring Guidelines:
    1. Names: 
       - 1.0 if exactly same
       - 0.8 if very similar (e.g., Matheo vs Matthew, CJ vs CJ Matthew)
       - Lower scores for significant differences
    
    2. Addresses: 
       - 1.0 if exact match
       - 0.8 if key location/area matches (e.g., same city/neighborhood)
       - 0.6 if partial match (e.g., just the city or part of address)
       - Lower scores for completely different locations
    
    3. Relation: 
       - 1.0 if exact semantic match
       - 0.8 if similar meaning (e.g., "colleague" vs "work together")
       - Lower scores for significantly different meanings
    
    4. Occupation: 
       - 1.0 if exact match
       - 0.8 if semantically equivalent (e.g., "no job" vs "unemployed")
       - Lower scores for significantly different descriptions
    
    Transcript:
    {transcript}
    
    IMPORTANT: Respond with a JSON object that EXACTLY matches this structure:
    {{
        "extracted_result": {{
            "reference_name": "...",
            "subject_name": "...",
            "subject_address": "...",
            "relation_to_subject": "...",
            "subject_occupation": "..."
        }},
        "scoring_results": {{
            "transcript": "Scoring the extraction result against the ground truth.",
            "field_by_field_scores": {{
                "reference_name": 0.0,
                "subject_name": 0.0,
                "subject_address": 0.0,
                "relation_to_subject": 0.0,
                "subject_occupation": 0.0
            }},
            "overall_score": 0.0,
            "explanation": {{
                "reference_name": "...",
                "subject_name": "...",
                "subject_address": "...",
                "relation_to_subject": "...",
                "subject_occupation": "..."
            }}
        }}
    }}
    
    The overall_score should be the average of all field scores.
    
    """
=======
    # Combined prompt for extraction and scoring
    combined_prompt_template = prompts.PROMPTS["EXTRACTION_SCORING_PROMPT"]
    combined_prompt = combined_prompt_template.format(
        transcript=transcript,
        ground_truth_str=ground_truth_str
    )
>>>>>>> 358758ac0303a57dca92f554bd388f2d3c19c1b4

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
<<<<<<< HEAD
            # Extract the content from the response
            content = result.get("choices", [{}])[0].get("message", {}).get("content", "")

            # Clean the content (remove code blocks, trim)
            content = content.strip()
            content = re.sub(r'^```json\s*|\s*```$', '', content, flags=re.MULTILINE).strip()

            # Parse the JSON
            analysis_results = json.loads(content)

            # Calculate overall score if not provided
            if "overall_score" not in analysis_results.get("scoring_results", {}):
                field_scores = analysis_results.get("scoring_results", {}).get("field_by_field_scores", {})
                if field_scores:
                    overall_score = sum(field_scores.values()) / len(field_scores)
                    analysis_results["scoring_results"]["overall_score"] = round(overall_score, 1)

            # Determine status based on overall score
            overall_score = analysis_results.get("scoring_results", {}).get("overall_score", 0)
            status = "accept" if overall_score >= 0.7 else "reject"

            # Create the final result structure
            final_result = {
                "transcript": structured_transcript,
                "extracted_result": analysis_results.get("extracted_result", {}),
                "scoring_results": analysis_results.get("scoring_results", {
                    "transcript": "Scoring the extraction result against the ground truth.",
                    "field_by_field_scores": {},
                    "overall_score": 0,
                    "explanation": {}
                }),
                "status": status
            }

            return final_result

        except json.JSONDecodeError as e:
            print(f"Error parsing result: {e}")
            print(f"Raw content: {content}")
        except Exception as e:
            print(f"Unexpected error: {e}")
    else:
        print(f"Error: API request failed with status code {response.status_code}")

    # Return empty result with the correct structure if something fails
    return {
        "transcript": structured_transcript,
        "extracted_result": {
            "reference_name": "",
            "subject_name": "",
            "subject_address": "",
            "relation_to_subject": "",
            "subject_occupation": ""
        },
        "scoring_results": {
            "transcript": "Scoring the extraction result against the ground truth.",
            "field_by_field_scores": {
                "reference_name": 0.0,
                "subject_name": 0.0,
                "subject_address": 0.0,
                "relation_to_subject": 0.0,
                "subject_occupation": 0.0
            },
            "overall_score": 0.0,
            "explanation": {
                "reference_name": "",
                "subject_name": "",
                "subject_address": "",
                "relation_to_subject": "",
                "subject_occupation": ""
            }
        },
        "status": "reject"
    }
=======
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
>>>>>>> 358758ac0303a57dca92f554bd388f2d3c19c1b4

def process_transcript(transcript, ground_truth):
    """
    Process the transcript by extracting information and scoring against ground truth in a single step.

    Args:
        transcript (str): The call transcript
        ground_truth (dict): The ground truth information

    Returns:
        dict: Comprehensive results including extraction and scoring
    """
<<<<<<< HEAD
    # Use the combined function to extract and score
    return extract_and_score_transcript(transcript, ground_truth)
=======
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
>>>>>>> 358758ac0303a57dca92f554bd388f2d3c19c1b4


# Example usage
if __name__ == "__main__":
    if len(sys.argv) < 2:

        sys.exit(1)

    uuid = sys.argv[1]  # UUID received from API
    audio_path = get_audio_file_path(uuid)

    # Call the transcription function from transcript.py
    transcript = Transcription.get_transcripts(audio_path)

    # Ground truth
    ground_truth = {
        "reference_name": "Arjun",
        "subject_name": " CJ Matthew",
        "subject_address": "45,sunshine blaze apartments, pattimathur ,ernakulam",
        "relation_to_subject": "work together",
        "subject_occupation": "unemployed"
    }

    # Process the transcript
    results = process_transcript(transcript, ground_truth)

    # Print the results in JSON format
    print(json.dumps(results))