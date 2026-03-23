# 实体类目录说明

## 文件夹结构

本目录包含项目中使用的实体类，对应数据库中的表结构。

```
entity/
├── Comment.java           # 评论实体
├── Favorite.java          # 收藏实体
├── Feedback.java          # 意见反馈实体
├── Inheritor.java         # 传承人实体
├── Knowledge.java         # 知识实体
├── OperationLog.java      # 操作日志实体
├── Plant.java             # 药材实体
├── PlantGameRecord.java   # 植物游戏记录实体
├── Qa.java                # 问答实体
├── QuizQuestion.java      # 答题题目实体
├── QuizRecord.java        # 答题记录实体
├── Resource.java          # 资源实体
└── User.java              # 用户实体
```

## 详细说明

### 1. Comment.java

**功能**：评论实体，对应数据库中的`comments`表。

**主要字段**：
- `id`：评论ID
- `targetType`：目标类型（药材、传承人、知识等）
- `targetId`：目标ID
- `content`：评论内容
- `userId`：用户ID
- `replyToId`：回复ID
- `status`：状态（正常、审核中、已删除）
- `createdAt`：创建时间
- `updatedAt`：更新时间

**关联关系**：
- 与`User`实体：多对一

### 2. Favorite.java

**功能**：收藏实体，对应数据库中的`favorites`表。

**主要字段**：
- `id`：收藏ID
- `userId`：用户ID
- `targetType`：目标类型
- `targetId`：目标ID
- `createdAt`：创建时间

**关联关系**：
- 与`User`实体：多对一

### 3. Feedback.java

**功能**：意见反馈实体，对应数据库中的`feedback`表。

**主要字段**：
- `id`：反馈ID
- `userId`：用户ID
- `type`：反馈类型（建议、bug、其他）
- `content`：反馈内容
- `contact`：联系方式
- `status`：状态（待处理、处理中、已处理）
- `processedBy`：处理人
- `processedAt`：处理时间
- `createdAt`：创建时间
- `updatedAt`：更新时间

**关联关系**：
- 与`User`实体：多对一

### 4. Inheritor.java

**功能**：传承人实体，对应数据库中的`inheritors`表。

**主要字段**：
- `id`：传承人ID
- `name`：姓名
- `level`：级别（国家级、自治区级、市级）
- `specialties`：技艺特色
- `experienceYears`：从业年限
- `bio`：个人简介
- `representativeCases`：代表案例
- `honors`：荣誉资质
- `createdAt`：创建时间
- `updatedAt`：更新时间

### 5. Knowledge.java

**功能**：知识实体，对应数据库中的`knowledge`表。

**主要字段**：
- `id`：知识ID
- `title`：标题
- `content`：内容
- `type`：类型（理论知识、实践经验、历史文化）
- `therapyCategory`：疗法分类
- `diseaseCategory`：疾病分类
- `viewCount`：浏览量
- `createdAt`：创建时间
- `updatedAt`：更新时间

### 6. OperationLog.java

**功能**：操作日志实体，对应数据库中的`operation_log`表。

**主要字段**：
- `id`：日志ID
- `userId`：用户ID
- `userName`：用户名
- `module`：操作模块
- `type`：操作类型（添加、修改、删除、查询）
- `content`：操作内容
- `ipAddress`：IP地址
- `userAgent`：用户代理
- `createdAt`：创建时间

**关联关系**：
- 与`User`实体：多对一

### 7. Plant.java

**功能**：药材实体，对应数据库中的`plants`表。

**主要字段**：
- `id`：药材ID
- `name`：名称
- `latinName`：拉丁名
- `category`：分类
- `usageWay`：用法
- `effect`：功效
- `distribution`：分布
- `description`：描述
- `imageUrl`：图片URL
- `viewCount`：浏览量
- `favoriteCount`：收藏量
- `createdAt`：创建时间
- `updatedAt`：更新时间

### 8. PlantGameRecord.java

**功能**：植物游戏记录实体，对应数据库中的`plant_game_record`表。

**主要字段**：
- `id`：记录ID
- `userId`：用户ID
- `score`：得分
- `difficulty`：难度
- `correctCount`：正确数量
- `totalCount`：总数量
- `createdAt`：创建时间

