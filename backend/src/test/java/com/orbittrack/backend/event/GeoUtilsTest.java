
package com.orbittrack.backend.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    void sameCoordinatesShouldReturnZero() {
        double distance = GeoUtils.distanceKm(
                48.8566, 2.3522,
                48.8566, 2.3522
        );

        assertEquals(0.0, distance, 0.001);
    }

    @Test
    void parisToLyonShouldBeApproximately390Km() {
        double distance = GeoUtils.distanceKm(
                48.8566, 2.3522,
                45.7640, 4.8357
        );

        assertEquals(390.0, distance, 15.0);
    }

    @Test
    void distanceShouldBeSymmetric() {
        double parisLyon = GeoUtils.distanceKm(
                48.8566, 2.3522,
                45.7640, 4.8357
        );

        double lyonParis = GeoUtils.distanceKm(
                45.7640, 4.8357,
                48.8566, 2.3522
        );

        assertEquals(parisLyon, lyonParis, 0.000001);
    }
}
