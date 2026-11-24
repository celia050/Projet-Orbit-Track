package com.orbittrack.backend.forum;

import java.util.List;

/** Une page de messages du forum (pagination). {@code page} commence à 0. */
public record ForumPage(List<ForumMessageResponse> items, int page, int size, long totalItems, int totalPages) {
}
