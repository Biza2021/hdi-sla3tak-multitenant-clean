package com.repairshop.app.dashboard;

import com.repairshop.app.repair.RepairItemStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DashboardRepairSummaryView(
        Long id,
        String title,
        String customerName,
        RepairItemStatus status,
        LocalDate expectedDeliveryDate,
        BigDecimal remainingBalance,
        OffsetDateTime updatedAt
) {
}
