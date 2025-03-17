package com.cars24.fraud_detection.exception;



    public class AudioProcessingException extends Exception {

        public AudioProcessingException(String message) {
            super(message);
        }

        public AudioProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

