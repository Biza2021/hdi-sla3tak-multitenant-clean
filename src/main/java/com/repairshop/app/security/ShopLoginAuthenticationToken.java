package com.repairshop.app.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class ShopLoginAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;
    private final String shopSlug;
    private final String username;

    private ShopLoginAuthenticationToken(
            Object principal,
            Object credentials,
            String shopSlug,
            String username,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.shopSlug = shopSlug;
        this.username = username;
    }

    public static ShopLoginAuthenticationToken unauthenticated(String shopSlug, String username, String password) {
        ShopLoginAuthenticationToken token =
                new ShopLoginAuthenticationToken(username, password, shopSlug, username, List.of());
        token.setAuthenticated(false);
        return token;
    }

    public static ShopLoginAuthenticationToken authenticated(AuthenticatedShopUser user) {
        ShopLoginAuthenticationToken token =
                new ShopLoginAuthenticationToken(
                        user,
                        null,
                        user.shopSlug(),
                        user.username(),
                        user.authorities()
                );
        token.setAuthenticated(true);
        return token;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getShopSlug() {
        return shopSlug;
    }

    public String getUsernameValue() {
        return username;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
