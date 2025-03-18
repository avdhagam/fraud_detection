package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.service.AudioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class AudioController {

    private final AudioService audioService;
    private static final String AUDIO_STORAGE_PATH = "src/main/resources/audio_storage"; // Define path here


    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping("/audio/trigger")  // Added /audio prefix for consistency
    public ResponseEntity<AudioResponse> trigger(@RequestParam("file") MultipartFile file) throws IOException, AudioProcessingException {
        log.info("Received audio file upload request");
        AudioRequest audioRequest = new AudioRequest();
        audioRequest.setAudioFile(file);
        AudioResponse response = audioService.processAudioRequest(audioRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/audio/{id}") // Added /audio prefix for consistency
    public ResponseEntity<AudioResponse> getAudioResult(@PathVariable String id) {
        log.info("Received get request for audio result with id: {}", id);
        AudioResponse response = audioService.getAudioResults(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/audio-file/{id}")
    public ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String id) {
        try {
            // Construct the file path
            Path filePath = Paths.get(AUDIO_STORAGE_PATH, id + ".mp3");
            File audioFile = filePath.toFile();

            if (!audioFile.exists()) {
                log.warn("Audio file not found for id: {}", id);
                return ResponseEntity.notFound().build();
            }

            FileSystemResource resource = new FileSystemResource(audioFile);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg")); // Set content type to audio/mpeg
            headers.setContentLength(audioFile.length());
            headers.setContentDispositionFormData("attachment", id + ".mp3"); // Optional:  Suggest a filename

            log.info("Successfully retrieved audio file for id: {}", id);
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);


        } catch (Exception e) {
            log.error("Error retrieving audio file for id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}