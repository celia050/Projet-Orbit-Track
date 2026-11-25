package com.orbittrack.backend.alert;

import com.orbittrack.backend.participation.EventInfo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

/**
 * Feature 11 : un phénomène correspond-il à une alerte ? Tous les critères remplis de l'alerte doivent l'être.
 * <ul>
 *   <li>Type : même type de phénomène.</li>
 *   <li>Mot-clé : présent dans le nom ou la description (sans tenir compte des majuscules).</li>
 *   <li>Période : le phénomène commence entre les deux dates (jours à l'heure de Paris, bornes comprises).</li>
 *   <li>Lieu : le phénomène est visible à moins de « rayon » km du point choisi
 *       (distance entre les deux points, moins le rayon de visibilité du phénomène).</li>
 * </ul>
 * Un phénomène annulé ne déclenche jamais d'alerte.
 */
public final class AlertMatcher {

    static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final double EARTH_RADIUS_KM = 6371.0;

    private AlertMatcher() {
    }

    public static boolean matches(Alert alert, EventInfo event) {
        if (event.isCancelled()) {
            return false;
        }
        if (alert.getEventType() != null && !alert.getEventType().equalsIgnoreCase(event.type())) {
            return false;
        }
        if (alert.getKeyword() != null && !containsKeyword(event, alert.getKeyword())) {
            return false;
        }
        LocalDate day = LocalDate.ofInstant(event.startAt(), PARIS);
        if (alert.getDateFrom() != null && day.isBefore(alert.getDateFrom())) {
            return false;
        }
        if (alert.getDateTo() != null && day.isAfter(alert.getDateTo())) {
            return false;
        }
        if (alert.hasPlace()) {
            if (!event.hasCoordinates()) {
                return false;
            }
            double distance = distanceKm(alert.getLatitude(), alert.getLongitude(), event.latitude(),
                    event.longitude());
            double eventRadius = event.visibilityRadiusKm() == null ? 0 : event.visibilityRadiusKm();
            return distance - eventRadius <= alert.getRadiusKm();
        }
        return true;
    }

    private static boolean containsKeyword(EventInfo event, String keyword) {
        String needle = keyword.toLowerCase(Locale.ROOT);
        String haystack = (event.name() + " " + (event.description() == null ? "" : event.description()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(needle);
    }

    /** Distance à vol d'oiseau entre deux points (formule de haversine). */
    static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1, Math.sqrt(a)));
    }
}
