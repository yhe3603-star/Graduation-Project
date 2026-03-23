-- =====================================================
-- Users 表添加 status 字段（用户状态）
-- 用于实现用户封禁功能
-- =====================================================

-- 1. 添加 status 字段（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'users';
SET @columnname = 'status';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = @dbname 
        AND TABLE_NAME = @tablename 
        AND COLUMN_NAME = @columnname
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `', @columnname, '` varchar(20) NULL DEFAULT ''active'' COMMENT ''用户状态: active/banned'' AFTER `role`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. 更新现有用户状态为 active（如果为 NULL）
UPDATE users SET status = 'active' WHERE status IS NULL;

-- 3. 验证结果
SELECT id, username, role, status, created_at FROM users LIMIT 10;
