
package com.orbittrack.backend.place;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping
    public List<PlaceResponse> getPlaces() {
        return placeService.findAll();
    }

    @GetMapping("/recommendations")
    public List<PlaceResponse> getRecommendations(
            @RequestParam Long eventId,
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false) Double maxDistanceKm,
            @RequestParam(defaultValue = "10") int limit) {

        return placeService.recommend(
                eventId,
                lat,
                lon,
                maxDistanceKm,
                limit
        );
    }
}
