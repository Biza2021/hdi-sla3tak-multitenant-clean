package com.repairshop.app.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ShopRegistrationForm {

    @NotBlank(message = "{shop.businessName.notBlank}")
    @Size(max = 150, message = "{shop.businessName.size}")
    private String businessName;

    @NotBlank(message = "{shop.slug.notBlank}")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "{shop.slug.pattern}")
    @Size(max = 80, message = "{shop.slug.size}")
    private String slug;

    @NotBlank(message = "{shop.ownerFullName.notBlank}")
    @Size(max = 120, message = "{shop.ownerFullName.size}")
    private String ownerFullName;

    @NotBlank(message = "{shop.ownerUsername.notBlank}")
    @Pattern(regexp = "^[a-z0-9](?:[a-z0-9._-]{2,29})$", message = "{shop.ownerUsername.pattern}")
    @Size(max = 60, message = "{shop.ownerUsername.size}")
    private String ownerUsername;

    @NotBlank(message = "{shop.ownerPassword.notBlank}")
    @Size(min = 8, max = 72, message = "{shop.ownerPassword.size}")
    private String ownerPassword;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getOwnerFullName() {
        return ownerFullName;
    }

    public void setOwnerFullName(String ownerFullName) {
        this.ownerFullName = ownerFullName;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }
}

