package com.orbittrack.backend.user;

/** Les deux niveaux d'accès du site. */
public enum Role {
    /** Membre connecté : peut participer, écrire dans les forums, créer des alertes. */
    USER,
    /** Administrateur : gère le site (lieux, modération, rôles, articles). */
    ADMIN
}
