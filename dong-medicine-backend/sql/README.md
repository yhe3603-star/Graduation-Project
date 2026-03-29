# SQL文件目录说明

## 文件夹结构

本目录包含项目的SQL文件，用于数据库初始化和维护。

```
sql/
├── add_user_status.sql        # 添加用户状态字段
├── dong_medicine.sql          # 数据库初始化脚本
├── fix_knowledge_table.sql    # 修复知识库表结构
├── fulltext_index.sql         # 全文索引创建脚本
└── optimize_indexes.sql       # 索引优化脚本
```

## 详细说明

### 1. dong_medicine.sql

**功能**：数据库初始化脚本，创建所有表结构和初始数据。

**主要内容**：
- 创建数据库（如果不存在）
- 创建所有表结构
- 创建索引
- 插入初始数据
- 创建存储过程和触发器（如果有）

**使用方法**：

```bash
# 执行初始化脚本
mysql -u root -p < dong_medicine.sql

# 或者在MySQL客户端中执行
source /path/to/dong_medicine.sql
```

**表结构**：

- `users`：用户表
- `plants`：药材表
- `inheritors`：传承人表
- `knowledge`：知识库表
- `comments`：评论表
- `favorites`：收藏表
- `feedback`：意见反馈表
- `qa`：问答表
- `quiz_questions`：答题题目表
- `quiz_record`：答题记录表
- `plant_game_record`：植物游戏记录表
- `resources`：资源表
- `operation_log`：操作日志表

### 2. add_user_status.sql

**功能**：添加用户状态字段。

**主要内容**：
- 为`users`表添加`status`字段
- 设置默认值为`'normal'`
- 更新现有数据

**使用方法**：

```bash
mysql -u root -p dong_medicine < add_user_status.sql
```

### 3. fix_knowledge_table.sql

**功能**：修复知识库表结构。

**主要内容**：
- 调整`knowledge`表的字段类型
- 添加或修改字段
- 修复约束

**使用方法**：

```bash
mysql -u root -p dong_medicine < fix_knowledge_table.sql
```

### 4. fulltext_index.sql

**功能**：创建全文索引，用于搜索功能。

**主要内容**：
- 为`plants`表的`name`、`description`字段创建全文索引
- 为`knowledge`表的`title`、`content`字段创建全文索引
- 为`inheritors`表的`name`、`bio`字段创建全文索引
- 为`qa`表的`title`、`content`字段创建全文索引

**使用方法**：

```bash
mysql -u root -p dong_medicine < fulltext_index.sql
```

### 5. optimize_indexes.sql

**功能**：优化数据库索引，提高查询性能。

**主要内容**：
- 为各表创建适当的索引
- 优化现有索引
- 分析表统计信息

**使用方法**：

```bash
mysql -u root -p dong_medicine < optimize_indexes.sql
```

## 数据库设计说明

### 1. 数据库结构

**数据库名称**：`dong_medicine`

**字符集**：`utf8mb4`
**校对规则**：`utf8mb4_unicode_ci`

### 2. 表结构说明

#### 2.1 users表

**功能**：存储用户信息。

**主要字段**：
- `id`：用户ID（主键）
- `username`：用户名（唯一）
- `password`：密码（加密）
- `email`：邮箱（唯一）
- `phone`：手机号
- `nickname`：昵称
- `avatar`：头像
- `role`：角色（admin、user、guest）
- `status`：状态（normal、disabled）
- `last_login_at`：最后登录时间
- `created_at`：创建时间
- `updated_at`：更新时间

#### 2.2 plants表

**功能**：存储药材信息。

**主要字段**：
- `id`：药材ID（主键）
- `name`：名称
- `latin_name`：拉丁名
- `category`：分类
- `usage_way`：用法
- `effect`：功效
- `distribution`：分布
- `description`：描述
- `image_url`：图片URL
- `view_count`：浏览量
- `favorite_count`：收藏量
- `created_at`：创建时间
- `updated_at`：更新时间

#### 2.3 inheritors表

