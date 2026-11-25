package com.orbittrack.backend.alert;

import java.time.Instant;
import java.time.LocalDate;

public record AlertResponse(
        Long id,
        String name,
        String eventType,
        String keyword,
        LocalDate dateFrom,
        LocalDate dateTo,
        Double latitude,
        Double longitude,
        Double radiusKm,
        boolean active,
        Instant createdAt
) {

    static AlertResponse from(Alert a) {
        return new AlertResponse(a.getId(), a.getName(), a.getEventType(), a.getKeyword(), a.getDateFrom(),
                a.getDateTo(), a.getLatitude(), a.getLongitude(), a.getRadiusKm(), a.isActive(), a.getCreatedAt());
    }
}
