
package com.orbittrack.backend.place;

public record PlaceRequest(
        String name,
        String description,
        Double latitude,
        Double longitude,
        Integer lightPollutionLevel,
        Integer accessibilityScore
) {
}
