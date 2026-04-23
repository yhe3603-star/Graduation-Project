# 数据库 SQL -- 侗乡医药数字展示平台的数据仓库

> 本目录存放所有数据库脚本，包括建表语句、初始数据和迁移脚本。

---

## 一、什么是数据库？

**类比：大型仓库，有规律地存放数据**

想象一个超大型仓库，里面有很多货架（表），每个货架有很多层（列），
每层放着一个属性值，一行就是一个完整的物品（记录）。

```
+---------------------------------------------------------------+
|                    数据库：dong_medicine                        |
|                    （侗乡医药数字展示平台仓库）                    |
+---------------------------------------------------------------+
|                                                                 |
|  货架1: plants（药用植物表）                                     |
|  +----+--------+----------+----------+----------+              |
|  | id | name_cn| name_dong| efficacy | category |              |
|  +----+--------+----------+----------+----------+              |
|  | 1  | 钩藤   | jas goc  | 清热平肝 | 藤本类   | <-- 一行记录  |
|  | 2  | 透骨草 | kgaox    | 祛风除湿 | 全草类   |              |
|  +----+--------+----------+----------+----------+              |
|                                                                 |
|  货架2: users（用户表）                                          |
|  +----+----------+---------------+-------+--------+            |
|  | id | username | password_hash | role  | status |            |
|  +----+----------+---------------+-------+--------+            |
|  | 1  | admin    | $2a$10$...    | admin | active |            |
|  +----+----------+---------------+-------+--------+            |
|                                                                 |
+---------------------------------------------------------------+
```

我们使用的数据库是 **MySQL 8.0**，它是目前最流行的关系型数据库之一。

---

## 二、什么是 SQL？

**类比：仓库管理语言**

SQL 全称是 **Structured Query Language**（结构化查询语言），
就像和仓库管理员沟通的专用语言：

```
你说的 SQL                              管理员理解的意思
------------------------------------    --------------------
SELECT * FROM plants                    "把 plants 货架上所有东西给我看看"
INSERT INTO users VALUES (...)          "在 users 货架上放一个新物品"
UPDATE plants SET view_count = 10       "把 plants 上的浏览量改成 10"
DELETE FROM comments WHERE id = 5       "把 comments 上 id=5 的物品扔掉"
CREATE TABLE ...                        "新建一个货架"
```

### 常用 SQL 命令分类

| 类型 | 命令 | 作用 | 示例 |
|------|------|------|------|
| DDL（定义） | CREATE TABLE | 创建表 | `CREATE TABLE plants (...)` |
| | ALTER TABLE | 修改表 | `ALTER TABLE plants ADD COLUMN ...` |
| | DROP TABLE | 删除表 | `DROP TABLE plants` |
| DML（操作） | INSERT | 插入数据 | `INSERT INTO plants VALUES (...)` |
| | SELECT | 查询数据 | `SELECT * FROM plants WHERE id = 1` |
| | UPDATE | 更新数据 | `UPDATE plants SET view_count = 10` |
| | DELETE | 删除数据 | `DELETE FROM plants WHERE id = 1` |

---

## 三、数据库核心概念

### 3.1 什么是表（Table）？

**类比：Excel 工作表**

表就像一个 Excel 工作表，有行有列：

```
plants 表
+----+--------+----------+-----------+
| id | name_cn| name_dong| efficacy  |  <-- 列（Column）：定义数据类型
+----+--------+----------+-----------+
| 1  | 钩藤   | jas goc  | 清热平肝   |  <-- 行（Row）：一条完整记录
| 2  | 透骨草 | kgaox    | 祛风除湿   |
+----+--------+----------+-----------+
```

### 3.2 什么是行（Row）和列（Column）？

- **列（Column）**：定义数据的类型和名称，就像 Excel 的表头
  - `name_cn` 列：存中文名称，类型是 VARCHAR(100)
  - `view_count` 列：存浏览量，类型是 INT
