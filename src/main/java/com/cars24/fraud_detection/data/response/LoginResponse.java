package com.cars24.fraud_detection.data.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String id; // user-uuid-1234
    private String name;
    private String email;
}
