package com.repairshop.app.dashboard;

import com.repairshop.app.customer.CustomerRepository;
import com.repairshop.app.repair.RepairItemRepository;
import com.repairshop.app.repair.RepairItemStatus;
import com.repairshop.app.repair.RepairStatusHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class DashboardService {

    private static final List<RepairItemStatus> ACTIVE_STATUSES = List.of(
            RepairItemStatus.RECEIVED,
            RepairItemStatus.DIAGNOSING,
            RepairItemStatus.WAITING_FOR_PARTS,
            RepairItemStatus.IN_REPAIR
    );

    private final CustomerRepository customerRepository;
    private final RepairItemRepository repairItemRepository;
    private final RepairStatusHistoryRepository repairStatusHistoryRepository;

    public DashboardService(
            CustomerRepository customerRepository,
            RepairItemRepository repairItemRepository,
            RepairStatusHistoryRepository repairStatusHistoryRepository
    ) {
        this.customerRepository = customerRepository;
        this.repairItemRepository = repairItemRepository;
        this.repairStatusHistoryRepository = repairStatusHistoryRepository;
    }

    @Transactional(readOnly = true)
    public DashboardView getDashboard(Long shopId) {
        long totalCustomers = customerRepository.countByShopId(shopId);
        long activeRepairs = repairItemRepository.countByShopIdAndStatusIn(shopId, ACTIVE_STATUSES);
        long readyForPickup = repairItemRepository.countByShopIdAndStatus(shopId, RepairItemStatus.READY_FOR_PICKUP);
        long deliveredRecently = repairStatusHistoryRepository.countDistinctRepairItemsByStatusSince(
                shopId,
                RepairItemStatus.DELIVERED,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(7)
        );

        List<DashboardRepairSummaryView> recentRepairs = repairItemRepository.findRecentByShopIdWithCustomer(
                        shopId,
                        PageRequest.of(0, 6)
                ).stream()
                .map(item -> new DashboardRepairSummaryView(
                        item.getId(),
                        item.getTitle(),
                        item.getCustomer().getFullName(),
                        item.getStatus(),
                        item.getExpectedDeliveryDate(),
                        item.getRemainingBalance(),
                        item.getUpdatedAt()
                ))
                .toList();

        return new DashboardView(totalCustomers, activeRepairs, readyForPickup, deliveredRecently, recentRepairs);
    }
}
