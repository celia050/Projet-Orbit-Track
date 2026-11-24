package com.orbittrack.backend.alert;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Formulaire de création d'une alerte. Tous les critères sont facultatifs, mais il en faut au moins un.
 * Le lieu se donne en entier (latitude, longitude et rayon) ou pas du tout.
 */
public record AlertRequest(
        @NotBlank(message = "Donnez un nom à l'alerte.")
        @Size(max = 80, message = "Le nom ne doit pas dépasser 80 caractères.")
        String name,

        @Size(max = 40, message = "Le type de phénomène est trop long.")
        String eventType,

        @Size(max = 80, message = "Le mot-clé ne doit pas dépasser 80 caractères.")
        String keyword,

        LocalDate dateFrom,
        LocalDate dateTo,

        @DecimalMin(value = "-90", message = "La latitude doit être comprise entre -90 et 90.")
        @DecimalMax(value = "90", message = "La latitude doit être comprise entre -90 et 90.")
        Double latitude,

        @DecimalMin(value = "-180", message = "La longitude doit être comprise entre -180 et 180.")
        @DecimalMax(value = "180", message = "La longitude doit être comprise entre -180 et 180.")
        Double longitude,

        @DecimalMin(value = "1", message = "Le rayon doit être compris entre 1 et 2000 km.")
        @DecimalMax(value = "2000", message = "Le rayon doit être compris entre 1 et 2000 km.")
        Double radiusKm
) {
}
