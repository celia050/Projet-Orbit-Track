package com.orbittrack.backend.participation;

/** État de l'inscription pour un phénomène : nombre d'inscrits et « suis-je inscrit ? ». */
public record ParticipationResponse(Long eventId, long participantCount, boolean participating) {
}
