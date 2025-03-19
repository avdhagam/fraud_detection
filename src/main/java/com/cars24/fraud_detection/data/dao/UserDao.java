package com.cars24.fraud_detection.data.dao;

import com.cars24.fraud_detection.data.entity.UserEntity;
import java.util.Optional;

public interface UserDao {
    UserEntity saveUser(UserEntity user);  // Save user to DB
    Optional<UserEntity> findUserById(String userId);// Get user by ID
    Optional<UserEntity> findByEmail(String email);
}
