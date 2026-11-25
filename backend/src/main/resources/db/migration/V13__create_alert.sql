-- Feature 11 : créer une alerte (par type de phénomène, mot-clé, période ou lieu).
-- Responsable : Selma Baibou (numéros de migration réservés : V10 à V19).

CREATE TABLE alert (
    id         BIGSERIAL        PRIMARY KEY,
    user_id    BIGINT           NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    name       VARCHAR(80)      NOT NULL,
    event_type VARCHAR(40),
    keyword    VARCHAR(80),
    date_from  DATE,
    date_to    DATE,
    latitude   DOUBLE PRECISION,
    longitude  DOUBLE PRECISION,
    radius_km  DOUBLE PRECISION,
    active     BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ      NOT NULL DEFAULT now(),
    CONSTRAINT ck_alert_name CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT ck_alert_dates CHECK (date_from IS NULL OR date_to IS NULL OR date_to >= date_from),
    CONSTRAINT ck_alert_place CHECK (
        (latitude IS NULL AND longitude IS NULL AND radius_km IS NULL)
        OR (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180 AND radius_km > 0)
    ),
    CONSTRAINT ck_alert_criteria CHECK (
        event_type IS NOT NULL OR keyword IS NOT NULL OR date_from IS NOT NULL OR date_to IS NOT NULL
        OR latitude IS NOT NULL
    )
);

CREATE INDEX idx_alert_user ON alert (user_id);

-- Une notification d'alerte est liée à son alerte : un même phénomène ne déclenche qu'une fois la même alerte.
-- Si l'alerte est supprimée, ses notifications restent (alert_id devient vide).
ALTER TABLE notification ADD COLUMN alert_id BIGINT REFERENCES alert (id) ON DELETE SET NULL;
CREATE UNIQUE INDEX uk_notification_alert_event ON notification (alert_id, event_id) WHERE alert_id IS NOT NULL;
