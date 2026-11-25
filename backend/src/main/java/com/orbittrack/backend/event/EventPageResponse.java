
package com.orbittrack.backend.event;

import java.util.List;

public record EventPageResponse(
        List<EventResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
