-- V2__create_vehicles.sql
-- Creates the vehicles table with FK to dealers and denormalized tenant_id.

CREATE TABLE vehicles (
    id          UUID            NOT NULL DEFAULT gen_random_uuid(),
    tenant_id   UUID            NOT NULL,
    dealer_id   UUID            NOT NULL,
    model       VARCHAR(255)    NOT NULL,
    price       NUMERIC(12, 2)  NOT NULL CHECK (price > 0),
    status      VARCHAR(20)     NOT NULL CHECK (status IN ('AVAILABLE', 'SOLD')),
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT pk_vehicles        PRIMARY KEY (id),
    CONSTRAINT fk_vehicle_dealer  FOREIGN KEY (dealer_id) REFERENCES dealers (id) ON DELETE CASCADE
);

-- Tenant scoping (most queries start here)
CREATE INDEX idx_vehicle_tenant         ON vehicles (tenant_id);
-- FK lookup
CREATE INDEX idx_vehicle_dealer         ON vehicles (dealer_id);
-- Composite indexes for common filter combinations
CREATE INDEX idx_vehicle_tenant_status  ON vehicles (tenant_id, status);
CREATE INDEX idx_vehicle_tenant_model   ON vehicles (tenant_id, model);

COMMENT ON TABLE  vehicles            IS 'Vehicles in a dealer''s inventory, isolated by tenant_id.';
COMMENT ON COLUMN vehicles.tenant_id  IS 'Denormalized from dealer for efficient tenant-scoped queries.';
COMMENT ON COLUMN vehicles.dealer_id  IS 'FK → dealers.id; cascades on delete.';
COMMENT ON COLUMN vehicles.status     IS 'AVAILABLE: on lot; SOLD: transaction complete.';