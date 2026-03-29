package com.dealership.inventory.vehicle.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dealership.inventory.dealer.model.SubscriptionType;
import com.dealership.inventory.shared.web.PageResponse;
import com.dealership.inventory.vehicle.dto.VehicleDtos.CreateVehicleRequest;
import com.dealership.inventory.vehicle.dto.VehicleDtos.UpdateVehicleRequest;
import com.dealership.inventory.vehicle.dto.VehicleDtos.VehicleResponse;
import com.dealership.inventory.vehicle.model.VehicleStatus;
import com.dealership.inventory.vehicle.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * POST /vehicles
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse create(@Valid @RequestBody CreateVehicleRequest request) {
        return vehicleService.create(request);
    }


    /**
     * GET /vehicles/{id}
     */
    @GetMapping("/{id}")
    public VehicleResponse findById(@PathVariable UUID id) {
        return vehicleService.findById(id);
    }


    @GetMapping
    public PageResponse<VehicleResponse> findAll(
            @RequestParam(required = false) String model,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) SubscriptionType subscription,
            @RequestParam(defaultValue = "0")      int page,
            @RequestParam(defaultValue = "20")     int size,
            @RequestParam(defaultValue = "model")  String sortBy,
            @RequestParam(defaultValue = "asc")    String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
 
        return vehicleService.findAll(model, status, priceMin, priceMax, subscription,
                PageRequest.of(page, size, sort));
    }


    /**
     * PATCH /vehicles/{id}
     */
    @PatchMapping("/{id}")
    public VehicleResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateVehicleRequest request) {
        return vehicleService.update(id, request);
    }
 
    /**
     * DELETE /vehicles/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        vehicleService.delete(id);
    }
 
    
}