**功能**：存储传承人信息。

**主要字段**：
- `id`：传承人ID（主键）
- `name`：姓名
- `level`：级别（国家级、自治区级、市级）
- `specialties`：技艺特色
- `experience_years`：从业年限
- `bio`：个人简介
- `representative_cases`：代表案例
- `honors`：荣誉资质
- `created_at`：创建时间
- `updated_at`：更新时间

#### 2.4 knowledge表

**功能**：存储知识库信息。

**主要字段**：
- `id`：知识ID（主键）
- `title`：标题
- `content`：内容
- `type`：类型（理论知识、实践经验、历史文化）
- `therapy_category`：疗法分类
- `disease_category`：疾病分类
- `view_count`：浏览量
- `created_at`：创建时间
- `updated_at`：更新时间

#### 2.5 comments表

**功能**：存储评论信息。

**主要字段**：
- `id`：评论ID（主键）
- `target_type`：目标类型（plant、inheritor、knowledge等）
- `target_id`：目标ID
- `content`：评论内容
- `user_id`：用户ID
- `reply_to_id`：回复ID
- `status`：状态（normal、pending、deleted）
- `created_at`：创建时间
- `updated_at`：更新时间

#### 2.6 favorites表

**功能**：存储收藏信息。

**主要字段**：
- `id`：收藏ID（主键）
- `user_id`：用户ID
- `target_type`：目标类型
- `target_id`：目标ID
- `created_at`：创建时间

#### 2.7 feedback表

**功能**：存储意见反馈信息。

**主要字段**：
- `id`：反馈ID（主键）
- `user_id`：用户ID
- `type`：反馈类型（suggestion、bug、other）
- `content`：反馈内容
- `contact`：联系方式
- `status`：状态（pending、processing、processed）
- `processed_by`：处理人
- `processed_at`：处理时间
- `created_at`：创建时间
- `updated_at`：更新时间

#### 2.8 qa表

**功能**：存储问答信息。

**主要字段**：
- `id`：问答ID（主键）
- `title`：标题
- `content`：内容
- `category`：分类
- `user_id`：用户ID
- `user_name`：用户名
- `answer_count`：回答数量
- `view_count`：浏览量
- `popularity`：热度
- `created_at`：创建时间
- `updated_at`：更新时间

#### 2.9 quiz_questions表

**功能**：存储答题题目信息。

**主要字段**：
- `id`：题目ID（主键）
- `content`：题目内容
- `options`：选项（JSON格式）
- `correct_answer`：正确答案
- `category`：分类
- `difficulty`：难度（easy、medium、hard）
- `created_at`：创建时间
- `updated_at`：更新时间

#### 2.10 quiz_record表

**功能**：存储答题记录信息。

**主要字段**：
- `id`：记录ID（主键）
- `user_id`：用户ID
- `score`：得分
- `correct_count`：正确数量
- `total_count`：总数量
- `created_at`：创建时间

#### 2.11 plant_game_record表

**功能**：存储植物游戏记录信息。

**主要字段**：
- `id`：记录ID（主键）
- `user_id`：用户ID
- `score`：得分
- `difficulty`：难度
- `correct_count`：正确数量
- `total_count`：总数量
- `created_at`：创建时间

#### 2.12 resources表

**功能**：存储资源信息。

**主要字段**：
- `id`：资源ID（主键）
- `name`：名称
- `description`：描述
- `category`：分类
- `file_url`：文件URL
- `file_size`：文件大小
- `file_type`：文件类型
- `download_count`：下载量
- `created_at`：创建时间
- `updated_at`：更新时间

#### 2.13 operation_log表

**功能**：存储操作日志信息。

**主要字段**：
- `id`：日志ID（主键）
- `user_id`：用户ID
- `user_name`：用户名
- `module`：操作模块
- `type`：操作类型（add、update、delete、query）
- `content`：操作内容
- `ip_address`：IP地址
- `user_agent`：用户代理
- `created_at`：创建时间

## 索引设计

### 1. 主键索引

