package com.repairshop.app.repair;

import com.repairshop.app.media.RepairImageSummaryView;

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
        RepairImageSummaryView image
) {

    public boolean canMarkDelivered() {
        return status.canBeMarkedDelivered();
    }

    public boolean isDelivered() {
        return status.isDelivered();
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
