package com.cars24.fraud_detection.data.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
public class AudioRequest {

        private MultipartFile audioFile;
        private String filepath;
        private String uuid;

        private String userReportId;
}