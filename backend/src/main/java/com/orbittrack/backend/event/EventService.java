
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

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Recherche les événements avec filtres combinables.
     *
     * La recherche géographique utilise les zones circulaires
     * simplifiées enregistrées en base de données.
     */
    public List<EventResponse> findAll(
            String type,
            String status,
            Instant from,
            Instant to,
            Double lat,
            Double lon,
            Double radiusKm) {

        validateFilters(from, to, lat, lon, radiusKm);

        Specification<Event> spec =
                (root, query, cb) -> cb.conjunction();

        if (type != null && !type.isBlank()) {
            String normalizedType = type.trim().toUpperCase();
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("type"), normalizedType));
        }

        if (status != null && !status.isBlank()) {
            String normalizedStatus = status.trim().toUpperCase();
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), normalizedStatus));
        }

        if (from != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(
                            root.get("startAt"), from));
        }

        if (to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(
                            root.get("startAt"), to));
        }

        List<Event> events = eventRepository.findAll(
                spec,
                Sort.by("startAt").ascending()
        );

        return events.stream()
                .filter(event ->
                        matchesLocation(event, lat, lon, radiusKm))
                .map(EventResponse::from)
                .toList();
    }

    /**
     * Retourne le détail d'un phénomène.
     */
    public Optional<EventResponse> findById(Long id) {
        return eventRepository.findById(id)
                .map(EventResponse::from);
    }

    /**
     * Vérifie les paramètres de recherche.
     */
    private void validateFilters(
            Instant from,
            Instant to,
            Double lat,
            Double lon,
            Double radiusKm) {

        if (from != null && to != null && from.isAfter(to)) {
            badRequest("La date de début doit précéder la date de fin");
        }

        if ((lat == null) != (lon == null)) {
            badRequest("Latitude et longitude doivent être fournies ensemble");
        }

        if (lat != null) {
            if (!Double.isFinite(lat) || lat < -90 || lat > 90
                    || !Double.isFinite(lon)
                    || lon < -180 || lon > 180) {
                badRequest("Coordonnées géographiques invalides");
            }
        }

        if (radiusKm != null) {
            if (!Double.isFinite(radiusKm)
                    || radiusKm <= 0
                    || radiusKm > 1000) {
                badRequest("Le rayon doit être compris entre 0 et 1000 km");
            }

            if (lat == null) {
                badRequest("Les coordonnées sont obligatoires avec radiusKm");
            }
        }
    }

    /**
     * Vérifie si la zone recherchée intersecte
     * la zone de visibilité simplifiée de l'événement.
     */
    private boolean matchesLocation(
            Event event,
            Double lat,
            Double lon,
            Double radiusKm) {

        if (lat == null) {
            return true;
        }

        if (event.getLatitude() == null
                || event.getLongitude() == null
                || event.getVisibilityRadiusKm() == null) {
            return false;
        }

        double distance = GeoUtils.distanceKm(
                lat,
                lon,
                event.getLatitude(),
                event.getLongitude()
        );

        double searchRadius = radiusKm == null ? 0.0 : radiusKm;

        return distance <=
                event.getVisibilityRadiusKm() + searchRadius;
    }

    private void badRequest(String message) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }
}
