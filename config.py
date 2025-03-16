import os
import logging
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")
DEEPGRAM_API_KEY = os.getenv("DEEPGRAM_API_KEY")

if not OPENROUTER_API_KEY:
    print("Error: OPENROUTER_API_KEY is not set in .env file.")
    #logging.error("Error: OPENROUTER_API_KEY is not set in .env file.")

if not DEEPGRAM_API_KEY:
    print("Error: DEEPGRAM_API_KEY is not set in .env file.")
    #logging.error("Error: DEEPGRAM_API_KEY is not set in .env file.")
