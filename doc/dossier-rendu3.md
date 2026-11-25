# Orbit Track — Dossier de projet

**Rendu 3 · application v0.2.0 · 25 novembre 2026 (état de préparation)**

| Version du dossier | Date       | Auteurs                            | Changements                                                                                                             |
|--------------------|------------|------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| 0.1                | 10/11/2026 | S. Baibou, C. Chioukh, R. Baaboura | Création du dossier de lancement (rendu 1)                                                                              |
| 0.2                | 18/11/2026 | S. Baibou, C. Chioukh, R. Baaboura | Features métier détaillées et nouvelle répartition, organisation Git, intégration continue, état de la v0.1.0 (rendu 2) |
| 0.3                | 25/11/2026 | S. Baibou, C. Chioukh, R. Baaboura | Rendu 3 : participation, forum, anti-spam, notifications, alertes, API événements et lieux, article du jour             |

État établi à partir de `develop` au commit `6d9fb17`. La release reste à publier ; la source astronomique externe reste à confirmer.

## 1. Équipe

| \#  | Membre         | N° étudiant | GitHub          | Partie du site                                                                       | Features |
|-----|----------------|-------------|-----------------|--------------------------------------------------------------------------------------|----------|
| 1   | Ritej BAABOURA | 42007200    | dontuseitiguess | « Quoi voir, quand et où ? » : les phénomènes, leur fiabilité, le lieu conseillé     | 1 à 6    |
| 2   | Selma BAIBOU   | 43001407    | selmabayb       | « Y aller ensemble » : comptes, sorties, forum, alertes                              | 7 à 12   |
| 3   | Celia CHIOUKH  | 43006515    | celia050        | « Se préparer et apprendre » : carte, agenda, article du jour, mise en ligne du site | 13 à 18  |

**Changement depuis le rendu 1.** Le découpage par couche technique (front-end, back-end, données) a été remplacé par un découpage par **features métier**. Chaque membre porte une partie du site de bout en bout (données, API, écrans, tests). La répartition est équitable : 6 features chacune, dont 3 grandes et 3 petites, et au moins 2 grandes features métier. Les tâches techniques transverses ont maintenant une responsable nommée. Toute Pull Request est relue par un autre membre avant fusion.

## 2. Comptes développeurs utilisés

| Service                    | Usage                                                                                  | Compte(s)                                                                           |
|----------------------------|----------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| GitHub                     | Dépôt, Pull Requests, GitHub Actions (CI), releases                                    | selmabayb (Selma), celia050 (Celia, propriétaire du dépôt), dontuseitiguess (Ritej) |
| API astronomiques externes | Prévisions d’événements (éclipses, pluies d’étoiles filantes, passages, conjonctions…) | Compte projet, clé stockée en secret GitHub \[à confirmer\]                         |
| Ollama — Qwen2.5:3b        | Génération locale de brouillons                                                        | Sans compte ni clé d’API externe                                                    |
| OpenStreetMap / tuiles     | Fond de carte Leaflet (attribution affichée)                                           | Sans compte (usage conforme à la politique de tuiles)                               |
| SonarCloud, Codecov        | Qualité de code, couverture de tests                                                   | Liés au dépôt GitHub (à brancher)                                                   |

Aucune clé n’est versionnée : le fichier `.env` est ignoré par Git, `.env.example` est fourni, et chaque membre crée ses propres valeurs locales.

## 3. Le projet et son caractère innovant

Orbit Track est un site web qui prévient les gens des phénomènes visibles dans le ciel près de chez eux (éclipses, pluies d’étoiles filantes, passage de la Station spatiale internationale, conjonctions de planètes) et les aide à aller les observer ensemble.

**Parcours cible.** Je découvre qu’une pluie d’étoiles filantes est visible cette semaine. Le site m’indique l’heure, le meilleur endroit pour la regarder et pourquoi. Je m’inscris à la sortie, j’échange avec les autres participants pour le covoiturage et j’ajoute le rendez-vous à mon agenda. Si l’horaire change, je suis prévenu.

Ce qui distingue Orbit Track des calendriers astronomiques existants :

- **De la prévision à la sortie collective** : un événement est un rendez-vous, avec participants, forum et notifications de changement.
- **Lieu conseillé justifié** : le site proposé est choisi selon la proximité, la fenêtre de visibilité et l’accès, et la justification est affichée. Le site précise qu’il ne garantit pas les conditions réelles.
- **Traçabilité des sources** : chaque fiche affiche sa source et sa date de mise à jour. Un événement n’est créé que si les données sont suffisantes.
- **IA encadrée** : les articles du jour sont rédigés à partir des faits stockés, puis relus par un humain avant publication. Un repli sans IA est prévu ; les brouillons doivent être vérifiés avant publication.
- **Résilience** : si une source externe tombe, les événements déjà stockés restent consultables.

