package com.orbittrack.backend.auth;

import com.orbittrack.backend.user.UserResponse;

import java.time.Instant;

/** Réponse après inscription ou connexion : le jeton à renvoyer dans l'en-tête Authorization. */
public record AuthResponse(String token, Instant expiresAt, UserResponse user) {
}