所有表的`id`字段都设置为主键，自动创建主键索引。

### 2. 唯一索引

- `users.username`：用户名唯一索引
- `users.email`：邮箱唯一索引

### 3. 普通索引

- `plants.category`：药材分类索引
- `plants.usage_way`：药材用法索引
- `plants.view_count`：药材浏览量索引
- `plants.favorite_count`：药材收藏量索引
- `inheritors.level`：传承人级别索引
- `knowledge.type`：知识类型索引
- `knowledge.therapy_category`：疗法分类索引
- `knowledge.disease_category`：疾病分类索引
- `comments.target_type`：评论目标类型索引
- `comments.target_id`：评论目标ID索引
- `comments.user_id`：评论用户ID索引
- `favorites.user_id`：收藏用户ID索引
- `favorites.target_type`：收藏目标类型索引
- `favorites.target_id`：收藏目标ID索引
- `feedback.user_id`：反馈用户ID索引
- `feedback.status`：反馈状态索引
- `qa.category`：问答分类索引
- `qa.popularity`：问答热度索引
- `quiz_questions.category`：答题题目分类索引
- `quiz_questions.difficulty`：答题题目难度索引
- `quiz_record.user_id`：答题记录用户ID索引
- `quiz_record.score`：答题记录得分索引
- `plant_game_record.user_id`：游戏记录用户ID索引
- `plant_game_record.score`：游戏记录得分索引
- `resources.category`：资源分类索引
- `resources.download_count`：资源下载量索引
- `operation_log.user_id`：操作日志用户ID索引
- `operation_log.module`：操作日志模块索引
- `operation_log.type`：操作日志类型索引

### 4. 全文索引

- `plants.name,plants.description`：药材全文索引
- `knowledge.title,knowledge.content`：知识全文索引
- `inheritors.name,inheritors.bio`：传承人全文索引
- `qa.title,qa.content`：问答全文索引

## 数据库维护

### 1. 备份数据库

```bash
# 备份数据库
mysqldump -u root -p dong_medicine > dong_medicine_backup.sql

# 恢复数据库
mysql -u root -p dong_medicine < dong_medicine_backup.sql
```

### 2. 优化数据库

```bash
# 分析表
ANALYZE TABLE plants, knowledge, inheritors, comments, favorites, feedback, qa, quiz_questions, quiz_record, plant_game_record, resources, operation_log, users;

# 优化表
OPTIMIZE TABLE plants, knowledge, inheritors, comments, favorites, feedback, qa, quiz_questions, quiz_record, plant_game_record, resources, operation_log, users;
```

### 3. 监控数据库

- 定期检查慢查询日志
- 监控数据库连接数
- 监控表空间使用情况
- 定期备份数据库

## 注意事项

- 执行SQL脚本前，确保MySQL服务已启动
- 执行初始化脚本时，会删除已存在的表结构，请谨慎操作
- 生产环境中，应该使用参数化查询，避免SQL注入
- 定期优化数据库，提高查询性能
- 合理设计索引，避免过度索引

---

## 数据库优化建议

### 1. 查询优化

#### 使用EXPLAIN分析查询

```sql
EXPLAIN SELECT * FROM plants WHERE category = '清热药';
```

**关注指标**：
- `type`：ALL（全表扫描）需要优化
- `key`：使用的索引
- `rows`：预估扫描行数
- `Extra`：额外信息（Using filesort、Using temporary需要优化）

#### 优化建议

```sql
-- 避免SELECT *
SELECT id, name, category FROM plants WHERE category = '清热药';

-- 使用LIMIT限制结果
SELECT * FROM plants ORDER BY view_count DESC LIMIT 10;

-- 避免在WHERE子句中使用函数
-- 不推荐
SELECT * FROM plants WHERE YEAR(created_at) = 2024;
-- 推荐
SELECT * FROM plants WHERE created_at >= '2024-01-01' AND created_at < '2025-01-01';

-- 使用JOIN代替子查询
-- 不推荐
SELECT * FROM plants WHERE id IN (SELECT plant_id FROM favorites WHERE user_id = 1);
-- 推荐
SELECT p.* FROM plants p 
INNER JOIN favorites f ON p.id = f.target_id 
WHERE f.user_id = 1 AND f.target_type = 'plant';
```

