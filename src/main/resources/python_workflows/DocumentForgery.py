import os
import cv2
import json
import numpy as np
import logging
import piexif
import magic
import uuid
from skimage.feature import graycomatrix, graycoprops
from datetime import datetime
from skimage.metrics import structural_similarity as ssim
from PIL import Image, ImageChops, ImageEnhance

# Configure logging
log_dir = "src/main/resources/document_storage/output"
os.makedirs(log_dir, exist_ok=True)
logging.basicConfig(
    filename=os.path.join(log_dir, "document_forgery_ml.log"),
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

# Paths
OUTPUT_PATH = "src/main/resources/document_storage/output"
os.makedirs(OUTPUT_PATH, exist_ok=True)

def detect_tampering(image_path):
    """Perform Error Level Analysis (ELA) to detect tampering."""
    try:
        original = Image.open(image_path).convert("RGB")
        temp_path = os.path.join(OUTPUT_PATH, "temp.jpg")
        original.save(temp_path, "JPEG", quality=90)
        recompressed = Image.open(temp_path)
        diff = ImageChops.difference(original, recompressed)
        extrema = diff.getextrema()
        max_diff = max([ex[1] for ex in extrema])
        tampering_score = min(1.0, max(0.0, max_diff / 255))
        return {"tamperingScore": tampering_score, "insight": "ELA-based tampering analysis completed."}
    except Exception as e:
        logging.error(f"Tampering detection error: {str(e)}")
        return {"tamperingScore": 0.5, "insight": "Error in ELA processing."}

def analyze_metadata(image_path):
    """Extract and analyze metadata for inconsistencies like future dates and modifications."""
    try:
        exif_data = piexif.load(image_path)

        if not exif_data:
            return {"metadataAnomalyScore": 1.0, "insight": "No metadata found. Possible forgery."}

        metadata_issues = []
        exif_info = exif_data.get("0th", {})
        exif_sub = exif_data.get("Exif", {})

        # Extracting timestamps
        date_time_original = exif_sub.get(piexif.ExifIFD.DateTimeOriginal, b"").decode("utf-8", errors="ignore")
        date_time_digitized = exif_sub.get(piexif.ExifIFD.DateTimeDigitized, b"").decode("utf-8", errors="ignore")
        current_time = datetime.now()

        # Check for future timestamps
        for date_field, label in [(date_time_original, "Original Date"), (date_time_digitized, "Digitized Date")]:
            if date_field:
                try:
                    parsed_date = datetime.strptime(date_field, "%Y:%m:%d %H:%M:%S")
                    if parsed_date > current_time:
                        metadata_issues.append(f"{label} is in the future: {date_field}")
                except ValueError:
                    metadata_issues.append(f"Invalid date format in {label}: {date_field}")

        # Check if digitized time is significantly different from original capture time
        if date_time_original and date_time_digitized and date_time_original != date_time_digitized:
            metadata_issues.append("Timestamp mismatch detected between original and digitized time.")

        # Checking for suspicious editing software
        software = exif_info.get(piexif.ImageIFD.Software, b"").decode("utf-8", errors="ignore")
        if software and any(tool in software.lower() for tool in ["photoshop", "gimp", "editor"]):
            metadata_issues.append(f"Suspicious software detected: {software}")

        # Compute risk score (normalize based on detected issues)
        metadata_score = min(1.0, len(metadata_issues) / 3.0)  # Cap the score at 1.0

        return {
            "metadataAnomalyScore": metadata_score,
            "insight": "Metadata analysis completed.",
            "issues": metadata_issues
        }

    except Exception as e:
        logging.error(f"Metadata analysis error: {str(e)}")
        return {"metadataAnomalyScore": 0.5, "insight": "Metadata extraction failed due to an error."}

def check_format_consistency(image_path):
    """Verify if the image format matches the file extension."""
    try:
        file_type = magic.Magic(mime=True).from_file(image_path)
        expected_types = ["image/jpeg", "image/png","image/jpg"]
        score = 0.0 if file_type in expected_types else 1.0
        return {"formatConsistencyScore": score, "insight": f"File format detected: {file_type}"}
    except Exception as e:
        logging.error(f"Format consistency error: {str(e)}")
        return {"formatConsistencyScore": 0.5, "insight": "File format detection failed."}

def detect_security_features(image_path):
    """Detect security features using edge detection."""
    try:
        img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
        edges = cv2.Canny(img, 100, 200)
        edge_density = np.mean(edges) / 255
        security_score = min(1.0, max(0.0, 1 - edge_density))
        return {"securityFeatureScore": security_score, "insight": "Edge detection performed for security features."}
    except Exception as e:
        logging.error(f"Security feature detection error: {str(e)}")
        return {"securityFeatureScore": 0.5, "insight": "Edge detection failed."}

def analyze_background_integrity(image_path):
    """Analyze background consistency using texture analysis (GLCM)."""
    try:
        img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
        glcm = graycomatrix(img, [1], [0], symmetric=True, normed=True)
        contrast = graycoprops(glcm, 'contrast')[0, 0]
        uniformity = graycoprops(glcm, 'ASM')[0, 0]
        score = min(1.0, max(0.0, contrast / 100 + (1 - uniformity)))
        return {"backgroundConsistencyScore": score, "insight": "Texture analysis performed for background consistency."}
    except Exception as e:
        logging.error(f"Background integrity error: {str(e)}")
        return {"backgroundConsistencyScore": 0.5, "insight": "Texture analysis failed."}

def analyze_forgery(image_path):
    """Main function to analyze document forgery."""
    try:
        if not os.path.exists(image_path):
            raise FileNotFoundError(f"File not found: {image_path}")

        tampering_results = detect_tampering(image_path)
        metadata_results = analyze_metadata(image_path)
        format_results = check_format_consistency(image_path)
        security_results = detect_security_features(image_path)
        background_results = analyze_background_integrity(image_path)

        final_score = np.mean([
            tampering_results['tamperingScore'],
            metadata_results['metadataAnomalyScore'],
            format_results['formatConsistencyScore'],
            security_results['securityFeatureScore'],
            background_results['backgroundConsistencyScore']
        ])
        decision = "Forgery Likely" if final_score > 0.6 else "Likely Authentic"

        combined_results = {
            "forgeryAnalysis": {
                "tamperingAnalysis": tampering_results,
                "metadataAnalysis": metadata_results,
                "formatConsistencyAnalysis": format_results,
                "securityFeatureAnalysis": security_results,
                "backgroundIntegrityAnalysis": background_results,
                "overallForgeryAssessment": {
                    "finalForgeryRiskScore": final_score,
                    "decision": decision,
                }
            },
            "success": True
        }

        output_filename = f"forgery_analysis_{uuid.uuid4().hex[:8]}.json"
        output_path = os.path.join(OUTPUT_PATH, output_filename)
        with open(output_path, "w", encoding="utf-8") as json_file:
            json.dump(combined_results, json_file, indent=4)

        logging.info(f"Forgery analysis results saved to: {output_path}")
        return combined_results
    except Exception as e:
        logging.error(f"Forgery analysis error: {str(e)}")
        return {"error": str(e), "finalForgeryRiskScore": 0.7, "success": False}

if __name__ == "__main__":
    import sys
    if len(sys.argv) != 2:
        print(json.dumps({"error": "Usage: python document_forgery_ml.py <image_path>", "success": False}))
        sys.exit(1)
    image_path = sys.argv[1]
    result = analyze_forgery(image_path)
    print(json.dumps(result, indent=4))
