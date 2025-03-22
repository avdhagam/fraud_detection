package com.cars24.fraud_detection.data.response;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import lombok.Data;

@Data
public class AadhaarGroundTruth {

    private String documentType;
    private String name;
    private String dateOfBirth;
    private String gender;
    private String idNumber;

    public static AadhaarGroundTruth fromLeadEntity(LeadEntity leadEntity) {
        AadhaarGroundTruth groundTruth = new AadhaarGroundTruth();
        groundTruth.setDocumentType("Aadhaar");
        groundTruth.setName(leadEntity.getName());
        groundTruth.setDateOfBirth(leadEntity.getDob());
        groundTruth.setGender(leadEntity.getGender());
        groundTruth.setIdNumber(leadEntity.getAdharNumber());
        return groundTruth;
    }

}
