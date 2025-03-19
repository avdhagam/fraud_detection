package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.UserDao;
import com.cars24.fraud_detection.data.entity.UserEntity;
import com.cars24.fraud_detection.data.request.LoginRequest;
import com.cars24.fraud_detection.data.request.UserRequest;
import com.cars24.fraud_detection.data.response.LoginResponse;
import com.cars24.fraud_detection.service.UserService;
import com.cars24.fraud_detection.utils.PdfGeneratorUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserEntity registerUser(UserRequest userRequest) {
        // Check if email is already taken
        if (userRequest.getName() == null || userRequest.getName().isBlank()) {
            throw new IllegalArgumentException("User name cannot be null or empty!");
        }

        UserEntity user = new UserEntity();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        String customId = userRequest.getName().replaceAll("\\s", "").toLowerCase() + (int)(Math.random() * 9000 + 1000);
        user.setId(customId);

        return userDao.saveUser(user);
    }

    public LoginResponse loginUser(LoginRequest loginRequest) {
        UserEntity user = userDao.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Construct minimal response
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return response;
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
