package com.devPilot.backend.auth.controller;

import com.devPilot.backend.User.entity.User;
import com.devPilot.backend.auth.dto.UserResponse;
import com.devPilot.backend.common.security.AppUserPrincipal;
import com.devPilot.backend.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final CurrentUser currentUser;

    @GetMapping("/login-url")
    public Map<String, String> login() {
        return Map.of("url" , "/oauth2/authorization/github");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        AppUserPrincipal principal = currentUser.require();
        User user = principal.getUser();
        return ResponseEntity.ok(new UserResponse(
                user.getId() ,
                user.getGithubId() ,
                user.getGithubUsername() ,
                user.getDisplayName() ,
                user.getAvatarUrl()));
    }
}
