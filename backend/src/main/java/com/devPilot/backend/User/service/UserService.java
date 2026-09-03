package com.devPilot.backend.User.service;

import com.devPilot.backend.User.entity.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public interface UserService {
    User requiredById(UUID id);

    String getDecryptedAccessToken(User user);

    User upsertFromGitHub(Map<String, Object> attributes, String accessToken, String scopes);
}
