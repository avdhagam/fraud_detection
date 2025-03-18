package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.UserDao;
import com.cars24.fraud_detection.data.entity.UserEntity;
import com.cars24.fraud_detection.service.UserService;
import com.cars24.fraud_detection.utils.PdfGeneratorUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public UserEntity registerUser(String name, String phone) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString());
        user.setName(name);
        user.setPhone(phone);
        return userDao.saveUser(user);
    }

    @Override
    public Optional<UserEntity> getUserById(String userId) {
        return userDao.findUserById(userId);
    }

    public byte[] generateUserPdf(String userId) throws Exception {
        // Step 1: Fetch User Data from API
        String userApiUrl = "http://localhost:8080/users/" + userId;  // Update with actual URL
        ResponseEntity<String> userDataResponse = restTemplate.getForEntity(userApiUrl, String.class);

        if (userDataResponse.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("User not found"); // Handle gracefully
        }

        // Step 2: Parse JSON Response
        JsonNode userData = objectMapper.readTree(userDataResponse.getBody());

        // Step 3: Generate PDF
        return PdfGeneratorUtil.generateUserPdf(userData);
    }
}
