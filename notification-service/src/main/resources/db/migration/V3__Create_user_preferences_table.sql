CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    email_notifications BOOLEAN DEFAULT true,
    push_notifications BOOLEAN DEFAULT true,
    websocket_notifications BOOLEAN DEFAULT true,
    task_assigned BOOLEAN DEFAULT true,
    task_updated BOOLEAN DEFAULT true,
    project_invitation BOOLEAN DEFAULT true,
    deadline_reminder BOOLEAN DEFAULT true,
    comment_mentions BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);