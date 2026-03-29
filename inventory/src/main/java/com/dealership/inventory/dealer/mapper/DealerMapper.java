package com.dealership.inventory.dealer.mapper;

import org.mapstruct.*;
import com.dealership.inventory.dealer.model.Dealer;
import com.dealership.inventory.dealer.dto.DealerDtos;
import com.dealership.inventory.dealer.dto.DealerDtos.DealerResponse;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DealerMapper {

    /** Create entity from request (tenantId set manually in service). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Dealer toEntity(DealerDtos.CreateDealerRequest request);

    /** Convert entity to response. */
    DealerResponse toResponse(Dealer dealer);


    /** Update entity from request (tenantId, createdAt, updatedAt set manually in service). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(DealerDtos.UpdateDealerRequest request, @MappingTarget Dealer entity);

}
