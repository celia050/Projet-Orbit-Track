package com.orbittrack.backend.user;

import java.time.Instant;

/** Ce que l'API renvoie d'un utilisateur : jamais le mot de passe haché. */
public record UserResponse(Long id, String email, String displayName, Role role, Instant createdAt) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(), user.getCreatedAt());
    }
}
