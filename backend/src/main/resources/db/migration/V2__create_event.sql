
-- OrbitTrack - Feature 1 : événements astronomiques
-- Responsable : Ritej Baaboura
-- Migration V2 : création de la table events

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,

    -- Informations du phénomène
    name VARCHAR(200) NOT NULL,
    type VARCHAR(40) NOT NULL,
    description TEXT,

    -- Dates et horaires
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,

    -- Zone géographique de visibilité
    visibility_zone VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    visibility_radius_km DOUBLE PRECISION,

    -- État du phénomène
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',

    -- Traçabilité de la source
    source_name VARCHAR(100) NOT NULL,
    source_url TEXT,
    source_event_id VARCHAR(200),
    source_updated_at TIMESTAMPTZ,

    -- Données de démonstration
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,

    -- Historique
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Contrôles de cohérence
    CONSTRAINT ck_events_dates
        CHECK (end_at >= start_at),

    CONSTRAINT ck_events_status
        CHECK (status IN ('SCHEDULED', 'UPDATED', 'CANCELLED')),

    CONSTRAINT ck_events_latitude
        CHECK (latitude BETWEEN -90 AND 90),

    CONSTRAINT ck_events_longitude
        CHECK (longitude BETWEEN -180 AND 180),

    CONSTRAINT ck_events_coordinates
        CHECK (
            (latitude IS NULL AND longitude IS NULL)
            OR
            (latitude IS NOT NULL AND longitude IS NOT NULL)
        ),

    CONSTRAINT ck_events_radius
        CHECK (
            visibility_radius_km IS NULL
            OR visibility_radius_km > 0
        ),

    CONSTRAINT ck_events_required_text
        CHECK (
            LENGTH(TRIM(name)) > 0
            AND LENGTH(TRIM(type)) > 0
            AND LENGTH(TRIM(visibility_zone)) > 0
            AND LENGTH(TRIM(source_name)) > 0
        )
);

-- Optimisation des recherches
CREATE INDEX idx_events_start_at
    ON events(start_at);

CREATE INDEX idx_events_type_start
    ON events(type, start_at);

CREATE INDEX idx_events_status
    ON events(status);

-- Empêcher les doublons provenant d'une même source
CREATE UNIQUE INDEX uk_events_source
    ON events(source_name, source_event_id)
    WHERE source_event_id IS NOT NULL;
