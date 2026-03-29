package com.dealership.inventory.dealer.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dealership.inventory.dealer.dto.DealerDtos.CreateDealerRequest;
import com.dealership.inventory.dealer.dto.DealerDtos.DealerResponse;
import com.dealership.inventory.dealer.dto.DealerDtos.UpdateDealerRequest;
import com.dealership.inventory.dealer.mapper.DealerMapper;
import com.dealership.inventory.dealer.model.Dealer;
import com.dealership.inventory.dealer.repository.DealerRepository;
import com.dealership.inventory.shared.security.TenantContext;
import com.dealership.inventory.shared.exception.ResourceNotFoundException;
import com.dealership.inventory.shared.exception.TenantAccessException;

import com.dealership.inventory.shared.web.PageResponse;

import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DealerService {

    private final DealerRepository dealerRepository;
    private final DealerMapper dealerMapper;

    // #CREATE
    @Transactional
    public DealerResponse create(CreateDealerRequest request){
        UUID tenantId = requireTenant();
        Dealer dealer = dealerMapper.toEntity(request);
        dealer.setTenantId(tenantId);
        Dealer saved = dealerRepository.save(dealer);
        log.debug("Created dealer {} for tenant {}", saved.getId(), tenantId);
        return dealerMapper.toResponse(saved);
    }

    // #UPDATE
    @Transactional
    public DealerResponse update(UUID id, UpdateDealerRequest request) {
        Dealer dealer = findOwnedDealer(id);
        dealerMapper.updateEntity(request, dealer);
        return dealerMapper.toResponse(dealerRepository.save(dealer));
    }

    // #DELETE
    @Transactional
    public void delete(UUID id) {
        Dealer dealer = findOwnedDealer(id);
        dealerRepository.delete(dealer);
        log.debug("Deleted dealer {} from tenant {}", id, dealer.getTenantId());
    }
 

    public DealerResponse findById(UUID id) {
        return dealerMapper.toResponse(findOwnedDealer(id));
    }

    public PageResponse<DealerResponse> findAll(Pageable pageable) {
        UUID tenantId = requireTenant();
        return PageResponse.from(
                dealerRepository.findAllByTenantId(tenantId, pageable)
                        .map(dealerMapper::toResponse)
        );
    }

    public boolean existsForCurrentTenant(UUID dealerId) {
        return dealerRepository.existsByIdAndTenantId(dealerId, requireTenant());
    }


    private Dealer findOwnedDealer(UUID id) {
        UUID tenantId = requireTenant();
        // First try a tenant-scoped lookup – avoids info leakage about resource existence
        return dealerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> {
                    // We need to distinguish "not found" from "forbidden".
                    // If the dealer exists but for another tenant → 403.
                    // If it doesn't exist at all → 404.
                    boolean existsElsewhere = dealerRepository.existsById(id);
                    if (existsElsewhere) {
                        return new TenantAccessException(
                                "Access denied: dealer " + id + " does not belong to your tenant");
                    }
                    return new ResourceNotFoundException("Dealer not found: " + id);
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
