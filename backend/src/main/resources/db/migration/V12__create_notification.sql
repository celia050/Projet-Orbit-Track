-- Feature 10 : être prévenu d'un changement (horaire modifié ou sortie annulée).
-- Responsable : Selma Baibou (numéros de migration réservés : V10 à V19).

-- Notifications affichées dans le site. Aussi utilisées par les alertes (feature 11, type ALERT_MATCH).
CREATE TABLE notification (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    event_id    BIGINT       REFERENCES events (id) ON DELETE CASCADE,
    type        VARCHAR(30)  NOT NULL,
    message     VARCHAR(500) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    read_at     TIMESTAMPTZ,
    CONSTRAINT ck_notification_type CHECK (type IN ('EVENT_CHANGED', 'EVENT_CANCELLED', 'ALERT_MATCH'))
);

CREATE INDEX idx_notification_user_created ON notification (user_id, created_at DESC);

-- Horaire et statut du phénomène tels que le participant les connaît.
-- Toutes les minutes, le serveur compare avec la table events : s'ils diffèrent, il prévient le participant
-- puis met ces colonnes à jour. Ça fonctionne quelle que soit la façon dont le phénomène a été modifié
-- (import automatique, administrateur…).
ALTER TABLE participation
    ADD COLUMN known_start_at TIMESTAMPTZ,
    ADD COLUMN known_end_at   TIMESTAMPTZ,
    ADD COLUMN known_status   VARCHAR(20);

UPDATE participation p
SET known_start_at = e.start_at,
    known_end_at   = e.end_at,
    known_status   = e.status
FROM events e
WHERE e.id = p.event_id;
