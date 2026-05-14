-- 添加评论表复合索引，优化按类型+目标+状态的查询性能
CREATE INDEX idx_target_status_created ON comments (target_type, target_id, status, created_at DESC);
