
package com.orbittrack.backend.event;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Récupérer les événements dans l'ordre chronologique
    public List<EventResponse> findAll() {
        return eventRepository.findAll(
                Sort.by("startAt").ascending()
        ).stream()
         .map(EventResponse::from)
         .toList();
    }

    // Récupérer un événement par son identifiant
    public Optional<EventResponse> findById(Long id) {
        return eventRepository.findById(id)
                .map(EventResponse::from);
    }
}
