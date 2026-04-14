package com.repairshop.app.repair;

import com.repairshop.app.media.RepairImageSummaryView;

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
        RepairImageSummaryView image,
        CustomerOptionView customer,
        List<RepairHistoryEntryView> history
) {

    public boolean hasOutstandingBalance() {
        return remainingBalance != null && remainingBalance.signum() > 0;
    }
}
