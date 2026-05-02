# Entity 层 -- 实体类（15个Entity + BaseEntity基类）

> Entity 类是数据库表在Java中的映射。每个Entity类对应数据库中的一张表，字段对应列。
> MyBatis-Plus自动完成下划线命名（name_cn）到驼峰命名（nameCn）的转换。

---

## 一、BaseEntity -- 公共基类

文件：`entity/BaseEntity.java`

```java
@Data
public abstract class BaseEntity {
    @TableId(type = IdType.AUTO)                        // 主键，自增
    private Integer id;

    @TableField(value = "created_at", fill = FieldFill.INSERT)       // 插入时自动填充
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE) // 插入+更新时自动填充
    private LocalDateTime updatedAt;
}
```

**使用方式**：所有Entity类统一继承BaseEntity，自动获得id、createdAt、updatedAt三个公共字段。
时间字段的自动填充由 `MyMetaObjectHandler`（config包）实现。

---

## 二、Entity 清单（15个）

| # | Entity | 表名 | 继承 | 核心字段 |
|---|--------|------|------|---------|
| 1 | Plant | plants | BaseEntity | nameCn, nameDong, scientificName, category, usageWay, habitat, efficacy, story, images, videos, documents, distribution, updateLog, viewCount, favoriteCount, popularity |
| 2 | Knowledge | knowledge | BaseEntity | title, type, therapyCategory, diseaseCategory, herbCategory, content, formula, usageMethod, steps, images, videos, documents, relatedPlants, updateLog, viewCount, favoriteCount, popularity |
| 3 | Inheritor | inheritors | BaseEntity | name, level, bio, specialties, experienceYears, videos, images, documents, representativeCases, honors, updateLog, viewCount, favoriteCount, popularity |
| 4 | Qa | qa | BaseEntity | category, question, answer |
| 5 | Resource | resources | BaseEntity | title, category, files, description, updateLog, viewCount, downloadCount, favoriteCount, popularity |
| 6 | User | users | BaseEntity | username, passwordHash, role, status (STATUS_ACTIVE/STATUS_BANNED) |
| 7 | Comment | comments | BaseEntity | userId, username, targetType, targetId, content, replyToId, replyToUserId, replyToUsername, likes, replyCount, status |
| 8 | Favorite | favorites | BaseEntity | userId, targetType, targetId |
| 9 | Feedback | feedback | 独立 | id, userId, userName, type, title, content, contact, status, reply, createdAt |
| 10 | QuizQuestion | quiz_questions | BaseEntity | question, options, answer, category, correctAnswer, explanation |
| 11 | QuizRecord | quiz_record | 独立 | id, userId, score, totalQuestions, correctAnswers, createdAt |
| 12 | PlantGameRecord | plant_game_record | 独立 | id, userId, difficulty, score, correctCount, totalCount, createdAt |
| 13 | OperationLog | operation_log | BaseEntity | userId, username, module, operation, type, method, ip, params, duration, success, errorMsg |
| 14 | BrowseHistory | browse_history | BaseEntity | userId, targetType, targetId |
| 15 | ChatHistory | chat_history | BaseEntity | userId, sessionId, role, content |

---

## 三、核心注解说明

| 注解 | 作用 | 示例 |
|------|------|------|
| `@Data` | Lombok自动生成getter/setter/toString/equals/hashCode | 几乎所有Entity都使用 |
| `@EqualsAndHashCode(callSuper = true)` | equals/hashCode包含父类字段 | 继承BaseEntity的Entity必须加 |
| `@TableName("表名")` | 映射数据库表名 | `@TableName("plants")` |
| `@TableId(type = IdType.AUTO)` | 主键，数据库自增 | 在BaseEntity中定义 |
| `@TableField("列名")` | 手动指定列名映射（默认自动转换） | `@TableField("password_hash")` |
| `@TableField(fill = FieldFill.INSERT)` | 插入时自动填充 | BaseEntity中的createdAt |
| `@TableField(fill = FieldFill.INSERT_UPDATE)` | 插入和更新时自动填充 | BaseEntity中的updatedAt |
| `@NotBlank` | 校验：不能为空字符串（仅DTO层有效） | -- |

### 命名自动转换规则

MyBatis-Plus自动将Java驼峰命名转换为数据库下划线命名：

| Java字段 | 数据库列 | 是否需要@TableField |
|---------|---------|-------------------|
| `nameCn` | `name_cn` | 不需要（自动匹配） |
| `scientificName` | `scientific_name` | 不需要 |
| `usageWay` | `usage_way` | 不需要 |
| `passwordHash` | `password_hash` | 需要（显式声明） |
| `createdAt` | `created_at` | 需要（BaseEntity中已声明） |

