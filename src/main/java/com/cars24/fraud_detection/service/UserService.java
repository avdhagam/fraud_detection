package com.cars24.fraud_detection.service;

import com.cars24.fraud_detection.data.entity.UserEntity;

import java.io.IOException;
import java.util.Optional;

public interface UserService {
    UserEntity registerUser(String name, String phone);  // Register new user
    Optional<UserEntity> getUserById(String userId);  // Fetch user details
    byte[] generateUserPdf(String userId) throws Exception;
}
