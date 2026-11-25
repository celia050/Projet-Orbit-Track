
package com.orbittrack.backend.event;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventControllerTest {

    private final EventService service = mock(EventService.class);
    private final EventController controller = new EventController(service);

    @Test
    void rechercheAvecPagination() {
        EventPageResponse expected =
                new EventPageResponse(List.of(), 1, 3, 10, 4);

        when(service.search(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq(1), eq(3)
        )).thenReturn(expected);

        EventPageResponse result = controller.searchEvents(
                null, null, null, null,
                null, null, null, 1, 3
        );

        assertEquals(1, result.page());
        assertEquals(3, result.size());
        assertEquals(10, result.totalElements());
        assertEquals(4, result.totalPages());
    }

    @Test
    void evenementInexistantRetourne404() {
        when(service.findById(99999L)).thenReturn(Optional.empty());

        ResponseEntity<EventResponse> result =
                controller.getEventById(99999L);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    void listeEvenementsRetourneUneListe() {
        when(service.findAll(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull()
        )).thenReturn(List.of());

        List<EventResponse> result = controller.getEvents(
                null, null, null, null, null, null, null
        );

        assertTrue(result.isEmpty());
    }
}
