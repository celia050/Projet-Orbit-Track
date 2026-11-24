
package com.orbittrack.backend.event;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Liste publique des événements astronomiques.
     *
     * Filtres disponibles :
     * type, status, from, to, lat, lon, radiusKm.
     */
    @GetMapping
    public List<EventResponse> getEvents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double radiusKm) {

        return eventService.findAll(
                type,
                status,
                from,
                to,
                lat,
                lon,
                radiusKm
        );
    }

    /**
     * Fiche détaillée d'un événement.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id) {

        return eventService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }
}