**关联关系**：
- 与`User`实体：多对一

### 9. Qa.java

**功能**：问答实体，对应数据库中的`qa`表。

**主要字段**：
- `id`：问答ID
- `title`：标题
- `content`：内容
- `category`：分类
- `userId`：用户ID
- `userName`：用户名
- `answerCount`：回答数量
- `viewCount`：浏览量
- `popularity`：热度
- `createdAt`：创建时间
- `updatedAt`：更新时间

**关联关系**：
- 与`User`实体：多对一

### 10. QuizQuestion.java

**功能**：答题题目实体，对应数据库中的`quiz_questions`表。

**主要字段**：
- `id`：题目ID
- `content`：题目内容
- `options`：选项（JSON格式）
- `correctAnswer`：正确答案
- `category`：分类
- `difficulty`：难度
- `createdAt`：创建时间
- `updatedAt`：更新时间

### 11. QuizRecord.java

**功能**：答题记录实体，对应数据库中的`quiz_record`表。

**主要字段**：
- `id`：记录ID
- `userId`：用户ID
- `score`：得分
- `correctCount`：正确数量
- `totalCount`：总数量
- `createdAt`：创建时间

**关联关系**：
- 与`User`实体：多对一

### 12. Resource.java

**功能**：资源实体，对应数据库中的`resources`表。

**主要字段**：
- `id`：资源ID
- `name`：名称
- `description`：描述
- `category`：分类
- `fileUrl`：文件URL
- `fileSize`：文件大小
- `fileType`：文件类型
- `downloadCount`：下载量
- `createdAt`：创建时间
- `updatedAt`：更新时间

### 13. User.java

**功能**：用户实体，对应数据库中的`users`表。

**主要字段**：
- `id`：用户ID
- `username`：用户名
- `password`：密码（加密存储）
- `email`：邮箱
- `phone`：手机号
- `nickname`：昵称
- `avatar`：头像
- `role`：角色（admin、user、guest）
- `status`：状态（正常、禁用）
- `lastLoginAt`：最后登录时间
- `createdAt`：创建时间
- `updatedAt`：更新时间

**关联关系**：
- 与`Comment`实体：一对多
- 与`Favorite`实体：一对多
- 与`Feedback`实体：一对多
- 与`OperationLog`实体：一对多
- 与`PlantGameRecord`实体：一对多
- 与`Qa`实体：一对多
- 与`QuizRecord`实体：一对多

## 实体类使用指南

### 1. 创建实体对象

```java
Plant plant = new Plant();
plant.setName("人参");
plant.setLatinName("Panax ginseng");
plant.setCategory("根茎类");
plant.setEffect("大补元气，复脉固脱，益精填髓");
plantService.save(plant);
```

### 2. 查询实体对象

```java
// 根据ID查询
Plant plant = plantService.getById(1L);

// 根据条件查询
QueryWrapper<Plant> wrapper = new QueryWrapper<>();
wrapper.eq("category", "根茎类");
List<Plant> plants = plantService.list(wrapper);
```

### 3. 更新实体对象

```java
Plant plant = plantService.getById(1L);
plant.setName("西洋参");
plantService.updateById(plant);
```

### 4. 删除实体对象

```java
plantService.removeById(1L);
```

## 开发规范

1. **命名规范**：实体类名使用大驼峰命名法，与表名对应
2. **字段命名**：字段名使用小驼峰命名法，与数据库字段对应
3. **注解使用**：使用MyBatis Plus的注解，如`@TableName`、`@TableId`、`@TableField`
4. **关联关系**：使用`@OneToMany`、`@ManyToOne`等注解定义关联关系
5. **数据验证**：使用`@NotNull`、`@Size`等注解进行数据验证
6. **序列化**：实现`Serializable`接口

## 注意事项

- 实体类应该与数据库表结构保持一致
- 避免在实体类中包含业务逻辑
- 合理使用索引，提高查询性能
- 注意字段类型和长度，避免数据溢出
- 敏感字段（如密码）应该加密存储

---

**最后更新时间**：2026年3月23日