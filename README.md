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
| 1 | Selma BAIBOU | 43001407 | [@selmabayb](https://github.com/selmabayb) | Participation, forum et alertes |
| 2 | Celia CHIOUKH | 43006515 | [@celia050](https://github.com/celia050) | Carte Leaflet, export .ics, articles IA |
| 3 | Ritej BAABOURA | 42007200 | [@dontuseitiguess](https://github.com/dontuseitiguess) | Import des événements, cache, lieu conseillé |

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

```text
Projet-Orbit-Track/
├── frontend/           # Application React + TypeScript
│   └── Dockerfile
├── backend/            # API Spring Boot (Java)
│   └── Dockerfile
├── ansible/
│   └── playbook.yml    # Automatisation du lancement
├── compose.yaml        # Configuration Docker Compose
├── README.md
└── ...
```

## Installation et lancement en local

### Prérequis

- Git
- Docker Desktop installé et démarré
- Ansible (facultatif)

### 1. Récupérer le projet

```bash
git clone https://github.com/celia050/Projet-Orbit-Track.git
cd Projet-Orbit-Track
```

### 2. Démarrer avec Docker Compose

Créer d'abord le fichier `.env` (il contient les secrets et n'est jamais versionné) :

```bash
cp .env.example .env
```

Puis remplir dans `.env` :

- `JWT_SECRET` : au moins 32 caractères aléatoires (`openssl rand -base64 48`) ;
- `ADMIN_PASSWORD` : mot de passe du premier administrateur (créé au démarrage).

Ensuite, à la racine du projet :

```bash
docker compose up -d --build
```

Cette commande construit et démarre les quatre services :

- Frontend React
- Backend Spring Boot
- Base de données PostgreSQL
- Cache Redis

### 3. Accéder à l'application

| Service | Adresse |
|---|---|
| Frontend React | http://localhost:5173 |
| Backend Spring Boot | http://localhost:8080 |
| PostgreSQL | localhost:5432 |
| Redis | Accessible aux autres conteneurs sur le port 6379 |

Le backend ne possède pas nécessairement de page d'accueil à l'adresse `/`.

### 4. Lancer avec Ansible (facultatif)

Si Ansible est installé :

```bash
ansible-playbook -i localhost, ansible/playbook.yml
```

Le playbook permet de vérifier que Docker fonctionne, de lancer les services et d'afficher leur état.

### 5. Vérifier les conteneurs

```bash
docker compose ps
```

### 6. Arrêter l'application

```bash
docker compose down
```

Cette commande arrête les services sans supprimer les volumes contenant les données PostgreSQL et Redis.

Pour redémarrer :

```bash
docker compose up -d
```

## Comptes et connexion (feature 12)

Responsable : Selma BAIBOU.

### Règles

- Consulter le site (requêtes `GET /api/...`) ne demande pas de compte.
- S'inscrire à une sortie, écrire dans un forum, créer une alerte : il faut être connecté.
- Les routes `/api/admin/**` sont réservées au rôle `ADMIN`.
- Toute inscription crée un compte `USER`. Seul un administrateur peut donner le rôle `ADMIN`.
- Le site garde toujours au moins un administrateur.
- Les mots de passe sont hachés avec BCrypt ; ils ne sont jamais stockés ni renvoyés en clair.
- Un changement de rôle prend effet à la connexion suivante (le rôle est inscrit dans le jeton).

### API

| Méthode | Route | Accès | Rôle |
|---|---|---|---|
| POST | `/api/auth/register` | libre | Créer un compte (renvoie un jeton) |
| POST | `/api/auth/login` | libre | Se connecter (renvoie un jeton) |
| GET | `/api/auth/me` | connecté | Utilisateur connecté |
| GET | `/api/admin/users` | ADMIN | Liste des comptes |
| PATCH | `/api/admin/users/{id}/role` | ADMIN | Changer le rôle (`{"role":"ADMIN"}`) |

Le jeton se renvoie dans l'en-tête `Authorization: Bearer <jeton>`.

### Pour les autres features

**Côté serveur.** Les routes qui modifient quelque chose (POST, PUT, PATCH, DELETE) demandent déjà d'être connecté. Les routes de **lecture privées** (mes notifications, mes alertes…) doivent être placées sous `/api/me/...` : elles demandent alors un compte, même en GET. Pour connaître l'utilisateur connecté :

```java
@PostMapping("/api/events/{id}/participation")
public void participer(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
    Long userId = CurrentUser.id(jwt);      // identifiant de l'utilisateur
    boolean admin = CurrentUser.isAdmin(jwt);
}
```

**Côté écrans.** `authFetch` ajoute le jeton automatiquement, et `useAuth()` donne l'utilisateur connecté :

```tsx
import { authFetch } from './auth/api'
import { useAuth } from './auth/useAuth'

const { user, isAdmin } = useAuth()
await authFetch(`/api/events/${id}/participation`, { method: 'POST' })
```

### Base de données

Les tables sont créées par des **migrations Flyway** (`backend/src/main/resources/db/migration`), numérotées `V1__...`, `V2__...`. Hibernate vérifie seulement que les entités correspondent (`ddl-auto: validate`). Chaque feature ajoute ses propres fichiers de migration ; ne jamais modifier une migration déjà fusionnée.

### Tests

```bash
cd backend
./mvnw test
```

Les tests démarrent un vrai PostgreSQL avec Testcontainers : **Docker Desktop doit être lancé**.

