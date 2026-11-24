
CREATE TABLE observation_places (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    light_pollution_level INTEGER NOT NULL,
    accessibility_score INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_place_latitude
        CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_place_longitude
        CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT chk_place_pollution
        CHECK (light_pollution_level BETWEEN 1 AND 5),
    CONSTRAINT chk_place_accessibility
        CHECK (accessibility_score BETWEEN 1 AND 5)
);

CREATE INDEX idx_observation_places_active
    ON observation_places(active);

INSERT INTO observation_places
(name, description, latitude, longitude,
 light_pollution_level, accessibility_score)
VALUES
('Fontainebleau', 'Zone de démonstration près de la forêt',
 48.4047, 2.7016, 3, 4),

('Rambouillet', 'Zone de démonstration en périphérie francilienne',
 48.6439, 1.8292, 3, 4),

('Meudon', 'Zone de démonstration proche de Paris',
 48.8123, 2.2384, 5, 5),

('Chartres', 'Zone de démonstration en Eure-et-Loir',
 48.4469, 1.4890, 3, 4),

('Pic du Midi', 'Zone de démonstration dans les Pyrénées',
 42.9369, 0.1411, 1, 2);
