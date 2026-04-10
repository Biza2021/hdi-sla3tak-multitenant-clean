package com.repairshop.app.repair;

import java.util.List;

public enum RepairItemStatus {
    RECEIVED,
    DIAGNOSING,
    WAITING_FOR_PARTS,
    IN_REPAIR,
    READY_FOR_PICKUP,
    DELIVERED,
    CANCELLED;

    public static List<RepairItemStatus> activeDashboardStatuses() {
        return List.of(RECEIVED, DIAGNOSING, WAITING_FOR_PARTS, IN_REPAIR);
    }

    public boolean canBeMarkedDelivered() {
        return this == READY_FOR_PICKUP;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }
}
