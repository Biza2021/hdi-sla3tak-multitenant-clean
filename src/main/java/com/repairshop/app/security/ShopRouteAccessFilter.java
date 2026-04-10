package com.repairshop.app.security;

import com.repairshop.app.common.ShopRouting;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ShopRouteAccessFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedShopUser principal) {
            var routeShopSlug = ShopRouting.extractShopSlug(request.getRequestURI());
            if (routeShopSlug.isPresent() && !principal.shopSlug().equals(routeShopSlug.get())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

