package com.cars24.fraud_detection.exception;

public class PythonExecutionException extends RuntimeException {

    public PythonExecutionException(String message) {
        super(message);
    }

    public PythonExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
