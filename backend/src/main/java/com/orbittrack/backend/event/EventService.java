
package com.orbittrack.backend.event;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class EventService {

    private static final Set<String> STATUSES =
            Set.of("SCHEDULED", "UPDATED", "CANCELLED");

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventResponse> findAll(
            String type, String status,
            Instant from, Instant to,
            Double lat, Double lon, Double radiusKm) {

        validateFilters(type, status, from, to, lat, lon, radiusKm);

        Specification<Event> spec =
                (root, query, cb) -> cb.conjunction();

        if (type != null && !type.isBlank()) {
            String value = type.trim().toUpperCase();
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("type"), value));
        }

        if (status != null && !status.isBlank()) {
            String value = status.trim().toUpperCase();
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), value));
        }

        if (from != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("startAt"), from));
        }

        if (to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("startAt"), to));
        }

        return eventRepository.findAll(
                spec, Sort.by("startAt").ascending()
        ).stream()
         .filter(event -> matchesLocation(event, lat, lon, radiusKm))
         .map(EventResponse::from)
         .toList();
    }

    public EventPageResponse search(
            String type, String status,
            Instant from, Instant to,
            Double lat, Double lon, Double radiusKm,
            int page, int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "page >= 0 et size entre 1 et 100 requis"
            );
        }

        List<EventResponse> results =
                findAll(type, status, from, to, lat, lon, radiusKm);

        long total = results.size();
        long offset = (long) page * size;

        int start = (int) Math.min(offset, total);
        int end = (int) Math.min(offset + size, total);

        int totalPages = (int) ((total + size - 1) / size);

        return new EventPageResponse(
                results.subList(start, end),
                page,
                size,
                total,
                totalPages
        );
    }

    public Optional<EventResponse> findById(Long id) {
        return eventRepository.findById(id)
                .map(EventResponse::from);
    }

    private void validateFilters(
            String type, String status,
            Instant from, Instant to,
            Double lat, Double lon, Double radiusKm) {

        if (from != null && to != null && from.isAfter(to)) {
            badRequest("Période invalide");
        }

        if (type != null && type.length() > 40) {
            badRequest("Type invalide");
        }

        if (status != null && !status.isBlank()
                && !STATUSES.contains(status.trim().toUpperCase())) {
            badRequest("Statut invalide");
        }

        if ((lat == null) != (lon == null)) {
            badRequest("Latitude et longitude requises ensemble");
        }

        if (lat != null &&
                (!Double.isFinite(lat) || lat < -90 || lat > 90
                || !Double.isFinite(lon) || lon < -180 || lon > 180)) {
            badRequest("Coordonnées invalides");
        }

        if (radiusKm != null) {
            if (!Double.isFinite(radiusKm)
                    || radiusKm <= 0 || radiusKm > 1000) {
                badRequest("Rayon invalide");
            }

            if (lat == null) {
                badRequest("Coordonnées requises avec radiusKm");
            }
        }
    }

    private boolean matchesLocation(
            Event event, Double lat, Double lon, Double radiusKm) {

        if (lat == null) return true;

        if (event.getLatitude() == null
                || event.getLongitude() == null
                || event.getVisibilityRadiusKm() == null) {
            return false;
        }

        double distance = GeoUtils.distanceKm(
                lat, lon,
                event.getLatitude(),
                event.getLongitude()
        );

        return distance <= event.getVisibilityRadiusKm()
                + (radiusKm == null ? 0 : radiusKm);
    }

    private void badRequest(String message) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, message
        );
    }
}
