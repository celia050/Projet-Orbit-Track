package com.orbittrack.backend.participation;

import java.time.Instant;

/**
 * Une sortie d'observation telle que l'affiche le site : le phénomène, le nombre d'inscrits
 * et, si l'utilisateur est connecté, s'il est inscrit.
 */
public record OutingResponse(
        Long eventId,
        String name,
        String type,
        Instant startAt,
        Instant endAt,
        String status,
        String visibilityZone,
        long participantCount,
        boolean participating
) {

    static OutingResponse of(EventInfo event, long participantCount, boolean participating) {
        return new OutingResponse(event.id(), event.name(), event.type(), event.startAt(), event.endAt(),
                event.status(), event.visibilityZone(), participantCount, participating);
    }
}