- **行（Row）**：一条完整的数据记录，就像 Excel 的一行数据
  - 第1行：钩藤的完整信息
  - 第2行：透骨草的完整信息

### 3.3 什么是主键（Primary Key）？

**类比：身份证号**

每条记录都需要一个唯一标识，就像每个人都有唯一的身份证号：

```
plants 表
+----+--------+----------+
| id | name_cn| efficacy |   id 就是主键
+----+--------+----------+   每条记录的 id 都不同
| 1  | 钩藤   | 清热平肝  |   id=1 唯一标识"钩藤"
| 2  | 透骨草 | 祛风除湿  |   id=2 唯一标识"透骨草"
| 3  | 九节茶 | 活血消斑  |   id=3 唯一标识"九节茶"
+----+--------+----------+   绝对不会重复！
```

主键的特点：
- **唯一**：每条记录的主键值都不同
- **非空**：主键不能为 NULL
- **自动递增**：新记录的 id 自动 +1（AUTO_INCREMENT）

### 3.4 什么是索引（Index）？

**类比：书的目录**

一本书没有目录，你要找"钩藤"的内容，只能从第1页翻到最后一页（全表扫描，很慢）。
有了目录，你可以直接翻到对应页码（索引查找，很快）。

```
没有索引：
  查找 name_cn = "钩藤"
  -> 从第1条开始逐条比较... 第1条、第2条... 找到了！
  -> 65条数据，最坏要比较65次

有索引（idx_name_cn）：
  查找 name_cn = "钩藤"
  -> 查索引目录："钩藤" -> 第1条
  -> 直接跳到第1条，1次就找到！
```

**本项目使用的索引类型**：

| 索引类型 | 作用 | 本项目示例 |
|---------|------|-----------|
| 普通索引 (INDEX) | 加速查询 | `INDEX idx_category(category)` |
| 唯一索引 (UNIQUE INDEX) | 保证值不重复 | `UNIQUE INDEX uk_username(username)` |
| 联合索引 (复合INDEX) | 多列组合查询加速 | `INDEX idx_target(target_type, target_id)` |
| 全文索引 (FULLTEXT) | 全文搜索加速 | `FULLTEXT INDEX ft_plants_search(name_cn, name_dong, efficacy, story)` |

### 3.5 什么是外键？

**类比：关联关系**

外键用于建立表与表之间的关联关系。比如 comments 表中的 user_id 关联到 users 表的 id：

```
users 表                    comments 表
+----+----------+          +----+---------+----------+
| id | username |          | id | user_id | content  |
+----+----------+          +----+---------+----------+
| 1  | admin    |<---------| 1  | 1       | 很好！   |
| 2  | dongyi001|<---------| 2  | 2       | 不错！   |
+----+----------+          +----+---------+----------+
                                  user_id 就是"外键"
                                  指向 users 表的 id
```

**注意**：本项目没有使用数据库层面的外键约束（FOREIGN KEY），而是在代码层面维护关联关系。
这是现代开发的常见做法，好处是更灵活、性能更好。

### 3.6 什么是 utf8mb4？

**utf8mb4** 是 MySQL 的字符编码，支持所有 Unicode 字符，包括 Emoji 表情：

```
utf8    ：最多3字节，不支持 Emoji（如 🌿💊🍵）
utf8mb4 ：最多4字节，支持所有字符，包括 Emoji

本项目必须用 utf8mb4，因为：
1. 侗语名称可能包含特殊字符
2. 用户评论可能输入 Emoji
3. 现代应用的标准配置
```

---

## 四、本项目全部 13 张表结构详解

### 4.1 users -- 用户表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| username | VARCHAR(20) | 用户名 | 非空, 唯一 |
| password_hash | VARCHAR(100) | 密码哈希(BCrypt) | 非空 |
| role | VARCHAR(20) | 角色: user/admin | 非空, 默认'user' |
| status | VARCHAR(20) | 状态: active/banned | 默认'active' |
| created_at | DATETIME | 创建时间 | 默认当前时间 |

