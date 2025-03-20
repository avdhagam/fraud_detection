package com.cars24.fraud_detection.data.dao;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    UserEntity saveUser(UserEntity user);  // Save user to DB
    List<DocumentEntity> findUserById(String userId);
    List<AudioEntity> findAudioByUserId(String userId);// Get user by ID
    Optional<UserEntity> findByEmail(String email);
}
