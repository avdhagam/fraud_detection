import sys
import librosa
import numpy as np
import os
import json
from pathlib import Path


def get_uuid():
    if len(sys.argv) > 1:
        #print(f"THIS IS THE SYS ARGV {sys.argv}")
        uuid_candidate = sys.argv[1]
        #print(f"UUID BEFORE FAIL SAFE {uuid_candidate}")

        # Extract just the filename (UUID) if a full path is mistakenly passed
        return Path(uuid_candidate).stem


    else:
        print("Error: UUID not provided")
        sys.exit(1)

uuid = get_uuid()
#print(f"UUID at point 0 is {uuid}")
root_path = Path(__file__).resolve().parent.parent# Update with actual path
base_path = root_path / "audio_storage"


def load_audio(file_path):
    try:
        y, sr = librosa.load(file_path, sr=None)
        return y, sr
    except Exception as e:
        print(f"Error loading audio file: {e}")
        return None, None

def calculate_snr_improved(y, sr):
    #calculate SNR using VAD
    from librosa.effects import split

    # Detect speech segments aka non silent segments
    non_silent = split(y, top_db=20)

    # Extract speech and create a speech mask
    speech_mask = np.zeros_like(y, dtype=bool)
    for start, end in non_silent:
        speech_mask[start:end] = True

    # Signal power from speech regions
    if np.sum(speech_mask) > 0:
        signal_power = np.mean(y[speech_mask] ** 2)
    else:
        return 0.0  # No speech

    # Noise power from non-speech regions, with minimum size check
    non_speech_mask = ~speech_mask
    if np.sum(non_speech_mask) > sr * 0.1:  # At least 100ms of non-speech
        noise_power = np.mean(y[non_speech_mask] ** 2)
    else:
        # If no significant non-speech segments, use lowest energy frame
        frame_length = int(0.02 * sr)  # 20ms frames
        hop_length = int(0.01 * sr)    # 10ms hop

        energy = np.array([
            np.mean(y[i:i+frame_length]**2)
            for i in range(0, len(y)-frame_length, hop_length)
        ])

        # Use the average of the lowest 5% of frames as noise estimate
        num_noise_frames = max(1, int(len(energy) * 0.05))
        lowest_indices = np.argsort(energy)[:num_noise_frames]
        noise_power = np.mean(energy[lowest_indices])

    if noise_power < 1e-10:
        return 80.0  # Cap at 80dB for extremely low noise?

    return 10 * np.log10(signal_power / noise_power)

def grade_snr(snr):
    #Grade audio quality based only on SNR
    if snr > 25:
        return "Good"
    elif 15 <= snr <= 25:
        return "Moderate"
    else:
        return "Poor"

def detect_volume_fluctuations(y, sr, frame_length=1024, hop_length=512):
    #Detects volume fluctuations based on short-term energy variation, with lenient thresholds.
    energy = np.array([
        np.mean(np.abs(y[i : i + frame_length])**2)
        for i in range(0, len(y), hop_length)
    ])

    energy = energy / np.max(energy)  # Normalize

    # Compute standard deviation of energy
    fluctuation_score = np.std(energy)

    # Classify fluctuation severity (lenient thresholds)
    if fluctuation_score < 0.24:
        level = "Stable"
    elif fluctuation_score < 0.4:
        level = "Moderate Fluctuations"
    else:
        level = "Severe Fluctuations"

    return fluctuation_score, level



def grade_audio_quality(snr, fluctuations):
    """Grade audio quality based on SNR and volume fluctuations"""
    if snr > 25 and fluctuations == "Stable":
        return "Good"
    elif 15 <= snr <= 25 and fluctuations in ["Stable", "Moderate Fluctuations"]:
        return "Medium"
    else:
        return "Poor"

def analyze_audio(uuid):
    """Analyze audio file and return quality metrics."""
    file_path = base_path / f"{uuid}.mp3"

    if not file_path.exists():  # Uses Pathlib's exists() method
        return {"error": f"File not found: {file_path}"}
    y, sr = load_audio(file_path)

    if y is None or sr is None:
        return {"error": "Failed to load audio file"}

    # Calculate SNR
    snr_value = calculate_snr_improved(y, sr)
    snr_grade = grade_snr(snr_value)

    # Calculate volume fluctuations
    fluctuation_score, fluctuation_level = detect_volume_fluctuations(y, sr)

    # Determine overall quality
    overall_quality = grade_audio_quality(snr_value, fluctuation_level)

    # Return results as JSON
    return {
        "snr_value": float(snr_value),
        "snr_grade": snr_grade,
        "fluctuation_score": float(fluctuation_score),
        "fluctuation_level": fluctuation_level,
        "overall_quality": overall_quality
    }

result = analyze_audio(uuid)
print(json.dumps(result))
