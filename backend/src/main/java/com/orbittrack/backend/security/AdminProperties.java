package com.orbittrack.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Premier administrateur créé au démarrage (orbittrack.security.admin.*). */
@ConfigurationProperties("orbittrack.security.admin")
public record AdminProperties(String email, String password) {

    public boolean isConfigured() {
        return email != null && !email.isBlank() && password != null && !password.isBlank();
    }
}
