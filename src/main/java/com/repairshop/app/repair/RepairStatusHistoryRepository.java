package com.repairshop.app.repair;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.OffsetDateTime;

public interface RepairStatusHistoryRepository extends JpaRepository<RepairStatusHistory, Long> {

    @Query("""
            select h
            from RepairStatusHistory h
            left join fetch h.changedByUser u
            where h.repairItem.id = :repairItemId and h.shop.id = :shopId
            order by h.createdAt desc
            """)
    List<RepairStatusHistory> findTimelineByRepairItemIdAndShopId(
            @Param("repairItemId") Long repairItemId,
            @Param("shopId") Long shopId
    );

    @Query("""
            select count(distinct h.repairItem.id)
            from RepairStatusHistory h
            where h.shop.id = :shopId
              and h.status = :status
              and h.createdAt >= :since
            """)
    long countDistinctRepairItemsByStatusSince(
            @Param("shopId") Long shopId,
            @Param("status") RepairItemStatus status,
            @Param("since") OffsetDateTime since
    );
}

