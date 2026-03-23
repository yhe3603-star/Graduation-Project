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

**最后更新时间**：2026年3月23日