package com.repairshop.app.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthenticatedShopUser require(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedShopUser principal)) {
            throw new ResponseStatusException(UNAUTHORIZED);
        }
        return principal;
    }
}

