package com.orbittrack.backend.security;

import com.orbittrack.backend.user.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

/** Fabrique les jetons de connexion (JWT signés en HS256). */
@Service
public class JwtService {

    static final String ISSUER = "orbit-track";

    private final JwtEncoder encoder;
    private final JwtProperties properties;
    private final Clock clock;

    // Deux constructeurs : @Autowired indique à Spring lequel utiliser (l'autre sert aux tests).
    @Autowired
    public JwtService(JwtEncoder encoder, JwtProperties properties) {
        this(encoder, properties, Clock.systemUTC());
    }

    JwtService(JwtEncoder encoder, JwtProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Jeton contenant : l'identifiant de l'utilisateur (sujet), son email et son rôle.
     * Le rôle est relu à chaque requête depuis le jeton : un changement de rôle
     * prend effet à la prochaine connexion (ou à l'expiration du jeton).
     */
    public IssuedToken issue(AppUser user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.expiration());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("email", user.getEmail())
                .claim(SecurityConfig.ROLE_CLAIM, user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expiresAt);
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
