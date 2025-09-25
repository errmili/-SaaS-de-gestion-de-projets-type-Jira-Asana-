
CREATE INDEX IF NOT EXISTS idx_projects_tenant_status ON projects(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_projects_tenant_created_by ON projects(tenant_id, created_by);
CREATE INDEX IF NOT EXISTS idx_projects_tenant_priority ON projects(tenant_id, priority);

CREATE INDEX IF NOT EXISTS idx_tasks_project_status ON tasks(project_id, status);
CREATE INDEX IF NOT EXISTS idx_tasks_tenant_assignee ON tasks(tenant_id, assignee_id);
CREATE INDEX IF NOT EXISTS idx_tasks_tenant_status ON tasks(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_tasks_sprint_status ON tasks(sprint_id, status) WHERE sprint_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_tasks_tenant_reporter ON tasks(tenant_id, reporter_id);

CREATE INDEX IF NOT EXISTS idx_sprints_project_status ON sprints(project_id, status);
CREATE INDEX IF NOT EXISTS idx_sprints_tenant_status ON sprints(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_sprints_tenant_created_by ON sprints(tenant_id, created_by);


CREATE INDEX IF NOT EXISTS idx_tasks_overdue ON tasks(tenant_id, due_date)
    WHERE due_date IS NOT NULL AND status NOT IN ('DONE');

CREATE INDEX IF NOT EXISTS idx_tasks_active_sprint ON tasks(sprint_id, status)
    WHERE sprint_id IS NOT NULL AND status != 'DONE';

CREATE INDEX IF NOT EXISTS idx_tasks_backlog ON tasks(project_id, tenant_id, created_at)
    WHERE sprint_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_tasks_recent_updates ON tasks(tenant_id, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_projects_recent ON projects(tenant_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comments_recent ON task_comments(tenant_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_project_members_user_tenant ON project_members(user_id, project_id);

CREATE INDEX IF NOT EXISTS idx_task_attachments_task_tenant ON task_attachments(task_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_task_attachments_uploaded_by_tenant ON task_attachments(tenant_id, uploaded_by);

CREATE INDEX IF NOT EXISTS idx_projects_active ON projects(tenant_id, created_at)
    WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_sprints_active ON sprints(project_id, start_date, end_date)
    WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_tasks_todo ON tasks(project_id, created_at)
    WHERE status = 'TODO';

CREATE INDEX IF NOT EXISTS idx_tasks_in_progress ON tasks(tenant_id, assignee_id, updated_at)
    WHERE status = 'IN_PROGRESS';