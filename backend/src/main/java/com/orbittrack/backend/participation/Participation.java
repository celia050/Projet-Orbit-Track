package com.orbittrack.backend.participation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/** Inscription d'un membre à la sortie d'observation d'un phénomène. Table créée par V10. */
@Entity
@Table(name = "participation")
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private Long eventId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ParticipationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Horaire et statut du phénomène connus du participant (feature 10). Colonnes ajoutées par V12. */
    @Column(name = "known_start_at")
    private Instant knownStartAt;

    @Column(name = "known_end_at")
    private Instant knownEndAt;

    @Column(name = "known_status", length = 20)
    private String knownStatus;

    protected Participation() {
        // requis par JPA
    }

    public Participation(Long eventId, Long userId) {
        this.eventId = eventId;
        this.userId = userId;
        this.status = ParticipationStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public boolean isActive() {
        return status == ParticipationStatus.ACTIVE;
    }

    public void activate() {
        this.status = ParticipationStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = ParticipationStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /** Feature 10 : mémorise l'état du phénomène au moment de l'inscription ou de la dernière notification. */
    public void rememberEvent(EventInfo event) {
        this.knownStartAt = event.startAt();
        this.knownEndAt = event.endAt();
        this.knownStatus = event.status();
    }

    public boolean hasSnapshot() {
        return knownStartAt != null;
    }

    /** Vrai si le participant connaît déjà l'horaire et le statut actuels du phénomène. */
    public boolean knows(EventInfo event) {
        return Objects.equals(knownStartAt, event.startAt())
                && Objects.equals(knownEndAt, event.endAt())
                && Objects.equals(isCancelledStatus(knownStatus), event.isCancelled());
    }

    private static boolean isCancelledStatus(String status) {
        return EventInfo.CANCELLED.equals(status);
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public ParticipationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getKnownStatus() {
        return knownStatus;
    }
}
