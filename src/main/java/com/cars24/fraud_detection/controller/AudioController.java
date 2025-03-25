package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.entity.AudioEntity;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/audio")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST,RequestMethod.GET, RequestMethod.PUT})

public class AudioController {

    private static final String AUDIO_STORAGE_PATH = "src/main/resources/audio_storage";
    private final AudioService audioService;
    private static final Logger logger = Logger.getLogger(AudioController.class.getName());

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping("/trigger")
    public ResponseEntity<AudioResponse> trigger(@RequestParam("file") MultipartFile file) throws IOException, AudioProcessingException {
        logger.info("Received audio file upload request");
        AudioRequest audioRequest = new AudioRequest();
        audioRequest.setAudioFile(file);
        AudioResponse response = audioService.processAudioRequest(audioRequest);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<AudioResponse> getAudioResult(@PathVariable String id) {
//        logger.info("Received get request");
//        AudioResponse response = audioService.getAudioResults(id);
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/upload-audio")
    public ResponseEntity<AudioResponse> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userReportId") String userReportId) throws IOException, AudioProcessingException {

        logger.info("Received audio file upload request for user");

        AudioRequest audioRequest = new AudioRequest();
        audioRequest.setAudioFile(file);
        audioRequest.setUserReportId(userReportId); // Ensure `AudioRequest` has this field

        AudioResponse response = audioService.processAudioRequest(audioRequest);

        return ResponseEntity.ok(response);
    }
    @PutMapping("/update-audio")
    public ResponseEntity<AudioResponse> updateAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userReportId") String userReportId,
            @RequestParam("documentType") String documentType) throws IOException, AudioProcessingException {

        // logger.info("Received audio update request for userReportId:"+ userReportId + " with documentType: "  + documentType);

        AudioRequest audioRequest = new AudioRequest();
        audioRequest.setAudioFile(file);
        audioRequest.setUserReportId(userReportId);
        audioRequest.setDocumentType(documentType); // Ensure `AudioRequest` has this field

        AudioResponse response = audioService.processAudioRequest(audioRequest);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/audio-file/{id}")
    public ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String id) {
        return audioService.getAudioFile(id);

    }

    @GetMapping("/{userId}")
    public ResponseEntity<AudioResponse> getAudioResult(@PathVariable String userId) {
        // logger.info("Received get request");
        AudioResponse response = audioService.getAudioResult(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent/{userId}")
    public ResponseEntity<List<String>> getRecentAudioUUIDs(
            @PathVariable String userId,
            @RequestParam(defaultValue = "5") int limit) {

        log.info("Fetching last {} audio UUIDs for user ID: {}", limit, userId);
        List<String> recentAudioUUIDs = audioService.getRecentAudios(userId, limit);

        return ResponseEntity.ok(recentAudioUUIDs);
    }
}