CREATE TABLE shipments (
    id uuid PRIMARY KEY,
    tracking_id VARCHAR(64),
    owner_user_id VARCHAR(64),
    company_id VARCHAR(64),
    sender VARCHAR(255) NOT NULL,
    receiver VARCHAR(255) NOT NULL,
    current_status VARCHAR(50) NOT NULL,
    expected_delivery TIMESTAMP,
    last_lat DOUBLE PRECISION,
    last_lng DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE checkpoints (
    id UUID PRIMARY KEY,
    shipment_id uuid not null references shipments(id) on delete cascade,
    ts timestamp not null,
    status varchar(50),
    message VARCHAR(500),
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION
);

CREATE INDEX idx_shipments_company ON shipments(company_id);
CREATE INDEX idx_checkpoints_shipment_ts ON checkpoints(shipment_id, ts);