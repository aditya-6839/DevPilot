package com.devPilot.backend.User.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_github_username", columnList = "github_username")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "github_id", unique = true, nullable = false, updatable = false)
    private Long githubId;

    @Column(name = "email", length = 320, updatable = false, unique = true) // drop nullable=false unless confirmed
    private String email;

    @Column(name = "github_username", nullable = false, length = 100)
    private String githubUsername;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "token_scopes", length = 500)
    private String tokenScopes;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role = Role.USER;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (role == null) {
            role = Role.USER;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum Role {
        USER, ADMIN
    }
}