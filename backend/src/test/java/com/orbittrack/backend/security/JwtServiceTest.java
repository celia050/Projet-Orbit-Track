package com.orbittrack.backend.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.orbittrack.backend.user.AppUser;
import com.orbittrack.backend.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests unitaires (sans base ni Docker) du jeton de connexion. */
class JwtServiceTest {

    private static final String SECRET = "secret-de-test-uniquement-0123456789-abcdef";

    @Test
    void leJetonContientLIdentifiantLeRoleEtUneDateDExpiration() {
        SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtProperties properties = new JwtProperties(SECRET, Duration.ofHours(2));
        Instant now = Instant.now();
        JwtService service = new JwtService(new NimbusJwtEncoder(new ImmutableSecret<>(key)), properties,
                Clock.fixed(now, ZoneOffset.UTC));

        AppUser user = new AppUser("astro@test.local", "hash", "Astro", Role.ADMIN);
        ReflectionTestUtils.setField(user, "id", 42L);

        JwtService.IssuedToken token = service.issue(user);

        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        Jwt jwt = decoder.decode(token.value());
        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("ADMIN");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("astro@test.local");
        assertThat(token.expiresAt()).isEqualTo(now.plus(Duration.ofHours(2)));
        assertThat(CurrentUser.id(jwt)).isEqualTo(42L);
        assertThat(CurrentUser.isAdmin(jwt)).isTrue();
    }

    @Test
    void unSecretTropCourtEstRefuseAuDemarrage() {
        assertThatThrownBy(() -> new JwtProperties("trop-court", Duration.ofHours(2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }
}
