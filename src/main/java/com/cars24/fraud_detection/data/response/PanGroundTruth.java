package com.cars24.fraud_detection.data.response;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import lombok.Data;

@Data
public class PanGroundTruth {

    private String document_type;
    private String name;
    private String date_of_birth;
    private String id_number;
    private String father_name;

    public static PanGroundTruth fromLeadEntity(LeadEntity leadEntity) {
        PanGroundTruth groundTruth = new PanGroundTruth();
        groundTruth.setDocument_type("PAN");
        groundTruth.setName(leadEntity.getName());
        groundTruth.setDate_of_birth(leadEntity.getDob());
        groundTruth.setId_number(leadEntity.getPanNumber());
        groundTruth.setFather_name(leadEntity.getFatherName());
        return groundTruth;
    }
}