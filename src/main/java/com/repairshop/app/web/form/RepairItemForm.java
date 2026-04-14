package com.repairshop.app.web.form;

import com.repairshop.app.repair.RepairItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RepairItemForm {

    @NotNull(message = "{repair.customer.notNull}")
    private Long customerId;

    @NotBlank(message = "{repair.title.notBlank}")
    @Size(max = 160, message = "{repair.title.size}")
    private String title;

    @Size(max = 4000, message = "{repair.description.size}")
    private String description;

    @Size(max = 4000, message = "{repair.repairNotes.size}")
    private String repairNotes;

    @NotNull(message = "{repair.status.notNull}")
    private RepairItemStatus status = RepairItemStatus.RECEIVED;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedDeliveryDate;

    @NotNull(message = "{repair.estimatedPrice.notNull}")
    @DecimalMin(value = "0.00", message = "{repair.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{repair.money.digits}")
    private BigDecimal estimatedPrice = BigDecimal.ZERO;

    @NotNull(message = "{repair.depositPaid.notNull}")
    @DecimalMin(value = "0.00", message = "{repair.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{repair.money.digits}")
    private BigDecimal depositPaid = BigDecimal.ZERO;

    private BigDecimal remainingBalance = BigDecimal.ZERO;

    private MultipartFile imageFile;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public String getRepairNotes() {
        return repairNotes;
    }

    public void setRepairNotes(String repairNotes) {
        this.repairNotes = repairNotes;
    }

    public RepairItemStatus getStatus() {
        return status;
    }

    public void setStatus(RepairItemStatus status) {
        this.status = status;
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

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }
}
