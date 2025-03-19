# import os
# import json
# import re
# import requests
# from openai import OpenAI
#
# import sys
#
# from pathlib import Path
# import Transcription # Import transcript.py for processing
#
# # Define base path for stored audio files
# root_path = Path(__file__).resolve().parent.parent  # Moves up two levels
# base_path = root_path / "audio_storage"
#
# # def get_uuid():
# #     if len(sys.argv)>1:
# #         return sys.argv[1]
# #     else:
# #         print("Error: UUID not provided")
# #         sys.exit(1)
# #
# # uuid = get_uuid()
#
# def get_audio_file_path(uuid):
#     """Reconstruct the full path of the audio file using UUID."""
#     file_name = f"{uuid}.mp3"  # Assuming files are stored as UUID.mp3
#     file_path = base_path / file_name
#
#     if not file_path.exists():
#         print(f"Error: File {file_name} not found in {base_path}")
#         sys.exit(1)
#
#     return str(file_path)
#
# def parse_transcript_to_structured_format(transcript_text):
#     """
#     Parse the raw transcript text into a structured array of transcript segments.
#
#     Args:
#         transcript_text (str): Raw transcript text
#
#     Returns:
#         list: List of dictionaries with structured transcript data
#     """
#     structured_transcript = []
#
#     # Regular expression to match transcript segments
#     # Format: <start_time> <end_time> <speaker> <text>
#     pattern = r'(\d+\.\d+)\s+(\d+\.\d+)\s+(SPEAKER_\d+)\s+(.+?)(?=\d+\.\d+\s+\d+\.\d+\s+SPEAKER_|\Z)'
#
#     matches = re.finditer(pattern, transcript_text, re.DOTALL)
#
#     for match in matches:
#         start_time = float(match.group(1))
#         end_time = float(match.group(2))
#         speaker = match.group(3)
#         text = match.group(4).strip()
#
#         structured_transcript.append({
#             "start_time": start_time,
#             "end_time": end_time,
#             "speaker": speaker,
#             "text": text
#         })
#
#     return structured_transcript
#
# def extract_transcript_information(transcript):
#     """
#     Extract key information from a call transcript using OpenRouter API.
#
#     Args:
#         transcript (str): The call transcript text.
#
#     Returns:
#         dict: Extracted information in dictionary format.
#     """
#     # OpenRouter API key
#     api_key = "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"
#     #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
#     # API URL
#     url = "https://openrouter.ai/api/v1/chat/completions"
#
#     # Headers
#     headers = {
#         "Authorization": f"Bearer {api_key}",
#         "Content-Type": "application/json",
#         "HTTP-Referer": "https://your-app-domain.com",
#         "X-Title": "Transcript Analysis App",
#     }
#
#     # Prompt for extraction
#     extraction_prompt = f"""
#     Extract the following information from this transcript.
#     Be extremely precise and ensure the response is in valid JSON format.
#
#     Keys to extract:
#     1. reference_name: Name of the person being called
#     2. subject_name: Name of the person who took the loan
#     3. subject_address: Full address of the subject
#     4. relation_to_subject: Relationship between reference and subject
#     5. subject_occupation: Current occupation of the subject
#
#     Transcript:
#     {transcript}
#
#     IMPORTANT: Respond EXACTLY in this JSON format:
#     {{
#         "reference_name": "...",
#         "subject_name": "...",
#         "subject_address": "...",
#         "relation_to_subject": "...",
#         "subject_occupation": "..."
#     }}
#     """
#
#     # Request payload
#     payload = {
#         "model": "google/gemini-2.0-flash-lite-001",
#         "messages": [
#             {"role": "user", "content": extraction_prompt}
#         ]
#     }
#
#     # Make the request
#     response = requests.post(url, headers=headers, data=json.dumps(payload))
#
#     # Parse response
#     if response.status_code == 200:
#         try:
#             result = response.json()
#             # Extract the content from the response
#             content = result.get("choices", [{}])[0].get("message", {}).get("content", "")
#
#             # Clean the content (remove code blocks, trim)
#             content = content.strip()
#             content = re.sub(r'^```json\s*|\s*```$', '', content, flags=re.MULTILINE).strip()
#
#             # Parse the JSON
#             parsed_result = json.loads(content)
#
#             return parsed_result
#         except json.JSONDecodeError as e:
#             print(f"Error parsing extraction result: {e}")
#             print(f"Raw content: {content}")
#             return {}
#         except Exception as e:
#             print(f"Unexpected error: {e}")
#             return {}
#     else:
#         print(f"Error: API request failed with status code {response.status_code}")
#         return {}
#
# def score_extraction_with_llm(result, ground_truth):
#     """
#     Use the LLM to score the extracted information against ground truth
#
#     Args:
#         result (dict/str): The extracted information (JSON or string)
#         ground_truth (dict): The ground truth information
#
#     Returns:
#         dict: Scoring results with field-by-field and overall scores
#     """
#     # Initialize client with OpenRouter API
#     client = OpenAI(
#         base_url="https://openrouter.ai/api/v1",
#         api_key= "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",
#     )
#
#     # Convert result to string if it's a dict
#     if isinstance(result, dict):
#         result_str = json.dumps(result)
#     else:
#         result_str = result
#
#     # Convert ground truth to string
#     ground_truth_str = json.dumps(ground_truth)
#
#     # Create the scoring prompt
#     scoring_prompt = f"""
#     I need you to carefully score the extraction result against the ground truth.
#
#     RESULT:
#     {result_str}
#
#     GROUND TRUTH:
#     {ground_truth_str}
#
#     Scoring Guidelines:
#     1. Names:
#        - 1.0 if exactly same
#        - 0.8 if very similar (e.g., Matheo vs Matthew)
#        - Lower scores for significant differences
#
#     2. Addresses:
#        - 1.0 if exact match
#        - 0.8 if key location/area matches (e.g., same city/neighborhood)
#        - 0.6 if partial match (e.g., just the city or part of address)
#        - Lower scores for completely different locations
#
#     3. Relation:
#        - 1.0 if exact semantic match
#        - 0.8 if similar meaning (e.g., "colleague" vs "work together")
#        - Lower scores for significantly different meanings
#
#     4. Occupation:
#        - 1.0 if exact match
#        - 0.8 if semantically equivalent (e.g., "no job" vs "unemployed")
#        - Lower scores for significantly different descriptions
#
#     Return a JSON with these fields:
#     1. transcript
#     2. field_by_field_scores: A dictionary with scores for each field (reference_name, subject_name, subject_address, relation_to_subject, subject_occupation)
#     2. overall_score: Average of all field scores
#     3. explanation: A dictionary with detailed explanations for each field score
#
#
#     IMPORTANT: Return ONLY a valid JSON object with no markdown formatting, code blocks, or additional text.
#     """
#
#     # Create request with the scoring prompt
#     completion = client.chat.completions.create(
#         extra_headers={
#             "HTTP-Referer": "https://your-app-domain.com",
#             "X-Title": "Extraction Scoring App",
#         },
#         model="google/gemini-2.0-flash-lite-001",  # Or any other suitable model
#         messages=[
#             {
#                 "role": "user",
#                 "content": scoring_prompt
#             }
#         ]
#     )
#
#     # Get the response
#     score_result = completion.choices[0].message.content
#
#     # Clean the response - remove markdown code blocks if present
#     # This pattern matches ```json and ``` at the beginning and end
#     # cleaned_result = re.sub(r'^```json\s*|\s*```$', '', score_result, flags=re.MULTILINE)
#     cleaned_result = re.sub(r'^```json\s*|\s*```$', '', score_result, flags=re.MULTILINE)
#
#     # Try to parse the JSON response
#     try:
#         score_data = json.loads(cleaned_result)
#
#         # Ensure the response has the correct structure
#         if "field_scores" in score_data and not "field_by_field_scores" in score_data:
#             score_data["field_by_field_scores"] = score_data.pop("field_scores")
#
#         # Ensure explanation is a dictionary if it's a string
#         if "explanation" in score_data and isinstance(score_data["explanation"], str):
#             # Create a dictionary with the same explanation for each field
#             explanation_text = score_data["explanation"]
#             score_data["explanation"] = {
#                 "reference_name": explanation_text,
#                 "subject_name": explanation_text,
#                 "subject_address": explanation_text,
#                 "relation_to_subject": explanation_text,
#                 "subject_occupation": explanation_text
#             }
#
#         return score_data
#     except json.JSONDecodeError:
#         # If still failing, try a more aggressive cleanup
#         # Extract anything that looks like JSON - content between { and }
#         json_match = re.search(r'({.*})', cleaned_result, re.DOTALL)
#         if json_match:
#             try:
#                 score_data = json.loads(json_match.group(1))
#                 return score_data
#             except json.JSONDecodeError:
#                 pass
#
#         # If all parsing attempts fail
#         return {
#             "error": "Failed to parse LLM response",
#             "raw_response": score_result,
#             "cleaned_response": cleaned_result
#         }
#
# def process_transcript(transcript, ground_truth):
#     """
#     Process the transcript by extracting information and scoring against ground truth
#
#     Args:
#         transcript (str): The call transcript
#         ground_truth (dict): The ground truth information
#
#     Returns:
#         dict: Comprehensive results including extraction and scoring
#     """
#     # Parse transcript into structured format
#     structured_transcript = parse_transcript_to_structured_format(transcript)
#
#     # Extract information from transcript
#     extracted_result = extract_transcript_information(transcript)
#
#     # Score the extracted information
#     scoring_results = score_extraction_with_llm(extracted_result, ground_truth)
#
#     # Combine results into the required format
#     return {
#         "transcript": structured_transcript,
#         "extracted_result": extracted_result,
#         "scoring_results": scoring_results
#     }
#
#
# # Example usage
# if __name__ == "__main__":
#     if len(sys.argv) < 2:
#         print("Usage: python llmextractor.py <UUID>")
#         sys.exit(1)
#
#     uuid = sys.argv[1]  # UUID received from API
#     audio_path = get_audio_file_path(uuid)
#
#     # Call the transcription function from transcript.py
#     transcript = Transcription.get_transcripts(audio_path)
#
#
#
#     # print("\nFinal Transcription Output:\n")
#     # print(type(transcript))
#     # print(transcript)
#
#     # Print final output (or return to API)
#
#     #print(transcript)
#
#     # Sample transcript
#     # transcript = """
#     # 2.191  2.574 SPEAKER_01                                                        Hello? 4.213  4.597 SPEAKER_00                                                        Hello? 5.014  7.257 SPEAKER_00                                             Hi, is it Ashish? 7.257  7.581 SPEAKER_01                                                        Hello? 8.499 10.182 SPEAKER_00                                           Sorry, is it Arjun?10.181 11.864 SPEAKER_01                                                          Yes.11.863 14.066 SPEAKER_00                                Hi, Arjun, Shilpa from Car 24.15.307 15.730 SPEAKER_01                                                         Okay.16.769 18.611 SPEAKER_00                               It's a verification called C.Q.18.611 20.174 SPEAKER_00                                Matheo has given your address.21.415 21.897 SPEAKER_01                                                     Ah, okay.23.217 26.461 SPEAKER_00 Actually, he has taken a loan from us, so that is the reason.26.481 27.223 SPEAKER_00                                          How do you know him?28.944 29.806 SPEAKER_01                                          Ah, I'm a colleague.31.334 33.698 SPEAKER_00              Okay, is he doing a job or a business right now?35.100 35.703 SPEAKER_01                                                   No, no job.36.883 38.006 SPEAKER_00                                      And where does he stays?38.005 40.229 SPEAKER_00                                                  His address?40.289 43.674 SPEAKER_01                          He is now in Pattimathur, Ernakulam.45.217 46.420 SPEAKER_00                                 Sorry, sorry, can you repeat?47.080 48.483 SPEAKER_00                                       Pattimathur, Ernakulam.49.224 50.146 SPEAKER_00                                              Okay, thank you.51.167 51.267 SPEAKER_00                                                         Okay.
#     # """
#
#
#     # Ground truth
#     ground_truth = {
#         "reference_name": "Arjun",
#         "subject_name": "Matthew",
#         "subject_address": "45,sunshine blaze apartments, pattimathur ,ernakulam",
#         "relation_to_subject": "work together",
#         "subject_occupation": "unemployed"
#     }
#
#     # Process the transcript
#     results = process_transcript(transcript, ground_truth)
#
#     # Print the results in JSON format
#     print(json.dumps(results))
#
#
#

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
    if len(sys.argv) < 2:
        print("Usage: python llmextractor.py <UUID>")
        sys.exit(1)

    uuid = sys.argv[1]  # UUID received from API
    audio_path = get_audio_file_path(uuid)

    # Call the transcription function from transcript.py
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

    # Output the results as a JSON string to standard output
    output_json(results)