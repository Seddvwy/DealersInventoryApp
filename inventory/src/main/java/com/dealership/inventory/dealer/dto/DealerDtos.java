package com.dealership.inventory.dealer.dto;

import java.time.Instant;
import java.util.UUID;

import com.dealership.inventory.dealer.model.SubscriptionType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DealerDtos {

    private DealerDtos() {}


    public record CreateDealerRequest(
        @NotBlank(message = "Name is required")
        String name,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotNull(message = "Subscription type is required")
        SubscriptionType subscriptionType
    ) {}

    public record UpdateDealerRequest(
        String name,
            
        @Email(message = "Email should be valid")
        String email,
    
        SubscriptionType subscriptionType
    ) {}



    public record DealerResponse(
        UUID id,
        UUID tenantId,
        String name,
        String email,
        SubscriptionType subscriptionType,
        Instant createdAt,
        Instant updatedAt
    ) {}

}
