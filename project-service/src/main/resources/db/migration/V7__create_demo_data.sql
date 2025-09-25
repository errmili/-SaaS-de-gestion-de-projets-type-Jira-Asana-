
INSERT INTO projects (id, tenant_id, name, description, key, status, priority, created_by)
VALUES (
    gen_random_uuid(),
    'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid,
    'Demo Project',
    'Projet de démonstration pour tester les fonctionnalités du système de gestion de projets',
    'DEMO',
    'ACTIVE',
    'HIGH',
    'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid
);

DO $$
DECLARE
    demo_project_id UUID;
    demo_sprint_id UUID;
BEGIN

    SELECT id INTO demo_project_id FROM projects WHERE key = 'DEMO';

    INSERT INTO project_members (project_id, user_id, role)
    VALUES (
        demo_project_id,
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid,
        'OWNER'
    );

    INSERT INTO sprints (id, tenant_id, project_id, name, goal, status, start_date, end_date, created_by)
    VALUES (
        gen_random_uuid(),
        'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid,
        demo_project_id,
        'Sprint Demo 1',
        'Premier sprint de démonstration avec des tâches exemple pour tester le workflow',
        'ACTIVE',
        CURRENT_DATE,
        CURRENT_DATE + INTERVAL '14 days',
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid
    );

    SELECT id INTO demo_sprint_id FROM sprints WHERE project_id = demo_project_id AND name = 'Sprint Demo 1';

    INSERT INTO tasks (id, tenant_id, project_id, title, description, task_key, status, priority, task_type, story_points, assignee_id, reporter_id, sprint_id)
    VALUES
    (
        gen_random_uuid(),
        'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid,
        demo_project_id,
        'Créer l''interface utilisateur principale',
        'Développer l''interface utilisateur principale de l''application avec React et Tailwind CSS',
        'DEMO-1',
        'IN_PROGRESS',
        'HIGH',
        'STORY',
        8,
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid,
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid,
        demo_sprint_id
    ),
    (
        gen_random_uuid(),
        'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid,
        demo_project_id,
        'Implémenter l''authentification JWT',
        'Mettre en place le système d''authentification JWT avec Spring Security',
        'DEMO-2',
        'TODO',
        'MEDIUM',
        'TASK',
        5,
        NULL,
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid,
        demo_sprint_id
    ),
    (
        gen_random_uuid(),
        'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid,
        demo_project_id,
        'Corriger bug de connexion intermittente',
        'Résoudre le problème de connexion intermittente lors du login utilisateur',
        'DEMO-3',
        'DONE',
        'CRITICAL',
        'BUG',
        3,
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid,
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid,
        demo_sprint_id
    ),
    (
        gen_random_uuid(),
        'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid,
        demo_project_id,
        'Écrire tests unitaires pour les services',
        'Créer une suite complète de tests unitaires pour tous les services principaux',
        'DEMO-4',
        'TODO',
        'MEDIUM',
        'TASK',
        3,
        NULL,
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid,
        NULL
    ),
    (
        gen_random_uuid(),
        'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid,
        demo_project_id,
        'Optimiser les performances de la base de données',
        'Analyser et optimiser les requêtes SQL les plus coûteuses',
        'DEMO-5',
        'IN_REVIEW',
        'LOW',
        'TASK',
        5,
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid,
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid,
        demo_sprint_id
    );

    INSERT INTO task_comments (id, tenant_id, task_id, author_id, content)
    SELECT
        gen_random_uuid(),
        'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid,
        t.id,
        'a1b2c3d4-e5f6-7890-1234-567890abcdef'::uuid,
        CASE
            WHEN t.task_key = 'DEMO-1' THEN 'Travail en cours sur les composants React. Interface utilisateur presque terminée. Reste à finaliser le responsive design.'
            WHEN t.task_key = 'DEMO-3' THEN 'Bug résolu en mettant à jour la configuration du timeout de connexion dans application.yml. Tests de validation effectués.'
            WHEN t.task_key = 'DEMO-5' THEN 'Analyse terminée. Identifié 3 requêtes principales à optimiser. Implémentation des index en cours.'
        END
    FROM tasks t
    WHERE t.project_id = demo_project_id
    AND t.task_key IN ('DEMO-1', 'DEMO-3', 'DEMO-5');

END $$;