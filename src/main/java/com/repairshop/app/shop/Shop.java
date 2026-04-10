package com.repairshop.app.shop;

import com.repairshop.app.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "shops")
public class Shop extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String businessName;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

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
}

