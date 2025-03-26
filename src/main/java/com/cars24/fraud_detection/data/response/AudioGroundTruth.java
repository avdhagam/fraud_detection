package com.cars24.fraud_detection.data.response;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import lombok.Data;

@Data
public class AudioGroundTruth {

    private String referenceName;
    private String subjectName;
    private String subjectAddress;
    private String relationToSubject;
    private String subjectOccupation;


    public static AudioGroundTruth fromLeadEntity(LeadEntity leadEntity) {
        AudioGroundTruth groundTruth = new AudioGroundTruth();
        groundTruth.setReferenceName(leadEntity.getReferenceName());
        groundTruth.setSubjectName(leadEntity.getName());
        groundTruth.setSubjectAddress(leadEntity.getSubjectAddress());
        groundTruth.setRelationToSubject(leadEntity.getRelationToSubject());
        groundTruth.setSubjectOccupation(leadEntity.getSubjectOccupation());
        return groundTruth;
    }
}
