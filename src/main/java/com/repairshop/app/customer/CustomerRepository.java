package com.repairshop.app.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    long countByShopId(Long shopId);

    boolean existsByShopIdAndPrimaryPhone(Long shopId, String primaryPhone);

    boolean existsByShopIdAndPrimaryPhoneAndIdNot(Long shopId, String primaryPhone, Long id);

    List<Customer> findAllByShopIdOrderByFullNameAsc(Long shopId);

    Optional<Customer> findByIdAndShopId(Long id, Long shopId);
}

