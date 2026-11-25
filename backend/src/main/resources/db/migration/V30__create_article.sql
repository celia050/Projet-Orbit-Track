-- Feature 15 : Article du jour
-- Création de la table articles

CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,

    -- Informations de l'article
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    article_date DATE NOT NULL,

    -- Statut de validation
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',

    -- Historique
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ,

    -- Contraintes
    CONSTRAINT ck_articles_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'REJECTED')),

    CONSTRAINT ck_articles_required_text
        CHECK (
            LENGTH(TRIM(title)) > 0
            AND LENGTH(TRIM(content)) > 0
        ),

    CONSTRAINT ck_articles_publication
        CHECK (
            (status = 'PUBLISHED' AND published_at IS NOT NULL)
            OR
            (status <> 'PUBLISHED' AND published_at IS NULL)
        )
);

CREATE INDEX idx_articles_date
    ON articles(article_date);

CREATE INDEX idx_articles_status
    ON articles(status);