索引：`uk_username(username)` -- 用户名唯一索引

### 4.2 plants -- 植物药材表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| name_cn | VARCHAR(100) | 中文名称 | 非空 |
| name_dong | VARCHAR(100) | 侗语名称 | |
| scientific_name | VARCHAR(200) | 学名 | |
| category | VARCHAR(20) | 分类: 根茎类/全草类/花叶类/果实类/皮类 | 非空 |
| usage_way | VARCHAR(20) | 使用方式: 外用/内服/药浴 | 非空 |
| habitat | VARCHAR(200) | 生境 | |
| efficacy | VARCHAR(500) | 功效 | |
| story | VARCHAR(2000) | 药用故事 | |
| images | TEXT | 图片(JSON数组) | |
| videos | TEXT | 视频(JSON数组) | |
| documents | TEXT | 相关文档(JSON数组) | |
| distribution | TEXT | 地域分布 | |
| created_at | DATETIME | 创建时间 | 默认当前时间 |
| view_count | INT | 浏览量 | 默认0 |
| favorite_count | INT | 收藏量 | 默认0 |
| popularity | INT | 热度值 | 默认1 |
| update_log | TEXT | 更新日志(JSON数组) | |

索引：`idx_category`, `idx_usage_way`, `idx_name_cn`, `idx_popularity`, `idx_plants_view_count`, `idx_plants_category_usage`
全文索引：`ft_plants_search(name_cn, name_dong, efficacy, story)` -- 支持中文全文搜索

### 4.3 knowledge -- 知识库表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| title | VARCHAR(200) | 标题 | 非空 |
| type | VARCHAR(20) | 类型: 药方/疗法 | 非空 |
| therapy_category | VARCHAR(50) | 疗法分类 | |
| disease_category | VARCHAR(50) | 疾病分类 | |
| herb_category | VARCHAR(50) | 药材分类 | |
| content | TEXT | 内容 | 非空 |
| formula | TEXT | 配方 | |
| usage_method | TEXT | 用法 | |
| steps | TEXT | 步骤(JSON数组) | |
| images | TEXT | 图片(JSON数组) | |
| videos | TEXT | 视频(JSON数组) | |
| documents | TEXT | 相关文档 | |
| related_plants | TEXT | 关联植物ID列表 | |
| update_log | TEXT | 更新日志 | |
| popularity | INT | 热度 | 默认0 |
| created_at | DATETIME | 创建时间 | 默认当前时间 |
| view_count | INT | 浏览量 | 默认0 |
| favorite_count | INT | 收藏量 | 默认0 |

索引：`idx_type`, `idx_popularity`, `idx_knowledge_popularity`
全文索引：`ft_knowledge_search(title, content)`

### 4.4 inheritors -- 传承人表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| name | VARCHAR(50) | 姓名 | 非空 |
| level | VARCHAR(20) | 等级: 国家级/省级/市级/县级/自治区级 | 非空 |
| bio | VARCHAR(1000) | 从业履历 | |
| specialties | VARCHAR(500) | 技艺特色 | |
| experience_years | INT | 经验年限 | 非空 |
| videos | TEXT | 视频(JSON数组) | |
| images | TEXT | 图片(JSON数组) | |
| documents | TEXT | 资质文档(JSON数组) | |
| representative_cases | TEXT | 代表案例(JSON数组) | |
| honors | VARCHAR(1000) | 荣誉资质 | |
| created_at | DATETIME | 创建时间 | 默认当前时间 |
| view_count | INT | 浏览量 | 默认0 |
| favorite_count | INT | 收藏量 | 默认0 |
| popularity | INT | 热度值 | 默认0 |
| update_log | TEXT | 更新日志 | |

索引：`idx_level`, `idx_popularity`
全文索引：`ft_inheritors_search(name, specialties, bio)`

