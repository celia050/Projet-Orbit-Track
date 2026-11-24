
package com.orbittrack.backend.place;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PlaceAdminService {

    private final ObservationPlaceRepository repository;

    public PlaceAdminService(ObservationPlaceRepository repository) {
        this.repository = repository;
    }

    public ObservationPlace create(PlaceRequest request) {
        validate(request);

        ObservationPlace place = new ObservationPlace();
        apply(place, request);

        return repository.save(place);
    }

    public ObservationPlace update(Long id, PlaceRequest request) {
        validate(request);

        ObservationPlace place = find(id);
        apply(place, request);

        return repository.save(place);
    }

    public ObservationPlace deactivate(Long id) {
        ObservationPlace place = find(id);
        place.setActive(false);
        return repository.save(place);
    }

    public ObservationPlace activate(Long id) {
        ObservationPlace place = find(id);
        place.setActive(true);
        return repository.save(place);
    }

    private ObservationPlace find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lieu introuvable"
                ));
    }

    private void apply(ObservationPlace place, PlaceRequest request) {
        place.setName(request.name().trim());
        place.setDescription(request.description());
        place.setLatitude(request.latitude());
        place.setLongitude(request.longitude());
        place.setLightPollutionLevel(request.lightPollutionLevel());
        place.setAccessibilityScore(request.accessibilityScore());
    }

    private void validate(PlaceRequest request) {
        if (request == null) {
            badRequest("Corps de requête obligatoire");
        }

        if (request.name() == null
                || request.name().isBlank()
                || request.name().length() > 150) {
            badRequest("Nom invalide");
        }

        if (request.latitude() == null
                || request.longitude() == null
                || !Double.isFinite(request.latitude())
                || !Double.isFinite(request.longitude())
                || request.latitude() < -90
                || request.latitude() > 90
                || request.longitude() < -180
                || request.longitude() > 180) {
            badRequest("Coordonnées invalides");
        }

        if (request.lightPollutionLevel() == null
                || request.lightPollutionLevel() < 1
                || request.lightPollutionLevel() > 5) {
            badRequest("Pollution lumineuse : valeur de 1 à 5");
        }

        if (request.accessibilityScore() == null
                || request.accessibilityScore() < 1
                || request.accessibilityScore() > 5) {
            badRequest("Accessibilité : valeur de 1 à 5");
        }
    }

    private void badRequest(String message) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, message
        );
    }
}
