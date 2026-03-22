-- =====================================================
-- 全文索引优化脚本
-- 用于提升植物搜索性能
-- 执行前请确保数据库备份
-- =====================================================

-- 1. 检查 MySQL 版本（需要 5.6+ 支持全文索引）
SELECT VERSION();

-- 2. 为 plants 表添加全文索引（支持中文分词）
-- 使用 ngram 解析器支持中文分词
ALTER TABLE plants 
ADD FULLTEXT INDEX ft_plants_search(name_cn, name_dong, efficacy, story) 
WITH PARSER ngram;

-- 3. 验证索引创建成功
SHOW INDEX FROM plants WHERE Key_name = 'ft_plants_search';

-- 4. 测试全文搜索
SELECT * FROM plants 
WHERE MATCH(name_cn, name_dong, efficacy, story) 
AGAINST('钩藤' IN NATURAL LANGUAGE MODE);

-- 5. 查看执行计划（验证索引使用）
EXPLAIN SELECT * FROM plants 
WHERE MATCH(name_cn, name_dong, efficacy, story) 
AGAINST('钩藤' IN NATURAL LANGUAGE MODE);

-- =====================================================
-- 性能对比测试
-- =====================================================

-- LIKE 查询（旧方式）
SELECT COUNT(*) FROM plants 
WHERE name_cn LIKE '%钩藤%' 
   OR name_dong LIKE '%钩藤%' 
   OR efficacy LIKE '%钩藤%' 
   OR story LIKE '%钩藤%';

-- 全文搜索（新方式）
SELECT COUNT(*) FROM plants 
WHERE MATCH(name_cn, name_dong, efficacy, story) 
AGAINST('钩藤' IN NATURAL LANGUAGE MODE);

-- =====================================================
-- 其他优化索引
-- =====================================================

-- 为常用查询字段添加索引
CREATE INDEX idx_plants_category ON plants(category);
CREATE INDEX idx_plants_difficulty ON plants(difficulty);
CREATE INDEX idx_plants_view_count ON plants(view_count);

-- 组合索引（用于分类过滤）
CREATE INDEX idx_plants_category_usage ON plants(category, usage_way);

-- =====================================================
-- 如果需要回滚
-- =====================================================
-- ALTER TABLE plants DROP INDEX ft_plants_search;
-- DROP INDEX idx_plants_category ON plants;
-- DROP INDEX idx_plants_difficulty ON plants;
-- DROP INDEX idx_plants_view_count ON plants;
-- DROP INDEX idx_plants_category_usage ON plants;
