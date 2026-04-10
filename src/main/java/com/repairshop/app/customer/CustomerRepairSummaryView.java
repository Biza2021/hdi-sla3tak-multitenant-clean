package com.repairshop.app.customer;

import com.repairshop.app.repair.RepairItemStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerRepairSummaryView(
        Long id,
        String title,
        RepairItemStatus status,
        LocalDate expectedDeliveryDate,
        BigDecimal remainingBalance
) {
}
