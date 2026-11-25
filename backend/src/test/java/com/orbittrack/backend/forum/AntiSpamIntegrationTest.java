package com.orbittrack.backend.forum;

import com.orbittrack.backend.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Feature 9 : empêcher le spam dans les forums. */
class AntiSpamIntegrationTest extends IntegrationTestSupport {

    @Test
    void auDelaDe5MessagesParMinuteLeMessageEstRefuse() throws Exception {
        Long eventId = createEvent("Anti-spam débit", 4);
        String token = participant(eventId);

        for (int i = 1; i <= 5; i++) {
            send(eventId, token, "Message " + i).andExpect(status().isCreated());
        }
        send(eventId, token, "Message 6")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail").value(AntiSpamGuard.TOO_MANY_MESSAGES));

        // La limite est par membre : un autre participant peut toujours écrire.
        send(eventId, participant(eventId), "Je peux encore écrire").andExpect(status().isCreated());
    }

    @Test
    void leMemeMessageDeuxFoisEstRefuse() throws Exception {
        Long eventId = createEvent("Anti-spam doublon", 4);
        String token = participant(eventId);

        send(eventId, token, "Qui vient ce soir ?").andExpect(status().isCreated());
        send(eventId, token, "  QUI VIENT CE SOIR ?  ")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(AntiSpamGuard.DUPLICATE_MESSAGE));
    }

    @Test
    void tropDeLiensRefuse() throws Exception {
        Long eventId = createEvent("Anti-spam liens", 4);
        String token = participant(eventId);

        send(eventId, token, "https://a.fr https://b.fr www.c.fr").andExpect(status().isCreated());
        send(eventId, token, "https://a.fr https://b.fr www.c.fr http://d.fr")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(AntiSpamGuard.TOO_MANY_LINKS));
    }

    private ResultActions send(Long eventId, String token, String content)
            throws Exception {
        return mvc.perform(post("/api/events/" + eventId + "/forum").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"" + content + "\"}"));
    }

    private String participant(Long eventId) throws Exception {
        String token = newUserToken();
        mvc.perform(post("/api/events/" + eventId + "/participation").header("Authorization", bearer(token)))
                .andExpect(status().isCreated());
        return token;
    }
}
