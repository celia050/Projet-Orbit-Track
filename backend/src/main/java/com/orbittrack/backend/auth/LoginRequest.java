package com.orbittrack.backend.auth;

import jakarta.validation.constraints.NotBlank;

/** Formulaire de connexion. */
public record LoginRequest(
        @NotBlank(message = "L'email est obligatoire.") String email,
        @NotBlank(message = "Le mot de passe est obligatoire.") String password
) {
}
