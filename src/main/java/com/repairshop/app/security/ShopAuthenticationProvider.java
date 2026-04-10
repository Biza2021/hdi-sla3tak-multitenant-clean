package com.repairshop.app.security;

import com.repairshop.app.common.NormalizationUtils;
import com.repairshop.app.user.ShopUserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ShopAuthenticationProvider implements AuthenticationProvider {

    private final ShopUserRepository shopUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ShopAuthenticationProvider(ShopUserRepository shopUserRepository, PasswordEncoder passwordEncoder) {
        this.shopUserRepository = shopUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ShopLoginAuthenticationToken token = (ShopLoginAuthenticationToken) authentication;
        String normalizedShopSlug = NormalizationUtils.normalizeSlug(token.getShopSlug());
        String normalizedUsername = NormalizationUtils.normalizeUsername(token.getUsernameValue());
        String rawPassword = String.valueOf(token.getCredentials());

        var user = shopUserRepository.findForAuthentication(normalizedShopSlug, normalizedUsername)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials."));

        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled.");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials.");
        }

        AuthenticatedShopUser principal = new AuthenticatedShopUser(
                user.getId(),
                user.getShop().getId(),
                user.getShop().getSlug(),
                user.getShop().getBusinessName(),
                user.getFullName(),
                user.getUsername(),
                user.getRole()
        );

        return ShopLoginAuthenticationToken.authenticated(principal);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ShopLoginAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

