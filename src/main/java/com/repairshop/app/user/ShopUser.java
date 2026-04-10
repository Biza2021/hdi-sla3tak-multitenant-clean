package com.repairshop.app.user;

import com.repairshop.app.common.BaseEntity;
import com.repairshop.app.shop.Shop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "shop_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shop_users_shop_username", columnNames = {"shop_id", "username"})
        }
)
public class ShopUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 60)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShopRole role;

    @Column(nullable = false)
    private boolean enabled = true;

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public ShopRole getRole() {
        return role;
    }

    public void setRole(ShopRole role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

