package com.devPilot.backend.common.security;

import com.devPilot.backend.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser {

    /**
     * Returns the currently authenticated application user.
     *
     * @throws UnauthorizedException if the user is not authenticated
     */
    public AppUserPrincipal require() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
            || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {

            throw new UnauthorizedException("Not authenticated");
        }

        return principal;
    }

    /**
     * Returns the ID of the currently authenticated user.
     *
     * @throws UnauthorizedException if the user is not authenticated
     */
    public UUID id() {
        return require().getId();
    }
}