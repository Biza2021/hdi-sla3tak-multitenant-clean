package com.repairshop.app.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopUserRepository extends JpaRepository<ShopUser, Long> {

    @Query("""
            select u
            from ShopUser u
            join fetch u.shop s
            where s.slug = :shopSlug and u.username = :username
            """)
    Optional<ShopUser> findForAuthentication(@Param("shopSlug") String shopSlug, @Param("username") String username);

    List<ShopUser> findAllByShopIdOrderByFullNameAsc(Long shopId);

    Optional<ShopUser> findByIdAndShopId(Long id, Long shopId);
}
