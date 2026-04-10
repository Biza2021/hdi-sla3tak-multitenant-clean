package com.repairshop.app.repair;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepairListItemView(
        Long id,
        String title,
        String customerName,
        RepairItemStatus status,
        LocalDate expectedDeliveryDate,
        String pickupCode,
        BigDecimal remainingBalance
) {
}
