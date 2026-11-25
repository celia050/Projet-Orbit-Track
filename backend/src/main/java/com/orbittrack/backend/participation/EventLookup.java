package com.orbittrack.backend.participation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * Lit les phénomènes directement dans la table {@code events} (créée par V2, feature 1).
 * On passe par SQL plutôt que par une entité JPA pour ne pas toucher au code de la feature 1,
 * développée en parallèle : les deux parties restent indépendantes.
 */
@Repository
public class EventLookup {

    private static final String COLUMNS = """
            id, name, type, description, start_at, end_at, status, visibility_zone,
            latitude, longitude, visibility_radius_km""";

    private static final RowMapper<EventInfo> MAPPER = EventLookup::map;

    private final JdbcTemplate jdbc;

    public EventLookup(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<EventInfo> find(Long id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM events WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    /** Phénomènes pas encore terminés, du plus proche au plus lointain (annulés compris). */
    public List<EventInfo> findNotOver(Instant now) {
        return jdbc.query("SELECT " + COLUMNS + " FROM events WHERE end_at > ? ORDER BY start_at, id",
                MAPPER, OffsetDateTime.ofInstant(now, ZoneOffset.UTC));
    }

    /** Types de phénomènes connus (pour le formulaire des alertes). */
    public List<String> findTypes() {
        return jdbc.queryForList("SELECT DISTINCT type FROM events ORDER BY type", String.class);
    }

    private static EventInfo map(ResultSet rs, int row) throws SQLException {
        return new EventInfo(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getString("description"),
                rs.getObject("start_at", OffsetDateTime.class).toInstant(),
                rs.getObject("end_at", OffsetDateTime.class).toInstant(),
                rs.getString("status"),
                rs.getString("visibility_zone"),
                rs.getObject("latitude", Double.class),
                rs.getObject("longitude", Double.class),
                rs.getObject("visibility_radius_km", Double.class));
    }
}
