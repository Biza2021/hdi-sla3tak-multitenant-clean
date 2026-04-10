package com.repairshop.app.shop;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    boolean existsBySlug(String slug);

    Optional<Shop> findBySlug(String slug);

    Optional<Shop> findFirstByOrderByIdAsc();
}

