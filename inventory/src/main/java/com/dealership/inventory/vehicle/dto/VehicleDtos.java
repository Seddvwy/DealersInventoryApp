package com.dealership.inventory.vehicle.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.dealership.inventory.dealer.model.SubscriptionType;
import com.dealership.inventory.vehicle.model.VehicleStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class VehicleDtos {
    private VehicleDtos() {}

    public record CreateVehicleRequest(
        @NotNull(message = "Dealer ID is required")
        UUID dealerId,


        @NotBlank(message = "Model is required")
        String model,
        

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        BigDecimal price,
        

        @NotNull(message = "Status is required")
        VehicleStatus status
    ) {}

    public record UpdateVehicleRequest(
        String model, 
        
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        BigDecimal price,
        
        VehicleStatus status
    ) {}

    public record VehicleResponse(
        UUID id,
        UUID tenantId,
        DealerSummary dealer,
        String model,
        BigDecimal price,
        VehicleStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {}
    

    public record DealerSummary(
        UUID id,
        String name,
        SubscriptionType subscriptionType
    ) {}
}
