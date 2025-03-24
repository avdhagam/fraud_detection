package com.cars24.fraud_detection.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "document.types")
public class DocumentTypeConfig {
    private Map<String, String> mapping;

    public String getDocumentDisplayName(String key) {
        return mapping.getOrDefault(key, "File not found");
    }
}
