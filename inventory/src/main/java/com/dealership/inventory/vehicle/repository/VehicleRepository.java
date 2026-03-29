package com.dealership.inventory.vehicle.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dealership.inventory.dealer.model.SubscriptionType;
import com.dealership.inventory.vehicle.model.Vehicle;
import com.dealership.inventory.vehicle.model.VehicleStatus;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByIdAndTenantId(UUID id, UUID tenantId);


    @Query("""
            SELECT v FROM Vehicle v
            JOIN v.dealer d
            WHERE v.tenantId = :tenantId
                AND (:model IS NULL OR v.model ILIKE %:model%)
                AND (:status   IS NULL OR v.status = :status)
                AND (:priceMin IS NULL OR v.price  >= :priceMin)
                AND (:priceMax IS NULL OR v.price  <= :priceMax)
            """)

    Page<Vehicle> findByFilters(
            @Param("tenantId") UUID tenantId,
            @Param("model") String model,
            @Param("status") VehicleStatus status,
            @Param("priceMin") BigDecimal priceMin,
            @Param("priceMax") BigDecimal priceMax,
            Pageable pageable
    );

    @Query("""
            SELECT v FROM Vehicle v
            JOIN v.dealer d
            WHERE v.tenantId = :tenantId
                AND d.subscriptionType = :subscriptionType
            """)
    Page<Vehicle> findByDealerSubscription(
            @Param("tenantId") UUID tenantId,
            @Param("subscriptionType") SubscriptionType subscriptionType,
            Pageable pageable
    );

}
