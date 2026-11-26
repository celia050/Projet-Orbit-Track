package com.orbittrack.backend.event;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalendarControllerTest {

    private final EventService service = mock(EventService.class);

    private final EventController controller =
            new EventController(service, new CalendarService());

    @Test
    void telechargeLeCalendrier() {
        EventResponse event = mock(EventResponse.class);

        when(event.id()).thenReturn(1L);
        when(event.name()).thenReturn("Éclipse lunaire");
        when(event.status()).thenReturn("SCHEDULED");
        when(event.startAt()).thenReturn(
                Instant.parse("2027-01-15T20:00:00Z"));

        when(service.findById(1L)).thenReturn(Optional.of(event));

        ResponseEntity<String> result = controller.downloadCalendar(1L);

        assertEquals(200, result.getStatusCode().value());

        assertEquals(
                "attachment; filename=orbittrack-1.ics",
                result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));

        assertEquals(
                "text/calendar",
                result.getHeaders().getContentType().getType()
                        + "/" + result.getHeaders().getContentType().getSubtype());

        assertNotNull(result.getBody());
        assertTrue(result.getBody().contains("BEGIN:VCALENDAR"));
        assertTrue(result.getBody().contains("SUMMARY:Éclipse lunaire"));
    }

    @Test
    void refuseUnEvenementInexistant() {
        when(service.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.downloadCalendar(999L));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void refuseUnEvenementAnnule() {
        EventResponse event = mock(EventResponse.class);
        when(event.status()).thenReturn("CANCELLED");

        when(service.findById(1L)).thenReturn(Optional.of(event));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.downloadCalendar(1L));

        assertEquals(409, exception.getStatusCode().value());
    }
}
