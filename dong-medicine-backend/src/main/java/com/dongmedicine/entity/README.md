# 实体类目录 (entity)

本目录存放与数据库表对应的实体类。

## 目录

- [什么是实体类？](#什么是实体类)
- [目录结构](#目录结构)
- [实体类列表](#实体类列表)
- [开发规范](#开发规范)
- [常用实体类详解](#常用实体类详解)

---

## 什么是实体类？

### 实体类的概念

**实体类（Entity）** 是与数据库表对应的 Java 类。它就像一个"数据容器"——类的属性对应表的字段，类的对象对应表中的一行数据。

### 实体类的作用

```
┌─────────────────────────────────────────────────────────────────┐
│                     实体类与数据库表                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  数据库表 users                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  id  │  username  │  password_hash  │  role  │  status │   │
│  ├──────┼────────────┼─────────────────┼────────┼─────────┤   │
│  │  1   │  admin     │  $2a$10$...     │  ADMIN │    0    │   │
│  │  2   │  zhangsan  │  $2a$10$...     │  USER  │    0    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            ↕                                    │
│                            映射                                 │
│                            ↕                                    │
│  Java实体类 User                                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  public class User {                                    │   │
│  │      private Integer id;                                │   │
│  │      private String username;                           │   │
│  │      private String passwordHash;                       │   │
│  │      private String role;                               │   │
│  │      private Integer status;                            │   │
│  │      // getter/setter...                                │   │
│  │  }                                                      │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### ORM框架的作用

MyBatis Plus 是一个 ORM（对象关系映射）框架，它自动完成：
- Java对象 → 数据库行（插入、更新）
- 数据库行 → Java对象（查询）

---

## 目录结构

```
entity/
│
├── User.java                          # 用户实体
├── Plant.java                         # 药用植物实体
├── Knowledge.java                     # 知识库实体
├── Inheritor.java                     # 传承人实体
├── Resource.java                      # 学习资源实体
├── Qa.java                            # 问答实体
├── Comment.java                       # 评论实体
├── Favorite.java                      # 收藏实体
├── Feedback.java                      # 反馈实体
├── QuizQuestion.java                  # 测验题目实体
├── QuizRecord.java                    # 测验记录实体
├── PlantGameRecord.java               # 植物游戏记录实体
└── OperationLog.java                  # 操作日志实体
```

---

## 实体类列表

| 实体类 | 对应表 | 说明 |
|--------|--------|------|
| User | users | 用户信息 |
| Plant | plants | 药用植物信息 |
| Knowledge | knowledge | 知识库信息 |
| Inheritor | inheritors | 传承人信息 |
| Resource | resources | 学习资源信息 |
| Qa | qa | 问答信息 |
| Comment | comments | 评论信息 |
| Favorite | favorites | 收藏信息 |
| Feedback | feedback | 反馈信息 |
| QuizQuestion | quiz_questions | 测验题目 |
| QuizRecord | quiz_record | 测验记录 |
| PlantGameRecord | plant_game_record | 植物游戏记录 |
| OperationLog | operation_log | 操作日志 |

---

## 开发规范

### 1. 实体类基本结构

```java
/**
 * 用户实体类
 * 对应数据库表 users
 */
@Data                               // Lombok：自动生成getter/setter/toString等
@TableName("users")                 // 指定对应的数据库表名
public class User {
    
    @TableId(type = IdType.AUTO)    // 主键，自增
    private Integer id;
    
    private String username;        // 用户名
    
    private String passwordHash;    // 密码哈希
    
    private String role;            // 角色
    
    private Integer status;         // 状态
    
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createdAt;       // 创建时间
}
```

### 2. 常用注解说明

| 注解 | 说明 | 示例 |
|------|------|------|
| `@TableName` | 指定表名 | `@TableName("users")` |
| `@TableId` | 标记主键 | `@TableId(type = IdType.AUTO)` |
| `@TableField` | 字段配置 | `@TableField(fill = FieldFill.INSERT)` |
| `@TableLogic` | 逻辑删除 | 删除时不真删，改为标记 |
| `@Version` | 乐观锁版本号 | 防止并发修改 |

### 3. 字段类型映射

| Java类型 | MySQL类型 | 说明 |
|----------|-----------|------|
| Integer | INT | 整数 |
| Long | BIGINT | 长整数 |
| String | VARCHAR | 字符串 |
| Double | DOUBLE | 浮点数 |
| Boolean | TINYINT(1) | 布尔值 |
| LocalDateTime | DATETIME | 日期时间 |
| LocalDate | DATE | 日期 |

---

## 常用实体类详解

### User - 用户实体

```java
/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String username;
    
    private String passwordHash;
    
    private String role;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 业务方法：判断是否被封禁
    public boolean isBanned() {
        return status != null && status == 1;
    }
    
    // 业务方法：判断是否是管理员
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
```

### Plant - 药用植物实体

```java
/**
 * 药用植物实体类
 */
@Data
@TableName("plants")
public class Plant {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String nameCn;           // 中文名称
    
    private String nameDong;         // 侗语名称
    
    private String scientificName;   // 学名
    
    private String category;         // 分类
    
    private String usageWay;         // 用法方式
    
    private String habitat;          // 生长环境
    
    private String efficacy;         // 功效
    
    private String story;            // 相关故事
    
    private String images;           // 图片(JSON)
    
    private String videos;           // 视频(JSON)
    
    private String documents;        // 文档(JSON)
    
    private String distribution;     // 分布地区
    
    private String difficulty;       // 难度级别
    
    private Integer viewCount;       // 浏览次数
    
    private Integer favoriteCount;   // 收藏次数
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

### Knowledge - 知识库实体

```java
/**
 * 知识库实体类
 */
@Data
@TableName("knowledge")
public class Knowledge {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String title;            // 标题
    
    private String type;             // 类型
    
    private String therapyCategory;  // 疗法分类
    
    private String diseaseCategory;  // 疾病分类
    
    private String content;          // 内容
    
    private String formula;          // 配方
    
    private String usageMethod;      // 使用方法
    
    private String steps;            // 步骤(JSON)
    
    private String images;           // 图片(JSON)
    
    private String videos;           // 视频(JSON)
    
    private String documents;        // 文档(JSON)
    
    private String relatedPlants;    // 相关植物
    
    private Integer viewCount;       // 浏览次数
    
    private Integer favoriteCount;   // 收藏次数
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

### Comment - 评论实体

```java
/**
 * 评论实体类
 */
@Data
@TableName("comments")
public class Comment {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer userId;          // 用户ID
    
    private String targetType;       // 目标类型(plant/knowledge/inheritor等)
    
    private Integer targetId;        // 目标ID
    
    private String content;          // 评论内容
    
    private Integer parentId;        // 父评论ID(用于嵌套回复)
    
    private Integer status;          // 状态(0待审核/1已审核)
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

---

## 最佳实践

### 1. 使用Lombok简化代码

```java
// 使用 @Data 自动生成 getter/setter/toString/equals/hashCode
@Data
public class User {
    private Integer id;
    private String username;
}

// 等价于手写以下代码：
public class User {
    private Integer id;
    private String username;
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    // toString, equals, hashCode...
}
```

### 2. 字段命名规范

```java
// Java使用驼峰命名
private String passwordHash;

// 数据库使用下划线命名
// password_hash

// MyBatis Plus 自动转换
```

### 3. 不要在实体类中写复杂逻辑

```java
// ✅ 好的做法：简单的判断方法
public boolean isBanned() {
    return status != null && status == 1;
}

// ❌ 不好的做法：复杂的业务逻辑
public void calculateScore() {
    // 复杂计算应该在Service层
}
```

---

**相关文档**

- [MyBatis Plus 实体类](https://baomidou.com/guide/entity.html)
- [Lombok 官方文档](https://projectlombok.org/)

---

**最后更新时间**：2026年4月3日
