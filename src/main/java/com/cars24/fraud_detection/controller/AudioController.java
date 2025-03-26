package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.exception.AudioProcessingException;
import com.cars24.fraud_detection.service.AudioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/audio")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST,RequestMethod.GET, RequestMethod.PUT})

public class AudioController {

    private final AudioService audioService;

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping("/upload")
    public ResponseEntity<AudioResponse> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("agentId") String agentId,
            @RequestParam("leadId") String leadId,
            @RequestParam("documentType") String documentType) {  // Removed throws declaration
        try {
            log.info("Received audio file upload request for lead: {}", leadId);

            AudioRequest audioRequest = new AudioRequest();
            audioRequest.setAudioFile(file);
            audioRequest.setAgentId(agentId);
            audioRequest.setLeadId(leadId);
            audioRequest.setDocumentType(documentType);

            AudioResponse response = audioService.processAudioRequest(audioRequest);

            return ResponseEntity.ok(response);

        } catch (AudioProcessingException e) {
            log.error("Error processing audio: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Or create an error AudioResponse
        } catch (IOException e) {
            log.error("IO error during audio upload", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);  // Or create an error AudioResponse
        }
    }

    @GetMapping("/{audioId}")
    public ResponseEntity<AudioResponse> getAudio(@PathVariable String audioId) throws AudioProcessingException {
        log.info("Received get request for audio ID: {}", audioId);
        AudioResponse response = audioService.getAudioResults(audioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/file/{audioId}")
    public ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String audioId) {
        log.info("Received get request for audio file ID: {}", audioId);
        return audioService.getAudioFile(audioId);
    }

    @GetMapping("/recent/{leadId}")
    public ResponseEntity<java.util.List<String>> getRecentAudios(
            @PathVariable String leadId,
            @RequestParam(defaultValue = "5") int limit) {

        log.info("Fetching last {} audio files for lead ID: {}", limit, leadId);
        java.util.List<String> recentAudioUUIDs = audioService.getRecentAudios(leadId, limit);

        return ResponseEntity.ok(recentAudioUUIDs);
    }

    @GetMapping("/recentaudio/{leadId}")
    public ResponseEntity<String> getRecentAudio(@PathVariable String leadId) {
        log.info("Fetching most recent audio file for lead ID: {}", leadId);
        java.util.List<String> recentAudioUUIDs = audioService.getRecentAudios(leadId, 1);
        if (recentAudioUUIDs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String latestAudioId = recentAudioUUIDs.get(0);
        return ResponseEntity.ok(latestAudioId);
    }
}