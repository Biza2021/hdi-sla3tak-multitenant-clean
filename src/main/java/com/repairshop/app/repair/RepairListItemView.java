package com.repairshop.app.repair;

import com.repairshop.app.media.RepairImageSummaryView;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepairListItemView(
        Long id,
        String title,
        String customerName,
        RepairItemStatus status,
        LocalDate expectedDeliveryDate,
        String pickupCode,
        BigDecimal remainingBalance,
        RepairImageSummaryView image
) {
}
