# Orbit Track

[![Version](https://img.shields.io/github/v/tag/celia050/Projet-Orbit-Track?label=version&sort=semver)](https://github.com/celia050/Projet-Orbit-Track/tags)
[![Licence](https://img.shields.io/github/license/celia050/Projet-Orbit-Track)](LICENSE)
[![CI](https://github.com/celia050/Projet-Orbit-Track/actions/workflows/ci.yml/badge.svg)](https://github.com/celia050/Projet-Orbit-Track/actions/workflows/ci.yml)
[![Couverture](https://codecov.io/gh/celia050/Projet-Orbit-Track/branch/main/graph/badge.svg)](https://codecov.io/gh/celia050/Projet-Orbit-Track)
[![Qualité](https://sonarcloud.io/api/project_badges/measure?project=celia050_Projet-Orbit-Track&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=celia050_Projet-Orbit-Track)

Orbit Track permet de suivre les événements astronomiques à venir, de trouver un lieu d'observation conseillé et de les observer ensemble : participation, forum par événement, alertes, export agenda (.ics), carte interactive et article quotidien relu par un humain.

> Projet universitaire, MIAGE, Université Paris Nanterre (2026-2027).

## Équipe

| # | Membre | N° étudiant | GitHub | Feature majeure |
|---|---|---|---|---|
| 1 | Ritej BAABOURA | 42007200 | [@dontuseitiguess](https://github.com/dontuseitiguess) | Participation, forum et alertes |
| 2 | Selma BAIBOU | 43001407 | [@selmabayb](https://github.com/selmabayb) | Carte Leaflet, export .ics, articles IA |
| 3 | Celia CHIOUKH | 43006515 | [@celia050](https://github.com/celia050) | Import des événements, cache, lieu conseillé |

## Fonctionnalités (MVP)

- **Événements d'observation** importés depuis des API astronomiques, normalisés, mis en cache et affichés avec leur source.
- **Lieu conseillé** parmi des sites gérés par un administrateur (proximité, fenêtre de visibilité, accès).
- **Comptes** USER et ADMIN ; consultation libre sans compte.
- **Participation** (une inscription active par utilisateur et par événement) et notification en cas de changement.
- **Forum par événement** réservé aux participants confirmés, avec modération, pagination et limite de débit.
- **Alertes** par objet, date ou lieu, avec notifications dans l'application.
- **Export .ics** : UID stable, horaires en UTC, SEQUENCE incrémentée à chaque modification.
- **Carte Leaflet 2D** exportable en PNG ou imprimable avec légende, date et attribution.
- **Articles quotidiens assistés par IA**, relus avant publication, avec repli sur un corpus « Le savais-tu ? ».

## Stack technique

| Couche | Technologie |
|---|---|
| Front | React + TypeScript, Leaflet |
| API | Spring Boot (Java) |
| Base de données | PostgreSQL (migrations Flyway) |
| Cache / limitation de débit | Redis |
| Conteneurs | Docker Compose |
| CI/CD | GitHub Actions |
| Déploiement | Ansible |
| Supervision | Prometheus + Grafana |

## Structure du dépôt

```
.
├── frontend/     # Application React + TypeScript
├── backend/      # API Spring Boot
├── infra/        # docker-compose, Ansible, Prometheus, Grafana
└── doc/          # Dossier de lancement (Markdown → PDF) et documentation
```

## Lancer le projet en local

Prérequis : Docker et Docker Compose.

```bash
git clone https://github.com/celia050/Projet-Orbit-Track.git
cd Projet-Orbit-Track
cp .env.example .env      # renseigner les clés d'API (jamais versionnées)
docker compose up -d --build
```

| Service | URL locale |
|---|---|
| Front | http://localhost:5173 |
| API | http://localhost:8080 |
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |

*(Ports indicatifs, à ajuster selon le fichier `docker-compose.yml`.)*

## Tests

```bash
cd backend && ./mvnw verify      # JUnit 5 + Testcontainers, couverture JaCoCo
cd frontend && npm test          # Vitest + Testing Library
```

## Documentation

- [Dossier de lancement](doc/dossier-lancement.pdf) ([source Markdown](doc/dossier-lancement.md))
- Protocole d'usage de l'IA : section 6 du dossier de lancement

## Contribuer

Branche `main` protégée, développement sur des branches `feature/*`, Pull Request relue par un autre membre et CI verte avant fusion. Versions taguées en SemVer (`v0.1.0`, …), voir `CHANGELOG.md`.

## Licence

Distribué sous licence MIT. Voir [LICENSE](LICENSE).
