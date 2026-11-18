
-- OrbitTrack - Données de démonstration
-- Responsable : Ritej Baaboura
-- Ces événements sont fictifs et ne constituent
-- pas des prévisions astronomiques réelles.

INSERT INTO events (
    name, type, description,
    start_at, end_at,
    visibility_zone, latitude, longitude,
    visibility_radius_km, status,
    source_name, source_event_id,
    source_updated_at, is_demo
)
VALUES
(
    'Pluie de météores des Géminides',
    'METEOR_SHOWER',
    'Simulation de la pluie de météores des Géminides.',
    '2026-12-14 01:00:00+00',
    '2026-12-14 05:00:00+00',
    'France métropolitaine',
    48.8566, 2.3522, 500,
    'SCHEDULED',
    'DEMO', 'demo-001', NOW(), TRUE
),
(
    'Conjonction Lune-Jupiter',
    'CONJUNCTION',
    'Simulation du rapprochement apparent entre la Lune et Jupiter.',
    '2026-12-20 18:00:00+00',
    '2026-12-20 21:00:00+00',
    'Île-de-France',
    48.8566, 2.3522, 100,
    'SCHEDULED',
    'DEMO', 'demo-002', NOW(), TRUE
),
(
    'Passage de la Station spatiale internationale',
    'ISS_PASS',
    'Simulation d’un passage visible de l’ISS.',
    '2026-12-22 19:00:00+00',
    '2026-12-22 19:10:00+00',
    'Île-de-France',
    48.8566, 2.3522, 80,
    'SCHEDULED',
    'DEMO', 'demo-003', NOW(), TRUE
),
(
    'Éclipse lunaire simulée',
    'LUNAR_ECLIPSE',
    'Éclipse lunaire fictive pour démonstration.',
    '2027-01-12 21:00:00+00',
    '2027-01-13 00:00:00+00',
    'France métropolitaine',
    46.6034, 1.8883, 600,
    'SCHEDULED',
    'DEMO', 'demo-004', NOW(), TRUE
),
(
    'Alignement de planètes',
    'PLANETARY_ALIGNMENT',
    'Alignement apparent fictif de plusieurs planètes.',
    '2027-01-18 17:30:00+00',
    '2027-01-18 20:00:00+00',
    'France métropolitaine',
    46.6034, 1.8883, 600,
    'SCHEDULED',
    'DEMO', 'demo-005', NOW(), TRUE
),
(
    'Pluie de météores des Quadrantides',
    'METEOR_SHOWER',
    'Simulation d’une activité météorique intense.',
    '2027-01-25 02:00:00+00',
    '2027-01-25 05:00:00+00',
    'France métropolitaine',
    45.7640, 4.8357, 500,
    'SCHEDULED',
    'DEMO', 'demo-006', NOW(), TRUE
),
(
    'Conjonction Vénus-Saturne',
    'CONJUNCTION',
    'Rapprochement apparent fictif entre Vénus et Saturne.',
    '2027-02-04 18:30:00+00',
    '2027-02-04 21:00:00+00',
    'Sud de la France',
    43.2965, 5.3698, 250,
    'SCHEDULED',
    'DEMO', 'demo-007', NOW(), TRUE
),
(
    'Passage ISS au-dessus de Lyon',
    'ISS_PASS',
    'Simulation du passage de l’ISS à Lyon.',
    '2027-02-10 19:15:00+00',
    '2027-02-10 19:25:00+00',
    'Région lyonnaise',
    45.7640, 4.8357, 100,
    'SCHEDULED',
    'DEMO', 'demo-008', NOW(), TRUE
),
(
    'Éclipse solaire simulée',
    'SOLAR_ECLIPSE',
    'Éclipse solaire fictive, non observable réellement à cette date.',
    '2027-03-15 10:00:00+00',
    '2027-03-15 12:00:00+00',
    'Sud-Ouest de la France',
    44.8378, -0.5792, 200,
    'SCHEDULED',
    'DEMO', 'demo-009', NOW(), TRUE
),
(
    'Observation de Mars',
    'PLANET_OBSERVATION',
    'Séance fictive d’observation de la planète Mars.',
    '2027-03-22 20:00:00+00',
    '2027-03-22 23:00:00+00',
    'Île-de-France',
    48.8566, 2.3522, 150,
    'SCHEDULED',
    'DEMO', 'demo-010', NOW(), TRUE
);
