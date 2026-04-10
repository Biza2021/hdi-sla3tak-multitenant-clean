package com.repairshop.app.security;

import com.repairshop.app.common.ShopRouting;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ShopAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        var shopSlug = ShopRouting.extractShopSlug(request.getRequestURI());
        if (shopSlug.isPresent()) {
            response.sendRedirect("/" + shopSlug.get() + "/login");
            return;
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}

