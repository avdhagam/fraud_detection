import os
import json
import re
import requests
from openai import OpenAI
import sys
from pathlib import Path
import Transcription # Import transcript.py for processing

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
    """
    structured_transcript = []
    pattern = r'(\d+\.\d+)\s+(\d+\.\d+)\s+(SPEAKER_\d+)\s+(.+?)(?=\d+\.\d+\s+\d+\.\d+\s+SPEAKER_|\Z)'
    matches = re.finditer(pattern, transcript_text, re.DOTALL)

    for match in matches:
        structured_transcript.append({
            "start_time": float(match.group(1)),
            "end_time": float(match.group(2)),
            "speaker": match.group(3),
            "text": match.group(4).strip()
        })

    return structured_transcript

def extract_and_score_transcript(transcript, ground_truth):
    """
    Combine extraction and scoring into a single API call to reduce API usage.

    Args:
        transcript (str): The call transcript text.
        ground_truth (dict): The ground truth information for scoring.

    Returns:
        dict: Combined extraction and scoring results.
    """
    # Get API key from environment
    api_key = os.environ.get("OPENROUTER_API_KEY", "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")

    # API URL
    url = "https://openrouter.ai/api/v1/chat/completions"

    # Headers
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
        "HTTP-Referer": "https://your-app-domain.com",
        "X-Title": "Transcript Analysis App",
    }

    # Ground truth as string
    ground_truth_str = json.dumps(ground_truth)

    # Combined prompt for both extraction and scoring in one go
    combined_prompt = f"""
    First, extract the following information from this transcript. Be extremely precise.
    
    Keys to extract:
    1. reference_name: Name of the person being called
    2. subject_name: Name of the person who took the loan
    3. subject_address: Full address of the subject
    4. relation_to_subject: Relationship between reference and subject
    5. subject_occupation: Current occupation of the subject
    
    Transcript:
    {transcript}
    
    Second, score your extraction against this ground truth:
    {ground_truth_str}
    
    Scoring Guidelines:
    1. Names: 
       - 1.0 if exactly same
       - 0.8 if very similar (e.g., Matheo vs Matthew)
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
    
    Return a JSON with these fields:
    1. extracted_result: A dictionary with the extracted information
    2. field_by_field_scores: A dictionary with scores for each field (reference_name, subject_name, subject_address, relation_to_subject, subject_occupation)
    3. overall_score: Average of all field scores
    4. explanation: A dictionary with detailed explanations for each field score
    
    IMPORTANT: Return ONLY a valid JSON object with no markdown formatting, code blocks, or additional text.
    """

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

            # Clean the content (remove code blocks, trim)
            content = content.strip()
            content = re.sub(r'^```json\s*|\s*```$', '', content, flags=re.MULTILINE).strip()

            # Parse the JSON
            parsed_result = json.loads(content)

            # Determine status based on overall score
            overall_score = parsed_result.get("overall_score", 0)
            status = "accept" if overall_score >= 0.7 else "reject"

            # Add status to result
            parsed_result["status"] = status

            return parsed_result
        except json.JSONDecodeError as e:
            print(f"Error parsing API response: {e}")
            # Try more aggressive JSON extraction
            json_match = re.search(r'({.*})', content, re.DOTALL)
            if json_match:
                try:
                    parsed_result = json.loads(json_match.group(1))
                    overall_score = parsed_result.get("overall_score", 0)
                    parsed_result["status"] = "accept" if overall_score >= 0.7 else "reject"
                    return parsed_result
                except:
                    pass

            return {
                "error": "Failed to parse response",
                "raw_response": content,
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
    Process the transcript by extracting information and scoring against ground truth

    Args:
        transcript (str): The call transcript
        ground_truth (dict): The ground truth information

    Returns:
        dict: Comprehensive results including extraction and scoring
    """
    # Parse transcript into structured format
    structured_transcript = parse_transcript_to_structured_format(transcript)

    # Extract and score in a single API call
    results = extract_and_score_transcript(transcript, ground_truth)

    # Construct the final output in the same format as the original code
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