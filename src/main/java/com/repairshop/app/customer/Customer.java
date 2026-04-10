package com.repairshop.app.customer;

import com.repairshop.app.common.BaseEntity;
import com.repairshop.app.shop.Shop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customers_shop_primary_phone", columnNames = {"shop_id", "primary_phone"})
        }
)
public class Customer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(length = 40)
    private String primaryPhone;

    @Column(length = 40)
    private String secondaryPhone;

    @Column(columnDefinition = "text")
    private String notes;

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

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

