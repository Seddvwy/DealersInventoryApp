package com.dealership.inventory.shared.security;


public enum UserRole {
    TENANT_USER,
    GLOBAL_ADMIN;

    /** Prefixed form expected by Spring Security's hasRole() / @Secured */
    public String authority() {
        return "ROLE_" + name();
    }
}