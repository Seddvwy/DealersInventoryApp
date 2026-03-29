package com.dealership.inventory.dealer.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import com.dealership.inventory.dealer.dto.DealerDtos.CreateDealerRequest;
import com.dealership.inventory.dealer.dto.DealerDtos.DealerResponse;
import com.dealership.inventory.dealer.dto.DealerDtos.UpdateDealerRequest;
import com.dealership.inventory.dealer.service.DealerService;
import com.dealership.inventory.shared.web.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/dealers")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;


    /**
     * POST /dealers
     * Create a new dealer for the calling tenant.
    */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DealerResponse create(@Valid @RequestBody CreateDealerRequest request) {
        return dealerService.create(request);
    }

    /**
     * GET /dealers/{id}
     * Retrieve a single dealer. Returns 404 if not found, 403 if cross-tenant.
    */
   @GetMapping("/{id}")
   public DealerResponse findById(@PathVariable UUID id) {
       return dealerService.findById(id);
   }

   /**
     * GET /dealers?page=0&size=20&sort=name,asc
     * Paginated + sorted list of dealers for the calling tenant.
    */
    @GetMapping
    public PageResponse<DealerResponse> findAll(
        @RequestParam(defaultValue = "0")int page,
        @RequestParam(defaultValue = "20")int size,
        @RequestParam(defaultValue = "name")String sortBy,
        @RequestParam(defaultValue = "asc")String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
        ? Sort.by(sortBy).descending()
        : Sort.by(sortBy).ascending();
        return dealerService.findAll(PageRequest.of(page, size, sort));
    }
   

    /**
     * PATCH /dealers/{id}
     * Partial update – only supplied fields are changed.
     */
    @PatchMapping("/{id}")
    public DealerResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateDealerRequest request) {
        return dealerService.update(id, request);
    }



    /**
     * DELETE /dealers/{id}
     * Permanently removes the dealer. Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        dealerService.delete(id);
    }
}
