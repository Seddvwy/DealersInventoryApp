package com.dealership.inventory.dealer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dealership.inventory.dealer.model.Dealer;
import com.dealership.inventory.dealer.model.SubscriptionType;

public interface DealerRepository extends JpaRepository<Dealer, UUID> {

    Optional<Dealer> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Dealer> findAllByTenantId(UUID tenantId, Pageable pageable);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT d.subscriptionType AS subscriptionType, COUNT(d) AS count FROM Dealer d GROUP BY d.subscriptionType")
    java.util.List<SubscriptionCountProjection> countBySubscriptionTypeGlobal();


     interface SubscriptionCountProjection {
        SubscriptionType getSubscriptionType();
        Long getCount();
    }

}
