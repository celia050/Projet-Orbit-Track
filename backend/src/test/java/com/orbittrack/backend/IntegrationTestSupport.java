package com.orbittrack.backend;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base commune des tests d'intégration des features 7 à 11 (sorties, forum, notifications, alertes) :
 * créer un compte, se connecter en admin, créer un phénomène de test.
 * Chaque test crée ses propres données, avec des dates relatives à maintenant.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTestSupport {

    protected static final String ADMIN_EMAIL = "admin@test.local";
    protected static final String ADMIN_PASSWORD = "mot-de-passe-admin-test";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JdbcTemplate jdbc;

    /** Crée un compte et renvoie son jeton de connexion. */
    protected String newUserToken() throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user-%s@test.local","password":"motdepasse123","displayName":"Testeur"}"""
                                .formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    protected String adminToken() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}""".formatted(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    /** Phénomène de test qui commence dans {@code daysFromNow} jours et dure 2 heures. */
    protected Long createEvent(String name, int daysFromNow) {
        return createEvent(name, "METEOR_SHOWER", Instant.now().plus(Duration.ofDays(daysFromNow)),
                "SCHEDULED", 48.8566, 2.3522, 100.0);
    }

    protected Long createEvent(String name, String type, Instant start, String status,
                               Double latitude, Double longitude, Double radiusKm) {
        return jdbc.queryForObject("""
                        INSERT INTO events (name, type, description, start_at, end_at, visibility_zone,
                                            latitude, longitude, visibility_radius_km, status, source_name,
                                            source_event_id, is_demo)
                        VALUES (?, ?, 'Phénomène de test', ?, ?, 'Île-de-France', ?, ?, ?, ?, 'TEST', ?, TRUE)
                        RETURNING id""",
                Long.class, name, type, Timestamp.from(start), Timestamp.from(start.plus(Duration.ofHours(2))),
                latitude, longitude, radiusKm, status, UUID.randomUUID().toString());
    }

    /** Simule une modification du phénomène par l'import (feature 5) ou un administrateur. */
    protected void updateEvent(Long eventId, Instant newStart, String newStatus) {
        jdbc.update("UPDATE events SET start_at = ?, end_at = ?, status = ?, updated_at = now() WHERE id = ?",
                Timestamp.from(newStart), Timestamp.from(newStart.plus(Duration.ofHours(2))), newStatus, eventId);
    }
}
