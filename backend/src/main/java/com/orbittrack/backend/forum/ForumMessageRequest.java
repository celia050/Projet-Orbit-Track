package com.orbittrack.backend.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Écrire ou modifier un message. */
public record ForumMessageRequest(
        @NotBlank(message = "Le message ne peut pas être vide.")
        @Size(max = 1000, message = "Le message ne doit pas dépasser 1000 caractères.")
        String content
) {
}
