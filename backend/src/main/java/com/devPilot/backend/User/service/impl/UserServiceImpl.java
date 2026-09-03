package com.devPilot.backend.User.service.impl;

import com.devPilot.backend.User.entity.User;
import com.devPilot.backend.User.repository.UserRepository;
import com.devPilot.backend.User.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TextEncryptor tokenEncryptor;

    public User requiredById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public String getDecryptedAccessToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (user.getAccessToken() == null || user.getAccessToken().isBlank()) {
            throw new IllegalStateException("GitHub access token is missing");
        }
        return tokenEncryptor.decrypt(user.getAccessToken());
    }

    @Override
    public User upsertFromGitHub(Map<String, Object> attributes , String accessToken , String scopes) {
        return null;
    }
}
