-- ============================================
-- 侗乡医药数字展示平台 - 数据库性能优化索引
-- 执行方式: mysql -u root -p dong_medicine < optimize_indexes.sql
-- ============================================

-- 检查现有索引
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'dong_medicine'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- ============================================
-- 1. plants 表索引优化
-- ============================================

-- 分类查询索引
CREATE INDEX IF NOT EXISTS idx_plants_category ON plants(category);

-- 用法方式查询索引
CREATE INDEX IF NOT EXISTS idx_plants_usage_way ON plants(usage_way);

-- 组合索引：分类 + 用法（常用筛选场景）
CREATE INDEX IF NOT EXISTS idx_plants_category_usage ON plants(category, usage_way);

-- 浏览量排序索引
CREATE INDEX IF NOT EXISTS idx_plants_view_count ON plants(view_count DESC);

-- 收藏量排序索引
CREATE INDEX IF NOT EXISTS idx_plants_favorite_count ON plants(favorite_count DESC);

-- ============================================
-- 2. knowledge 表索引优化
-- ============================================

-- 类型索引
CREATE INDEX IF NOT EXISTS idx_knowledge_type ON knowledge(type);

-- 疗法分类索引
CREATE INDEX IF NOT EXISTS idx_knowledge_therapy ON knowledge(therapy_category);

-- 疾病分类索引
CREATE INDEX IF NOT EXISTS idx_knowledge_disease ON knowledge(disease_category);

-- 组合索引：类型 + 疗法分类
CREATE INDEX IF NOT EXISTS idx_knowledge_type_therapy ON knowledge(type, therapy_category);

-- 浏览量排序索引
CREATE INDEX IF NOT EXISTS idx_knowledge_view_count ON knowledge(view_count DESC);

-- ============================================
-- 3. inheritors 表索引优化
-- ============================================

-- 等级索引
CREATE INDEX IF NOT EXISTS idx_inheritors_level ON inheritors(level);

-- 从业年限索引
CREATE INDEX IF NOT EXISTS idx_inheritors_experience ON inheritors(experience_years DESC);

-- ============================================
-- 4. comments 表索引优化
-- ============================================

-- 目标类型和ID组合索引（最常用查询）
CREATE INDEX IF NOT EXISTS idx_comments_target ON comments(target_type, target_id);

-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);

-- 状态索引（审核状态）
CREATE INDEX IF NOT EXISTS idx_comments_status ON comments(status);

-- 创建时间索引（排序）
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at DESC);

-- 回复索引
CREATE INDEX IF NOT EXISTS idx_comments_reply_to ON comments(reply_to_id);

-- ============================================
-- 5. favorites 表索引优化
-- ============================================

-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);

-- 目标类型和ID组合索引
CREATE INDEX IF NOT EXISTS idx_favorites_target ON favorites(target_type, target_id);

-- 唯一组合索引（防止重复收藏，如果不存在）
-- CREATE UNIQUE INDEX IF NOT EXISTS uk_favorites_user_target ON favorites(user_id, target_type, target_id);

-- ============================================
-- 6. feedback 表索引优化
-- ============================================

-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_feedback_user_id ON feedback(user_id);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_feedback_status ON feedback(status);

-- 类型索引
CREATE INDEX IF NOT EXISTS idx_feedback_type ON feedback(type);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_feedback_created_at ON feedback(created_at DESC);

-- ============================================
-- 7. quiz_questions 表索引优化
-- ============================================

-- 分类索引
CREATE INDEX IF NOT EXISTS idx_quiz_category ON quiz_questions(category);

-- 难度索引
CREATE INDEX IF NOT EXISTS idx_quiz_difficulty ON quiz_questions(difficulty);

-- 组合索引
CREATE INDEX IF NOT EXISTS idx_quiz_category_difficulty ON quiz_questions(category, difficulty);

-- ============================================
-- 8. quiz_record 表索引优化
-- ============================================

-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_quiz_record_user_id ON quiz_record(user_id);

-- 分数排序索引
CREATE INDEX IF NOT EXISTS idx_quiz_record_score ON quiz_record(score DESC);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_quiz_record_created_at ON quiz_record(created_at DESC);

-- ============================================
-- 9. plant_game_record 表索引优化
-- ============================================

-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_game_record_user_id ON plant_game_record(user_id);

-- 难度索引
CREATE INDEX IF NOT EXISTS idx_game_record_difficulty ON plant_game_record(difficulty);

-- 分数排序索引
CREATE INDEX IF NOT EXISTS idx_game_record_score ON plant_game_record(score DESC);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_game_record_created_at ON plant_game_record(created_at DESC);

-- ============================================
-- 10. qa 表索引优化
-- ============================================

-- 分类索引
CREATE INDEX IF NOT EXISTS idx_qa_category ON qa(category);

-- 热度索引
CREATE INDEX IF NOT EXISTS idx_qa_popularity ON qa(popularity DESC);

-- ============================================
-- 11. resources 表索引优化
-- ============================================

-- 分类索引
CREATE INDEX IF NOT EXISTS idx_resources_category ON resources(category);

-- 下载量排序索引
CREATE INDEX IF NOT EXISTS idx_resources_download_count ON resources(download_count DESC);

-- ============================================
-- 12. operation_log 表索引优化
-- ============================================

-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_log_user_id ON operation_log(user_id);

-- 模块索引
CREATE INDEX IF NOT EXISTS idx_log_module ON operation_log(module);

-- 操作类型索引
CREATE INDEX IF NOT EXISTS idx_log_type ON operation_log(type);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_log_created_at ON operation_log(created_at DESC);

-- 组合索引：用户 + 时间
CREATE INDEX IF NOT EXISTS idx_log_user_time ON operation_log(user_id, created_at DESC);

-- ============================================
-- 13. users 表索引优化
-- ============================================

-- 用户名已存在唯一索引
-- 角色索引
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at DESC);

-- ============================================
-- 验证索引创建结果
-- ============================================

SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'dong_medicine'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- ============================================
-- 分析表统计信息
-- ============================================

ANALYZE TABLE plants;
ANALYZE TABLE knowledge;
ANALYZE TABLE inheritors;
ANALYZE TABLE comments;
ANALYZE TABLE favorites;
ANALYZE TABLE feedback;
ANALYZE TABLE quiz_questions;
ANALYZE TABLE quiz_record;
ANALYZE TABLE plant_game_record;
ANALYZE TABLE qa;
ANALYZE TABLE resources;
ANALYZE TABLE operation_log;
ANALYZE TABLE users;
