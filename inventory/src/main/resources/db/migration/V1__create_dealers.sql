-- V1__create_dealers.sql
-- Creates the dealers table with multi-tenant isolation.

CREATE TABLE dealers (
    id                UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id         UUID        NOT NULL,
    name              VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NOT NULL,
    subscription_type VARCHAR(20)  NOT NULL CHECK (subscription_type IN ('BASIC', 'PREMIUM')),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_dealers PRIMARY KEY (id),
    CONSTRAINT uq_dealers_email UNIQUE (email)
);

-- Tenant isolation index: nearly every query scopes by tenant_id
CREATE INDEX idx_dealer_tenant ON dealers (tenant_id);

-- Supports the admin countBySubscription grouping and the subscription filter on vehicles
CREATE INDEX idx_dealer_tenant_subscription ON dealers (tenant_id, subscription_type);

COMMENT ON TABLE  dealers                       IS 'Car dealers, isolated by tenant_id.';
COMMENT ON COLUMN dealers.tenant_id             IS 'Opaque tenant identifier set from X-Tenant-Id header.';
COMMENT ON COLUMN dealers.subscription_type     IS 'BASIC or PREMIUM tier determines feature access.';