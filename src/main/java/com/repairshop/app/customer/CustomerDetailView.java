package com.repairshop.app.customer;

import java.util.List;

public record CustomerDetailView(
        Long id,
        String fullName,
        String primaryPhone,
        String secondaryPhone,
        String notes,
        List<CustomerRepairSummaryView> repairs
) {
}
