package com.orbittrack.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Réglages des jetons de connexion (orbittrack.security.jwt.* dans application.yaml).
 * Le secret vient de la variable d'environnement JWT_SECRET : il n'est jamais versionné.
 */
@ConfigurationProperties("orbittrack.security.jwt")
public record JwtProperties(String secret, Duration expiration) {

    /** HS256 exige une clé d'au moins 256 bits, soit 32 octets. */
    static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET doit contenir au moins " + MIN_SECRET_BYTES + " caractères. "
                            + "Copier .env.example en .env et le remplir (ex. : openssl rand -base64 48).");
        }
        if (expiration == null) {
            expiration = Duration.ofHours(2);
        }
    }
}
