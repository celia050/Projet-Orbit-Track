package com.orbittrack.backend.participation;

import com.orbittrack.backend.notification.NotificationService;
import com.orbittrack.backend.notification.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Feature 10 : être prévenu d'un changement.
 * Compare régulièrement chaque inscription active avec le phénomène dans la table events.
 * Si l'horaire a changé ou si la sortie est annulée, le participant reçoit une notification,
 * une seule fois par changement.
 */
@Service
public class EventChangeDetector {

    private static final Logger log = LoggerFactory.getLogger(EventChangeDetector.class);

    private final ParticipationRepository participations;
    private final EventLookup events;
    private final NotificationService notifications;

    public EventChangeDetector(ParticipationRepository participations, EventLookup events,
                               NotificationService notifications) {
        this.participations = participations;
        this.events = events;
        this.notifications = notifications;
    }

    @Scheduled(initialDelayString = "${orbittrack.notifications.check-delay:PT1M}",
            fixedDelayString = "${orbittrack.notifications.check-delay:PT1M}")
    public void scheduledCheck() {
        int sent = detectChanges();
        if (sent > 0) {
            log.info("{} participant(s) prévenu(s) d'un changement de sortie", sent);
        }
    }

    /** Renvoie le nombre de notifications envoyées. */
    @Transactional
    public int detectChanges() {
        Map<Long, Optional<EventInfo>> cache = new HashMap<>();
        int sent = 0;
        for (Participation participation : participations.findByStatus(ParticipationStatus.ACTIVE)) {
            Optional<EventInfo> found = cache.computeIfAbsent(participation.getEventId(), events::find);
            if (found.isEmpty()) {
                continue;
            }
            EventInfo event = found.get();
            if (!participation.knows(event)) {
                if (participation.hasSnapshot()) {
                    sendNotification(participation, event);
                    sent++;
                }
                participation.rememberEvent(event);
            }
        }
        return sent;
    }

    private void sendNotification(Participation participation, EventInfo event) {
        if (event.isCancelled()) {
            if (!Objects.equals(participation.getKnownStatus(), EventInfo.CANCELLED)) {
                notifications.notify(participation.getUserId(), event.id(), NotificationType.EVENT_CANCELLED,
                        "La sortie « %s » est annulée.".formatted(event.name()));
            }
            return;
        }
        String when = NotificationService.formatParis(event.startAt());
        String message = Objects.equals(participation.getKnownStatus(), EventInfo.CANCELLED)
                ? "La sortie « %s » est de nouveau prévue : %s.".formatted(event.name(), when)
                : "Nouvel horaire pour « %s » : %s.".formatted(event.name(), when);
        notifications.notify(participation.getUserId(), event.id(), NotificationType.EVENT_CHANGED, message);
    }
}