### 4.5 qa -- 问答表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| category | VARCHAR(50) | 分类: 侗药常识/侗医疗法/文化背景 | |
| question | TEXT | 问题 | 非空 |
| answer | TEXT | 答案 | 非空 |
| popularity | INT | 热度 | 默认0 |
| created_at | DATETIME | 创建时间 | 默认当前时间 |
| view_count | INT | 浏览量 | 默认0 |
| favorite_count | INT | 收藏量 | 默认0 |

索引：`idx_category`, `idx_popularity`
全文索引：`ft_qa_search(question, answer)`

### 4.6 resources -- 学习资源表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| title | VARCHAR(200) | 标题 | 非空 |
| category | VARCHAR(20) | 分类: 入门/进阶/专业 | 非空 |
| files | TEXT | 文件列表(JSON数组) | |
| description | VARCHAR(1000) | 描述 | |
| download_count | INT | 下载次数 | 默认0 |
| popularity | INT | 热度 | 默认0 |
| created_at | DATETIME | 创建时间 | 默认当前时间 |
| view_count | INT | 浏览量 | 默认0 |
| favorite_count | INT | 收藏量 | 默认0 |
| update_log | TEXT | 更新日志 | |

索引：`idx_category`, `idx_popularity`
全文索引：`ft_resources_search(title, description)`

### 4.7 comments -- 评论表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| user_id | INT | 用户ID | 非空 |
| username | VARCHAR(50) | 用户名 | 非空 |
| target_type | VARCHAR(50) | 目标类型: plant/knowledge/inheritor等 | 非空 |
| target_id | INT | 目标ID | 非空 |
| content | VARCHAR(1000) | 评论内容 | 非空 |
| reply_to_id | INT | 回复的评论ID | |
| reply_to_user_id | INT | 回复的用户ID | |
| reply_to_username | VARCHAR(50) | 回复的用户名 | |
| likes | INT | 点赞数 | 默认0 |
| reply_count | INT | 回复数 | 默认0 |
| status | VARCHAR(20) | 状态: pending/approved/rejected | 默认'approved' |
| created_at | DATETIME | 创建时间 | 默认当前时间 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

索引：`idx_target(target_type, target_id)`, `idx_user_id`, `idx_status`, `idx_reply_to_id`, `idx_created_at`, `idx_likes`

### 4.8 favorites -- 收藏表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| user_id | INT | 用户ID | 非空 |
| target_type | VARCHAR(50) | 目标类型 | 非空 |
| target_id | INT | 目标ID | 非空 |
| created_at | DATETIME | 创建时间 | 默认当前时间 |

索引：`uk_user_target(user_id, target_type, target_id)` -- 唯一索引，同一用户对同一目标只能收藏一次
索引：`idx_user_id`

### 4.9 feedback -- 反馈表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| user_id | INT | 用户ID | |
| user_name | VARCHAR(50) | 用户名 | 非空 |
| type | VARCHAR(20) | 类型: 建议/问题/其他 | 非空 |
| title | VARCHAR(200) | 标题 | 非空 |
| content | TEXT | 内容 | 非空 |
| contact | VARCHAR(100) | 联系方式 | |
| status | VARCHAR(20) | 状态: pending/resolved | 默认'pending' |
| reply | TEXT | 回复内容 | |
| created_at | DATETIME | 创建时间 | 默认当前时间 |

索引：`idx_user_id`, `idx_status`

### 4.10 quiz_questions -- 测验题目表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| question | TEXT | 题目内容 | 非空 |
| options | TEXT | 选项(JSON数组) | 非空 |
| answer | VARCHAR(500) | 正确答案 | 非空 |
| category | VARCHAR(50) | 题目分类 | |
| correct_answer | VARCHAR(10) | 正确选项: A/B/C/D | |
| explanation | TEXT | 答案解析 | |

### 4.11 quiz_record -- 测验记录表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| user_id | INT | 用户ID | 非空 |
| score | INT | 得分 | 非空 |
| total_questions | INT | 总题数 | 非空 |
| correct_answers | INT | 正确数 | 非空 |
| created_at | DATETIME | 答题时间 | 默认当前时间 |

