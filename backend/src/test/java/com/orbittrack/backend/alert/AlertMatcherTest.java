package com.orbittrack.backend.alert;

import com.orbittrack.backend.participation.EventInfo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Feature 11 : règles de correspondance entre une alerte et un phénomène (sans base de données). */
class AlertMatcherTest {

    private static final Instant GEMINIDS = Instant.parse("2026-12-14T01:00:00Z");

    private static EventInfo event(String status, Double lat, Double lon, Double radius) {
        return new EventInfo(1L, "Pluie de météores des Géminides", "METEOR_SHOWER", "Très belle pluie d'étoiles",
                GEMINIDS, GEMINIDS.plusSeconds(4 * 3600), status, "France", lat, lon, radius);
    }

    private static final EventInfo PARIS_EVENT = event("SCHEDULED", 48.8566, 2.3522, 100.0);

    private static Alert alert(String type, String keyword, LocalDate from, LocalDate to,
                               Double lat, Double lon, Double radius) {
        return new Alert(1L, "Test", type, keyword, from, to, lat, lon, radius);
    }

    @Test
    void parType() {
        assertThat(AlertMatcher.matches(alert("METEOR_SHOWER", null, null, null, null, null, null), PARIS_EVENT))
                .isTrue();
        assertThat(AlertMatcher.matches(alert("ISS_PASS", null, null, null, null, null, null), PARIS_EVENT))
                .isFalse();
    }

    @Test
    void parMotCleDansLeNomOuLaDescriptionSansMajuscules() {
        assertThat(AlertMatcher.matches(alert(null, "géminides", null, null, null, null, null), PARIS_EVENT)).isTrue();
        assertThat(AlertMatcher.matches(alert(null, "ÉTOILES", null, null, null, null, null), PARIS_EVENT)).isTrue();
        assertThat(AlertMatcher.matches(alert(null, "éclipse", null, null, null, null, null), PARIS_EVENT)).isFalse();
    }

    @Test
    void parPeriodeBornesComprises() {
        LocalDate day = LocalDate.of(2026, 12, 14); // 02h00 à Paris
        assertThat(AlertMatcher.matches(alert(null, null, day, day, null, null, null), PARIS_EVENT)).isTrue();
        assertThat(AlertMatcher.matches(alert(null, null, day.plusDays(1), null, null, null, null), PARIS_EVENT))
                .isFalse();
        assertThat(AlertMatcher.matches(alert(null, null, null, day.minusDays(1), null, null, null), PARIS_EVENT))
                .isFalse();
    }

    @Test
    void parLieuEnTenantCompteDuRayonDeVisibilite() {
        // Lyon est à environ 390 km de Paris ; le phénomène est visible à 100 km autour de Paris.
        assertThat(AlertMatcher.matches(alert(null, null, null, null, 45.764, 4.8357, 300.0), PARIS_EVENT)).isTrue();
        assertThat(AlertMatcher.matches(alert(null, null, null, null, 45.764, 4.8357, 200.0), PARIS_EVENT)).isFalse();
        // Phénomène sans coordonnées : une alerte par lieu ne peut pas le retenir.
        assertThat(AlertMatcher.matches(alert(null, null, null, null, 48.85, 2.35, 50.0),
                event("SCHEDULED", null, null, null))).isFalse();
    }

    @Test
    void tousLesCriteresDoiventEtreRemplis() {
        assertThat(AlertMatcher.matches(alert("METEOR_SHOWER", "perséides", null, null, null, null, null),
                PARIS_EVENT)).isFalse();
    }

    @Test
    void unPhenomeneAnnuleNeDeclenchePasDAlerte() {
        assertThat(AlertMatcher.matches(alert("METEOR_SHOWER", null, null, null, null, null, null),
                event("CANCELLED", 48.8566, 2.3522, 100.0))).isFalse();
    }

    @Test
    void distanceParisLyon() {
        assertThat(AlertMatcher.distanceKm(48.8566, 2.3522, 45.764, 4.8357)).isCloseTo(392, within(5.0));
    }
}
