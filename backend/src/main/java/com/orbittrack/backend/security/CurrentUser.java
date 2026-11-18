package com.orbittrack.backend.security;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Petit utilitaire pour les autres features : récupérer l'identifiant de l'utilisateur connecté.
 *
 * <pre>
 * &#64;PostMapping("/api/events/{id}/participation")
 * public void participer(&#64;PathVariable Long id, &#64;AuthenticationPrincipal Jwt jwt) {
 *     Long userId = CurrentUser.id(jwt);
 * }
 * </pre>
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    public static boolean isAdmin(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString(SecurityConfig.ROLE_CLAIM));
    }
}
