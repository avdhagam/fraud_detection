import os
import json
import re
import requests
from openai import OpenAI

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

def extract_transcript_information(transcript):
    """
    Extract key information from a call transcript using OpenRouter API.

    Args:
        transcript (str): The call transcript text.

    Returns:
        dict: Extracted information in dictionary format.
    """
    # OpenRouter API key
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

    # Prompt for extraction
    extraction_prompt = f"""
    Extract the following information from this transcript. 
    Be extremely precise and ensure the response is in valid JSON format.
    
    Keys to extract:
    1. reference_name: Name of the person being called
    2. subject_name: Name of the person who took the loan
    3. subject_address: Full address of the subject
    4. relation_to_subject: Relationship between reference and subject
    5. subject_occupation: Current occupation of the subject

    Transcript:
    {transcript}

    IMPORTANT: Respond EXACTLY in this JSON format:
    {{
        "reference_name": "...",
        "subject_name": "...",
        "subject_address": "...",
        "relation_to_subject": "...",
        "subject_occupation": "..."
    }}
    """

    # Request payload
    payload = {
        "model": "google/gemini-2.0-flash-lite-001",
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
            # Extract the content from the response
            content = result.get("choices", [{}])[0].get("message", {}).get("content", "")

            # Clean the content (remove code blocks, trim)
            content = content.strip()
            content = re.sub(r'^```json\s*|\s*```$', '', content, flags=re.MULTILINE).strip()

            # Parse the JSON
            parsed_result = json.loads(content)

            return parsed_result
        except json.JSONDecodeError as e:
            print(f"Error parsing extraction result: {e}")
            print(f"Raw content: {content}")
            return {}
        except Exception as e:
            print(f"Unexpected error: {e}")
            return {}
    else:
        print(f"Error: API request failed with status code {response.status_code}")
        return {}

def score_extraction_with_llm(result, ground_truth):
    """
    Use the LLM to score the extracted information against ground truth

    Args:
        result (dict/str): The extracted information (JSON or string)
        ground_truth (dict): The ground truth information

    Returns:
        dict: Scoring results with field-by-field and overall scores
    """
    # Initialize client with OpenRouter API
    client = OpenAI(
        base_url="https://openrouter.ai/api/v1",
        api_key=os.environ.get("OPENROUTER_API_KEY", "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"),
    )

    # Convert result to string if it's a dict
    if isinstance(result, dict):
        result_str = json.dumps(result)
    else:
        result_str = result

    # Convert ground truth to string
    ground_truth_str = json.dumps(ground_truth)

    # Create the scoring prompt
    scoring_prompt = f"""
    I need you to carefully score the extraction result against the ground truth.
    
    RESULT:
    {result_str}
    
    GROUND TRUTH:
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
    1. transcript
    2. field_by_field_scores: A dictionary with scores for each field (reference_name, subject_name, subject_address, relation_to_subject, subject_occupation)
    2. overall_score: Average of all field scores
    3. explanation: A dictionary with detailed explanations for each field score
    
    
    IMPORTANT: Return ONLY a valid JSON object with no markdown formatting, code blocks, or additional text.
    """

    # Create request with the scoring prompt
    completion = client.chat.completions.create(
        extra_headers={
            "HTTP-Referer": "https://your-app-domain.com",
            "X-Title": "Extraction Scoring App",
        },
        model="google/gemini-2.0-flash-lite-001",  # Or any other suitable model
        messages=[
            {
                "role": "user",
                "content": scoring_prompt
            }
        ]
    )

    # Get the response
    score_result = completion.choices[0].message.content

    # Clean the response - remove markdown code blocks if present
    # This pattern matches ```json and ``` at the beginning and end
    cleaned_result = re.sub(r'^```json\s*|\s*```$', '', score_result, flags=re.MULTILINE)

    # Try to parse the JSON response
    try:
        score_data = json.loads(cleaned_result)

        # Ensure the response has the correct structure
        if "field_scores" in score_data and not "field_by_field_scores" in score_data:
            score_data["field_by_field_scores"] = score_data.pop("field_scores")

        # Ensure explanation is a dictionary if it's a string
        if "explanation" in score_data and isinstance(score_data["explanation"], str):
            # Create a dictionary with the same explanation for each field
            explanation_text = score_data["explanation"]
            score_data["explanation"] = {
                "reference_name": explanation_text,
                "subject_name": explanation_text,
                "subject_address": explanation_text,
                "relation_to_subject": explanation_text,
                "subject_occupation": explanation_text
            }

        return score_data
    except json.JSONDecodeError:
        # If still failing, try a more aggressive cleanup
        # Extract anything that looks like JSON - content between { and }
        json_match = re.search(r'({.*})', cleaned_result, re.DOTALL)
        if json_match:
            try:
                score_data = json.loads(json_match.group(1))
                return score_data
            except json.JSONDecodeError:
                pass

        # If all parsing attempts fail
        return {
            "error": "Failed to parse LLM response",
            "raw_response": score_result,
            "cleaned_response": cleaned_result
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

    # Extract information from transcript
    extracted_result = extract_transcript_information(transcript)

    # Score the extracted information
    scoring_results = score_extraction_with_llm(extracted_result, ground_truth)

    # Combine results into the required format
    return {
        "transcript": structured_transcript,
        "extracted_result": extracted_result,
        "scoring_results": scoring_results
    }

# Example usage
if __name__ == "__main__":
    # Sample transcript
    transcript = """
   start    end    speaker                                                     utterance
 2.191  2.574 SPEAKER_01                                                        Hello?
 4.213  4.597 SPEAKER_00                                                        Hello?
 5.014  7.257 SPEAKER_00                                             Hi, is it Ashish?
 7.257  7.581 SPEAKER_01                                                        Hello?
 8.499 10.182 SPEAKER_00                                           Sorry, is it Arjun?
10.181 11.864 SPEAKER_01                                                          Yes.
11.863 14.066 SPEAKER_00                                Hi, Arjun, Shilpa from Car 24.
15.307 15.730 SPEAKER_01                                                         Okay.
16.769 18.611 SPEAKER_00                               It's a verification called C.Q.
18.611 20.174 SPEAKER_00                                Matheo has given your address.
21.415 21.897 SPEAKER_01                                                     Ah, okay.
23.217 26.461 SPEAKER_00 Actually, he has taken a loan from us, so that is the reason.
26.481 27.223 SPEAKER_00                                          How do you know him?
28.944 29.806 SPEAKER_01                                          Ah, I'm a colleague.
31.334 33.698 SPEAKER_00              Okay, is he doing a job or a business right now?
35.100 35.703 SPEAKER_01                                                   No, no job.
36.883 38.006 SPEAKER_00                                      And where does he stays?
38.005 40.229 SPEAKER_00                                                  His address?
40.289 43.674 SPEAKER_01                          He is now in Pattimathur, Ernakulam.
45.217 46.420 SPEAKER_00                                 Sorry, sorry, can you repeat?
47.080 48.483 SPEAKER_00                                       Pattimathur, Ernakulam.
49.224 50.146 SPEAKER_00                                              Okay, thank you.
51.167 51.267 SPEAKER_00                                                         Okay.
"""

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
    print(json.dumps(results, indent=2))