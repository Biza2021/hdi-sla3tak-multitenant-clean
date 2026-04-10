package com.repairshop.app.shop;

public record ShopRegistrationRequest(
        String businessName,
        String slug,
        String ownerFullName,
        String ownerUsername,
        String ownerPassword
) {
}

