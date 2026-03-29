package com.dealership.inventory.admin.controller;

import com.dealership.inventory.admin.service.AdminService;
import com.dealership.inventory.dealer.model.SubscriptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller exposing administrative endpoints.
 *
 * <p>All routes under {@code /admin/**} require the {@code GLOBAL_ADMIN} role,
 * enforced at two layers:
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /admin/dealers/countBySubscription
     *
     * <p>Returns the total number of dealers grouped by subscription type
     * <strong>across all tenants</strong> (global, not per-tenant).
     */
    @GetMapping("/dealers/countBySubscription")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public Map<SubscriptionType, Long> countBySubscription() {
        return adminService.countDealersBySubscription();
    }
}