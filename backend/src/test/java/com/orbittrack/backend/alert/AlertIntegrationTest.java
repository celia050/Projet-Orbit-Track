package com.orbittrack.backend.alert;

import com.jayway.jsonpath.JsonPath;
import com.orbittrack.backend.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Feature 11 : créer une alerte. Chaque test utilise un mot-clé unique pour ne voir que ses phénomènes. */
class AlertIntegrationTest extends IntegrationTestSupport {

    @Autowired
    AlertService alertService;

    @Test
    void unPhenomeneExistantQuiCorrespondPrevientDesLaCreation() throws Exception {
        String keyword = uniqueKeyword();
        createEvent("Comète " + keyword, 10);
        String token = newUserToken();

        createAlert(token, "{\"name\":\"Comètes\",\"keyword\":\"" + keyword + "\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true));

        mvc.perform(get("/api/me/notifications").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("ALERT_MATCH"))
                .andExpect(jsonPath("$[0].message", containsString("Alerte « Comètes »")));
    }

    @Test
    void unNouveauPhenomenePrevientAuPassageSuivantUneSeuleFois() throws Exception {
        String keyword = uniqueKeyword();
        String token = newUserToken();
        createAlert(token, "{\"name\":\"Nouveautés\",\"keyword\":\"" + keyword + "\"}")
                .andExpect(status().isCreated());
        expectNotifications(token, 0);

        createEvent("Aurore " + keyword, 12);
        alertService.scanAll();
        alertService.scanAll();

        expectNotifications(token, 1);
    }

    @Test
    void uneAlerteEnPauseNePrevientPas() throws Exception {
        String keyword = uniqueKeyword();
        String token = newUserToken();
        long alertId = id(createAlert(token, "{\"name\":\"Pause\",\"keyword\":\"" + keyword + "\"}")
                .andReturn().getResponse().getContentAsString());

        mvc.perform(patch("/api/me/alerts/" + alertId).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        createEvent("Pause " + keyword, 12);
        alertService.scanAll();
        expectNotifications(token, 0);

        // Réactivée : elle signale tout de suite le phénomène qui correspond.
        mvc.perform(patch("/api/me/alerts/" + alertId).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}"))
                .andExpect(status().isOk());
        expectNotifications(token, 1);
    }

    @Test
    void lesPhenomenesAnnulesOuHorsCriteresNeDeclenchentRien() throws Exception {
        String keyword = uniqueKeyword();
        createEvent("Annulé " + keyword, "METEOR_SHOWER", Instant.now().plus(Duration.ofDays(5)), "CANCELLED",
                48.85, 2.35, 50.0);
        createEvent("Mauvais type " + keyword, "ISS_PASS", Instant.now().plus(Duration.ofDays(5)), "SCHEDULED",
                48.85, 2.35, 50.0);
        String token = newUserToken();

        createAlert(token, "{\"name\":\"Météores\",\"eventType\":\"METEOR_SHOWER\",\"keyword\":\"" + keyword + "\"}")
                .andExpect(status().isCreated());
        expectNotifications(token, 0);
    }

    @Test
    void reglesDuFormulaire() throws Exception {
        String token = newUserToken();

        createAlert(token, "{\"name\":\"Vide\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(AlertService.NO_CRITERIA));

        createAlert(token, "{\"name\":\"Lieu incomplet\",\"latitude\":48.8}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(AlertService.INCOMPLETE_PLACE));

        createAlert(token, "{\"name\":\"Période\",\"dateFrom\":\"2027-02-10\",\"dateTo\":\"2027-02-01\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(AlertService.BAD_PERIOD));

        createAlert(token, "{\"name\":\"Hors limites\",\"latitude\":120,\"longitude\":2,\"radiusKm\":10}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.latitude").exists());

        createAlert(token, "{\"name\":\"\",\"eventType\":\"ISS_PASS\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void auMaximum20AlertesParMembre() throws Exception {
        String token = newUserToken();
        for (int i = 1; i <= AlertService.MAX_ALERTS_PER_USER; i++) {
            createAlert(token, "{\"name\":\"Alerte " + i + "\",\"keyword\":\"" + uniqueKeyword() + "\"}")
                    .andExpect(status().isCreated());
        }
        createAlert(token, "{\"name\":\"De trop\",\"keyword\":\"" + uniqueKeyword() + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(AlertService.TOO_MANY_ALERTS));
    }

    @Test
    void chacunNeGereQueSesAlertes() throws Exception {
        String owner = newUserToken();
        String other = newUserToken();
        long alertId = id(createAlert(owner, "{\"name\":\"À moi\",\"keyword\":\"" + uniqueKeyword() + "\"}")
                .andReturn().getResponse().getContentAsString());

        mvc.perform(get("/api/me/alerts").header("Authorization", bearer(other)))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(delete("/api/me/alerts/" + alertId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/me/alerts/" + alertId).header("Authorization", bearer(owner)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/me/alerts").header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(get("/api/me/alerts")).andExpect(status().isUnauthorized());
    }

    @Test
    void listeDesTypesDePhenomenesLibreDAcces() throws Exception {
        mvc.perform(get("/api/outings/event-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("METEOR_SHOWER")));
    }

    // ---------- Outils ----------

    private ResultActions createAlert(String token, String json)
            throws Exception {
        return mvc.perform(post("/api/me/alerts").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private void expectNotifications(String token, int count) throws Exception {
        mvc.perform(get("/api/me/notifications").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(count));
    }

    private static long id(String body) {
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static String uniqueKeyword() {
        return "kw" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
