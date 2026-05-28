-- ================================================
-- AutoInsight CI/CD Failure Intelligence Platform
-- PostgreSQL Schema
-- ================================================

-- Drop tables if they exist (for clean re-run)
DROP TABLE IF EXISTS incidents CASCADE;
DROP TABLE IF EXISTS uploaded_logs CASCADE;
DROP TABLE IF EXISTS error_patterns CASCADE;

-- ================================================
-- TABLE: uploaded_logs
-- Stores metadata of uploaded CI/CD log files
-- ================================================
CREATE TABLE uploaded_logs (
    id                  BIGSERIAL PRIMARY KEY,
    file_name           VARCHAR(255)    NOT NULL,
    original_file_name  VARCHAR(255)    NOT NULL,
    file_size_bytes     BIGINT          NOT NULL,
    content_type        VARCHAR(100),
    file_path           VARCHAR(500)    NOT NULL,
    upload_status       VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    pipeline_name       VARCHAR(255),
    branch_name         VARCHAR(255),
    uploaded_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at        TIMESTAMP
);

COMMENT ON TABLE uploaded_logs IS 'Stores metadata for all uploaded CI/CD log files';
COMMENT ON COLUMN uploaded_logs.upload_status IS 'PENDING | PROCESSING | PROCESSED | FAILED';

-- ================================================
-- TABLE: incidents
-- Stores parsed failure incidents from logs
-- ================================================
CREATE TABLE incidents (
    id                  BIGSERIAL PRIMARY KEY,
    log_id              BIGINT          NOT NULL REFERENCES uploaded_logs(id) ON DELETE CASCADE,
    title               VARCHAR(500)    NOT NULL,
    failure_category    VARCHAR(100)    NOT NULL,
    severity_level      VARCHAR(50)     NOT NULL,
    summary             TEXT            NOT NULL,
    probable_root_cause TEXT,
    suggested_fix       TEXT,
    error_count         INT             NOT NULL DEFAULT 0,
    warning_count       INT             NOT NULL DEFAULT 0,
    exception_count     INT             NOT NULL DEFAULT 0,
    stack_trace_count   INT             NOT NULL DEFAULT 0,
    raw_error_lines     TEXT,
    raw_exception_lines TEXT,
    pipeline_stage      VARCHAR(255),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE incidents IS 'Stores parsed incidents detected from CI/CD log analysis';
COMMENT ON COLUMN incidents.failure_category IS 'BUILD_FAILURE | TEST_FAILURE | DEPENDENCY_FAILURE | DEPLOYMENT_FAILURE | UNKNOWN_FAILURE';
COMMENT ON COLUMN incidents.severity_level IS 'CRITICAL | HIGH | MEDIUM | LOW';

-- ================================================
-- TABLE: error_patterns
-- Configurable regex patterns for failure detection
-- ================================================
CREATE TABLE error_patterns (
    id              BIGSERIAL PRIMARY KEY,
    pattern_name    VARCHAR(255)    NOT NULL UNIQUE,
    regex_pattern   TEXT            NOT NULL,
    failure_category VARCHAR(100)   NOT NULL,
    description     TEXT,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    priority        INT             NOT NULL DEFAULT 100,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE error_patterns IS 'Configurable regex patterns used for log failure categorization';

-- ================================================
-- INDEXES
-- ================================================
CREATE INDEX idx_incidents_log_id          ON incidents(log_id);
CREATE INDEX idx_incidents_severity        ON incidents(severity_level);
CREATE INDEX idx_incidents_category        ON incidents(failure_category);
CREATE INDEX idx_incidents_created_at      ON incidents(created_at DESC);
CREATE INDEX idx_uploaded_logs_status      ON uploaded_logs(upload_status);
CREATE INDEX idx_uploaded_logs_uploaded_at ON uploaded_logs(uploaded_at DESC);
CREATE INDEX idx_error_patterns_category   ON error_patterns(failure_category);
CREATE INDEX idx_error_patterns_active     ON error_patterns(is_active);

-- ================================================
-- SEED DATA: Default Error Patterns
-- ================================================
INSERT INTO error_patterns (pattern_name, regex_pattern, failure_category, description, priority) VALUES
('Maven Build Failure',      '(?i)(BUILD FAILURE|BUILD FAILED)',                    'BUILD_FAILURE',      'Maven/Gradle build compilation failure',         10),
('Compilation Error',        '(?i)(compilation failed|cannot find symbol|error:)',  'BUILD_FAILURE',      'Java compilation error',                         20),
('JUnit Test Failure',       '(?i)(Tests run:.*Failures:|TEST FAILED|FAILED)',      'TEST_FAILURE',       'JUnit or other unit test failure',               10),
('Test Assertion Error',     '(?i)(AssertionError|expected.*but was)',               'TEST_FAILURE',       'Test assertion mismatch',                        20),
('NPE Exception',            '(?i)(NullPointerException)',                           'BUILD_FAILURE',      'Null pointer exception during build/test',       15),
('Dependency Not Found',     '(?i)(Could not resolve|Artifact.*not found|dependency.*missing)', 'DEPENDENCY_FAILURE', 'Maven/Gradle dependency resolution failure', 10),
('Connection Refused',       '(?i)(Connection refused|ECONNREFUSED)',               'DEPLOYMENT_FAILURE', 'Service connection refused during deployment',    10),
('Port Binding Error',       '(?i)(Address already in use|bind.*failed)',           'DEPLOYMENT_FAILURE', 'Port binding failure on deployment',              15),
('Container Failure',        '(?i)(container.*exit|docker.*error|OOMKilled)',       'DEPLOYMENT_FAILURE', 'Container/Docker failure during deployment',      10),
('Out of Memory',            '(?i)(OutOfMemoryError|java.lang.OutOfMemory)',        'DEPLOYMENT_FAILURE', 'JVM out of memory during deployment or runtime',  5),
('Stack Overflow',           '(?i)(StackOverflowError)',                            'BUILD_FAILURE',      'Stack overflow error during execution',           15),
('Network Timeout',          '(?i)(timeout|timed out|Read timed out)',              'DEPENDENCY_FAILURE', 'Network timeout reaching dependencies/registry',  20),
('SSH/Deploy Error',         '(?i)(ssh.*error|deploy.*failed|kubectl.*error)',      'DEPLOYMENT_FAILURE', 'SSH or Kubernetes deployment error',              10),
('Generic Exception',        '(?i)(Exception|Error:)',                              'UNKNOWN_FAILURE',    'Generic exception caught in logs',               90);
