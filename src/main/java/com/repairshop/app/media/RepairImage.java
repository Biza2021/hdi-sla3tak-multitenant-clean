package com.repairshop.app.media;

import com.repairshop.app.common.BaseEntity;
import com.repairshop.app.repair.RepairItem;
import com.repairshop.app.shop.Shop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "repair_images")
public class RepairImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_item_id", nullable = false)
    private RepairItem repairItem;

    @Column(nullable = false, length = 255)
    private String storageKey;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private boolean visibleOnPublicTracking = false;

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public RepairItem getRepairItem() {
        return repairItem;
    }

    public void setRepairItem(RepairItem repairItem) {
        this.repairItem = repairItem;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isVisibleOnPublicTracking() {
        return visibleOnPublicTracking;
    }

    public void setVisibleOnPublicTracking(boolean visibleOnPublicTracking) {
        this.visibleOnPublicTracking = visibleOnPublicTracking;
    }
}

