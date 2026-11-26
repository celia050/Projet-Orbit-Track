
package com.orbittrack.backend.event;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final CalendarService calendarService;

    public EventController(EventService eventService, CalendarService calendarService) {
        this.eventService = eventService;
        this.calendarService = calendarService;
    }

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
                type, status, from, to, lat, lon, radiusKm
        );
    }

    @GetMapping("/search")
    public EventPageResponse searchEvents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return eventService.search(
                type, status, from, to,
                lat, lon, radiusKm, page, size
        );
    }

    @GetMapping("/{id}/calendar")
    public ResponseEntity<String> downloadCalendar(@PathVariable Long id) {
        EventResponse event = eventService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Événement introuvable"));

        if ("CANCELLED".equals(event.status())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cet événement est annulé");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=orbittrack-" + id + ".ics")
                .body(calendarService.generate(event));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id) {

        return eventService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }
}
