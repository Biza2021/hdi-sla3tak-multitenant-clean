package com.repairshop.app.repair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RepairDetailView(
        Long id,
        String title,
        String description,
        String repairNotes,
        RepairItemStatus status,
        LocalDate expectedDeliveryDate,
        BigDecimal estimatedPrice,
        BigDecimal depositPaid,
        BigDecimal remainingBalance,
        String pickupCode,
        String publicTrackingToken,
        CustomerOptionView customer,
        List<RepairHistoryEntryView> history
) {
}
