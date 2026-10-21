package com.bookmyticket.security;

import com.bookmyticket.common.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<AppUserDetails> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static AppUserDetails requireUser() {
        return currentUser().orElseThrow(() -> ApiException.unauthorized("Please sign in to continue."));
    }

    public static Long requireUserId() {
        return requireUser().getId();
    }
}
