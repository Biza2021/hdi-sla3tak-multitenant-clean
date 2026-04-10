package com.repairshop.app.web;

import com.repairshop.app.security.AuthenticatedShopUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUser")
    public AuthenticatedShopUser currentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedShopUser principal) {
            return principal;
        }
        return null;
    }
}

