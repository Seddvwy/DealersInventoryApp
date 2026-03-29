package com.dealership.inventory.vehicle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.dealership.inventory.dealer.model.Dealer;
import com.dealership.inventory.vehicle.dto.VehicleDtos.DealerSummary;
import com.dealership.inventory.vehicle.dto.VehicleDtos.UpdateVehicleRequest;
import com.dealership.inventory.vehicle.dto.VehicleDtos.VehicleResponse;
import com.dealership.inventory.vehicle.model.Vehicle;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VehicleMapper {

    /** Map entity to response DTO. */
    @Mapping(target = "dealer", expression = "java(toDealerSummary(vehicle.getDealer()))")
    VehicleResponse toResponse(Vehicle vehicle);



    /* Map Dealer entity to DealerSummary DTO for embedding in VehicleResponse. */
    default DealerSummary toDealerSummary(Dealer dealer) {
        if (dealer == null) {
            return null;
        }
        return new DealerSummary(dealer.getId(), dealer.getName(), dealer.getSubscriptionType());
    }



    /** Update entity from update request DTO (tenantId, dealer, createdAt, updatedAt set manually in service). */
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "dealer",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateVehicleRequest request, @MappingTarget Vehicle vehicle);

}
