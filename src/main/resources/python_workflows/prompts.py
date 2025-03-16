PROMPTS = {

    "EXTRACTION_PROMPT": """
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
}