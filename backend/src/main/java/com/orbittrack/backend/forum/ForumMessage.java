package com.orbittrack.backend.forum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Message du forum d'une sortie. Table créée par V11. */
@Entity
@Table(name = "forum_message")
public class ForumMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private Long eventId;

    @Column(name = "author_id", nullable = false, updatable = false)
    private Long authorId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by_admin", nullable = false)
    private boolean deletedByAdmin;

    protected ForumMessage() {
        // requis par JPA
    }

    public ForumMessage(Long eventId, Long authorId, String content) {
        this.eventId = eventId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public void edit(String newContent) {
        this.content = newContent;
        this.editedAt = Instant.now();
    }

    public void delete(boolean byAdmin) {
        this.deletedAt = Instant.now();
        this.deletedByAdmin = byAdmin;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getEditedAt() {
        return editedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeletedByAdmin() {
        return deletedByAdmin;
    }
}
