
package com.orbittrack.backend.event;

/**
 * Outils de calcul géographique.
 * Utilise la formule de Haversine.
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private GeoUtils() {
        // Classe utilitaire non instanciable
    }

    /**
     * Calcule la distance approximative en kilomètres
     * entre deux coordonnées GPS.
     */
    public static double distanceKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(deltaLon / 2), 2);

        a = Math.max(0.0, Math.min(1.0, a));

        return 2 * EARTH_RADIUS_KM
                * Math.asin(Math.sqrt(a));
    }
}
