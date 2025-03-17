package com.cars24.fraud_detection.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles document processing exceptions.
     */
    @ExceptionHandler(DocumentProcessingException.class)
    public ResponseEntity<CustomErrorResponse> handleDocumentProcessingException(DocumentProcessingException ex) {
        log.error("DocumentProcessingException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomErrorResponse("Document processing failed", ex.getMessage(), 4001));
    }

    /**
     * Handles Python script execution errors.
     */
    @ExceptionHandler(PythonExecutionException.class)
    public ResponseEntity<CustomErrorResponse> handlePythonExecutionException(PythonExecutionException ex) {
        log.error("PythonExecutionException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomErrorResponse("Python script execution failed", ex.getMessage(), 1001));
    }

    /**
     * Handles validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomErrorResponse("Validation error", "Invalid input data", 4002));
    }

    /**
     * Handles generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomErrorResponse("Unexpected error", "Please try again later.", 9999));
    }

    /**
     * Handles audio processing exceptions.
     */
    @ExceptionHandler(AudioProcessingException.class)
    public ResponseEntity<CustomErrorResponse> handleAudioProcessingException(AudioProcessingException ex) {
        log.error("AudioProcessingException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomErrorResponse("Audio Processing Failed", ex.getMessage(),4003));
    }
}