package com.dcaguard.security;

import com.dcaguard.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized");
        }
        if (auth.getPrincipal() instanceof AuthenticatedUser au) {
            return au.getUser();
        }
        throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized");
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}