## 4. Features métier

Le détail complet de chaque feature (description concrète, outils, règles métier) est dans le **tableau des features** (`tableau-features-orbit-track.pdf`, à la racine du dépôt). Une feature **grande** mobilise front, back et données avec plusieurs écrans ou règles ; une **petite** tient en un écran ou une règle. Une feature **métier** est visible et utile pour l’utilisateur ; une feature **technique** la rend possible ou fiable.

| \#  | Feature                                        | Responsable | Taille | Type      |
|-----|------------------------------------------------|-------------|--------|-----------|
| 1   | Voir les phénomènes à venir près de chez soi   | Ritej       | Grande | Métier    |
| 2   | Consulter la fiche d’un phénomène              | Ritej       | Petite | Métier    |
| 3   | Savoir où aller observer (lieu conseillé)      | Ritej       | Grande | Métier    |
| 4   | Gérer la liste des lieux d’observation         | Ritej       | Petite | Métier    |
| 5   | Récupérer automatiquement les prévisions       | Ritej       | Grande | Technique |
| 6   | Rester utilisable si une source tombe en panne | Ritej       | Petite | Technique |
| 7   | S’inscrire à une sortie d’observation          | Selma       | Petite | Métier    |
| 8   | Discuter avec les autres participants (forum)  | Selma       | Grande | Métier    |
| 9   | Empêcher le spam dans les forums               | Selma       | Petite | Technique |
| 10  | Être prévenu d’un changement                   | Selma       | Petite | Métier    |
| 11  | Créer une alerte                               | Selma       | Grande | Métier    |
| 12  | Créer un compte et se connecter                | Selma       | Grande | Technique |
| 13  | Voir les phénomènes sur une carte              | Celia       | Grande | Métier    |
| 14  | Ajouter un phénomène à son agenda (.ics)       | Celia       | Petite | Métier    |
| 15  | Lire l’article du jour (assisté par IA)        | Celia       | Grande | Métier    |
| 16  | Installer et lancer le site facilement         | Celia       | Petite | Technique |
| 17  | Vérifier automatiquement le code (CI)          | Celia       | Grande | Technique |
| 18  | Surveiller le bon fonctionnement               | Celia       | Petite | Technique |

### Règles métier principales (objectifs)

**Partie 1 — Phénomènes (Ritej).** Un phénomène n’est publié que si sa date, son type et sa zone de visibilité sont connus. Chaque fiche affiche sa source et sa date de mise à jour. Un même phénomène n’apparaît jamais deux fois. Les horaires sont stockés en UTC et affichés dans le fuseau de l’utilisateur. Le lieu conseillé est choisi selon la distance, la visibilité au bon moment et l’accès, avec sa justification. En cas de panne d’une source, les fiches connues restent consultables avec un bandeau.

**Partie 2 — Sorties collectives (Selma).** Consulter le site ne demande pas de compte ; participer, écrire et créer une alerte en demandent un. Une seule participation active par utilisateur et par événement ; pas d’inscription à un événement annulé ou passé. Seuls les inscrits écrivent dans le forum ; chacun ne modifie que ses propres messages ; l’administrateur modère. Le nombre de messages par minute est limité. Tous les inscrits sont prévenus d’un changement d’horaire ou d’une annulation. Une alerte ne notifie qu’une fois par phénomène. Le site garde toujours au moins un administrateur.

**Partie 3 — Préparer et s’informer (Celia).** Un nouvel export agenda d’un même événement remplace l’ancien sans doublon (même UID, SEQUENCE incrémentée). La carte imprimée porte légende, date et attribution. L’IA ne fait que reformuler les faits stockés ; aucun article n’est publié sans relecture humaine ; sans faits fiables, une anecdote validée ou rien.

### Ordre de développement

Une feature ne peut commencer que si ce dont elle dépend existe :

1.  **Installer et lancer le site** (16) : sans elle, personne ne peut lancer le projet.
2.  **Créer un compte et se connecter** (12) : nécessaire à la participation, au forum, aux alertes et à l’administration.
3.  **Modèle des phénomènes et données de démonstration** (début de la feature 1) : toutes les autres features utilisent les phénomènes. Les données de démonstration permettent aux autres de travailler sans attendre l’import réel.
4.  Ensuite, chaque membre avance sur ses features de son côté.

## 5. Scénarios d’usage cibles

