-- ============================================================================
-- PHASE 3: DATABASE PERSISTENCE - EDIT HISTORY TABLES
-- ============================================================================

-- ============================================================================
-- TABLE: edit_history
-- Description: Tracks all code edits performed by users
-- ============================================================================
CREATE TABLE IF NOT EXISTS edit_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    original_code LONGTEXT,
    edited_code LONGTEXT,
    edit_type VARCHAR(50),
    suggestion_source VARCHAR(100),
    suggestion_id BIGINT,
    accepted BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500),
    context VARCHAR(1000),
    lines_changed INT,
    complexity INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_file_path (file_path),
    INDEX idx_created_at (created_at),
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_edit_type (edit_type),
    INDEX idx_accepted (accepted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: user_pattern
-- Description: Tracks patterns in user editing behavior
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_pattern (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    pattern_type VARCHAR(100) NOT NULL,
    frequency INT NOT NULL DEFAULT 0,
    acceptance_rate DOUBLE NOT NULL DEFAULT 0.0,
    description VARCHAR(500),
    examples VARCHAR(1000),
    total_suggestions INT DEFAULT 0,
    accepted_suggestions INT DEFAULT 0,
    last_used TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    
    INDEX idx_user_pattern_id (user_id),
    INDEX idx_pattern_type (pattern_type),
    INDEX idx_user_pattern_type (user_id, pattern_type),
    INDEX idx_acceptance_rate (acceptance_rate),
    INDEX idx_active (active),
    UNIQUE KEY uk_user_pattern (user_id, pattern_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: suggestion_feedback
-- Description: Collects user feedback on AI suggestions
-- ============================================================================
CREATE TABLE IF NOT EXISTS suggestion_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    suggestion_id BIGINT NOT NULL,
    suggestion_type VARCHAR(100),
    suggestion_content LONGTEXT,
    rating INT NOT NULL,
    action VARCHAR(50),
    feedback VARCHAR(1000),
    user_modification LONGTEXT,
    reason VARCHAR(500),
    helpful BOOLEAN,
    relevant BOOLEAN,
    accurate BOOLEAN,
    sentiment VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_feedback_user_id (user_id),
    INDEX idx_feedback_suggestion_id (suggestion_id),
    INDEX idx_feedback_rating (rating),
    INDEX idx_feedback_created (created_at),
    INDEX idx_feedback_user_created (user_id, created_at),
    INDEX idx_action (action),
    INDEX idx_sentiment (sentiment),
    INDEX idx_helpful (helpful)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- VIEWS FOR ANALYTICS
-- ============================================================================

-- View: User Edit Statistics
CREATE OR REPLACE VIEW v_user_edit_stats AS
SELECT 
    eh.user_id,
    COUNT(*) as total_edits,
    SUM(CASE WHEN eh.accepted = TRUE THEN 1 ELSE 0 END) as accepted_edits,
    SUM(CASE WHEN eh.accepted = FALSE THEN 1 ELSE 0 END) as rejected_edits,
    ROUND(SUM(CASE WHEN eh.accepted = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as acceptance_rate,
    COUNT(DISTINCT eh.file_path) as files_edited,
    COUNT(DISTINCT eh.edit_type) as edit_types_used,
    MIN(eh.created_at) as first_edit,
    MAX(eh.created_at) as last_edit
FROM edit_history eh
GROUP BY eh.user_id;

-- View: Edit Type Statistics
CREATE OR REPLACE VIEW v_edit_type_stats AS
SELECT 
    eh.edit_type,
    COUNT(*) as usage_count,
    SUM(CASE WHEN eh.accepted = TRUE THEN 1 ELSE 0 END) as accepted_count,
    ROUND(SUM(CASE WHEN eh.accepted = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as acceptance_rate,
    AVG(eh.lines_changed) as avg_lines_changed
FROM edit_history eh
WHERE eh.edit_type IS NOT NULL
GROUP BY eh.edit_type;

-- View: Suggestion Source Statistics
CREATE OR REPLACE VIEW v_suggestion_source_stats AS
SELECT 
    eh.suggestion_source,
    COUNT(*) as usage_count,
    SUM(CASE WHEN eh.accepted = TRUE THEN 1 ELSE 0 END) as accepted_count,
    ROUND(SUM(CASE WHEN eh.accepted = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as acceptance_rate
FROM edit_history eh
WHERE eh.suggestion_source IS NOT NULL
GROUP BY eh.suggestion_source;

-- View: User Pattern Statistics
CREATE OR REPLACE VIEW v_user_pattern_stats AS
SELECT 
    up.user_id,
    COUNT(*) as total_patterns,
    SUM(up.frequency) as total_pattern_uses,
    AVG(up.acceptance_rate) as avg_acceptance_rate,
    MAX(up.last_used) as last_pattern_used,
    SUM(CASE WHEN up.active = TRUE THEN 1 ELSE 0 END) as active_patterns
FROM user_pattern up
GROUP BY up.user_id;

-- View: Feedback Statistics
CREATE OR REPLACE VIEW v_feedback_stats AS
SELECT 
    sf.user_id,
    COUNT(*) as total_feedback,
    AVG(sf.rating) as avg_rating,
    SUM(CASE WHEN sf.helpful = TRUE THEN 1 ELSE 0 END) as helpful_count,
    SUM(CASE WHEN sf.helpful = FALSE THEN 1 ELSE 0 END) as not_helpful_count,
    SUM(CASE WHEN sf.action = 'accepted' THEN 1 ELSE 0 END) as accepted_suggestions,
    SUM(CASE WHEN sf.action = 'rejected' THEN 1 ELSE 0 END) as rejected_suggestions
FROM suggestion_feedback sf
GROUP BY sf.user_id;

-- ============================================================================
-- STORED PROCEDURES FOR COMMON OPERATIONS
-- ============================================================================

-- Procedure: Get User Analytics
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_get_user_analytics(IN p_user_id VARCHAR(255))
BEGIN
    SELECT 
        'Edit Statistics' as metric_type,
        user_id,
        total_edits,
        accepted_edits,
        rejection_edits,
        acceptance_rate,
        files_edited,
        edit_types_used,
        first_edit,
        last_edit
    FROM v_user_edit_stats
    WHERE user_id = p_user_id;
END //
DELIMITER ;

-- ============================================================================
-- INITIAL DATA (Optional)
-- ============================================================================

-- Insert sample data for testing (commented out)
-- INSERT INTO edit_history (user_id, file_path, edit_type, suggestion_source, accepted, description, created_at)
-- VALUES ('test-user', 'src/main/java/Test.java', 'extract_method', 'AI', TRUE, 'Extracted helper method', NOW());
