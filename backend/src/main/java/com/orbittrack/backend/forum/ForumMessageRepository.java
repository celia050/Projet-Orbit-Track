package com.orbittrack.backend.forum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumMessageRepository extends JpaRepository<ForumMessage, Long> {

    /** Messages d'une sortie, les plus récents d'abord. */
    Page<ForumMessage> findByEventIdOrderByCreatedAtDescIdDesc(Long eventId, Pageable pageable);
}
