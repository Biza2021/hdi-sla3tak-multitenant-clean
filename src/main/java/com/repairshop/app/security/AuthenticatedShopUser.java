package com.repairshop.app.security;

import com.repairshop.app.user.ShopRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public record AuthenticatedShopUser(
        Long userId,
        Long shopId,
        String shopSlug,
        String shopName,
        String fullName,
        String username,
        ShopRole role
) {

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}

