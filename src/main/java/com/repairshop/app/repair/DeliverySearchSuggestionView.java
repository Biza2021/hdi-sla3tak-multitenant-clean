package com.repairshop.app.repair;

public record DeliverySearchSuggestionView(
        Long repairId,
        String repairTitle,
        String customerName,
        String primaryPhone,
        String secondaryPhone,
        String pickupCode,
        RepairItemStatus status
) {
}
