package com.orbittrack.backend.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    Optional<Participation> findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserIdAndStatus(Long eventId, Long userId, ParticipationStatus status);

    long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    List<Participation> findByUserIdAndStatus(Long userId, ParticipationStatus status);

    List<Participation> findByStatus(ParticipationStatus status);

    /** Nombre d'inscrits actifs par phénomène : chaque ligne vaut [eventId, nombre]. */
    @Query("""
            select p.eventId, count(p) from Participation p
            where p.status = com.orbittrack.backend.participation.ParticipationStatus.ACTIVE
            group by p.eventId""")
    List<Object[]> countActiveByEvent();
}
