package com.cars24.fraud_detection.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeadNameEmail {
    private String id;
    private String name;
    private String email;
}