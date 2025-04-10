import requests
import json
import numpy as np
import librosa
import tempfile

from pydub import AudioSegment
from sklearn.cluster import KMeans, SpectralClustering, AgglomerativeClustering, DBSCAN
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA
from scipy.spatial.distance import cdist

import sys
from pathlib import Path

script_path = Path(__file__).resolve() # finds absolute path of script
root_dir = script_path.parents[4]  # Calculate root directory by moving up four levels
sys.path.append(str(root_dir)) # Add the project's root directory to the Python path

import config
import subprocess

# Deepgram API Key
DEEPGRAM_API_KEY = config.DEEPGRAM_API_KEY

import subprocess
import os
from pydub import AudioSegment

def is_ffmpeg_installed():
    """Check if FFmpeg is installed and accessible."""
    try:
        subprocess.run(["ffmpeg", "-version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return True
    except FileNotFoundError:
        return False

def force_reencode_mp3(mp3_path):
    """Re-encode a potentially corrupt MP3 file using FFmpeg."""
    reencoded_mp3 = mp3_path.replace(".mp3", "_fixed.mp3")

    if not is_ffmpeg_installed():
        raise RuntimeError("FFmpeg is not installed or not in PATH. Please install FFmpeg.")

    try:
        result = subprocess.run(
            ["ffmpeg", "-y", "-i", mp3_path, "-acodec", "libmp3lame", "-q:a", "2", reencoded_mp3],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        if not os.path.exists(reencoded_mp3):
            print("FFmpeg re-encode failed:", result.stderr)
            return None
        return reencoded_mp3
    except Exception as e:
        print(f"FFmpeg execution error: {e}")
        return None

def convert_mp3_to_wav(mp3_path):
    """Convert MP3 to WAV, re-encoding if necessary."""
    wav_path = mp3_path.replace(".mp3", ".wav")

    try:
        audio = AudioSegment.from_mp3(mp3_path)
    except Exception as e:
        print(f"MP3 is corrupt, attempting re-encode: {e}")
        fixed_mp3_path = force_reencode_mp3(mp3_path)
        if fixed_mp3_path and os.path.exists(fixed_mp3_path):
            print("Re-encode successful, using fixed MP3")
            mp3_path = fixed_mp3_path
        else:
            raise ValueError(f"Failed to fix MP3 file: {mp3_path}")

    # Convert to WAV
    audio = AudioSegment.from_mp3(mp3_path)
    audio.export(wav_path, format="wav")
    return wav_path

# def convert_mp3_to_wav(mp3_path):
#     """Converts an MP3 file to a temporary WAV file."""
#     temp_wav = tempfile.NamedTemporaryFile(delete=False, suffix=".wav")
#     audio = AudioSegment.from_mp3(mp3_path)
#     audio.export(temp_wav.name, format="wav")
#     #logging.info(f"Converted MP3 to temporary WAV: {temp_wav.name}")
#     return temp_wav

def transcribe_audio_with_timestamps(wav_file):
    """Transcribes audio using Deepgram and returns utterances with timestamps."""
    url = "https://api.deepgram.com/v1/listen?model=whisper-large&language=en-IN&punctuate=true&smart_format=true&utterances=true&words=true&diarize=true"
    headers = {"Authorization": f"Token {DEEPGRAM_API_KEY}", "Content-Type": "audio/wav"}


    with open(wav_file, "rb") as audio_file:
        try:
            response = requests.post(url, headers=headers, data=audio_file)
            response.raise_for_status()

            try:
                json_data = response.json()
                return json_data
            except json.JSONDecodeError as e:

                return None
        except requests.exceptions.RequestException as e:

            return None
        except Exception as e:

            return None

def extract_features(audio_path, start, end, sr=16000):
    """Extracts audio features for speaker clustering."""
    y, sr = librosa.load(audio_path, sr=sr, offset=start, duration=(end - start))
    if len(y) == 0:
        return None
    mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=20)
    delta_mfcc = librosa.feature.delta(mfcc)
    spectral_contrast = librosa.feature.spectral_contrast(y=y, sr=sr)
    chroma = librosa.feature.chroma_stft(y=y, sr=sr)
    rms = librosa.feature.rms(y=y)
    features = np.concatenate([
        np.mean(mfcc.T, axis=0), np.mean(delta_mfcc.T, axis=0),
        np.mean(spectral_contrast.T, axis=0), np.mean(chroma.T, axis=0),
        np.mean(rms.T, axis=0)
    ])
    return features

def estimate_speaker_count(embeddings):
    """Estimates the optimal number of speakers using K-Means distortion analysis."""
    distortions = []
    K = range(1, 6)
    for k in K:
        model = KMeans(n_clusters=k, random_state=42).fit(embeddings)
        distortions.append(sum(np.min(cdist(embeddings, model.cluster_centers_, 'euclidean'), axis=1)) / embeddings.shape[0])
    optimal_k = np.argmin(np.gradient(distortions)) + 1
    return max(2, optimal_k)

def cluster_speakers(embeddings, num_speakers):
    """Clusters audio features to determine speaker labels."""
    scaler = StandardScaler()
    embeddings_scaled = scaler.fit_transform(embeddings)

    # Ensure PCA component count does not exceed the number of features
    pca_components = min(5, embeddings_scaled.shape[1], len(embeddings_scaled))
    pca = PCA(n_components=pca_components, random_state=42)
    embeddings_pca = pca.fit_transform(embeddings_scaled)

    clustering_algorithms = {
        "KMeans": KMeans(n_clusters=num_speakers, random_state=42),
        "Spectral": SpectralClustering(n_clusters=num_speakers, affinity='nearest_neighbors', random_state=42),
        "Agglomerative": AgglomerativeClustering(n_clusters=num_speakers, linkage="ward"),
        "DBSCAN": DBSCAN(eps=1.5, min_samples=2)
    }

    best_labels = None
    for name, model in clustering_algorithms.items():
        try:
            labels = model.fit_predict(embeddings_pca)
            if len(set(labels)) > 1:  # Ensure multiple speakers are detected
                best_labels = labels
                break
        except Exception as e:
            # logging.warning(f"{name} clustering failed: {e}")
            continue

    return best_labels if best_labels is not None else np.zeros(len(embeddings))


def format_transcription_output(utterances, speaker_labels):
    """Formats transcription output in a structured string format."""
    output = '"""\n    start   end    speaker                                                              utterance\n\n'
    for idx, utterance in enumerate(utterances):
        speaker = f"SPEAKER_{speaker_labels[idx % len(speaker_labels)]:02d}"
        output += f"    {utterance['start']:.3f}  {utterance['end']:.3f} {speaker:<70}{utterance['transcript']}\n"
    output+='"""'
    return output

def get_transcripts(mp3_path):
    temp_wav = convert_mp3_to_wav(mp3_path)

    try:
        speaker_transcription_result = transcribe_audio_with_timestamps(temp_wav)
        if speaker_transcription_result:
            utterances = speaker_transcription_result.get("results", {}).get("utterances", [])

            if not utterances:
                # logging.info("No utterances found.")
                return "No utterances found."
            else:
                embeddings = [extract_features(temp_wav, utt['start'], utt['end']) for utt in utterances if utt['end'] - utt['start'] > 0.5]
                embeddings = [emb for emb in embeddings if emb is not None]

                if embeddings:
                    num_speakers = estimate_speaker_count(np.array(embeddings))

                    speaker_labels = cluster_speakers(np.array(embeddings), num_speakers)
                    formatted_output = format_transcription_output(utterances, speaker_labels)


                    return formatted_output  # Return the formatted string
                else:

                    return "No embeddings found for clustering."
        else:

            return "Failed to process audio."
    finally:
        os.remove(temp_wav)