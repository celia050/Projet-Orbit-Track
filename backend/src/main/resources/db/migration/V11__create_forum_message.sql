-- Feature 8 : forum de chaque sortie d'observation.
-- Responsable : Selma Baibou (numéros de migration réservés : V10 à V19).
-- Un message supprimé n'est pas effacé : il est masqué (deleted_at), pour garder le fil de la discussion
-- et la trace de la modération.

CREATE TABLE forum_message (
    id               BIGSERIAL     PRIMARY KEY,
    event_id         BIGINT        NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    author_id        BIGINT        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    content          VARCHAR(1000) NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    edited_at        TIMESTAMPTZ,
    deleted_at       TIMESTAMPTZ,
    deleted_by_admin BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_forum_message_content CHECK (LENGTH(TRIM(content)) > 0)
);

CREATE INDEX idx_forum_message_event_created ON forum_message (event_id, created_at DESC);
CREATE INDEX idx_forum_message_author_created ON forum_message (author_id, created_at DESC);
