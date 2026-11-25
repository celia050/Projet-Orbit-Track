
package com.orbittrack.backend.event;

import java.time.Instant;

/**
 * Données d'un phénomène astronomique exposées par l'API.
 * Contrat utilisé par le front-end et les autres modules.
 */
public record EventResponse(
        Long id,
        String name,
        String type,
        String description,
        Instant startAt,
        Instant endAt,
        String visibilityZone,
        Double latitude,
        Double longitude,
        Double visibilityRadiusKm,
        String status,
        String sourceName,
        String sourceUrl,
        String sourceEventId,
        Instant sourceUpdatedAt,
        boolean demo,
        Instant createdAt,
        Instant updatedAt
) {

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getType(),
                event.getDescription(),
                event.getStartAt(),
                event.getEndAt(),
                event.getVisibilityZone(),
                event.getLatitude(),
                event.getLongitude(),
                event.getVisibilityRadiusKm(),
                event.getStatus(),
                event.getSourceName(),
                event.getSourceUrl(),
                event.getSourceEventId(),
                event.getSourceUpdatedAt(),
                event.isDemo(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
