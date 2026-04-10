package com.repairshop.app.repair;

import java.time.LocalDate;

public record PublicTrackingView(
        String shopBusinessName,
        String title,
        RepairItemStatus status,
        LocalDate expectedDeliveryDate,
        String pickupCode,
        String publicTrackingToken,
        boolean hasImage
) {
}