| \#  | Acteur      | Scénario                                                                                                                                                  | Features   |
|-----|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| S1  | Visiteur    | Ouvre l’accueil, voit les prochains événements près du lieu choisi et l’article du jour, filtre la liste et la carte par date, type et lieu, sans compte. | 1, 13, 15  |
| S2  | Utilisateur | Ouvre la fiche de la pluie des Géminides : horaire local, lieu conseillé et justification, source. Clique « Je participe » et accède au forum.            | 2, 3, 7, 8 |
| S3  | Participant | Publie, modifie ou supprime ses messages (pagination, limite de débit). Est notifié si l’horaire ou le statut change.                                     | 8, 9, 10   |
| S4  | Utilisateur | Crée une alerte « éclipses à moins de 50 km de chez moi » et reçoit une notification à l’annonce d’un événement correspondant.                            | 11         |
| S5  | Utilisateur | Exporte un événement en .ics ; après un changement d’horaire, le nouvel export met à jour le même rendez-vous.                                            | 14         |
| S6  | Utilisateur | Exporte la carte en PNG ou l’imprime, avec légende, date et attribution.                                                                                  | 13         |
| S7  | Responsable | Relit le brouillon d’article de la nuit, le corrige et le publie, ou le rejette.                                                                          | 15         |
| S8  | Admin       | Gère les sites d’observation, modère le forum, gère les rôles.                                                                                            | 4, 8, 12   |
| S9  | Tous        | Une source externe est indisponible : les fiches stockées restent affichées avec un bandeau.                                                              | 6          |

## 6. Architecture et frameworks

| Couche          | Technologie                        | Rôle                                                                                                   |
|-----------------|------------------------------------|--------------------------------------------------------------------------------------------------------|
| Front           | React + TypeScript (Vite), Leaflet | Interface ; carte 2D et export PNG / impression prévus                                                 |
| API             | Spring Boot 4 (Java 21)            | API REST, logique métier, sécurité (JWT, rôles USER/ADMIN), tâches planifiées ; génération .ics prévue |
| Base de données | PostgreSQL 17 + migrations Flyway  | Utilisateurs, événements, sites, participations, messages, notifications, alertes, articles            |
| Cache           | Redis                              | Limitation de débit du forum ; cache des API externes prévu                                            |
| Conteneurs      | Docker Compose                     | Front, API, PostgreSQL, Redis, Ollama                                                                  |
| IA locale       | Ollama + Qwen2.5:3b                | Génération de brouillons à partir des événements                                                       |
| CI              | GitHub Actions                     | Build et tests du backend, lint et build du frontend, construction des images Docker                   |
| Déploiement     | Ansible                            | Lancement reproductible sur un PC ou une VM                                                            |
| Supervision     | Prometheus + Grafana               | Métriques et tableaux de bord (à venir)                                                                |

**Choix structurants.** Horaires stockés en UTC et affichés dans le fuseau de l’utilisateur. Tables créées uniquement par des migrations Flyway numérotées (V1 comptes, V2 événements, V3 données de démonstration, V10 participations, V11 forum, V12 notifications, V13 alertes, V20 lieux, V30 articles) ; Hibernate se contente de vérifier les entités. Lecture libre (`GET /api/...`), écriture réservée aux membres connectés, données personnelles sous `/api/me/...`, administration sous `/api/admin/...`. Les mots de passe sont hachés avec BCrypt.

## 7. État de préparation de la version v0.2.0

| Feature                   | Responsable | État                                                                                                      |
|---------------------------|-------------|-----------------------------------------------------------------------------------------------------------|
| 1 — Liste des phénomènes  | Ritej       | API intégrée : filtres, recherche géographique, pagination ; interface dédiée à confirmer.                |
| 2 — Fiche d’un phénomène  | Ritej       | API de détail intégrée ; écran à confirmer.                                                               |
| 3 — Lieu conseillé        | Ritej       | Recommandations côté backend intégrées (distance, pollution lumineuse, accès).                            |
| 4 — Gestion des lieux     | Ritej       | API d’administration intégrée ; écran à confirmer.                                                        |
| 5 — Import des prévisions | Ritej       | Import réel non confirmé dans cette version.                                                              |
| 6 — Mode dégradé          | Ritej       | À confirmer.                                                                                              |
| 7 — Participation         | Selma       | Inscription et désinscription aux sorties intégrées.                                                      |
| 8 — Forum                 | Selma       | Forum par sortie intégré.                                                                                 |
| 9 — Anti-spam             | Selma       | Limitation de débit intégrée.                                                                             |
| 10 — Notifications        | Selma       | Détection des changements et notifications intégrées.                                                     |
| 11 — Alertes              | Selma       | Création et gestion des alertes intégrées.                                                                |
| 12 — Authentification     | Selma       | Terminée depuis la v0.1.0.                                                                                |
| 13 — Carte                | Celia       | Intégration dans la version livrée à vérifier.                                                            |
| 14 — Agenda               | Celia       | À développer.                                                                                             |
| 15 — Article du jour      | Celia       | Intégrée : génération locale, brouillons, édition, publication ou rejet, affichage public, repli sans IA. |
| 16 — Installation         | Celia       | Docker Compose et Ansible depuis la v0.1.0 ; ajout du service Ollama.                                     |
| 17 — CI                   | Celia       | Pipeline opérationnel ; couverture et qualité à compléter.                                                |
| 18 — Supervision          | Celia       | À développer.                                                                                             |

