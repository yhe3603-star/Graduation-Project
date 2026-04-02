# 实体类目录 (entity)

本目录存放与数据库表对应的实体类（Entity）。

## 📖 什么是实体类？

实体类是数据库表在Java代码中的映射。每个实体类对应数据库中的一张表，类的属性对应表的字段。

## 📁 文件列表

| 文件名 | 对应表名 | 功能说明 |
|--------|----------|----------|
| `User.java` | `users` | 用户信息表 |
| `Plant.java` | `plants` | 药用植物表 |
| `Knowledge.java` | `knowledge` | 知识库表 |
| `Inheritor.java` | `inheritors` | 传承人表 |
| `Resource.java` | `resources` | 学习资源表 |
| `Qa.java` | `qa` | 问答表 |
| `QuizQuestion.java` | `quiz_questions` | 答题题目表 |
| `QuizRecord.java` | `quiz_records` | 答题记录表 |
| `PlantGameRecord.java` | `plant_game_records` | 植物游戏记录表 |
| `Comment.java` | `comments` | 评论表 |
| `Favorite.java` | `favorites` | 收藏表 |
| `Feedback.java` | `feedback` | 反馈表 |
| `OperationLog.java` | `operation_logs` | 操作日志表 |

## 📦 详细说明

### 1. User.java - 用户实体

**对应表结构:**
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 主键ID |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(255) | 密码（加密） |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| avatar | VARCHAR(255) | 头像URL |
| role | VARCHAR(20) | 角色（USER/ADMIN） |
| status | TINYINT | 状态（0禁用/1正常） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 2. Plant.java - 植物实体

**对应表结构:**
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 主键ID |
| name_cn | VARCHAR(100) | 中文名 |
| name_dong | VARCHAR(100) | 侗语名 |
| scientific_name | VARCHAR(200) | 学名 |
| category | VARCHAR(50) | 分类 |
| usage_way | VARCHAR(50) | 用法 |
| habitat | TEXT | 生境 |
| efficacy | TEXT | 功效 |
| story | TEXT | 故事 |
| images | TEXT | 图片（JSON数组） |
| videos | TEXT | 视频（JSON数组） |
| documents | TEXT | 文档（JSON数组） |
| distribution | TEXT | 分布（JSON） |
| view_count | INT | 浏览次数 |
| update_log | TEXT | 更新日志 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 3. Knowledge.java - 知识实体

**对应表结构:**
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 主键ID |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容 |
| type | VARCHAR(50) | 类型 |
| category | VARCHAR(50) | 分类 |
| therapy | VARCHAR(100) | 疗法 |
| disease | VARCHAR(100) | 疾病 |
| herb | VARCHAR(100) | 药材 |
| images | TEXT | 图片 |
| videos | TEXT | 视频 |
| documents | TEXT | 文档 |
| view_count | INT | 浏览次数 |
| popularity | INT | 热度 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 4. QuizQuestion.java - 答题题目实体

**对应表结构:**
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 主键ID |
| question | TEXT | 题目内容 |
| options | TEXT | 选项（JSON数组） |
| answer | VARCHAR(10) | 正确答案 |
| category | VARCHAR(50) | 分类 |
| correct_answer | VARCHAR(10) | 正确答案字段 |
| explanation | TEXT | 解析 |
| create_time | DATETIME | 创建时间 |

## 🎯 实体类规范

### 基本结构
```java
@Data
@TableName("table_name")
public class Example {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("field_name")
    private String fieldName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
    
    @Version
    private Integer version;
}
```

### 常用注解

| 注解 | 说明 | 示例 |
|------|------|------|
| `@TableName` | 指定表名 | `@TableName("users")` |
| `@TableId` | 主键注解 | `@TableId(type = IdType.AUTO)` |
| `@TableField` | 字段注解 | `@TableField("user_name")` |
| `@TableLogic` | 逻辑删除 | `@TableLogic` |
| `@Version` | 乐观锁 | `@Version` |

### 主键类型
```java
@TableId(type = IdType.AUTO)      // 自增
@TableId(type = IdType.ASSIGN_ID) // 雪花算法
@TableId(type = IdType.INPUT)     // 手动输入
@TableId(type = IdType.UUID)      // UUID
```

### 字段填充策略
```java
@TableField(fill = FieldFill.INSERT)       // 插入时填充
@TableField(fill = FieldFill.UPDATE)       // 更新时填充
@TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时填充
```

### 最佳实践
1. **命名规范**: 类名使用大驼峰，属性名使用小驼峰
2. **使用Lombok**: 使用@Data等注解简化代码
3. **类型选择**: 使用包装类（Integer）而非基本类型（int）
4. **时间类型**: 使用LocalDateTime代替Date
5. **逻辑删除**: 使用@TableLogic实现软删除

## 📚 扩展阅读

- [MyBatis Plus 实体类](https://baomidou.com/guide/entity.html)
- [Lombok 注解](https://projectlombok.org/features/)
- [Java Bean 规范](https://en.wikipedia.org/wiki/JavaBean)
