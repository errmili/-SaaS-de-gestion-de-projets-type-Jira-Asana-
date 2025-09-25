
ALTER TABLE projects ADD CONSTRAINT check_project_status
    CHECK (status IN ('ACTIVE', 'COMPLETED', 'ARCHIVED', 'ON_HOLD'));

ALTER TABLE projects ADD CONSTRAINT check_project_priority
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));

ALTER TABLE tasks ADD CONSTRAINT check_task_status
    CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'BLOCKED'));

ALTER TABLE tasks ADD CONSTRAINT check_task_priority
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));

ALTER TABLE tasks ADD CONSTRAINT check_task_type
    CHECK (task_type IN ('TASK', 'BUG', 'STORY', 'EPIC', 'SUBTASK'));

ALTER TABLE sprints ADD CONSTRAINT check_sprint_status
    CHECK (status IN ('PLANNING', 'ACTIVE', 'COMPLETED', 'CANCELLED'));

ALTER TABLE project_members ADD CONSTRAINT check_member_role
    CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER'));

ALTER TABLE projects ADD CONSTRAINT check_project_dates
    CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date);

ALTER TABLE sprints ADD CONSTRAINT check_sprint_dates
    CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date);

ALTER TABLE tasks ADD CONSTRAINT check_story_points
    CHECK (story_points IS NULL OR story_points >= 0);

ALTER TABLE task_attachments ADD CONSTRAINT check_file_size
    CHECK (file_size IS NULL OR file_size >= 0);

ALTER TABLE projects ADD CONSTRAINT check_project_key_format
    CHECK (key ~ '^[A-Z][A-Z0-9]*$' AND LENGTH(key) BETWEEN 2 AND 10);

ALTER TABLE projects ADD CONSTRAINT check_project_name_length
    CHECK (LENGTH(TRIM(name)) >= 3);

ALTER TABLE sprints ADD CONSTRAINT check_sprint_name_length
    CHECK (LENGTH(TRIM(name)) >= 3);

ALTER TABLE tasks ADD CONSTRAINT check_task_title_length
    CHECK (LENGTH(TRIM(title)) >= 3);

ALTER TABLE task_comments ADD CONSTRAINT check_comment_content
    CHECK (LENGTH(TRIM(content)) >= 1);

ALTER TABLE task_attachments ADD CONSTRAINT check_attachment_filename
    CHECK (LENGTH(TRIM(file_name)) >= 1);