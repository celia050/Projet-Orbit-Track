package com.orbittrack.backend.forum;

import java.time.Instant;

/**
 * Un message tel que l'affiche le forum. Un message supprimé garde sa place,
 * mais son texte est remplacé et {@code deleted} vaut true.
 * {@code canEdit} et {@code canDelete} disent au front quels boutons afficher.
 */
public record ForumMessageResponse(
        Long id,
        Long eventId,
        Long authorId,
        String authorName,
        String content,
        Instant createdAt,
        boolean edited,
        boolean deleted,
        boolean canEdit,
        boolean canDelete
) {
}
