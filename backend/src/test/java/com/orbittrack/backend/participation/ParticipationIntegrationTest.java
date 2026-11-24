package com.orbittrack.backend.participation;

import com.orbittrack.backend.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Feature 7 : s'inscrire à une sortie d'observation. */
class ParticipationIntegrationTest extends IntegrationTestSupport {

    @Test
    void sInscrirePuisSeDesinscrire() throws Exception {
        Long eventId = createEvent("Sortie test", 5);
        String token = newUserToken();

        mvc.perform(post("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participating").value(true))
                .andExpect(jsonPath("$.participantCount").value(1));

        mvc.perform(get("/api/me/participations").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventId", hasItem(eventId.intValue())));

        mvc.perform(delete("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participating").value(false))
                .andExpect(jsonPath("$.participantCount").value(0));
    }

    @Test
    void uneSeuleParticipationActiveParUtilisateur() throws Exception {
        Long eventId = createEvent("Sortie doublon", 5);
        String token = newUserToken();

        mvc.perform(post("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(ParticipationService.ALREADY_JOINED));
    }

    @Test
    void onPeutSeReinscrireApresSEtreDesinscrit() throws Exception {
        Long eventId = createEvent("Sortie retour", 5);
        String token = newUserToken();

        mvc.perform(post("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)));
        mvc.perform(delete("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)));
        mvc.perform(post("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participantCount").value(1));
    }

    @Test
    void pasDInscriptionAUnPhenomeneAnnule() throws Exception {
        Long eventId = createEvent("Annulé", "METEOR_SHOWER", Instant.now().plus(Duration.ofDays(3)),
                "CANCELLED", null, null, null);

        mvc.perform(post("/api/events/" + eventId + "/participation").header("Authorization", bearer(newUserToken())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(ParticipationService.EVENT_CANCELLED));
    }

    @Test
    void pasDInscriptionAUnPhenomeneTermine() throws Exception {
        Long eventId = createEvent("Passé", "METEOR_SHOWER", Instant.now().minus(Duration.ofDays(3)),
                "SCHEDULED", null, null, null);

        mvc.perform(post("/api/events/" + eventId + "/participation").header("Authorization", bearer(newUserToken())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(ParticipationService.EVENT_OVER));
    }

    @Test
    void phenomeneInconnu() throws Exception {
        mvc.perform(post("/api/events/999999999/participation").header("Authorization", bearer(newUserToken())))
                .andExpect(status().isNotFound());
    }

    @Test
    void seDesinscrireSansEtreInscrit() throws Exception {
        Long eventId = createEvent("Jamais inscrit", 5);
        mvc.perform(delete("/api/events/" + eventId + "/participation").header("Authorization", bearer(newUserToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(ParticipationService.NOT_JOINED));
    }

    @Test
    void sInscrireDemandeUnCompteMaisConsulterNon() throws Exception {
        Long eventId = createEvent("Visiteur", 5);

        mvc.perform(post("/api/events/" + eventId + "/participation")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/events/" + eventId + "/participation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participating").value(false));

        mvc.perform(get("/api/outings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventId", hasItem(eventId.intValue())));
    }

    @Test
    void lesSortiesTermineesNApparaissentPlus() throws Exception {
        Long past = createEvent("Ancienne sortie", "METEOR_SHOWER", Instant.now().minus(Duration.ofDays(10)),
                "SCHEDULED", null, null, null);

        mvc.perform(get("/api/outings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventId", not(hasItem(past.intValue()))));
    }
}
