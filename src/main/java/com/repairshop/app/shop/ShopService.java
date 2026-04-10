package com.repairshop.app.shop;

import com.repairshop.app.common.NormalizationUtils;
import com.repairshop.app.common.ShopRouting;
import com.repairshop.app.user.ShopRole;
import com.repairshop.app.user.ShopUser;
import com.repairshop.app.user.ShopUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopUserRepository shopUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ShopService(
            ShopRepository shopRepository,
            ShopUserRepository shopUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.shopRepository = shopRepository;
        this.shopUserRepository = shopUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public long countShops() {
        return shopRepository.count();
    }

    public Optional<Shop> findOnlyShop() {
        return shopRepository.count() == 1 ? shopRepository.findFirstByOrderByIdAsc() : Optional.empty();
    }

    public boolean slugExists(String slug) {
        return shopRepository.existsBySlug(NormalizationUtils.normalizeSlug(slug));
    }

    public boolean isReservedSlug(String slug) {
        return ShopRouting.isReservedSlug(NormalizationUtils.normalizeSlug(slug));
    }

    public Shop getBySlugOrThrow(String slug) {
        String normalizedSlug = NormalizationUtils.normalizeSlug(slug);
        return shopRepository.findBySlug(normalizedSlug)
                .orElseThrow(() -> new ShopNotFoundException(normalizedSlug));
    }

    @Transactional
    public Shop registerShop(ShopRegistrationRequest request) {
        String normalizedSlug = NormalizationUtils.normalizeSlug(request.slug());
        String normalizedOwnerUsername = NormalizationUtils.normalizeUsername(request.ownerUsername());

        if (ShopRouting.isReservedSlug(normalizedSlug)) {
            throw new IllegalArgumentException("Reserved slugs cannot be used.");
        }
        if (shopRepository.existsBySlug(normalizedSlug)) {
            throw new IllegalArgumentException("Shop slug is already in use.");
        }

        Shop shop = new Shop();
        shop.setBusinessName(request.businessName().trim());
        shop.setSlug(normalizedSlug);
        Shop savedShop = shopRepository.save(shop);

        ShopUser owner = new ShopUser();
        owner.setShop(savedShop);
        owner.setFullName(request.ownerFullName().trim());
        owner.setUsername(normalizedOwnerUsername);
        owner.setPasswordHash(passwordEncoder.encode(request.ownerPassword()));
        owner.setRole(ShopRole.OWNER);
        owner.setEnabled(true);
        shopUserRepository.save(owner);

        return savedShop;
    }
}

