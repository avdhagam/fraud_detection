package com.cars24.fraud_detection.utils;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Logger;

public class AudioServiceUtils {
    private static final Logger logger = Logger.getLogger(AudioServiceUtils.class.getName());
    private static final String STORAGE_PATH = "src/main/resources/audio_storage";

    public static AudioResponse mapToResponse(AudioEntity entity) {
        AudioResponse response = new AudioResponse();
        response.setUuid(entity.getId());
        response.setTranscript(entity.getTranscript());
        response.setReferenceName(entity.getReferenceName());
        response.setSubjectName(entity.getSubjectName());
        response.setSubjectAddress(entity.getSubjectAddress());
        response.setRelationToSubject(entity.getRelationToSubject());
        response.setSubjectOccupation(entity.getSubjectOccupation());
        response.setOverallScore(entity.getOverallScore());
        response.setExplanation(entity.getExplanation());
        response.setFieldByFieldScores(entity.getFieldByFieldScores());
        response.setAudioAnalysis(entity.getAudioAnalysis());
        response.setStatus(entity.getStatus());
        return response;
    }
    public static String saveAudio(MultipartFile file) throws AudioProcessingException {
        try {
            // Check if file is empty
            if (file.isEmpty()) {
                logger.severe("Failed to save audio file: File is empty");
                throw new AudioProcessingException("Failed to store audio file: File is empty");
            }

            // Ensure the storage directory exists
            Path storagePath = Paths.get(STORAGE_PATH);
            Files.createDirectories(storagePath);

            // Generate a unique file name
            String uniqueFileName = UUID.randomUUID().toString() + ".mp3";

            // Properly construct path using Path API
            Path destinationFile = storagePath.resolve(uniqueFileName);

            logger.info("Attempting to save file to: " + destinationFile.toString());

            // Save the file to disk
            Files.copy(file.getInputStream(), destinationFile);

            logger.info("Audio file saved successfully to " + destinationFile.toString());
            return destinationFile.toString();

        } catch (IOException e) {
            logger.log(java.util.logging.Level.SEVERE, "Error saving audio file", e);
            throw new AudioProcessingException("Failed to store audio file: " + e.getMessage());
        }
    }
}
