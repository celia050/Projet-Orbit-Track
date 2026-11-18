# Changelog

Toutes les évolutions notables du projet sont listées ici.
Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et les versions suivent [SemVer](https://semver.org/lang/fr/).

## [0.1.0] - 2026-11-18

Première version intermédiaire (rendu 2).

### Ajouté

- **Installation (feature 16, Celia)** : Docker Compose (frontend React, backend Spring Boot, PostgreSQL, Redis) et playbook Ansible ; tout le site se lance avec `docker compose up --build`.
- **Comptes et connexion (feature 12, Selma)** : inscription, connexion par jeton JWT, déconnexion, rôles USER et ADMIN, premier administrateur créé au démarrage, gestion des rôles par l'admin ; lecture libre, écriture réservée aux membres connectés, `/api/me/**` et `/api/admin/**` protégés ; écrans d'inscription et de connexion ; tests d'intégration avec Testcontainers.
- **Modèle des événements (feature 1, Ritej, en cours)** : table `events` (migration V2) avec contrôles de cohérence et anti-doublon par source, 10 phénomènes de démonstration (migration V3), documentation du modèle.
- **Intégration continue (feature 17)** : GitHub Actions sur chaque push et Pull Request vers `main` et `develop` (build et tests du backend, lint et build du frontend, construction des images Docker).
- Migrations de base de données avec Flyway, secrets dans `.env` (modèle `.env.example`).
- README avec la répartition des features et l'organisation Git ; dossier de projet mis à jour (`doc/dossier-lancement.md`).

[0.1.0]: https://github.com/celia050/Projet-Orbit-Track/releases/tag/v0.1.0
