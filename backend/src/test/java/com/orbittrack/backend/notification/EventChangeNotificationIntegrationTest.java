package com.orbittrack.backend.notification;

import com.jayway.jsonpath.JsonPath;
import com.orbittrack.backend.IntegrationTestSupport;
import com.orbittrack.backend.participation.EventChangeDetector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Feature 10 : être prévenu d'un changement (et lecture des notifications). */
class EventChangeNotificationIntegrationTest extends IntegrationTestSupport {

    @Autowired
    EventChangeDetector detector;

    @Test
    void leParticipantEstPrevenuDUnNouvelHoraireUneSeuleFois() throws Exception {
        Long eventId = createEvent("Horaire mobile", 6);
        String token = participant(eventId);

        detector.detectChanges();
        expectNotifications(token, 0);

        updateEvent(eventId, Instant.now().plus(Duration.ofDays(7)), "UPDATED");
        detector.detectChanges();
        detector.detectChanges(); // pas de doublon au passage suivant

        mvc.perform(get("/api/me/notifications").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("EVENT_CHANGED"))
                .andExpect(jsonPath("$[0].eventId").value(eventId))
                .andExpect(jsonPath("$[0].message", containsString("Nouvel horaire pour « Horaire mobile »")))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void leParticipantEstPrevenuDUneAnnulation() throws Exception {
        Long eventId = createEvent("Sortie annulée", 6);
        String token = participant(eventId);

        jdbc.update("UPDATE events SET status = 'CANCELLED' WHERE id = ?", eventId);
        detector.detectChanges();

        mvc.perform(get("/api/me/notifications").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("EVENT_CANCELLED"))
                .andExpect(jsonPath("$[0].message").value("La sortie « Sortie annulée » est annulée."));
    }

    @Test
    void lesNonInscritsEtLesDesinscritsNeSontPasPrevenus() throws Exception {
        Long eventId = createEvent("Sans moi", 6);
        String leaver = participant(eventId);
        mvc.perform(delete("/api/events/" + eventId + "/participation").header("Authorization", bearer(leaver)))
                .andExpect(status().isNoContent());
        String outsider = newUserToken();

        updateEvent(eventId, Instant.now().plus(Duration.ofDays(8)), "UPDATED");
        detector.detectChanges();

        expectNotifications(leaver, 0);
        expectNotifications(outsider, 0);
    }

    @Test
    void marquerCommeLu() throws Exception {
        Long eventId = createEvent("À lire", 6);
        String token = participant(eventId);
        updateEvent(eventId, Instant.now().plus(Duration.ofDays(9)), "UPDATED");
        detector.detectChanges();
        jdbc.update("UPDATE events SET status = 'CANCELLED' WHERE id = ?", eventId);
        detector.detectChanges();

        mvc.perform(get("/api/me/notifications/unread-count").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.count").value(2));

        String body = mvc.perform(get("/api/me/notifications").header("Authorization", bearer(token)))
                .andReturn().getResponse().getContentAsString();
        Number firstId = JsonPath.read(body, "$[0].id");
        mvc.perform(post("/api/me/notifications/" + firstId + "/read").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
        mvc.perform(get("/api/me/notifications/unread-count").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.count").value(1));

        mvc.perform(post("/api/me/notifications/read-all").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/me/notifications/unread-count").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void onNePeutPasLireLesNotificationsDUnAutre() throws Exception {
        Long eventId = createEvent("Privé", 6);
        String owner = participant(eventId);
        updateEvent(eventId, Instant.now().plus(Duration.ofDays(9)), "UPDATED");
        detector.detectChanges();
        String body = mvc.perform(get("/api/me/notifications").header("Authorization", bearer(owner)))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$[0].id");

        mvc.perform(post("/api/me/notifications/" + id + "/read").header("Authorization", bearer(newUserToken())))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/me/notifications")).andExpect(status().isUnauthorized());
    }

    private void expectNotifications(String token, int count) throws Exception {
        mvc.perform(get("/api/me/notifications").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(count));
    }

    private String participant(Long eventId) throws Exception {
        String token = newUserToken();
        mvc.perform(post("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)))
                .andExpect(status().isCreated());
        return token;
    }
}
