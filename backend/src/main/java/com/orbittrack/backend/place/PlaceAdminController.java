
package com.orbittrack.backend.place;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/places")
public class PlaceAdminController {

    private final PlaceAdminService service;

    public PlaceAdminController(PlaceAdminService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceResponse create(
            @RequestBody PlaceRequest request,
            Authentication authentication) {

        requireAdmin(authentication);
        return toResponse(service.create(request));
    }

    @PutMapping("/{id}")
    public PlaceResponse update(
            @PathVariable Long id,
            @RequestBody PlaceRequest request,
            Authentication authentication) {

        requireAdmin(authentication);
        return toResponse(service.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public PlaceResponse deactivate(
            @PathVariable Long id,
            Authentication authentication) {

        requireAdmin(authentication);
        return toResponse(service.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    public PlaceResponse activate(
            @PathVariable Long id,
            Authentication authentication) {

        requireAdmin(authentication);
        return toResponse(service.activate(id));
    }

    private void requireAdmin(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream()
                    .noneMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ADMIN"))) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Accès réservé aux administrateurs"
            );
        }
    }

    private PlaceResponse toResponse(ObservationPlace place) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getDescription(),
                place.getLatitude(),
                place.getLongitude(),
                place.getLightPollutionLevel(),
                place.getAccessibilityScore(),
                null,
                null
        );
    }
}
