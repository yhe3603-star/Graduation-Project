-- 移除plants表的difficulty字段
-- 植物识别游戏现在从所有植物中随机抽取，不再按难度分类

-- 1. 先删除difficulty字段的索引
DROP INDEX idx_plants_difficulty ON plants;

-- 2. 删除difficulty字段
ALTER TABLE plants DROP COLUMN difficulty;
