package com.cars24.fraud_detection.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "document.types")
@Getter
@Setter
public class DocumentTypeConfig {

    private Map<String, String> mapping;

    public String getDocumentType(String key) {
        return mapping.getOrDefault(key, "Document not found");
    }
}