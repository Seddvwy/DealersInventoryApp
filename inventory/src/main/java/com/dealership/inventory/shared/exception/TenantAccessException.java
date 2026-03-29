package com.dealership.inventory.shared.exception;

/**
 * Thrown when a caller attempts to access a resource belonging to a different tenant (maps to 403).
 */
public class TenantAccessException extends RuntimeException {
    public TenantAccessException(String message) {
        super(message);
    }
}