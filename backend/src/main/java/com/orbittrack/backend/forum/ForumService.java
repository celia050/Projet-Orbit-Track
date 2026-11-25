package com.orbittrack.backend.forum;

import com.orbittrack.backend.common.BusinessRuleException;
import com.orbittrack.backend.participation.ParticipationService;
import com.orbittrack.backend.user.AppUser;
import com.orbittrack.backend.user.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Feature 8 : discuter avec les autres participants.
 * <ul>
 *   <li>Le forum d'une sortie est réservé à ses participants inscrits (et aux administrateurs).</li>
 *   <li>Chacun ne modifie et ne supprime que ses propres messages.</li>
 *   <li>Un administrateur peut supprimer n'importe quel message (modération).</li>
 * </ul>
 */
@Service
public class ForumService {

    public static final String PARTICIPANTS_ONLY = "Le forum est réservé aux participants de la sortie.";
    public static final String MESSAGE_NOT_FOUND = "Message introuvable.";
    public static final String NOT_AUTHOR = "Vous ne pouvez modifier que vos propres messages.";
    public static final String CANNOT_DELETE = "Vous ne pouvez supprimer que vos propres messages.";
    public static final String ALREADY_DELETED = "Ce message a été supprimé.";
    public static final String DELETED_BY_AUTHOR = "Message supprimé par son auteur.";
    public static final String DELETED_BY_ADMIN = "Message retiré par la modération.";

    static final int MAX_PAGE_SIZE = 50;

    private final ForumMessageRepository messages;
    private final ParticipationService participations;
    private final AppUserRepository users;
    private final AntiSpamGuard antiSpam;

    public ForumService(ForumMessageRepository messages, ParticipationService participations,
                        AppUserRepository users, AntiSpamGuard antiSpam) {
        this.messages = messages;
        this.participations = participations;
        this.users = users;
        this.antiSpam = antiSpam;
    }

    @Transactional(readOnly = true)
    public ForumPage list(Long eventId, Long userId, boolean admin, int page, int size) {
        requireAccess(eventId, userId, admin);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        Page<ForumMessage> result = messages.findByEventIdOrderByCreatedAtDescIdDesc(eventId,
                PageRequest.of(Math.max(page, 0), safeSize));

        Set<Long> authorIds = result.stream().map(ForumMessage::getAuthorId).collect(Collectors.toSet());
        Map<Long, String> names = users.findAllById(authorIds).stream()
                .collect(Collectors.toMap(AppUser::getId, AppUser::getDisplayName));
        Function<ForumMessage, ForumMessageResponse> toResponse =
                m -> toResponse(m, names.getOrDefault(m.getAuthorId(), "Ancien membre"), userId, admin);

        return new ForumPage(result.map(toResponse).getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public ForumMessageResponse post(Long eventId, Long userId, boolean admin, String content) {
        requireAccess(eventId, userId, admin);
        String text = content.strip();
        antiSpam.checkNewMessage(eventId, userId, text);
        ForumMessage message = messages.save(new ForumMessage(eventId, userId, text));
        return toResponse(message, displayName(userId), userId, admin);
    }

    @Transactional
    public ForumMessageResponse edit(Long messageId, Long userId, boolean admin, String content) {
        ForumMessage message = requireMessage(messageId);
        if (!message.getAuthorId().equals(userId)) {
            throw new BusinessRuleException(HttpStatus.FORBIDDEN, NOT_AUTHOR);
        }
        if (message.isDeleted()) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, ALREADY_DELETED);
        }
        String text = content.strip();
        antiSpam.checkLinks(text);
        message.edit(text);
        return toResponse(message, displayName(userId), userId, admin);
    }

    @Transactional
    public void delete(Long messageId, Long userId, boolean admin) {
        ForumMessage message = requireMessage(messageId);
        boolean author = message.getAuthorId().equals(userId);
        if (!author && !admin) {
            throw new BusinessRuleException(HttpStatus.FORBIDDEN, CANNOT_DELETE);
        }
        if (message.isDeleted()) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, ALREADY_DELETED);
        }
        message.delete(!author);
    }

    private void requireAccess(Long eventId, Long userId, boolean admin) {
        participations.requireEvent(eventId);
        if (!admin && !participations.isParticipant(eventId, userId)) {
            throw new BusinessRuleException(HttpStatus.FORBIDDEN, PARTICIPANTS_ONLY);
        }
    }

    private ForumMessage requireMessage(Long messageId) {
        return messages.findById(messageId)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.NOT_FOUND, MESSAGE_NOT_FOUND));
    }

    private String displayName(Long userId) {
        return users.findById(userId).map(AppUser::getDisplayName).orElse("Ancien membre");
    }

    private static ForumMessageResponse toResponse(ForumMessage m, String authorName, Long userId, boolean admin) {
        boolean mine = m.getAuthorId().equals(userId);
        boolean deleted = m.isDeleted();
        String content = !deleted ? m.getContent() : m.isDeletedByAdmin() ? DELETED_BY_ADMIN : DELETED_BY_AUTHOR;
        return new ForumMessageResponse(m.getId(), m.getEventId(), m.getAuthorId(), authorName, content,
                m.getCreatedAt(), m.getEditedAt() != null, deleted, mine && !deleted, (mine || admin) && !deleted);
    }
}
