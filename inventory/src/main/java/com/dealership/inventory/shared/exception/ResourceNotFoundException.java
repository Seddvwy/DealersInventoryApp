package com.dealership.inventory.shared.exception;

/**
 * Thrown when a resource is not found (maps to 404).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}