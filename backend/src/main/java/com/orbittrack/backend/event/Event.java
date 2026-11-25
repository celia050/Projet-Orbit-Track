
package com.orbittrack.backend.event;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Phénomène astronomique.
 * Table créée par V2__create_event.sql.
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "visibility_zone", nullable = false, length = 255)
    private String visibilityZone;

    private Double latitude;
    private Double longitude;

    @Column(name = "visibility_radius_km")
    private Double visibilityRadiusKm;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "source_name", nullable = false, length = 100)
    private String sourceName;

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(name = "source_event_id", length = 200)
    private String sourceEventId;

    @Column(name = "source_updated_at")
    private Instant sourceUpdatedAt;

    @Column(name = "is_demo", nullable = false)
    private boolean demo;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false,
            insertable = false, updatable = false)
    private Instant updatedAt;

    protected Event() {
        // Constructeur requis par JPA
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public String getVisibilityZone() { return visibilityZone; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getVisibilityRadiusKm() { return visibilityRadiusKm; }
    public String getStatus() { return status; }
    public String getSourceName() { return sourceName; }
    public String getSourceUrl() { return sourceUrl; }
    public String getSourceEventId() { return sourceEventId; }
    public Instant getSourceUpdatedAt() { return sourceUpdatedAt; }
    public boolean isDemo() { return demo; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
