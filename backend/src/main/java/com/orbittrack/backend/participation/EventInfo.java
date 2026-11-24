package com.orbittrack.backend.participation;

import java.time.Instant;

/**
 * Ce dont les sorties, le forum et les alertes ont besoin d'un phénomène (table {@code events}, feature 1).
 * Lecture seule : les phénomènes sont créés et modifiés par la partie de Ritej.
 */
public record EventInfo(
        Long id,
        String name,
        String type,
        String description,
        Instant startAt,
        Instant endAt,
        String status,
        String visibilityZone,
        Double latitude,
        Double longitude,
        Double visibilityRadiusKm
) {

    public static final String CANCELLED = "CANCELLED";

    public boolean isCancelled() {
        return CANCELLED.equals(status);
    }

    /** Un phénomène est passé quand il est terminé. */
    public boolean isOver(Instant now) {
        return !endAt.isAfter(now);
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}
