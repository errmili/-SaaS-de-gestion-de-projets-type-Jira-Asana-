
CREATE SCHEMA IF NOT EXISTS auth_db;
CREATE SCHEMA IF NOT EXISTS project_db;
CREATE SCHEMA IF NOT EXISTS file_db;
CREATE SCHEMA IF NOT EXISTS analytics_db;
CREATE SCHEMA IF NOT EXISTS notification_db;

GRANT ALL PRIVILEGES ON SCHEMA auth_db TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA project_db TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA file_db TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA analytics_db TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA notification_db TO postgres;


CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


DO $$ BEGIN RAISE NOTICE 'Schémas créés: auth_db, project_db, file_db, analytics_db, notification_db'; END $$;