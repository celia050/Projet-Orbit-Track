package com.orbittrack.backend.participation;

import com.orbittrack.backend.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Feature 7 : s'inscrire à une sortie d'observation. */
@RestController
public class ParticipationController {

    private final ParticipationService service;

    public ParticipationController(ParticipationService service) {
        this.service = service;
    }

    /** Liste des sorties à venir : libre, sans compte. */
    @GetMapping("/api/outings")
    public List<OutingResponse> outings(@AuthenticationPrincipal Jwt jwt) {
        return service.outings(userIdOrNull(jwt));
    }

    /** Nombre d'inscrits et « suis-je inscrit ? » : libre, sans compte. */
    @GetMapping("/api/events/{eventId}/participation")
    public ParticipationResponse status(@PathVariable Long eventId, @AuthenticationPrincipal Jwt jwt) {
        return service.status(eventId, userIdOrNull(jwt));
    }

    @PostMapping("/api/events/{eventId}/participation")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationResponse join(@PathVariable Long eventId, @AuthenticationPrincipal Jwt jwt) {
        return service.join(eventId, CurrentUser.id(jwt));
    }

    @DeleteMapping("/api/events/{eventId}/participation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable Long eventId, @AuthenticationPrincipal Jwt jwt) {
        service.leave(eventId, CurrentUser.id(jwt));
    }

    @GetMapping("/api/me/participations")
    public List<OutingResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return service.myOutings(CurrentUser.id(jwt));
    }

    private static Long userIdOrNull(Jwt jwt) {
        return jwt == null ? null : CurrentUser.id(jwt);
    }
}
