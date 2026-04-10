package com.repairshop.app.dashboard;

import java.util.List;

public record DashboardView(
        long totalCustomers,
        long activeRepairs,
        long readyForPickup,
        long deliveredRecently,
        List<DashboardRepairSummaryView> recentRepairs
) {
}
