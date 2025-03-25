package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.InsightsEntity;
import com.cars24.fraud_detection.data.entity.UserEntity;
import com.cars24.fraud_detection.data.request.LoginRequest;
import com.cars24.fraud_detection.data.request.UserRequest;
import com.cars24.fraud_detection.data.response.LoginResponse;

import java.util.List;

public interface UserService {
    UserEntity registerUser(UserRequest userRequest);
    LoginResponse loginUser(LoginRequest loginRequest);// Register new user
    List<DocumentEntity> getUserById(String userId);  // Fetch user details
    byte[] generateUserPdf(String userId) throws Exception;

    List<InsightsEntity> getInsights(String userId);
}
