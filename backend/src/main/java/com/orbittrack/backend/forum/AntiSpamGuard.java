package com.orbittrack.backend.forum;

import com.orbittrack.backend.common.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feature 9 : empêcher le spam dans les forums. Vérifié avant chaque nouveau message :
 * <ul>
 *   <li>pas plus de N messages par minute et par membre, tous forums confondus (429) ;</li>
 *   <li>pas deux fois le même message dans le même forum en peu de temps (409) ;</li>
 *   <li>pas plus de 3 liens par message (400), aussi vérifié quand on modifie un message.</li>
 * </ul>
 * Les compteurs viennent de la base (table forum_message) : ils restent justes même si le serveur redémarre
 * ou s'il y a plusieurs serveurs. Les limites se règlent dans application.yaml (orbittrack.forum.*).
 */
@Component
public class AntiSpamGuard {

    public static final String TOO_MANY_MESSAGES =
            "Vous envoyez trop de messages. Attendez une minute avant de réessayer.";
    public static final String DUPLICATE_MESSAGE = "Vous avez déjà envoyé ce message dans ce forum.";
    public static final String TOO_MANY_LINKS = "Un message ne peut pas contenir plus de 3 liens.";

    static final int MAX_LINKS = 3;
    private static final Pattern LINK = Pattern.compile("(https?://|www\\.)", Pattern.CASE_INSENSITIVE);

    private final ForumMessageRepository messages;
    private final int maxMessagesPerMinute;
    private final Duration duplicateWindow;

    public AntiSpamGuard(ForumMessageRepository messages,
                         @Value("${orbittrack.forum.max-messages-per-minute:5}") int maxMessagesPerMinute,
                         @Value("${orbittrack.forum.duplicate-window:PT10M}") Duration duplicateWindow) {
        this.messages = messages;
        this.maxMessagesPerMinute = maxMessagesPerMinute;
        this.duplicateWindow = duplicateWindow;
    }

    /** À appeler avant d'enregistrer un nouveau message. */
    public void checkNewMessage(Long eventId, Long authorId, String content) {
        checkLinks(content);
        Instant now = Instant.now();
        if (messages.countByAuthorIdAndCreatedAtAfter(authorId, now.minus(Duration.ofMinutes(1)))
                >= maxMessagesPerMinute) {
            throw new BusinessRuleException(HttpStatus.TOO_MANY_REQUESTS, TOO_MANY_MESSAGES);
        }
        if (messages.existsByAuthorIdAndEventIdAndContentIgnoreCaseAndDeletedAtIsNullAndCreatedAtAfter(
                authorId, eventId, content, now.minus(duplicateWindow))) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, DUPLICATE_MESSAGE);
        }
    }

    /** Limite le nombre de liens (aussi à la modification d'un message). */
    public void checkLinks(String content) {
        Matcher matcher = LINK.matcher(content);
        int links = 0;
        while (matcher.find()) {
            links++;
        }
        if (links > MAX_LINKS) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, TOO_MANY_LINKS);
        }
    }
}
