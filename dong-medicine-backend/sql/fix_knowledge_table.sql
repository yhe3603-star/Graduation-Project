-- =====================================================
-- Knowledge 表 herb_category 字段修复脚本（简化版）
-- 适用于手动执行
-- =====================================================

-- 1. 添加字段（如果已存在会报错，可忽略）
ALTER TABLE knowledge ADD COLUMN `herb_category` varchar(50) NULL COMMENT '药材分类' AFTER `disease_category`;

-- 2. 更新 herb_category 数据（根据 related_plants 中的植物分类）
-- MySQL 8.0+ 版本使用 JSON_TABLE

-- 更新每条知识记录，取第一个关联植物的分类
UPDATE knowledge k
SET k.herb_category = (
    SELECT p.category
    FROM JSON_TABLE(
        COALESCE(k.related_plants, '[]'),
        '$[*]' COLUMNS (plant_id INT PATH '$')
    ) AS jt
    JOIN plants p ON p.id = jt.plant_id
    LIMIT 1
)
WHERE k.related_plants IS NOT NULL 
  AND k.related_plants != '[]'
  AND k.related_plants != '';

-- 3. 验证结果
SELECT id, title, related_plants, herb_category FROM knowledge LIMIT 20;

-- 4. 统计
SELECT 
    COUNT(*) AS total,
    SUM(CASE WHEN herb_category IS NOT NULL THEN 1 ELSE 0 END) AS has_herb_category
FROM knowledge;
