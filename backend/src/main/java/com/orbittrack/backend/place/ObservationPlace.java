
package com.orbittrack.backend.place;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "observation_places")
public class ObservationPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "light_pollution_level", nullable = false)
    private Integer lightPollutionLevel;

    @Column(name = "accessibility_score", nullable = false)
    private Integer accessibilityScore;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    public Integer getLightPollutionLevel() {
        return lightPollutionLevel;
    }

    public Integer getAccessibilityScore() {
        return accessibilityScore;
    }

    public Boolean getActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public void setLightPollutionLevel(Integer level) {
        this.lightPollutionLevel = level;
    }
    public void setAccessibilityScore(Integer score) {
        this.accessibilityScore = score;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
}
