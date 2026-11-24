package com.orbittrack.backend.alert;

import com.orbittrack.backend.common.BusinessRuleException;
import com.orbittrack.backend.notification.NotificationService;
import com.orbittrack.backend.participation.EventInfo;
import com.orbittrack.backend.participation.EventLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Feature 11 : créer une alerte.
 * <ul>
 *   <li>Il faut au moins un critère : type, mot-clé, période ou lieu.</li>
 *   <li>20 alertes au maximum par membre.</li>
 *   <li>Dès la création, puis régulièrement, chaque phénomène à venir qui correspond donne une notification,
 *       une seule fois par alerte et par phénomène.</li>
 *   <li>Une alerte mise en pause ne prévient plus.</li>
 * </ul>
 */
@Service
public class AlertService {

    public static final int MAX_ALERTS_PER_USER = 20;
    public static final String NO_CRITERIA = "Choisissez au moins un critère : type, mot-clé, période ou lieu.";
    public static final String INCOMPLETE_PLACE =
            "Pour une alerte par lieu, indiquez la latitude, la longitude et le rayon.";
    public static final String BAD_PERIOD = "La date de fin doit être après la date de début.";
    public static final String TOO_MANY_ALERTS = "Vous avez atteint le maximum de 20 alertes.";
    public static final String NOT_FOUND = "Alerte introuvable.";

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alerts;
    private final EventLookup events;
    private final NotificationService notifications;

    public AlertService(AlertRepository alerts, EventLookup events, NotificationService notifications) {
        this.alerts = alerts;
        this.events = events;
        this.notifications = notifications;
    }

    @Transactional
    public AlertResponse create(Long userId, AlertRequest request) {
        String type = blankToNull(request.eventType());
        String keyword = blankToNull(request.keyword());
        boolean anyPlace = request.latitude() != null || request.longitude() != null || request.radiusKm() != null;
        boolean fullPlace = request.latitude() != null && request.longitude() != null && request.radiusKm() != null;

        if (anyPlace && !fullPlace) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, INCOMPLETE_PLACE);
        }
        if (type == null && keyword == null && request.dateFrom() == null && request.dateTo() == null && !fullPlace) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, NO_CRITERIA);
        }
        if (request.dateFrom() != null && request.dateTo() != null && request.dateTo().isBefore(request.dateFrom())) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, BAD_PERIOD);
        }
        if (alerts.countByUserId(userId) >= MAX_ALERTS_PER_USER) {
            throw new BusinessRuleException(HttpStatus.CONFLICT, TOO_MANY_ALERTS);
        }

        Alert alert = alerts.save(new Alert(userId, request.name().strip(), type, keyword, request.dateFrom(),
                request.dateTo(), request.latitude(), request.longitude(), request.radiusKm()));
        scan(List.of(alert), events.findNotOver(Instant.now()));
        return AlertResponse.from(alert);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> list(Long userId) {
        return alerts.findByUserIdOrderByCreatedAtDescIdDesc(userId).stream().map(AlertResponse::from).toList();
    }

    @Transactional
    public AlertResponse setActive(Long alertId, Long userId, boolean active) {
        Alert alert = requireOwn(alertId, userId);
        alert.setActive(active);
        if (active) {
            scan(List.of(alert), events.findNotOver(Instant.now()));
        }
        return AlertResponse.from(alert);
    }

    @Transactional
    public void delete(Long alertId, Long userId) {
        alerts.delete(requireOwn(alertId, userId));
    }

    @Scheduled(initialDelayString = "${orbittrack.alerts.check-delay:PT5M}",
            fixedDelayString = "${orbittrack.alerts.check-delay:PT5M}")
    public void scheduledScan() {
        int sent = scanAll();
        if (sent > 0) {
            log.info("{} notification(s) d'alerte envoyée(s)", sent);
        }
    }

    /** Compare toutes les alertes actives avec les phénomènes à venir. Renvoie le nombre de notifications. */
    @Transactional
    public int scanAll() {
        return scan(alerts.findByActiveTrue(), events.findNotOver(Instant.now()));
    }

    private int scan(List<Alert> activeAlerts, List<EventInfo> upcoming) {
        int sent = 0;
        for (Alert alert : activeAlerts) {
            for (EventInfo event : upcoming) {
                if (AlertMatcher.matches(alert, event)
                        && notifications.notifyAlertMatch(alert.getUserId(), event.id(), alert.getId(),
                        message(alert, event))) {
                    sent++;
                }
            }
        }
        return sent;
    }

    private static String message(Alert alert, EventInfo event) {
        return "Alerte « %s » : « %s », %s, %s.".formatted(alert.getName(), event.name(),
                NotificationService.formatParis(event.startAt()), event.visibilityZone());
    }

    private Alert requireOwn(Long alertId, Long userId) {
        return alerts.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.NOT_FOUND, NOT_FOUND));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
