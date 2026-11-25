package com.orbittrack.backend.article;

import java.time.OffsetDateTime;

public record ArticleEvent(
    Long id,
    String name,
    String type,
    String description,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    String visibilityZone,
    String sourceName,
    String sourceUrl
) {}
