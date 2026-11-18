
# OrbitTrack — Modèle des événements astronomiques

Responsable : Ritej Baaboura  
Statut : Validé par l'équipe — 18 novembre 2026

## Table events

| Champ | Type SQL | Description |
|---|---|---|
| id | BIGSERIAL | Identifiant unique |
| name | VARCHAR(200) | Nom du phénomène |
| type | VARCHAR(40) | Type de phénomène |
| description | TEXT | Description |
| start_at | TIMESTAMPTZ | Début |
| end_at | TIMESTAMPTZ | Fin |
| visibility_zone | VARCHAR(255) | Zone de visibilité |
| latitude | DOUBLE PRECISION | Latitude de référence |
| longitude | DOUBLE PRECISION | Longitude de référence |
| visibility_radius_km | DOUBLE PRECISION | Rayon de visibilité simplifié |
| status | VARCHAR(20) | SCHEDULED, UPDATED, CANCELLED |
| source_name | VARCHAR(100) | Nom de la source |
| source_url | TEXT | Adresse de la source |
| source_event_id | VARCHAR(200) | Identifiant externe |
| source_updated_at | TIMESTAMPTZ | Actualisation de la source |
| is_demo | BOOLEAN | Donnée fictive |
| created_at | TIMESTAMPTZ | Date de création |
| updated_at | TIMESTAMPTZ | Dernière modification |

## Règles métier

- Les horaires sont stockés en UTC.
- Un événement doit avoir un nom, un type, une date et une zone de visibilité.
- Un événement peut être programmé, modifié ou annulé.
- Les événements importés conservent leur source.
- Les doublons doivent être détectés et fusionnés.
- Les événements fictifs sont identifiés par is_demo.
- La visibilité géographique de la V1 est simplifiée.
- Les routes GET /api/events sont publiques.
- La gestion administrative des lieux utilise /api/admin/.

## Intégration

Les autres modules utiliseront events.id pour associer
les participations, forums, notifications et exports agenda.
