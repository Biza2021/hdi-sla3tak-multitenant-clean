package com.repairshop.app.repair;

import com.repairshop.app.common.BaseEntity;
import com.repairshop.app.shop.Shop;
import com.repairshop.app.user.ShopUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "repair_status_history")
public class RepairStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_item_id", nullable = false)
    private RepairItem repairItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private ShopUser changedByUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RepairItemStatus status;

    @Column(columnDefinition = "text")
    private String notes;

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

    public ShopUser getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(ShopUser changedByUser) {
        this.changedByUser = changedByUser;
    }

    public RepairItemStatus getStatus() {
        return status;
    }

    public void setStatus(RepairItemStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

