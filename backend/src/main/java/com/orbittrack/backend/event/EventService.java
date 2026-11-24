
package com.orbittrack.backend.event;

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

    // Récupérer tous les événements, classés par date
    public List<Event> findAll() {
        return eventRepository.findAll(
            org.springframework.data.domain.Sort.by("startAt").ascending()
        );
    }

    // Récupérer un événement grâce à son identifiant
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }
}

