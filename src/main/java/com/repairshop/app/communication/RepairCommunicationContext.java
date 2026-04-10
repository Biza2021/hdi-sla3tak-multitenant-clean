package com.repairshop.app.communication;

public record RepairCommunicationContext(
        String shopBusinessName,
        String customerName,
        String primaryPhone,
        String secondaryPhone,
        String repairTitle,
        String pickupCode,
        String publicTrackingToken
) {
}
