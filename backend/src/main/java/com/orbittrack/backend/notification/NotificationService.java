package com.orbittrack.backend.notification;

import com.orbittrack.backend.common.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Notifications dans le site : création (features 10 et 11), lecture, « marquer comme lu ». */
@Service
public class NotificationService {

    public static final String NOT_FOUND = "Notification introuvable.";

    /** Les horaires sont stockés en UTC ; dans le texte des notifications, on les écrit à l'heure de Paris. */
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
            .ofPattern("EEEE d MMMM yyyy 'à' HH'h'mm", Locale.FRENCH)
            .withZone(ZoneId.of("Europe/Paris"));

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional
    public Notification notify(Long userId, Long eventId, NotificationType type, String message) {
        return notifications.save(new Notification(userId, eventId, type, message));
    }

    /** Feature 11 : prévient d'un phénomène qui correspond à une alerte, une seule fois par phénomène. */
    @Transactional
    public boolean notifyAlertMatch(Long userId, Long eventId, Long alertId, String message) {
        if (notifications.existsByAlertIdAndEventId(alertId, eventId)) {
            return false;
        }
        notifications.save(new Notification(userId, eventId, alertId, NotificationType.ALERT_MATCH, message));
        return true;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> latest(Long userId) {
        return notifications.findTop50ByUserIdOrderByCreatedAtDescIdDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notifications.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public NotificationResponse markRead(Long notificationId, Long userId) {
        Notification notification = notifications.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.NOT_FOUND, NOT_FOUND));
        notification.markRead();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notifications.markAllRead(userId, Instant.now());
    }

    /** Ex. : « samedi 14 décembre 2026 à 02h00 (heure de Paris) ». */
    public static String formatParis(Instant instant) {
        return DATE_TIME.format(instant) + " (heure de Paris)";
    }
}
