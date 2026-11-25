
package com.orbittrack.backend.place;

public record PlaceResponse(
        Long id,
        String name,
        String description,
        Double latitude,
        Double longitude,
        Integer lightPollutionLevel,
        Integer accessibilityScore,
        Double distanceKm,
        Double score
) {
}