---

## 四、关键Entity详解

### 4.1 Plant -- 药用植物

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plants")
public class Plant extends BaseEntity {
    @NotBlank(message = "中文名称不能为空")
    private String nameCn;              // 中文名
    private String nameDong;            // 侗语名
    private String scientificName;      // 学名
    private String category;            // 分类
    private String usageWay;            // 用法
    private String habitat;             // 生长环境
    private String efficacy;            // 功效
    private String story;               // 民间故事
    private String images;              // 图片列表（JSON字符串）
    private String videos;              // 视频列表（JSON字符串）
    private String documents;           // 文档列表（JSON字符串）
    private String distribution;        // 分布地区
    private String updateLog;           // 更新日志
    private Integer viewCount;          // 浏览次数
    private Integer favoriteCount;      // 收藏次数
    private Integer popularity;         // 人气值
}
```

**JSON字段说明**：`images`、`videos`、`documents` 存储为JSON字符串（如 `["/images/plants/1.jpg", "/images/plants/2.jpg"]`），前端负责解析。

### 4.2 User -- 用户

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_BANNED = "banned";

    private String username;
    @TableField("password_hash")       // 显式指定列名
    private String passwordHash;       // BCrypt加密后的密码
    private String role;               // "user" 或 "admin"
    private String status;             // "active" 或 "banned"

    public boolean isBanned() {
        return STATUS_BANNED.equals(this.status);
    }
}
```

### 4.3 Knowledge -- 知识库

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge")
public class Knowledge extends BaseEntity {
    @NotBlank(message = "标题不能为空")
    private String title;              // 标题
    private String type;               // 类型（药方/疗法等）
    private String therapyCategory;    // 疗法分类
    private String diseaseCategory;    // 疾病分类
    private String herbCategory;       // 药材分类
    @NotBlank(message = "内容不能为空")
    private String content;            // 内容
    private String formula;            // 配方
    private String usageMethod;        // 使用方法
    private String steps;              // 步骤
    private String images;             // 图片（JSON）
    private String videos;             // 视频（JSON）
    private String documents;          // 文档（JSON）
    private String relatedPlants;      // 关联植物（JSON）
    private String updateLog;          // 更新日志
    private Integer popularity, viewCount, favoriteCount;
}
```

### 4.4 QuizQuestion -- 测验题目

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quiz_questions")
public class QuizQuestion extends BaseEntity {
    private String question;           // 题目
    private String options;            // 选项（JSON字符串，如 ["A","B","C","D"]）
    private String answer;             // 答案
    private String category;           // 分类
    private String correctAnswer;      // 正确答案
    private String explanation;        // 解析

    // 手动转换方法：JSON字符串 ↔ List<String>
    public List<String> getOptionList() { ... }
    public void setOptionList(List<String> optionList) { ... }
}
```

### 4.5 OperationLog -- 操作日志

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_log")
public class OperationLog extends BaseEntity {
    private Integer userId;            // 操作用户ID
    private String username;           // 操作用户名
    private String module;             // 模块（USER/PLANT/KNOWLEDGE等）
    private String operation;          // 操作（方法名）
    private String type;               // 类型（CREATE/UPDATE/DELETE/QUERY）
    private String method;             // HTTP方法+URI
    private String ip;                 // 客户端IP
    private String params;             // 请求参数（JSON）
    private Integer duration;          // 执行耗时（毫秒）
    private Boolean success;           // 是否成功
    private String errorMsg;           // 错误信息
}
```

---

## 五、不继承BaseEntity的Entity

以下3个Entity是独立类，不继承BaseEntity：

| Entity | 原因 | 替代方案 |
|--------|------|---------|
| Feedback | 无updatedAt字段，不满足BaseEntity结构 | 手动定义id和createdAt |
| QuizRecord | 字段名createTime与created_at不匹配 | 手动用@TableField映射 |
| PlantGameRecord | 同上 | 手动用@TableField映射 |

---

## 六、Entity编程规范

```java
@Data
@EqualsAndHashCode(callSuper = true)    // 必须加！确保equals/hashCode包含父类字段
@TableName("表名")                       // 手动指定表名（避免MyBatis-Plus默认规则不匹配）
public class Xxx extends BaseEntity {
    // 1. 必填字段加 @NotBlank（但实际校验应在DTO层）
    // 2. 字段名与列名不匹配时加 @TableField
    // 3. JSON类型数据用String存储
    // 4. 多值字段（images/videos/documents）用JSON数组字符串
    // 5. 业务状态常量定义为 public static final String
}
```
