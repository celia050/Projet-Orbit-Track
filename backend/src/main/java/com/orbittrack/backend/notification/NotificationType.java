package com.orbittrack.backend.notification;

public enum NotificationType {
    /** L'horaire d'une sortie à laquelle on est inscrit a changé (feature 10). */
    EVENT_CHANGED,
    /** Une sortie à laquelle on est inscrit est annulée (feature 10). */
    EVENT_CANCELLED,
    /** Un phénomène correspond à une alerte (feature 11). */
    ALERT_MATCH
}
