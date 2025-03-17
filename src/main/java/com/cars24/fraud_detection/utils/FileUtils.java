package com.cars24.fraud_detection.utils;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.data.response.DocumentResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtils {

    private static final String JPG_EXTENSION = ".jpg";
    private static final String PNG_EXTENSION = ".png";

    public static boolean isValidFileType(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(JPG_EXTENSION) || lowerCaseFileName.endsWith(PNG_EXTENSION);
    }

    public static DocumentEntity buildDocumentEntity(DocumentRequest request, DocumentResponse response) {
        return DocumentEntity.builder()
                .userId(request.getUserId())
                .fileName(request.getFileName())
                .status(response.isValid() ? "COMPLETED" : "FAILED")
                .remarks(response.getRemarks())
                .ocrResults(response.getOcrResults())
                .qualityResults(response.getQualityResults())
                .forgeryResults(response.getForgeryResults())
                .validationResults(response.getValidationResults())
                .finalRiskScore(response.getFinalRiskScore())
                .riskLevel(response.getRiskLevel())
                .decision(response.getDecision())
                .nextSteps(response.getNextSteps())
                .build();
    }
}
