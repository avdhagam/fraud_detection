package com.cars24.fraud_detection.data.request;

import com.cars24.fraud_detection.data.entity.LeadEntity;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Date;

@Data
public class LeadRequest {

    @NotNull(message = "Agent ID is required")
    @NotBlank(message = "Agent ID cannot be blank")
    @Size(min = 1, max = 50, message = "Agent ID must be between 1 and 50 characters")
    private String agentId;

    @NotNull(message = "Name is required")
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Email is required")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(min = 1, max = 100, message = "Email must be between 1 and 100 characters")
    private String email;

    @NotNull(message = "Date of Birth is required")
    @NotBlank(message = "Date of Birth cannot be blank")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Invalid date format. Use YYYY-MM-DD")
    private String dob;

    @NotNull(message = "Gender is required")
    @NotBlank(message = "Gender cannot be blank")
    @Size(min = 1, max = 10, message = "Gender must be between 1 and 10 characters")
    private String gender;

    @NotNull(message = "Father's Name is required")
    @NotBlank(message = "Father's Name cannot be blank")
    @Size(min = 1, max = 100, message = "Father's Name must be between 1 and 100 characters")
    private String fatherName;

    @NotNull(message = "Aadhar Number is required")
    @NotBlank(message = "Aadhar Number cannot be blank")
    @Pattern(regexp = "^\\d{12}$", message = "Invalid Aadhar Number. Use 12 digits")
    private String adharNumber;

    @NotNull(message = "PAN Number is required")
    @NotBlank(message = "PAN Number cannot be blank")
    @Pattern(regexp = "^[A-Z]{5}\\d{4}[A-Z]$", message = "Invalid PAN Number format. Use XXXXX1234X")
    private String panNumber;

    @NotNull(message = "Document Type is required")
    @NotBlank(message = "Document Type cannot be blank")
    @Size(min = 1, max = 50, message = "Document Type must be between 1 and 50 characters")
    private String docType;

    @NotNull(message = "Reference Name is required")
    @NotBlank(message = "Reference Name cannot be blank")
    @Size(min = 1, max = 100, message = "Reference Name must be between 1 and 100 characters")
    private String referenceName;

    @NotNull(message = "Subject Address is required")
    @NotBlank(message = "Subject Address cannot be blank")
    @Size(min = 1, max = 200, message = "Subject Address must be between 1 and 200 characters")
    private String subjectAddress;

    @NotNull(message = "Relation to Subject is required")
    @NotBlank(message = "Relation to Subject cannot be blank")
    @Size(min = 1, max = 50, message = "Relation to Subject must be between 1 and 50 characters")
    private String relationToSubject;

    @NotNull(message = "Subject Occupation is required")
    @NotBlank(message = "Subject Occupation cannot be blank")
    @Size(min = 1, max = 50, message = "Subject Occupation must be between 1 and 50 characters")
    private String subjectOccupation;

    @NotNull(message = "Address is required")
    @NotBlank(message = "Address cannot be blank")
    @Size(min = 1, max = 200, message = "Address must be between 1 and 200 characters")
    private String address;

    @NotNull(message = "Phone Number is required")
    @NotBlank(message = "Phone Number cannot be blank")
    @Pattern(regexp = "^\\d{10}$", message = "Invalid Phone Number. Use 10 digits")
    private String phoneNumber;

    public LeadEntity toLeadEntity() {
        LeadEntity leadEntity = new LeadEntity();
        leadEntity.setAgentId(this.agentId);
        leadEntity.setName(this.name);
        leadEntity.setEmail(this.email);
        leadEntity.setDob(this.dob);
        leadEntity.setGender(this.gender);
        leadEntity.setFatherName(this.fatherName); // Add this line
        leadEntity.setAdharNumber(this.adharNumber);
        leadEntity.setPanNumber(this.panNumber);
        leadEntity.setDocType(this.docType); // Add this line
        leadEntity.setReferenceName(this.referenceName);
        leadEntity.setRelationToSubject(this.relationToSubject);
        leadEntity.setSubjectOccupation(this.subjectOccupation);
        leadEntity.setAddress(this.address);
        leadEntity.setPhoneNumber(this.phoneNumber);
        leadEntity.setCreatedAt(LocalDate.now().atStartOfDay()); // Add this line
        return leadEntity;
    }
}
