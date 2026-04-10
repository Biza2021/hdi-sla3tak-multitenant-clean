package com.repairshop.app.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairImageRepository extends JpaRepository<RepairImage, Long> {

    List<RepairImage> findAllByRepairItemIdAndShopIdOrderByCreatedAtDesc(Long repairItemId, Long shopId);
}
