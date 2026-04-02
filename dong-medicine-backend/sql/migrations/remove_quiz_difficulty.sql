-- 移除quiz_questions表的difficulty字段
-- 答题模块现在从所有题目随机抽取，不再按难度区分

-- 删除difficulty字段
ALTER TABLE quiz_questions DROP COLUMN difficulty;
