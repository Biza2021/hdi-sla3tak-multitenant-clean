package com.repairshop.app.repair;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepairItemRepository extends JpaRepository<RepairItem, Long> {

    boolean existsByPublicTrackingToken(String publicTrackingToken);

    boolean existsByShopIdAndPickupCode(Long shopId, String pickupCode);

    boolean existsByShopIdAndPickupCodeAndIdNot(Long shopId, String pickupCode, Long id);

    long countByShopIdAndStatusIn(Long shopId, List<RepairItemStatus> statuses);

    long countByShopIdAndStatus(Long shopId, RepairItemStatus status);

    @Query("""
            select r
            from RepairItem r
            join fetch r.customer c
            where r.shop.id = :shopId
            order by r.updatedAt desc
            """)
    List<RepairItem> findAllByShopIdWithCustomerOrderByUpdatedAtDesc(@Param("shopId") Long shopId);

    @Query("""
            select r
            from RepairItem r
            join fetch r.customer c
            where r.shop.id = :shopId
            order by r.updatedAt desc
            """)
    List<RepairItem> findRecentByShopIdWithCustomer(@Param("shopId") Long shopId, Pageable pageable);

    @Query("""
            select r
            from RepairItem r
            join fetch r.customer c
            where r.shop.id = :shopId and r.id = :id
            """)
    Optional<RepairItem> findDetailedByIdAndShopId(@Param("id") Long id, @Param("shopId") Long shopId);

    @Query("""
            select r
            from RepairItem r
            join fetch r.customer c
            where r.shop.id = :shopId and r.customer.id = :customerId
            order by r.updatedAt desc
            """)
    List<RepairItem> findAllByCustomerIdAndShopIdWithCustomer(
            @Param("customerId") Long customerId,
            @Param("shopId") Long shopId
    );

    Optional<RepairItem> findByIdAndShopId(Long id, Long shopId);

    Optional<RepairItem> findByPublicTrackingToken(String publicTrackingToken);
}

