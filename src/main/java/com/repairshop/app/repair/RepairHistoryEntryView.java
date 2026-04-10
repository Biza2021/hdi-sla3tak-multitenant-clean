package com.repairshop.app.repair;

import java.time.OffsetDateTime;

public record RepairHistoryEntryView(
        OffsetDateTime createdAt,
        RepairItemStatus status,
        String changedByName,
        String notes
) {
}
