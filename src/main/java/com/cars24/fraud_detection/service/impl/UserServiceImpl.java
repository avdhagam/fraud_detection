package com.cars24.fraud_detection.service.impl;

import com.cars24.fraud_detection.data.dao.UserDao;
import com.cars24.fraud_detection.data.entity.UserEntity;
import com.cars24.fraud_detection.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

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
}
