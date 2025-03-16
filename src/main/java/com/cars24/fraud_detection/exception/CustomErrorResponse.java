package com.cars24.fraud_detection.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomErrorResponse {
    private String error;     // Short error message
    private String details;   // Additional details (optional)
    private int errorCode;    // Custom error code
}
