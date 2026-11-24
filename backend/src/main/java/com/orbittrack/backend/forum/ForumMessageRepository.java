package com.orbittrack.backend.forum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface ForumMessageRepository extends JpaRepository<ForumMessage, Long> {

    /** Messages d'une sortie, les plus récents d'abord. */
    Page<ForumMessage> findByEventIdOrderByCreatedAtDescIdDesc(Long eventId, Pageable pageable);

    /** Anti-spam (feature 9) : messages envoyés par un membre depuis une date, tous forums confondus. */
    long countByAuthorIdAndCreatedAtAfter(Long authorId, Instant since);

    /** Anti-spam (feature 9) : le même texte a-t-il déjà été envoyé récemment dans ce forum ? */
    boolean existsByAuthorIdAndEventIdAndContentIgnoreCaseAndDeletedAtIsNullAndCreatedAtAfter(
            Long authorId, Long eventId, String content, Instant since);
}
