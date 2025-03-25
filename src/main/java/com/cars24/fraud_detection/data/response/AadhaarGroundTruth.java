package com.cars24.fraud_detection.data.response;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import lombok.Data;

@Data
public class AadhaarGroundTruth {

    private String document_type;
    private String name;
    private String date_of_birth;
    private String gender;
    private String id_number;

    public static AadhaarGroundTruth fromLeadEntity(LeadEntity leadEntity) {
        AadhaarGroundTruth groundTruth = new AadhaarGroundTruth();
        groundTruth.setDocument_type("Aadhaar");
        groundTruth.setName(leadEntity.getName());
        groundTruth.setDate_of_birth(leadEntity.getDob());
        groundTruth.setGender(leadEntity.getGender());
        groundTruth.setId_number(leadEntity.getAdharNumber());
        return groundTruth;
    }

}
