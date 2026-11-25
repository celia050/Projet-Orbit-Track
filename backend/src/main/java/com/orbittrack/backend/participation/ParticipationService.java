package com.orbittrack.backend.participation;

import com.orbittrack.backend.common.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Feature 7 : s'inscrire à une sortie d'observation.
 * <ul>
 *   <li>Une seule participation active par utilisateur et par phénomène.</li>
 *   <li>Pas d'inscription à un phénomène annulé ou terminé.</li>
 *   <li>On peut se désinscrire puis se réinscrire.</li>
 * </ul>
 */
@Service
public class ParticipationService {

    public static final String EVENT_NOT_FOUND = "Phénomène introuvable.";
    public static final String EVENT_CANCELLED = "Ce phénomène a été annulé : impossible de s'inscrire.";
    public static final String EVENT_OVER = "Ce phénomène est terminé : impossible de s'inscrire.";
    public static final String ALREADY_JOINED = "Vous êtes déjà inscrit à cette sortie.";
    public static final String NOT_JOINED = "Vous n'êtes pas inscrit à cette sortie.";

    private final ParticipationRepository participations;
    private final EventLookup events;

    public ParticipationService(ParticipationRepository participations, EventLookup events) {
        this.participations = participations;
        this.events = events;
    }

    @Transactional
    public ParticipationResponse join(Long eventId, Long userId) {
        EventInfo event = requireEvent(eventId);
        if (event.isCancelled()) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, EVENT_CANCELLED);
        }
        if (event.isOver(Instant.now())) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, EVENT_OVER);
        }
        Participation participation = participations.findByEventIdAndUserId(eventId, userId)
                .orElse(null);
        if (participation == null) {
            participation = new Participation(eventId, userId);
        } else if (participation.isActive()) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, ALREADY_JOINED);
        } else {
            participation.activate();
        }
        participation.rememberEvent(event);
        participations.saveAndFlush(participation);
        return status(eventId, userId);
    }

    @Transactional
    public void leave(Long eventId, Long userId) {
        requireEvent(eventId);
        Participation participation = participations.findByEventIdAndUserId(eventId, userId)
                .filter(Participation::isActive)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.NOT_FOUND, NOT_JOINED));
        participation.cancel();
    }

    /** {@code userId} vaut null pour un visiteur sans compte. */
    @Transactional(readOnly = true)
    public ParticipationResponse status(Long eventId, Long userId) {
        requireEvent(eventId);
        long count = participations.countByEventIdAndStatus(eventId, ParticipationStatus.ACTIVE);
        return new ParticipationResponse(eventId, count, isParticipant(eventId, userId));
    }

    @Transactional(readOnly = true)
    public boolean isParticipant(Long eventId, Long userId) {
        return userId != null
                && participations.existsByEventIdAndUserIdAndStatus(eventId, userId, ParticipationStatus.ACTIVE);
    }

    /** Toutes les sorties pas encore terminées. {@code userId} vaut null pour un visiteur. */
    @Transactional(readOnly = true)
    public List<OutingResponse> outings(Long userId) {
        Map<Long, Long> counts = activeCounts();
        Set<Long> mine = userId == null ? Set.of() : myEventIds(userId);
        return events.findNotOver(Instant.now()).stream()
                .map(e -> OutingResponse.of(e, counts.getOrDefault(e.id(), 0L), mine.contains(e.id())))
                .toList();
    }

    /** Les sorties auxquelles l'utilisateur est inscrit, terminées comprises. */
    @Transactional(readOnly = true)
    public List<OutingResponse> myOutings(Long userId) {
        Map<Long, Long> counts = activeCounts();
        return myEventIds(userId).stream()
                .map(events::find)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(EventInfo::startAt))
                .map(e -> OutingResponse.of(e, counts.getOrDefault(e.id(), 0L), true))
                .toList();
    }

    public EventInfo requireEvent(Long eventId) {
        return events.find(eventId)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND));
    }

    private Set<Long> myEventIds(Long userId) {
        return participations.findByUserIdAndStatus(userId, ParticipationStatus.ACTIVE).stream()
                .map(Participation::getEventId)
                .collect(Collectors.toSet());
    }

    private Map<Long, Long> activeCounts() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : participations.countActiveByEvent()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }
}
