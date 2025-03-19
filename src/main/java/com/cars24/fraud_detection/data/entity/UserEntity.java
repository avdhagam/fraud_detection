package com.cars24.fraud_detection.data.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
@Document(collection = "users")
public class UserEntity {

    @Id
    private String id; // Unique User ID (UUID)

    private String name;
    private String email;

    private String password;

    private List<AudioEntity> audioCalls = new ArrayList<>(); // List of transcribed audio calls
    private List<DocumentEntity> documents = new ArrayList<>(); // List of uploaded documents


}