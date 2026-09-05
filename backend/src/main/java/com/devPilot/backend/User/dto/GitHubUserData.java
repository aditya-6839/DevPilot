package com.devPilot.backend.User.dto;

public record GitHubUserData(
        Long githubId,
        String login,
        String name,
        String avatarUrl
) {}
