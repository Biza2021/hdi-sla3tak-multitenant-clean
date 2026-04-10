package com.repairshop.app.repair;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairStatusHistoryRepository extends JpaRepository<RepairStatusHistory, Long> {

    List<RepairStatusHistory> findAllByRepairItemIdAndShopIdOrderByCreatedAtDesc(Long repairItemId, Long shopId);
}