### 2. 索引优化

#### 索引设计原则

| 原则 | 说明 |
|------|------|
| 最左前缀 | 复合索引从左到右匹配 |
| 选择性高 | 选择区分度高的列 |
| 避免冗余 | 不创建已有索引覆盖的索引 |
| 适度索引 | 索引不是越多越好 |

#### 创建复合索引

```sql
-- 根据查询模式创建复合索引
CREATE INDEX idx_plants_category_usage ON plants(category, usage_way);
CREATE INDEX idx_comments_target ON comments(target_type, target_id);
CREATE INDEX idx_favorites_user_target ON favorites(user_id, target_type, target_id);
```

#### 索引维护

```sql
-- 查看索引使用情况
SELECT * FROM sys.schema_index_statistics 
WHERE table_schema = 'dong_medicine';

-- 删除未使用的索引
DROP INDEX idx_unused ON table_name;
```

### 3. 表结构优化

#### 字段类型选择

| 数据类型 | 推荐使用场景 |
|---------|-------------|
| TINYINT | 状态、标志位 |
| INT | ID、计数器 |
| BIGINT | 大数值ID |
| VARCHAR | 可变长度字符串 |
| TEXT | 长文本（少用） |
| DATETIME | 时间戳 |
| DECIMAL | 精确数值（金额） |

#### 分区表设计

```sql
-- 按时间分区（适用于日志表）
ALTER TABLE operation_log 
PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);
```

### 4. 连接池配置

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      connection-timeout: 30000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
```

### 5. 慢查询优化

#### 开启慢查询日志

```sql
-- 查看慢查询配置
SHOW VARIABLES LIKE 'slow_query%';
SHOW VARIABLES LIKE 'long_query_time';

-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;  -- 超过2秒记录
```

#### 分析慢查询

```sql
-- 查看慢查询数量
SHOW GLOBAL STATUS LIKE 'Slow_queries';

-- 使用mysqldumpslow分析
-- mysqldumpslow -s t -t 10 /var/log/mysql/mysql-slow.log
```

### 6. 缓存策略

#### 查询缓存

```sql
-- 使用SQL_CACHE提示（MySQL 8.0已移除）
SELECT SQL_CACHE * FROM plants WHERE id = 1;

-- 应用层缓存更有效
-- 使用Redis缓存热点数据
```

#### 缓存更新策略

```java
// Cache-Aside模式
public Plant getPlant(Integer id) {
    Plant plant = redisTemplate.opsForValue().get("plant:" + id);
    if (plant == null) {
        plant = plantMapper.selectById(id);
        redisTemplate.opsForValue().set("plant:" + id, plant, 6, TimeUnit.HOURS);
    }
    return plant;
}
```

---

## 数据迁移指南

### 1. 导出数据

```bash
# 导出整个数据库
mysqldump -u root -p dong_medicine > backup.sql

# 仅导出表结构
mysqldump -u root -p --no-data dong_medicine > schema.sql

# 仅导出数据
mysqldump -u root -p --no-create-info dong_medicine > data.sql

# 导出特定表
mysqldump -u root -p dong_medicine plants knowledge > tables.sql
```

### 2. 导入数据

```bash
# 导入SQL文件
mysql -u root -p dong_medicine < backup.sql

# 使用source命令
mysql> USE dong_medicine;
mysql> SOURCE /path/to/backup.sql;
```

### 3. 数据迁移脚本

```sql
-- 批量插入数据
INSERT INTO plants (name, category, description, created_at)
VALUES 
    ('钩藤', '清热药', '清热平肝，息风止痉', NOW()),
    ('透骨草', '祛风湿药', '祛风除湿，舒筋活血', NOW());

-- 从其他表迁移数据
INSERT INTO new_plants (name, category)
SELECT name, category FROM old_plants WHERE status = 'active';

