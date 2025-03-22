package com.cars24.fraud_detection.data.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AudioRequest {

        private String uuid;

        private String agentId;
        private String leadId;

        private MultipartFile audioFile;

        private String documentType;
}