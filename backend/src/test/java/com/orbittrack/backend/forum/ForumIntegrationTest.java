package com.orbittrack.backend.forum;

import com.jayway.jsonpath.JsonPath;
import com.orbittrack.backend.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Feature 8 : forum de chaque sortie. */
class ForumIntegrationTest extends IntegrationTestSupport {

    @Test
    void unParticipantEcritEtLitLeForum() throws Exception {
        Long eventId = createEvent("Forum ouvert", 4);
        String token = participant(eventId);

        postMessage(eventId, token, "On se retrouve au parking à 21 h ?");

        mvc.perform(get("/api/events/" + eventId + "/forum").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].content").value("On se retrouve au parking à 21 h ?"))
                .andExpect(jsonPath("$.items[0].authorName").value("Testeur"))
                .andExpect(jsonPath("$.items[0].canEdit").value(true));
    }

    @Test
    void leForumEstReserveAuxInscrits() throws Exception {
        Long eventId = createEvent("Forum privé", 4);
        String outsider = newUserToken();

        mvc.perform(get("/api/events/" + eventId + "/forum")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/events/" + eventId + "/forum").header("Authorization", bearer(outsider)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value(ForumService.PARTICIPANTS_ONLY));

        mvc.perform(post("/api/events/" + eventId + "/forum").header("Authorization", bearer(outsider))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Coucou\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void messageVideOuTropLongRefuse() throws Exception {
        Long eventId = createEvent("Forum validation", 4);
        String token = participant(eventId);

        mvc.perform(post("/api/events/" + eventId + "/forum").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/events/" + eventId + "/forum").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + "a".repeat(1001) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chacunNeModifieQueSesMessages() throws Exception {
        Long eventId = createEvent("Forum édition", 4);
        String author = participant(eventId);
        String other = participant(eventId);
        long messageId = postMessage(eventId, author, "Texte initial");

        mvc.perform(put("/api/forum/messages/" + messageId).header("Authorization", bearer(other))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Piraté\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value(ForumService.NOT_AUTHOR));

        mvc.perform(put("/api/forum/messages/" + messageId).header("Authorization", bearer(author))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Texte corrigé\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Texte corrigé"))
                .andExpect(jsonPath("$.edited").value(true));

        mvc.perform(delete("/api/forum/messages/" + messageId).header("Authorization", bearer(other)))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/api/forum/messages/" + messageId).header("Authorization", bearer(author)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/events/" + eventId + "/forum").header("Authorization", bearer(other)))
                .andExpect(jsonPath("$.items[0].deleted").value(true))
                .andExpect(jsonPath("$.items[0].content").value(ForumService.DELETED_BY_AUTHOR));
    }

    @Test
    void lAdministrateurModereSansEtreInscrit() throws Exception {
        Long eventId = createEvent("Forum modéré", 4);
        String author = participant(eventId);
        long messageId = postMessage(eventId, author, "Message à retirer");
        String admin = adminToken();

        mvc.perform(get("/api/events/" + eventId + "/forum").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].canDelete").value(true));

        mvc.perform(delete("/api/forum/messages/" + messageId).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/events/" + eventId + "/forum").header("Authorization", bearer(author)))
                .andExpect(jsonPath("$.items[0].content").value(ForumService.DELETED_BY_ADMIN))
                .andExpect(jsonPath("$.items[0].canEdit").value(false));
    }

    @Test
    void paginationDesMessages() throws Exception {
        Long eventId = createEvent("Forum paginé", 4);
        String token = participant(eventId);
        for (int i = 1; i <= 3; i++) {
            postMessage(eventId, token, "Message numéro " + i);
        }

        mvc.perform(get("/api/events/" + eventId + "/forum?page=0&size=2").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items[0].content").value("Message numéro 3"));

        mvc.perform(get("/api/events/" + eventId + "/forum?page=1&size=2").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].content").value("Message numéro 1"));
    }

    // ---------- Outils ----------

    /** Nouveau compte inscrit à la sortie. */
    private String participant(Long eventId) throws Exception {
        String token = newUserToken();
        mvc.perform(post("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)))
                .andExpect(status().isCreated());
        return token;
    }

    private long postMessage(Long eventId, String token, String content) throws Exception {
        String body = mvc.perform(post("/api/events/" + eventId + "/forum").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}
