package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.response.AudioResponse;
import com.cars24.fraud_detection.service.AudioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/audio")
public class AudioController {

    private final AudioService audioService;
    private static final Logger logger = Logger.getLogger(AudioController.class.getName());

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping("/trigger")
    public ResponseEntity<AudioResponse> trigger(@RequestParam("file") MultipartFile file) throws IOException {
        logger.info("Received audio file upload request");
        AudioRequest audioRequest = new AudioRequest();
        audioRequest.setAudioFile(file);
        AudioResponse response = audioService.processAudioRequest(audioRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public String getmapping(){
        logger.info("Received GET request");
        return "hello";
    }
}