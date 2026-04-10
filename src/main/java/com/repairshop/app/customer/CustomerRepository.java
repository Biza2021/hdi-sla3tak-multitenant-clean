package com.repairshop.app.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByShopIdOrderByFullNameAsc(Long shopId);

    Optional<Customer> findByIdAndShopId(Long id, Long shopId);
}