索引：`idx_user_id`, `idx_created_at`

### 4.12 plant_game_record -- 植物游戏记录表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| user_id | INT | 用户ID | 非空 |
| difficulty | VARCHAR(20) | 难度: easy/medium/hard | 非空 |
| score | INT | 得分 | 非空 |
| correct_count | INT | 正确数 | 非空 |
| total_count | INT | 总题数 | 非空 |
| created_at | DATETIME | 游戏时间 | 默认当前时间 |

索引：`idx_user_id`, `idx_created_at`

### 4.13 operation_log -- 操作日志表

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键ID | 自增, 非空 |
| user_id | INT | 用户ID | |
| username | VARCHAR(50) | 用户名 | |
| module | VARCHAR(50) | 模块名称 | 非空 |
| operation | VARCHAR(100) | 操作描述 | 非空 |
| type | VARCHAR(20) | 操作类型: CREATE/UPDATE/DELETE/QUERY | 非空 |
| method | VARCHAR(200) | 请求方法 | |
| params | TEXT | 请求参数 | |
| ip | VARCHAR(50) | IP地址 | |
| duration | INT | 执行时长(ms) | |
| success | TINYINT | 是否成功: 0失败/1成功 | 默认1 |
| error_msg | TEXT | 错误信息 | |
| created_at | DATETIME | 操作时间 | 默认当前时间 |

索引：`idx_user_id`, `idx_module`, `idx_created_at`

---

## 五、表关系总览图

```
                    +-----------+
                    |   users   |
                    +-----------+
                    | id (PK)   |
                    | username  |
                    +-----------+
                         |
          +--------------+--------------+--------------+-----------+
          |              |              |              |           |
          v              v              v              v           v
   +-----------+  +-----------+  +-----------+  +-----------+ +-----------+
   | comments  |  | favorites |  | feedback  |  |quiz_record| |plant_game |
   +-----------+  +-----------+  +-----------+  +-----------+ +_record    |
   | user_id   |  | user_id   |  | user_id   |  | user_id   | +-----------+
   | target_   |  | target_   |  +-----------+  +-----------+ | user_id   |
   | type+id   |  | type+id   |                                +-----------+
   +-----------+  +-----------+
        |              |
        v              v
   +-----------+  +-----------+  +-----------+  +-----------+ +-----------+
   |  plants   |  | knowledge |  |inheritors |  |    qa     | | resources |
   +-----------+  +-----------+  +-----------+  +-----------+ +-----------+
   | id (PK)   |  | id (PK)   |  | id (PK)   |  | id (PK)   | | id (PK)   |
   | name_cn   |  | title     |  | name      |  | question  | | title     |
   | efficacy  |  | content   |  | level     |  | answer    | | category  |
   +-----------+  +-----------+  +-----------+  +-----------+ +-----------+

   +-----------------+          +-----------+
   | quiz_questions  |          |operation  |
   +-----------------+          |_log       |
   | id (PK)         |          +-----------+
   | question        |          | user_id   |
   | options (JSON)  |          | module    |
   | correct_answer  |          | operation |
   +-----------------+          +-----------+
```

**关联方式**：通过 `target_type + target_id` 实现多态关联
- `target_type='plant'`, `target_id=1` -> 指向 plants 表 id=1 的记录
- `target_type='knowledge'`, `target_id=2` -> 指向 knowledge 表 id=2 的记录

---

## 六、如何初始化数据库

### 步骤 1：创建数据库

```sql
-- 登录 MySQL
mysql -u root -p

-- 创建数据库（指定字符集为 utf8mb4）
CREATE DATABASE dong_medicine DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 使用该数据库
USE dong_medicine;
```

### 步骤 2：导入建表和初始数据

```sql
-- 导入主 SQL 文件（包含所有建表语句和初始数据）
SOURCE /path/to/dong_medicine.sql;

-- 或者用命令行直接导入
mysql -u root -p dong_medicine < dong_medicine.sql
```

### 步骤 3：执行迁移脚本（如果需要）

