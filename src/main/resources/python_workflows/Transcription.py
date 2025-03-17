
import requests
import json
import numpy as np
import librosa
# import logging
import tempfile
from pydub import AudioSegment
from sklearn.cluster import KMeans, SpectralClustering, AgglomerativeClustering, DBSCAN
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA
from scipy.spatial.distance import cdist


# logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

# Deepgram API Key
DEEPGRAM_API_KEY = "09cd1982446e4aeca26de7cd6cba5b7b63f668bb"

def convert_mp3_to_wav(mp3_path):
    """Converts an MP3 file to a temporary WAV file."""
    temp_wav = tempfile.NamedTemporaryFile(delete=False, suffix=".wav")
    audio = AudioSegment.from_mp3(mp3_path)
    audio.export(temp_wav.name, format="wav")
    # logging.info(f"Converted MP3 to temporary WAV: {temp_wav.name}")
    return temp_wav

def transcribe_audio_with_timestamps(wav_file):
    """Transcribes audio using Deepgram and returns utterances with timestamps."""
    url = "https://api.deepgram.com/v1/listen?model=whisper-large&language=en-IN&punctuate=true&smart_format=true&utterances=true&words=true&diarize=true"
    headers = {"Authorization": f"Token {DEEPGRAM_API_KEY}", "Content-Type": "audio/wav"}

    with open(wav_file.name, "rb") as audio_file:
        response = requests.post(url, headers=headers, data=audio_file)

    if response.status_code == 200:
        return response.json()
    else:
        # logging.error("Failed to process audio. Deepgram API Error.")
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
    pca = PCA(n_components=min(5, embeddings_scaled.shape[1]))
    embeddings_pca = pca.fit_transform(embeddings_scaled)

    clustering_algorithms = {
        "KMeans": KMeans(n_clusters=num_speakers, random_state=42),
        "Spectral": SpectralClustering(n_clusters=num_speakers, affinity='nearest_neighbors'),
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
            return "error"
    return best_labels if best_labels is not None else np.zeros(len(embeddings))

def format_transcription_output(utterances, speaker_labels):
    """Formats transcription output in a structured string format."""
    output = "    start   end    speaker                                                              utterance\n\n"
    for idx, utterance in enumerate(utterances):
        speaker = f"SPEAKER_{speaker_labels[idx % len(speaker_labels)]:02d}"
        output += f"    {utterance['start']:.3f}  {utterance['end']:.3f} {speaker:<70}{utterance['transcript']}\n"
    return output

# Main execution
# def get_transcripts(mp3_path):
#     temp_wav = convert_mp3_to_wav(mp3_path)
#
#     try:
#         speaker_transcription_result = transcribe_audio_with_timestamps(temp_wav)
#         if speaker_transcription_result:
#             utterances = speaker_transcription_result.get("results", {}).get("utterances", [])
#
#             if not utterances:
#                 logging.info("No utterances found.")
#             else:
#                 embeddings = [extract_features(temp_wav.name, utt['start'], utt['end']) for utt in utterances if utt['end'] - utt['start'] > 0.5]
#                 embeddings = [emb for emb in embeddings if emb is not None]
#
#                 if embeddings:
#                     num_speakers = estimate_speaker_count(np.array(embeddings))
#
#                     speaker_labels = cluster_speakers(np.array(embeddings), num_speakers)
#                     speaker_mapping = {spk: f"Speaker {idx+1}" for idx, spk in enumerate(set(speaker_labels))}
#
#                     logging.info(f"Detected {num_speakers} speakers.")
#
#                     formatted_output = format_transcription_output(utterances, speaker_labels)
#                     logging.info("\n" + formatted_output)
#                 else:
#                     logging.warning("No embeddings found for clustering.")
#         else:
#             logging.error("Failed to process audio.")
#     finally:
#         temp_wav.close()
#         logging.info(f"Deleting temporary file: {temp_wav.name}")

# Run the script
# if __name__ == "__main__":
#     mp3_input_path = "C:\\Users\\dayad\\Downloads\\cars24\\fraud_detection\\src\\main\\resources\\audio_storage\\cc74d250-4403-496c-9c88-4d3618db56e1.mp3"
#     main(mp3_input_path)

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
                embeddings = [extract_features(temp_wav.name, utt['start'], utt['end']) for utt in utterances if utt['end'] - utt['start'] > 0.5]
                embeddings = [emb for emb in embeddings if emb is not None]

                if embeddings:
                    num_speakers = estimate_speaker_count(np.array(embeddings))

                    speaker_labels = cluster_speakers(np.array(embeddings), num_speakers)
                    formatted_output = format_transcription_output(utterances, speaker_labels)

                    # logging.info(f"Detected {num_speakers} speakers.")
                    return formatted_output  # Return the formatted string
                else:
                    # logging.warning("No embeddings found for clustering.")
                    return "No embeddings found for clustering."
        else:
            # logging.error("Failed to process audio.")
            return "Failed to process audio."
    finally:
        temp_wav.close()
        # logging.info(f"Deleting temporary file: {temp_wav.name}")
