package com.cars24.fraud_detection.data.response;

import lombok.Data;
import java.util.List;

@Data
public class UserResponse {
    private String id; // user-uuid-1234
    private String name;
    private String email;
    private List<AudioResponse> audioCalls;
    private List<DocumentResponse> documents;
}
