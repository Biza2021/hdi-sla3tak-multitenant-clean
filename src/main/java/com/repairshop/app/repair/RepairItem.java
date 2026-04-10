package com.repairshop.app.repair;

import com.repairshop.app.common.BaseEntity;
import com.repairshop.app.customer.Customer;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "repair_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_repair_items_shop_pickup_code", columnNames = {"shop_id", "pickup_code"})
        }
)
public class RepairItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RepairItemStatus status = RepairItemStatus.RECEIVED;

    @Column(columnDefinition = "text")
    private String repairNotes;

    private LocalDate expectedDeliveryDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal depositPaid = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal remainingBalance = BigDecimal.ZERO;

    @Column(nullable = false, unique = true, length = 80)
    private String publicTrackingToken;

    @Column(nullable = false, length = 40)
    private String pickupCode;

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RepairItemStatus getStatus() {
        return status;
    }

    public void setStatus(RepairItemStatus status) {
        this.status = status;
    }

    public String getRepairNotes() {
        return repairNotes;
    }

    public void setRepairNotes(String repairNotes) {
        this.repairNotes = repairNotes;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public BigDecimal getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(BigDecimal estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public BigDecimal getDepositPaid() {
        return depositPaid;
    }

    public void setDepositPaid(BigDecimal depositPaid) {
        this.depositPaid = depositPaid;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public String getPublicTrackingToken() {
        return publicTrackingToken;
    }

    public void setPublicTrackingToken(String publicTrackingToken) {
        this.publicTrackingToken = publicTrackingToken;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }
}

