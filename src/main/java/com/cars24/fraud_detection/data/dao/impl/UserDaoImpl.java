package com.cars24.fraud_detection.data.dao.impl;

import com.cars24.fraud_detection.data.dao.UserDao;
import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.DocumentEntity;
import com.cars24.fraud_detection.data.entity.UserEntity;
import com.cars24.fraud_detection.repository.AudioRepository;
import com.cars24.fraud_detection.repository.DocumentRepository;
import com.cars24.fraud_detection.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;
    private final AudioRepository audioRepository;
    private final DocumentRepository documentRepository;

    @Override
    public UserEntity saveUser(UserEntity user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null!");
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<UserEntity> findUserById(String userId) {
        return userRepository.findById(userId).map(user -> {
            // Fetch audio entities related to the user's report
            List<AudioEntity> audioEntities = audioRepository.findByUserReportId(user.getId());
            user.setAudioCalls(audioEntities);

            // Fetch document entities related to the user's report
            List<DocumentEntity> documentEntities = documentRepository.findByUserReportId(user.getId());
            user.setDocuments(documentEntities);

            return user;

        });
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}