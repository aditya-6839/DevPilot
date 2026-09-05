package com.devPilot.backend.User.service.impl;

import com.devPilot.backend.User.dto.GitHubUserData;
import com.devPilot.backend.User.entity.User;
import com.devPilot.backend.User.repository.UserRepository;
import com.devPilot.backend.User.service.UserService;
import com.devPilot.backend.exception.InvalidOAuthUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
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
    @Transactional
    public User upsertFromGitHub(
            Map<String, Object> attributes,
            String accessToken,
            String scopes
    ) {
        GitHubUserData githubUser = mapGitHubUser(attributes);

        User user = userRepository
                .findByGithubId(githubUser.githubId())
                .orElseGet(User::new);

        user.setGithubId(githubUser.githubId());
        user.setGithubUsername(githubUser.login());
        user.setDisplayName(githubUser.name());
        user.setAvatarUrl(githubUser.avatarUrl());

        if (accessToken != null && !accessToken.isBlank()) {
            user.setAccessToken(tokenEncryptor.encrypt(accessToken));
        }

        user.setTokenScopes(scopes);

        return userRepository.save(user);
    }

    private GitHubUserData mapGitHubUser(Map<String, Object> attributes) {

        Long githubId = toLong(attributes.get("id"));

        if (githubId == null) {
            throw new InvalidOAuthUserException(
                    "GitHub user ID is missing"
            );
        }

        String login = Objects.toString(
                attributes.get("login"),
                null
        );

        if (login == null || login.isBlank()) {
            throw new InvalidOAuthUserException(
                    "GitHub username is missing"
            );
        }

        String name = Objects.toString(
                attributes.get("name"),
                login
        );

        String avatarUrl = Objects.toString(
                attributes.get("avatar_url"),
                null
        );

        return new GitHubUserData(
                githubId,
                login,
                name,
                avatarUrl
        );
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String string) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        return null;
    }
}
