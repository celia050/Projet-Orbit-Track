package com.orbittrack.backend.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalendarServiceTest {

    private final CalendarService service = new CalendarService();

    private EventResponse createEvent() {
        EventResponse event = mock(EventResponse.class);

        when(event.id()).thenReturn(1L);
        when(event.name()).thenReturn("Éclipse lunaire");
        when(event.description()).thenReturn("Observation de la Lune");
        when(event.visibilityZone()).thenReturn("Paris");
        when(event.startAt()).thenReturn(
                Instant.parse("2027-01-15T20:00:00Z"));
        when(event.endAt()).thenReturn(
                Instant.parse("2027-01-15T22:00:00Z"));

        return event;
    }

    @Test
    void genereUnCalendrierValide() {
        String result = service.generate(createEvent());

        assertTrue(result.startsWith("BEGIN:VCALENDAR\r\n"));
        assertTrue(result.contains("VERSION:2.0\r\n"));
        assertTrue(result.contains("BEGIN:VEVENT\r\n"));
        assertTrue(result.contains("UID:event-1@orbittrack\r\n"));
        assertTrue(result.contains("DTSTAMP:"));
        assertTrue(result.endsWith("END:VCALENDAR\r\n"));
    }

    @Test
    void contientLesInformationsEvenement() {
        String result = service.generate(createEvent());

        assertTrue(result.contains("SUMMARY:Éclipse lunaire\r\n"));
        assertTrue(result.contains("DESCRIPTION:Observation de la Lune\r\n"));
        assertTrue(result.contains("LOCATION:Paris\r\n"));
        assertTrue(result.contains("DTSTART:20270115T200000Z\r\n"));
        assertTrue(result.contains("DTEND:20270115T220000Z\r\n"));
    }

    @Test
    void accepteUnEvenementSansDateDeFin() {
        EventResponse event = createEvent();
        when(event.endAt()).thenReturn(null);

        String result = service.generate(event);

        assertFalse(result.contains("DTEND:"));
    }

    @Test
    void ignoreUneDateDeFinInvalide() {
        EventResponse event = createEvent();
        when(event.endAt()).thenReturn(
                Instant.parse("2027-01-15T19:00:00Z"));

        assertFalse(service.generate(event).contains("DTEND:"));
    }

    @Test
    void echappeLesCaracteresSpeciaux() {
        EventResponse event = createEvent();
        when(event.description()).thenReturn("Lune, étoiles;\nObservation");

        String result = service.generate(event);

        assertTrue(result.contains(
                "DESCRIPTION:Lune\\, étoiles\\;\\nObservation\r\n"));
    }

    @Test
    void respecteLaLongueurDesLignes() {
        EventResponse event = createEvent();
        when(event.description()).thenReturn("Étoile ".repeat(50));

        String result = service.generate(event);

        for (String line : result.split("\r\n")) {
            assertTrue(line.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8).length <= 75);
        }

        assertTrue(result.contains("\r\n "));
    }
}
