package com.repairshop.app.web;

import com.repairshop.app.security.AuthenticatedShopUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUser")
    public AuthenticatedShopUser currentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedShopUser principal) {
            return principal;
        }
        return null;
    }

    @ModelAttribute("currentLanguage")
    public String currentLanguage(Locale locale) {
        return locale.getLanguage();
    }

    @ModelAttribute("textDirection")
    public String textDirection(Locale locale) {
        return "ar".equals(locale.getLanguage()) ? "rtl" : "ltr";
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
