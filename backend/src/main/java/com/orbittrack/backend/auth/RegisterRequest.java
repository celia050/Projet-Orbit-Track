package com.orbittrack.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Formulaire d'inscription. */
public record RegisterRequest(
        @NotBlank(message = "L'email est obligatoire.")
        @Email(message = "L'email n'est pas valide.")
        @Size(max = 254, message = "L'email est trop long.")
        String email,

        // BCrypt ignore tout ce qui dépasse 72 octets : on limite donc la longueur.
        @NotBlank(message = "Le mot de passe est obligatoire.")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères.")
        String password,

        @NotBlank(message = "Le pseudo est obligatoire.")
        @Size(min = 2, max = 50, message = "Le pseudo doit contenir entre 2 et 50 caractères.")
        String displayName
) {
}
