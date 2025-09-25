CREATE TABLE notification_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    type VARCHAR(50),
    subject VARCHAR(500) NOT NULL,
    body_template TEXT,
    html_template TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


INSERT INTO notification_templates (name, type, subject, body_template, html_template) VALUES
('TASK_ASSIGNED', 'TASK_ASSIGNED', 'Nouvelle tâche assignée: {{taskTitle}}',
 'Bonjour {{userName}}, vous avez été assigné à la tâche "{{taskTitle}}" dans le projet {{projectName}}.',
 '<h2>Nouvelle tâche assignée</h2><p>Bonjour <strong>{{userName}}</strong>,</p><p>Vous avez été assigné à la tâche "<strong>{{taskTitle}}</strong>" dans le projet <em>{{projectName}}</em>.</p>'),

('PROJECT_INVITATION', 'PROJECT_INVITATION', 'Invitation au projet: {{projectName}}',
 'Bonjour {{userName}}, vous avez été invité à rejoindre le projet {{projectName}}.',
 '<h2>Invitation au projet</h2><p>Bonjour <strong>{{userName}}</strong>,</p><p>Vous avez été invité à rejoindre le projet "<strong>{{projectName}}</strong>".</p>'),

('DEADLINE_REMINDER', 'DEADLINE_REMINDER', 'Rappel d''échéance: {{taskTitle}}',
 'Rappel: La tâche "{{taskTitle}}" arrive à échéance le {{deadline}}.',
 '<h2>Rappel d''échéance</h2><p>La tâche "<strong>{{taskTitle}}</strong>" arrive à échéance le <strong>{{deadline}}</strong>.</p>');