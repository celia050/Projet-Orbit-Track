package com.orbittrack.backend.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Active les tâches automatiques (détection des changements, feature 10 ; alertes, feature 11).
 * Désactivé dans les tests avec {@code orbittrack.scheduling.enabled=false} : les tests appellent les services
 * directement, pour ne pas dépendre du temps qui passe.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "orbittrack.scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
