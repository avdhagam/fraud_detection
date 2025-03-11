import logging
import os
import numpy as np
import faster_whisper
import noisereduce as nr
from pydub import AudioSegment
from scipy.io import wavfile
from pyannote.audio.pipelines.speaker_diarization import SpeakerDiarization
from pyannote.core import Segment
import torch
import tempfile

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Set FFmpeg path manually
# ffmpeg_path = r"C:\Users\rashm\Documents\ffmpeg\ffmpeg-7.1-essentials_build\bin\ffmpeg.exe"
# AudioSegment.converter = ffmpeg_path
# os.environ["PATH"] += os.pathsep + os.path.dirname(ffmpeg_path)

# Load Faster Whisper model (medium, float32 for CPU)
model_size = "medium"
logger.info(f"Loading Faster Whisper model: {model_size}, compute_type=float32")
model = faster_whisper.WhisperModel(model_size, compute_type="float32")

# Load Pyannote Speaker Diarization Model
logger.info("Loading Pyannote speaker diarization model...")
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
pipeline = SpeakerDiarization.from_pretrained("pyannote/speaker-diarization-3.0", use_auth_token="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")
pipeline = pipeline.to(device)

def preprocess_audio(input_path: str) -> str:
    """Converts audio to WAV (16kHz, mono) & applies noise reduction."""
    logger.info(f"Preprocessing audio: {input_path}")

    # Load and process the audio
    audio = AudioSegment.from_file(input_path)
    audio = audio.set_frame_rate(16000).set_channels(1)

    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        temp_wav_path = tmp.name

    audio.export(temp_wav_path, format="wav")

    rate, data = wavfile.read(temp_wav_path)
    reduced_noise = nr.reduce_noise(y=data.astype(np.float32), sr=rate, prop_decrease=0.1)

    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        cleaned_wav_path = tmp.name

    wavfile.write(cleaned_wav_path, rate, reduced_noise.astype(np.int16))

    logger.info(f"Preprocessing complete. Saved at: {cleaned_wav_path}")
    return cleaned_wav_path

def diarize_audio(audio_path: str):
    """Performs speaker diarization."""
    logger.info(f"Performing speaker diarization on: {audio_path}")
    pipeline.instantiate({})
    diarization = pipeline(audio_path)
    speaker_segments = []

    for segment, _, speaker in diarization.itertracks(yield_label=True):
        speaker_segments.append({
            "start": round(segment.start, 2),
            "end": round(segment.end, 2),
            "speaker": speaker
        })

    logger.info("Speaker diarization completed.")
    return speaker_segments

def merge_transcription_with_speakers(transcription, speakers):
    """Merges Whisper transcription with speaker diarization results."""
    merged_output = []

    for segment in transcription:
        start, end = segment["start"], segment["end"]
        text = segment["text"]

        speaker = next(
            (sp["speaker"] for sp in speakers if sp["start"] <= start <= sp["end"]),
            "SPEAKER_02"
        )

        merged_output.append({
            "start": start,
            "end": end,
            "speaker": speaker,
            "text": text
        })
    return merged_output

def format_transcription_string(merged_results):
    """Formats the transcription output into the required string format."""
    output = "start      end        speaker                  utterance\n"
    for segment in merged_results:
        output += f"{segment['start']: <10} {segment['end']: <10} {segment['speaker']: <25} {segment['text']}\n"
    return output

def transcribe_audio_from_file(file_path: str):
    """Main function to transcribe audio without FastAPI (for direct execution & import)."""
    logger.info(f"Transcribing audio file: {file_path}")

    processed_audio = preprocess_audio(file_path)
    segments, info = model.transcribe(processed_audio, language="en")
    segments = list(segments)

    logger.info(f"Model processed audio: duration={info.duration:.2f}s, language={info.language}")

    transcript = [{"start": round(segment.start, 2), "end": round(segment.end, 2), "text": segment.text.strip()} for segment in segments]
    speaker_labels = diarize_audio(processed_audio)
    merged_results = merge_transcription_with_speakers(transcript, speaker_labels)
    formatted_output = format_transcription_string(merged_results)

    logger.info("Final formatted transcription response:")
    logger.info(f"\n{formatted_output}")

    return formatted_output