-- Feature 7 : s'inscrire à une sortie d'observation.
-- Responsable : Selma Baibou (numéros de migration réservés : V10 à V19).
-- Une seule ligne par couple (phénomène, utilisateur) : se désinscrire passe le statut à CANCELLED,
-- se réinscrire le repasse à ACTIVE. Il y a donc au plus une participation active par utilisateur et par phénomène.

CREATE TABLE participation (
    id          BIGSERIAL   PRIMARY KEY,
    event_id    BIGINT      NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    status      VARCHAR(12) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_participation_event_user UNIQUE (event_id, user_id),
    CONSTRAINT ck_participation_status CHECK (status IN ('ACTIVE', 'CANCELLED'))
);

CREATE INDEX idx_participation_user_status ON participation (user_id, status);
CREATE INDEX idx_participation_event_status ON participation (event_id, status);
