
package com.orbittrack.backend.place;

import com.orbittrack.backend.event.Event;
import com.orbittrack.backend.event.EventRepository;
import com.orbittrack.backend.event.GeoUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlaceService {

    private final ObservationPlaceRepository placeRepository;
    private final EventRepository eventRepository;

    public PlaceService(
            ObservationPlaceRepository placeRepository,
            EventRepository eventRepository) {
        this.placeRepository = placeRepository;
        this.eventRepository = eventRepository;
    }

    public List<PlaceResponse> findAll() {
        return placeRepository.findByActiveTrue()
                .stream()
                .map(place -> toResponse(place, null))
                .sorted(Comparator.comparing(PlaceResponse::name))
                .toList();
    }

    public List<PlaceResponse> recommend(
            Long eventId,
            Double lat,
            Double lon,
            Double maxDistanceKm,
            int limit) {

        validateLocation(lat, lon);

        if (limit < 1 || limit > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "limit doit être compris entre 1 et 100"
            );
        }

        if (maxDistanceKm == null) {
            maxDistanceKm = 200.0;
        }

        if (!Double.isFinite(maxDistanceKm)
                || maxDistanceKm <= 0
                || maxDistanceKm > 20000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "maxDistanceKm invalide"
            );
        }

        final double maxDistance = maxDistanceKm;

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Événement introuvable"
                ));

        if (event.getLatitude() == null
                || event.getLongitude() == null
                || event.getVisibilityRadiusKm() == null) {
            return List.of();
        }

        return placeRepository.findByActiveTrue()
                .stream()
                .map(place -> {
                    double distance = GeoUtils.distanceKm(
                            lat, lon,
                            place.getLatitude(),
                            place.getLongitude()
                    );
                    return toResponse(place, distance);
                })
                .filter(place ->
                        place.distanceKm() <= maxDistance)
                .filter(place ->
                        GeoUtils.distanceKm(
                                event.getLatitude(),
                                event.getLongitude(),
                                place.latitude(),
                                place.longitude()
                        ) <= event.getVisibilityRadiusKm())
                .sorted(
                        Comparator.comparingDouble(
                                PlaceResponse::score
                        ).reversed()
                        .thenComparingDouble(
                                PlaceResponse::distanceKm
                        )
                )
                .limit(limit)
                .toList();
    }

    private void validateLocation(Double lat, Double lon) {
        if (lat == null || lon == null
                || !Double.isFinite(lat)
                || !Double.isFinite(lon)
                || lat < -90 || lat > 90
                || lon < -180 || lon > 180) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coordonnées invalides"
            );
        }
    }

    private PlaceResponse toResponse(
            ObservationPlace place,
            Double distanceKm) {

        double score =
                (6 - place.getLightPollutionLevel()) * 15.0
                + place.getAccessibilityScore() * 5.0;

        if (distanceKm != null) {
            score = Math.max(0, score
                    - Math.min(distanceKm / 20.0, 25.0));
        }

        score = Math.round(score * 100.0) / 100.0;

        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getDescription(),
                place.getLatitude(),
                place.getLongitude(),
                place.getLightPollutionLevel(),
                place.getAccessibilityScore(),
                distanceKm == null
                        ? null
                        : Math.round(distanceKm * 100.0) / 100.0,
                score
        );
    }
}
