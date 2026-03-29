package com.dealership.inventory.vehicle.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dealership.inventory.dealer.model.Dealer;
import com.dealership.inventory.dealer.model.SubscriptionType;
import com.dealership.inventory.dealer.repository.DealerRepository;
import com.dealership.inventory.shared.web.PageResponse;
import com.dealership.inventory.vehicle.dto.VehicleDtos.CreateVehicleRequest;
import com.dealership.inventory.vehicle.dto.VehicleDtos.UpdateVehicleRequest;
import com.dealership.inventory.vehicle.dto.VehicleDtos.VehicleResponse;
import com.dealership.inventory.vehicle.mapper.VehicleMapper;
import com.dealership.inventory.vehicle.model.Vehicle;
import com.dealership.inventory.vehicle.model.VehicleStatus;
import com.dealership.inventory.vehicle.repository.VehicleRepository;
import com.dealership.inventory.shared.security.TenantContext;
import com.dealership.inventory.shared.exception.ResourceNotFoundException;
import com.dealership.inventory.shared.exception.TenantAccessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;
    private final VehicleMapper vehicleMapper;


    @Transactional
    public VehicleResponse create(CreateVehicleRequest request) {
        UUID tenantId = requireTenant();
 
        // Validate dealer exists and belongs to this tenant
        Dealer dealer = dealerRepository.findByIdAndTenantId(request.dealerId(), tenantId)
                .orElseThrow(() -> {
                    boolean existsElsewhere = dealerRepository.existsById(request.dealerId());
                    if (existsElsewhere) {
                        return new TenantAccessException(
                                "Access denied: dealer " + request.dealerId() + " does not belong to your tenant");
                    }
                    return new ResourceNotFoundException("Dealer not found: " + request.dealerId());
                });
 
        Vehicle vehicle = Vehicle.builder()
                .tenantId(tenantId)
                .dealer(dealer)
                .model(request.model())
                .price(request.price())
                .status(request.status())
                .build();
 
        Vehicle saved = vehicleRepository.save(vehicle);
        log.debug("Created vehicle {} for dealer {} in tenant {}", saved.getId(), dealer.getId(), tenantId);
        return vehicleMapper.toResponse(saved);
    }


    @Transactional
    public VehicleResponse update(UUID id, UpdateVehicleRequest request) {
        Vehicle vehicle = findOwnedVehicle(id);
        vehicleMapper.updateEntity(request, vehicle);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }
 
    @Transactional
    public void delete(UUID id) {
        Vehicle vehicle = findOwnedVehicle(id);
        vehicleRepository.delete(vehicle);
        log.debug("Deleted vehicle {} from tenant {}", id, vehicle.getTenantId());
    }


     public VehicleResponse findById(UUID id) {
        return vehicleMapper.toResponse(findOwnedVehicle(id));
    }



    public PageResponse<VehicleResponse> findAll(
            String model,
            VehicleStatus status,
            BigDecimal priceMin,
            BigDecimal priceMax,
            SubscriptionType subscriptionType,
            org.springframework.data.domain.Pageable pageable
    ) {
        UUID tenantId = requireTenant();
 
        if (subscriptionType != null) {
            // Subscription-filtered query joins Vehicle → Dealer
            return PageResponse.from(
                    vehicleRepository
                            .findByDealerSubscription(tenantId, subscriptionType, pageable)
                            .map(vehicleMapper::toResponse)
            );
        }
 
        // Standard filter query (model / status / price range)
        return PageResponse.from(
                vehicleRepository
                        .findByFilters(tenantId, model, status, priceMin, priceMax, pageable)
                        .map(vehicleMapper::toResponse)
        );
    }


    private Vehicle findOwnedVehicle(UUID id) {
        UUID tenantId = requireTenant();
        return vehicleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> {
                    boolean existsElsewhere = vehicleRepository.existsById(id);
                    if (existsElsewhere) {
                        return new TenantAccessException(
                                "Access denied: vehicle " + id + " does not belong to your tenant");
                    }
                    return new ResourceNotFoundException("Vehicle not found: " + id);
                });
    }
 
    private UUID requireTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return tenantId;
    }

}
