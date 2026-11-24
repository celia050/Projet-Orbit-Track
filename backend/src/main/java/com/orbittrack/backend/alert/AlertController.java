package com.orbittrack.backend.alert;

import com.orbittrack.backend.participation.EventLookup;
import com.orbittrack.backend.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Feature 11 : mes alertes (sous /api/me : il faut être connecté). */
@RestController
public class AlertController {

    /** Activer ou mettre en pause une alerte. */
    public record AlertActiveRequest(@NotNull(message = "Indiquez si l'alerte est active.") Boolean active) {
    }

    private final AlertService service;
    private final EventLookup events;

    public AlertController(AlertService service, EventLookup events) {
        this.service = service;
        this.events = events;
    }

    @GetMapping("/api/me/alerts")
    public List<AlertResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(CurrentUser.id(jwt));
    }

    @PostMapping("/api/me/alerts")
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse create(@Valid @RequestBody AlertRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.create(CurrentUser.id(jwt), request);
    }

    @PatchMapping("/api/me/alerts/{id}")
    public AlertResponse setActive(@PathVariable Long id, @Valid @RequestBody AlertActiveRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.setActive(id, CurrentUser.id(jwt), request.active());
    }

    @DeleteMapping("/api/me/alerts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.delete(id, CurrentUser.id(jwt));
    }

    /** Types de phénomènes connus, pour la liste déroulante du formulaire : libre, sans compte. */
    @GetMapping("/api/outings/event-types")
    public List<String> eventTypes() {
        return events.findTypes();
    }
}
