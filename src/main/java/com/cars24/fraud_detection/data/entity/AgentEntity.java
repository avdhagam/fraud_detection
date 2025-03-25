package com.cars24.fraud_detection.data.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Document(collection = "agents")
public class AgentEntity {

    @Id
    private String id = UUID.randomUUID().toString(); // Generate UUID on creation

    private String name;

    @Indexed(unique = true) // Ensure emails are unique
    private String email;

    private String password;  // Store HASHED password

    @CreatedDate
    private LocalDateTime createdAt;
}