-- 批量更新
UPDATE plants SET view_count = 0 WHERE view_count IS NULL;
```

---

## 性能监控SQL

### 1. 查看表状态

```sql
-- 查看所有表状态
SHOW TABLE STATUS FROM dong_medicine;

-- 查看表大小
SELECT 
    table_name,
    ROUND(data_length / 1024 / 1024, 2) AS data_size_mb,
    ROUND(index_length / 1024 / 1024, 2) AS index_size_mb,
    table_rows
FROM information_schema.tables
WHERE table_schema = 'dong_medicine'
ORDER BY data_length DESC;
```

### 2. 查看锁信息

```sql
-- 查看当前锁
SHOW OPEN TABLES WHERE In_use > 0;

-- 查看InnoDB锁
SELECT * FROM information_schema.INNODB_LOCKS;
SELECT * FROM information_schema.INNODB_LOCK_WAITS;
```

### 3. 查看连接信息

```sql
-- 查看当前连接
SHOW PROCESSLIST;

-- 查看连接数
SHOW STATUS LIKE 'Threads_connected';
SHOW STATUS LIKE 'Max_used_connections';
```

### 4. 查看缓冲池状态

```sql
-- InnoDB缓冲池状态
SHOW STATUS LIKE 'Innodb_buffer_pool%';

-- 缓冲池命中率
SELECT 
    (1 - (Innodb_buffer_pool_reads / Innodb_buffer_pool_read_requests)) * 100 
    AS buffer_pool_hit_rate
FROM (
    SELECT variable_value AS Innodb_buffer_pool_reads
    FROM information_schema.global_status
    WHERE variable_name = 'Innodb_buffer_pool_reads'
) r,
(
    SELECT variable_value AS Innodb_buffer_pool_read_requests
    FROM information_schema.global_status
    WHERE variable_name = 'Innodb_buffer_pool_read_requests'
) rr;
```

---

## 常见问题

### 1. 如何解决死锁问题？

```sql
-- 查看死锁信息
SHOW ENGINE INNODB STATUS;

-- 解决方案
-- 1. 减小事务范围
-- 2. 按相同顺序访问表
-- 3. 添加适当索引
-- 4. 降低隔离级别
```

### 2. 如何处理大表？

```sql
-- 分批删除
DELETE FROM operation_log WHERE created_at < '2023-01-01' LIMIT 1000;

-- 使用分区
ALTER TABLE operation_log PARTITION BY RANGE (YEAR(created_at)) (...);

-- 归档历史数据
CREATE TABLE operation_log_archive AS 
SELECT * FROM operation_log WHERE created_at < '2023-01-01';
```

### 3. 如何优化COUNT查询？

```sql
-- 使用近似值
EXPLAIN SELECT COUNT(*) FROM plants;

-- 使用缓存计数
-- 在应用层维护计数器

-- 使用覆盖索引
CREATE INDEX idx_count ON plants(category);
SELECT COUNT(*) FROM plants WHERE category = '清热药';
```

### 4. 如何处理字符集问题？

```sql
-- 查看字符集
SHOW VARIABLES LIKE 'character%';
SHOW CREATE TABLE plants;

-- 修改字符集
ALTER TABLE plants CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修改字段字符集
ALTER TABLE plants MODIFY description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 未来改进建议

### 短期改进 (1-2周)

1. **索引优化**
   - 分析慢查询日志
   - 添加缺失索引
   - 删除冗余索引

2. **查询优化**
   - 优化复杂查询
   - 添加查询缓存
   - 使用批量操作

### 中期改进 (1-2月)

1. **分区表**
   - 日志表按时间分区
   - 历史数据归档

2. **读写分离**
   - 配置主从复制
   - 读写分离中间件

### 长期规划 (3-6月)

1. **分布式数据库**
   - 分库分表
   - 数据中间件

2. **数据仓库**
   - 数据同步
   - 数据分析

---

**最后更新时间**：2026年3月30日