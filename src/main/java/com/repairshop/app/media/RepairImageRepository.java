package com.repairshop.app.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepairImageRepository extends JpaRepository<RepairImage, Long> {

    List<RepairImage> findAllByRepairItemIdAndShopIdOrderByCreatedAtDesc(Long repairItemId, Long shopId);

    List<RepairImage> findAllByRepairItemIdInAndShopId(List<Long> repairItemIds, Long shopId);

    Optional<RepairImage> findByRepairItemIdAndShopId(Long repairItemId, Long shopId);
}
