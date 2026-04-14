package com.repairshop.app.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    long countByShopId(Long shopId);

    boolean existsByShopIdAndPrimaryPhone(Long shopId, String primaryPhone);

    boolean existsByShopIdAndPrimaryPhoneAndIdNot(Long shopId, String primaryPhone, Long id);

    List<Customer> findAllByShopIdOrderByFullNameAsc(Long shopId);

    Optional<Customer> findByIdAndShopId(Long id, Long shopId);

    @Query("""
            select c
            from Customer c
            where c.shop.id = :shopId
              and (
                    lower(c.fullName) like concat('%', :lowerQuery, '%')
                    or (:phoneSearch = true and replace(replace(replace(replace(replace(replace(coalesce(c.primaryPhone, ''), ' ', ''), '-', ''), '+', ''), '(', ''), ')', ''), '.', '') like concat('%', :digitsQuery, '%'))
                    or (:phoneSearch = true and replace(replace(replace(replace(replace(replace(coalesce(c.secondaryPhone, ''), ' ', ''), '-', ''), '+', ''), '(', ''), ')', ''), '.', '') like concat('%', :digitsQuery, '%'))
                  )
            order by
                case when lower(c.fullName) = :lowerQuery then 0 else 1 end,
                case
                    when :phoneSearch = true and replace(replace(replace(replace(replace(replace(coalesce(c.primaryPhone, ''), ' ', ''), '-', ''), '+', ''), '(', ''), ')', ''), '.', '') = :digitsQuery then 0
                    when :phoneSearch = true and replace(replace(replace(replace(replace(replace(coalesce(c.secondaryPhone, ''), ' ', ''), '-', ''), '+', ''), '(', ''), ')', ''), '.', '') = :digitsQuery then 1
                    else 2
                end,
                case when lower(c.fullName) like concat(:lowerQuery, '%') then 0 else 1 end,
                case
                    when :phoneSearch = true and replace(replace(replace(replace(replace(replace(coalesce(c.primaryPhone, ''), ' ', ''), '-', ''), '+', ''), '(', ''), ')', ''), '.', '') like concat(:digitsQuery, '%') then 0
                    when :phoneSearch = true and replace(replace(replace(replace(replace(replace(coalesce(c.secondaryPhone, ''), ' ', ''), '-', ''), '+', ''), '(', ''), ')', ''), '.', '') like concat(:digitsQuery, '%') then 1
                    else 2
                end,
                c.fullName asc
            """)
    List<Customer> searchRepairPickerMatches(
            @Param("shopId") Long shopId,
            @Param("lowerQuery") String lowerQuery,
            @Param("digitsQuery") String digitsQuery,
            @Param("phoneSearch") boolean phoneSearch,
            Pageable pageable
    );
}

