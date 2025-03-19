package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.UserEntity;
import com.cars24.fraud_detection.data.request.LoginRequest;
import com.cars24.fraud_detection.data.request.UserRequest;
import com.cars24.fraud_detection.data.response.LoginResponse;

import java.io.IOException;
import java.util.Optional;

public interface UserService {
    UserEntity registerUser(UserRequest userRequest);
    LoginResponse loginUser(LoginRequest loginRequest);// Register new user
    Optional<UserEntity> getUserById(String userId);  // Fetch user details
    byte[] generateUserPdf(String userId) throws Exception;
}
