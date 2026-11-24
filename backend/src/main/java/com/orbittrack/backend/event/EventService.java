
package com.orbittrack.backend.event;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventResponse> findAll(
            String type,
            String status,
            Instant from,
            Instant to) {

        Specification<Event> spec = (root, query, cb) ->
                cb.conjunction();

        if (type != null && !type.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("type"), type));
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (from != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(
                            root.get("startAt"), from));
        }

        if (to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(
                            root.get("startAt"), to));
        }

        return eventRepository.findAll(
                spec, Sort.by("startAt").ascending()
        ).stream()
         .map(EventResponse::from)
         .toList();
    }

    public Optional<EventResponse> findById(Long id) {
        return eventRepository.findById(id)
                .map(EventResponse::from);
    }
}