**Article du jour.** Une tâche planifiée à 2 h (Europe/Paris) prépare un brouillon à partir des événements réels à venir dans les sept jours. En l’absence d’événement exploitable, une anecdote du corpus interne est utilisée. En cas d’échec du modèle, un texte de repli peut être produit sans IA. L’administrateur peut modifier, publier ou rejeter le brouillon. Le modèle se récupère avec `docker compose exec ollama ollama pull qwen2.5:3b` après le démarrage des services.

**Tests et limites.** Les PR intégrées ajoutent des tests pour les événements, la géolocalisation, la participation, le forum, les notifications et les alertes. Les trois jobs CI de la PR article ont réussi. Un test local du modèle a également été exécuté ; des erreurs factuelles restent possibles, d’où la validation humaine. Le parcours complet avec des événements réels et la recette finale de la release restent à vérifier.

**Tests de la feature 12.** Inscription (compte USER, mot de passe jamais renvoyé), email déjà utilisé même avec des majuscules (409), formulaire invalide (400), connexion correcte et incorrecte avec le même message, profil réservé aux connectés, jeton falsifié refusé, écriture sans compte refusée, données personnelles protégées même en lecture, administration réservée aux admins, promotion d’un utilisateur, impossibilité de retirer le dernier administrateur, mot de passe trop long pour BCrypt refusé proprement. Le parcours a aussi été vérifié à la main dans le navigateur.

## 8. Organisation Git et livraison

- `main` : versions livrées, chacune figée par un **tag** SemVer (`v0.1.0`, `v0.2.0`…) et une **release** GitHub.
- `develop` : branche d’intégration commune.
- Une branche par feature, créée depuis `develop` à jour (`feature/installation`, `feature/authentification`, `feature/evenements`, `feature/integration-continue`…).
- Une **Pull Request** vers `develop` par feature, relue par un autre membre, vérifiée par la CI.
- Pour livrer : Pull Request `develop` → `main`, puis release avec tag sur le commit de `main`.
- Les changements de chaque version sont listés dans `CHANGELOG.md`.

**Historique de la v0.1.0 :** PR \#1 (installation), PR \#2 (comptes et connexion), PR \#3 (README et répartition), PR \#4 (modèle des événements), PR \#5 (intégration continue), puis `develop` → `main` et tag `v0.1.0`.

**Historique depuis la v0.1.0 :** PR \#8 (participation), \#9 (forum), \#10 (anti-spam), \#11 (notifications), \#12 (alertes), \#13 (API événements et lieux), \#14 (article du jour). Toutes sont fusionnées dans `develop` au 25 novembre. La livraison sur `main` et le tag `v0.2.0` restent à réaliser.

## 9. Protocole d’usage de l’IA

**Dans le produit.** Le modèle Qwen2.5:3b est exécuté localement via Ollama. Il reçoit des informations sur les événements astronomiques réels à venir, sans données personnelles. Il sert à rédiger un brouillon et ne constitue pas une source scientifique. La publication exige une validation humaine. Un texte déterministe ou une anecdote interne permet un repli sans IA. La journalisation complète du prompt, du modèle et de la date reste à vérifier.

**Dans le développement.**

1.  Les assistants IA (génération de code, rédaction, revue) sont autorisés comme aides, jamais comme auteurs non relus.
2.  Tout code issu d’une IA est compris, testé et relu en Pull Request. Son auteur doit pouvoir l’expliquer à l’oral.
3.  Les informations techniques fournies par une IA (API, versions, licences) sont vérifiées dans la documentation officielle.
4.  Ce protocole est versionné dans `/doc` et mis à jour à chaque livraison.

## 10. Dépôt

| Dépôt                | Contenu                                                                          | URL                                            |
|----------------------|----------------------------------------------------------------------------------|------------------------------------------------|
| Principal (monorepo) | `/frontend`, `/backend`, `/ansible`, `compose.yaml`, `/doc`, `.github/workflows` | https://github.com/celia050/Projet-Orbit-Track |

Le `README.md` présente l’équipe, la répartition des features, l’organisation Git, les instructions de lancement (`docker compose up --build`) et la documentation de la feature 12. Le dossier du rendu 2 reste conservé dans `/doc/dossier-lancement.md`. Le présent dossier est maintenu séparément dans `/doc/dossier-rendu3.md`.

**Release du rendu 3 (lien prévu, à vérifier après publication) :** [v0.2.0](https://github.com/celia050/Projet-Orbit-Track/releases/tag/v0.2.0).
