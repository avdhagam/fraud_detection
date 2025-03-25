import os
import logging
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")
DEEPGRAM_API_KEY = os.getenv("DEEPGRAM_API_KEY")
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

if not OPENROUTER_API_KEY:
    print("Error: OPENROUTER_API_KEY is not set in .env file.")
    #logging.error("Error: OPENROUTER_API_KEY is not set in .env file.")

if not DEEPGRAM_API_KEY:
    print("Error: DEEPGRAM_API_KEY is not set in .env file.")
    #logging.error("Error: DEEPGRAM_API_KEY is not set in .env file.")

if not GEMINI_API_KEY:
    print("Error: GEMINI_API_KEY is not set in .env file.")
    #logging.error("Error: GEMINI_API_KEY is not set in .env file.")
'''
if not OPENROUTER_API_KEY_DOCUMENTS:
    print("Error: OPENROUTER_API_KEY_DOCUMENTS is not set in .env file.")
    #logging.error("Error: OPENROUTER_API_KEY_DOCUMENTS is not set in .env file.")
'''