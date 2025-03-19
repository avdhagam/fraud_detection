PROMPTS = {

    "EXTRACTION_SCORING_PROMPT":"""
    First, extract the following information from this transcript. Be extremely precise.
    

    Keys to extract:
1. reference_name: Name of the person being called
2. subject_name: Name of the person who took the loan
3. subject_address: Full address of the subject.  Extract the complete address if available.
4. relation_to_subject: Relationship between reference and subject. **Extract only the key term describing the relationship (e.g., "colleague","work together", "friend", "family").  Do not include extraneous phrases.**
5. subject_occupation: Current occupation of the subject. **Extract only the key term describing the occupation status (e.g., "employed", "unemployed", "no job", "student","developer").  Do not include extraneous phrases.**
    
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
    1. transcript
    2. extracted_result: A dictionary with the extracted information
    3. field_by_field_scores: A dictionary with scores for each field (reference_name, subject_name, subject_address, relation_to_subject, subject_occupation)
    4. overall_score: Average of all field scores
    5. explanation: A dictionary with detailed explanations for each field score
    
    
    IMPORTANT: Return ONLY a valid JSON object with no markdown formatting, code blocks, or additional text.
    """,


    "VALIDATION_SCORING_PROMPT": """
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
   - 0.0 if extracted name is NULL but exists in ground truth
   - 0.0 if ground truth name is NULL but extracted name exists

2. Addresses:
   - 1.0 if exact match
   - 0.8 if key location/area matches (e.g., same city/neighborhood)
   - 0.6 if partial match (e.g., just the city or part of address)
   - Lower scores for completely different locations
   - 0.0 if extracted address is NULL but exists in ground truth
   - 0.0 if ground truth address is NULL but extracted address exists

... (Add similar rules for other fields)

5. gender:
   - 1.0 if extracted gender is the correct one from GROUND TRUTH
   - 0.0 if gender extracted is not the gender in GROUND TRUTH
6. Father Name:
   - 1.0 if extracted father name is the correct one from GROUND TRUTH
   - 0.0 if father name extracted is not in GROUND TRUTH
7. Date:
   - 1.0 if extracted date is the correct one from GROUND TRUTH
   - 0.0 if date extracted is not in GROUND TRUTH

Return a JSON with these fields:
1. transcript
2. field_by_field_scores: A dictionary with scores for each field (reference_name, subject_name, subject_address, relation_to_subject, subject_occupation)
2. overall_score: Average of all field scores
3. explanation: A dictionary with detailed explanations for each field score
IMPORTANT: Return ONLY a valid JSON object with no markdown formatting, code blocks, or additional text.
""",

    "DOCUMENT_QUALITY_PROMPT": """
        Analyze this document image for quality assessment with a focus on readability, clarity, and completeness. Identify the following aspects:

        **Readability Analysis** - Assess if text is clear, legible, and distortion-free.
        **Completeness Analysis** - Verify if all document sections are fully visible.
        **Blur Detection** - Identify if blur affects readability.
        **Lighting Issues** - Detect overexposure, shadows, or uneven brightness.
        **Color Accuracy** - Identify distortions or unnatural color shifts.
        **Document Alignment** - Check for tilt or misalignment impacting clarity.
        **Noise & Artifacts** - Identify unwanted marks, background noise, or distortions.

        Provide the results in **structured JSON format** with:
        - **Scores (0-1)** for each category.
        - **Detailed insights** explaining the score.
        - **Clear recommendations** for improvement.
        - **An overall quality score** and a final decision (Good, Acceptable, Poor).
    """,
    "DOCUMENT_FORGERY_PROMPT": """
        Analyze this document image for potential forgery. Identify the following:
        **Tampering Analysis** - Check for image manipulation, alterations, or suspicious artifacts.
        **Metadata Analysis** - Verify if metadata anomalies indicate document modification.
        **Format Consistency** - Check if the font, alignment, and layout match the standard document structure.
        **Security Features** - Detect missing watermarks, holograms, or embedded security elements.
        **Background Integrity** - Identify unnatural noise patterns or splicing artifacts in the document's background.
        Provide the results in structured JSON format with:
        - Scores (0-1) for each category.
        - Detailed insights explaining why the score was given.
        - Clear recommendations on the next steps.
        - An overall forgery risk score and a final decision (Low, Medium, High Risk).
    """,

    "OCR_PROMPT": """
  Extract the following information from this document image and return the result as a JSON object:

  1. document_type (string): The type of document ("Aadhaar" or "PAN").
  2. name (string): The full name of the individual as per the document.  If the name cannot be confidently extracted, return null.
  3. date_of_birth (string, optional): The date of birth in YYYY-MM-DD format if present.  If not found, return null.
  4. gender (string, optional): The gender of the individual ("Male", "Female", or "Other"), applicable only for Aadhaar.  If not found, return null.
  5. father_name (string, optional): The father's name, applicable only for PAN. If not found, return null.
  6. id_number (string): The unique document number (Aadhaar Number or PAN Number).

  - If a field is not present in the document, return `null`.
  - For each extracted field, include a "confidence" score (0.0-1.0) representing the accuracy of the extraction.
  - Ensure that the JSON response is valid, consistent, and properly structured.
  - If unable to extract a field set "confidence" score 0.0 and value set to be null
"""
}