package com.repairshop.app.repair;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepairItemRepository extends JpaRepository<RepairItem, Long> {

    List<RepairItem> findTop10ByShopIdOrderByCreatedAtDesc(Long shopId);

    List<RepairItem> findAllByShopIdOrderByCreatedAtDesc(Long shopId);

    Optional<RepairItem> findByIdAndShopId(Long id, Long shopId);

    Optional<RepairItem> findByPublicTrackingToken(String publicTrackingToken);
}

