package com.orbittrack.backend.notification;

import com.orbittrack.backend.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Mes notifications (sous /api/me : il faut être connecté, même en lecture). */
@RestController
@RequestMapping("/api/me/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    /** Les 50 dernières notifications, les plus récentes d'abord. */
    @GetMapping
    public List<NotificationResponse> latest(@AuthenticationPrincipal Jwt jwt) {
        return service.latest(CurrentUser.id(jwt));
    }

    /** Nombre de notifications non lues (pastille dans le menu). */
    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("count", service.unreadCount(CurrentUser.id(jwt)));
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.markRead(id, CurrentUser.id(jwt));
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal Jwt jwt) {
        service.markAllRead(CurrentUser.id(jwt));
    }
}
