
package com.orbittrack.backend.place;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "observation_places")
public class ObservationPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double latitude;
    private Double longitude;

    @Column(name = "light_pollution_level")
    private Integer lightPollutionLevel;

    @Column(name = "accessibility_score")
    private Integer accessibilityScore;

    private Boolean active;

    @Column(name = "created_at")
    private Instant createdAt;

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
}
