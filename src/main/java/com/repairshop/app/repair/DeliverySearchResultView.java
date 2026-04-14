package com.repairshop.app.repair;

import com.repairshop.app.media.RepairImageSummaryView;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeliverySearchResultView(
        Long id,
        String title,
        String customerName,
        String customerPrimaryPhone,
        String customerSecondaryPhone,
        RepairItemStatus status,
        LocalDate expectedDeliveryDate,
        String pickupCode,
        String publicTrackingToken,
        BigDecimal estimatedPrice,
        BigDecimal depositPaid,
        BigDecimal remainingBalance,
        RepairImageSummaryView image
) {

    public boolean canMarkDelivered() {
        return status.canBeMarkedDelivered();
    }

    public boolean isDelivered() {
        return status.isDelivered();
    }

    public boolean hasOutstandingBalance() {
        return remainingBalance != null && remainingBalance.signum() > 0;
    }

    public boolean canDeliverNow() {
        return canMarkDelivered() && !hasOutstandingBalance();
    }

    public String displayPhone() {
        if (hasText(customerPrimaryPhone)) {
            return customerPrimaryPhone;
        }
        if (hasText(customerSecondaryPhone)) {
            return customerSecondaryPhone;
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
