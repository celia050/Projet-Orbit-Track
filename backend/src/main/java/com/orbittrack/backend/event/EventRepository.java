
package com.orbittrack.backend.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Accès aux phénomènes astronomiques stockés en base.
 * Spring Data JPA génère automatiquement les requêtes CRUD.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
