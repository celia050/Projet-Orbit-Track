package com.orbittrack.backend.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

/** Alerte d'un membre : critères qu'un phénomène doit remplir pour le prévenir. Table créée par V13. */
@Entity
@Table(name = "alert")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "event_type", length = 40)
    private String eventType;

    @Column(length = 80)
    private String keyword;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    private Double latitude;

    private Double longitude;

    @Column(name = "radius_km")
    private Double radiusKm;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Alert() {
        // requis par JPA
    }

    public Alert(Long userId, String name, String eventType, String keyword, LocalDate dateFrom, LocalDate dateTo,
                 Double latitude, Double longitude, Double radiusKm) {
        this.userId = userId;
        this.name = name;
        this.eventType = eventType;
        this.keyword = keyword;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusKm = radiusKm;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public boolean hasPlace() {
        return latitude != null && longitude != null && radiusKm != null;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEventType() {
        return eventType;
    }

    public String getKeyword() {
        return keyword;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
