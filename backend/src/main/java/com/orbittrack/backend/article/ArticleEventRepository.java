package com.orbittrack.backend.article;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class ArticleEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public ArticleEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ArticleEvent> findUpcomingEvents() {
        String sql = """
            SELECT
                id,
                name,
                type,
                description,
                start_at,
                end_at,
                visibility_zone,
                source_name,
                source_url
            FROM events
            WHERE status IN ('SCHEDULED', 'UPDATED')
              AND is_demo = FALSE
              AND start_at >= NOW()
              AND start_at < NOW() + INTERVAL '7 days'
            ORDER BY start_at ASC
            LIMIT 10
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new ArticleEvent(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getString("description"),
                rs.getObject("start_at", OffsetDateTime.class),
                rs.getObject("end_at", OffsetDateTime.class),
                rs.getString("visibility_zone"),
                rs.getString("source_name"),
                rs.getString("source_url")
            )
        );
    }
}