```sql
-- 按顺序执行 migrations 目录下的脚本
SOURCE /path/to/migrations/remove_plant_difficulty.sql;
SOURCE /path/to/migrations/remove_quiz_difficulty.sql;
```

### 步骤 4：验证

```sql
-- 查看所有表
SHOW TABLES;

-- 查看各表数据量
SELECT 'users' AS tbl, COUNT(*) AS cnt FROM users
UNION ALL SELECT 'plants', COUNT(*) FROM plants
UNION ALL SELECT 'knowledge', COUNT(*) FROM knowledge
UNION ALL SELECT 'inheritors', COUNT(*) FROM inheritors
UNION ALL SELECT 'qa', COUNT(*) FROM qa
UNION ALL SELECT 'resources', COUNT(*) FROM resources
UNION ALL SELECT 'comments', COUNT(*) FROM comments
UNION ALL SELECT 'favorites', COUNT(*) FROM favorites
UNION ALL SELECT 'feedback', COUNT(*) FROM feedback
UNION ALL SELECT 'quiz_questions', COUNT(*) FROM quiz_questions
UNION ALL SELECT 'quiz_record', COUNT(*) FROM quiz_record
UNION ALL SELECT 'plant_game_record', COUNT(*) FROM plant_game_record
UNION ALL SELECT 'operation_log', COUNT(*) FROM operation_log;
```

---

## 七、迁移脚本说明

`migrations/` 目录存放数据库结构变更脚本，按时间顺序执行：

| 脚本文件 | 说明 | 操作 |
|---------|------|------|
| `remove_plant_difficulty.sql` | 移除 plants 表的 difficulty 字段 | 删除索引 `idx_plants_difficulty`，删除列 `difficulty` |
| `remove_quiz_difficulty.sql` | 移除 quiz_questions 表的 difficulty 字段 | 删除列 `difficulty` |

**为什么要迁移？**
最初设计时，植物和题目都有难度分类（easy/medium/hard），后来决定改为随机抽取，
所以需要从数据库中移除这些字段。迁移脚本记录了每一次结构变更，确保可追溯。

---

## 八、数据库备份与恢复

### 8.1 备份数据库

```bash
# 备份整个数据库（包含表结构和数据）
mysqldump -u root -p dong_medicine > backup_20260423.sql

# 只备份表结构（不含数据）
mysqldump -u root -p --no-data dong_medicine > backup_structure.sql

# 只备份数据（不含表结构）
mysqldump -u root -p --no-create-info dong_medicine > backup_data.sql
```

### 8.2 恢复数据库

```bash
# 从备份文件恢复
mysql -u root -p dong_medicine < backup_20260423.sql
```

---

## 九、常用调试 SQL 命令

### 查看表结构

```sql
-- 查看表的字段定义
DESC plants;

-- 查看完整的建表语句（包含索引）
SHOW CREATE TABLE plants;
```

### 查询数据

```sql
-- 查看所有植物（前10条）
SELECT id, name_cn, category, view_count FROM plants LIMIT 10;

-- 按热度排序
SELECT id, name_cn, popularity FROM plants ORDER BY popularity DESC LIMIT 10;

-- 按分类统计数量
SELECT category, COUNT(*) AS count FROM plants GROUP BY category;

-- 模糊搜索
SELECT id, name_cn, efficacy FROM plants WHERE name_cn LIKE '%钩%';

-- 全文搜索（需要全文索引支持）
SELECT id, name_cn, efficacy FROM plants
WHERE MATCH(name_cn, name_dong, efficacy, story)
AGAINST('清热解毒' IN NATURAL LANGUAGE MODE);
```

### 查看索引

```sql
-- 查看表的所有索引
SHOW INDEX FROM plants;
```

### 查看表大小

```sql
-- 查看各表占用空间
SELECT table_name, table_rows,
       ROUND(data_length / 1024 / 1024, 2) AS data_mb,
       ROUND(index_length / 1024 / 1024, 2) AS index_mb
FROM information_schema.tables
WHERE table_schema = 'dong_medicine'
ORDER BY data_length DESC;
```

