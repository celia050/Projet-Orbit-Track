package com.orbittrack.backend.notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long eventId,
        NotificationType type,
        String message,
        Instant createdAt,
        boolean read
) {

    static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getEventId(), n.getType(), n.getMessage(), n.getCreatedAt(),
                n.getReadAt() != null);
    }
}
