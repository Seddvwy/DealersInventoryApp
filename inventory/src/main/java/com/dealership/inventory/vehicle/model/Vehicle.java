package com.dealership.inventory.vehicle.model;

import com.dealership.inventory.dealer.model.Dealer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
 
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "vehicles",
    indexes = {
        @Index(name = "idx_vehicle_tenant", columnList = "tenant_id"),
        @Index(name = "idx_vehicle_dealer", columnList = "dealer_id"),
        @Index(name = "idx_vehicle_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_vehicle_tenant_model", columnList = "tenant_id, model")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;


    @Column(name = "tenant_id", updatable = false, nullable = false)
    private UUID tenantId;


    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "dealer_id", nullable = false, updatable = false)
    private Dealer dealer;


    @Column(name = "model", nullable = false)
    private String model;
    

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VehicleStatus status;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
 

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
 
}