### 清空表数据（慎用！）

```sql
-- 清空表数据（重置自增ID）
TRUNCATE TABLE operation_log;

-- 只删除数据（保留自增ID计数器）
DELETE FROM operation_log WHERE created_at < '2026-01-01';
```

---

## 十、JSON 字段说明

本项目多个表使用 TEXT 类型存储 JSON 格式数据，前端负责解析：

| 表 | 字段 | JSON 格式示例 |
|------|------|-------------|
| plants | images | `[{"name":"钩藤.jpg","path":"/images/plant1.jpg","size":1024,"type":"image"}]` |
| plants | videos | `[{"name":"钩藤.mp4","path":"/videos/plant1.mp4","size":0,"type":"video"}]` |
| plants | update_log | `[{"id":1,"time":"2026-03-04","content":"创建植物","operator":"系统"}]` |
| knowledge | steps | `["准备药材","加水煎煮","取汁服用"]` |
| knowledge | related_plants | `[1, 2, 3, 7, 8]` |
| inheritors | representative_cases | `["成立工作室","治疗患者数千例"]` |
| inheritors | honors | `["国家级传承人","评估优秀"]` |
| quiz_questions | options | `["选项A","选项B","选项C","选项D"]` |
| resources | files | `[{"path":"/doc.pdf","name":"研究.pdf","size":421560,"type":"pdf"}]` |

**为什么用 JSON 字符串而不是单独的表？**
- 简化查询：一次查询就能拿到所有数据，不需要 JOIN
- 灵活性：JSON 结构可以随时调整，不需要改表结构
- 适合读多写少的场景：图片列表、步骤列表等几乎不会单独查询

---

## 十一、视图说明

本项目还创建了两个视图（View），用于简化常用查询：

```sql
-- v_hot_plants：热门植物视图（按热度和收藏量排序）
-- v_hot_knowledge：热门知识视图（按热度和收藏量排序）
```

视图就像一个"虚拟表"，它本身不存储数据，而是基于查询定义的。
每次查询视图时，MySQL 会自动执行视图背后的 SELECT 语句。

---

## 十二、常见错误与解决方案

### 错误 1：导入 SQL 时编码错误

```
报错：Invalid utf8 character set
原因：SQL 文件编码不是 UTF-8
解决：确保 SQL 文件以 UTF-8 编码保存，开头有 SET NAMES utf8mb4;
```

### 错误 2：全文索引搜索无结果

```
问题：MATCH ... AGAINST 搜索中文无结果
原因：MySQL 全文索引对中文需要 ngram 解析器，且最小搜索长度为2
解决：建表时已指定 WITH PARSER ngram，确保搜索词至少2个字符
```

### 错误 3：AUTO_INCREMENT 值冲突

```
报错：Duplicate entry '1' for key 'PRIMARY'
原因：手动插入了指定 id 的数据，和 AUTO_INCREMENT 冲突
解决：让数据库自动分配 id，或导入后重置自增值
      ALTER TABLE plants AUTO_INCREMENT = (SELECT MAX(id) + 1 FROM plants);
```

### 错误 4：外键约束导致无法删除

```
报错：Cannot delete or update a parent row
原因：有其他表的数据引用了当前记录
解决：先删除关联数据，再删除主记录（本项目未使用外键约束，不会遇到此问题）
```

### 错误 5：连接数据库被拒绝

```
报错：Access denied for user 'root'@'localhost'
原因：密码错误或用户没有权限
解决：检查 application.yml 中的数据库连接配置
```

---

## 十三、速记口诀

```
数据库是仓库，SQL 是管理语言
表是货架，行是物品，列是属性
主键像身份证，唯一标识每条记录
索引像目录，查找数据快如闪电
utf8mb4 不能忘，中文 Emoji 都支持
JSON 字段存复杂数据，前端解析最方便
备份恢复要常做，数据安全最重要
```
