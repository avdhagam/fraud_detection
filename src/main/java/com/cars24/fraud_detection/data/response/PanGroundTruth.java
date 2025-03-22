package com.cars24.fraud_detection.data.response;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import lombok.Data;

@Data
public class PanGroundTruth {

    private String documentType;
    private String name;
    private String dateOfBirth;
    private String idNumber;
    private String fatherName;

    public static PanGroundTruth fromLeadEntity(LeadEntity leadEntity) {
        PanGroundTruth groundTruth = new PanGroundTruth();
        groundTruth.setDocumentType("PAN");
        groundTruth.setName(leadEntity.getName());
        groundTruth.setDateOfBirth(leadEntity.getDob());
        groundTruth.setIdNumber(leadEntity.getPanNumber());
        groundTruth.setFatherName(leadEntity.getFatherName());
        return groundTruth;
    }
}