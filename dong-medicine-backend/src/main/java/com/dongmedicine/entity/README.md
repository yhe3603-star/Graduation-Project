# Entity 层 -- 实体类（16个Entity + 1个BaseEntity）

> Entity 是三层架构的"数据模型层"，每个Entity = 一张数据库表 = 一组Java字段。
> 使用MyBatis-Plus注解映射到数据库，使用Lombok注解消除样板代码。
> 大部分Entity继承 `BaseEntity`（id + createdAt + updatedAt），少数独立实体直接定义字段。

---

## 一、Entity 清单

| # | Entity | 对应表 | 继承BaseEntity | 核心字段 | 特殊设计 |
|---|--------|--------|---------------|---------|---------|
| 1 | **Plant** | plants | 是 | nameCn/nameDong/scientificName/category/efficacy/story/images/videos | @NotBlank(nameCn)，JSON字段(images/videos/documents) |
| 2 | **Knowledge** | knowledge | 是 | title/type/therapyCategory/diseaseCategory/herbCategory/content/formula | @NotBlank(title/content)，多维分类筛选 |
| 3 | **Inheritor** | inheritors | 是 | name/level/bio/specialties/experienceYears/representativeCases/honors | @NotBlank(name)，扩展信息JSON |
| 4 | **User** | users | 是 | username/passwordHash/role/status | isBanned()方法，STATUS_ACTIVE/STATUS_BANNED常量 |
| 5 | **Comment** | comments | 是 | userId/username/targetType/targetId/content/replyToId/likes/replyCount/status | 嵌套回复(replyToId/replyToUserId)，审核状态 |
| 6 | **Favorite** | favorites | 否 | userId/targetType/targetId/createdAt | 独立实体，仅createdAt |
| 7 | **Feedback** | feedback | 否 | userId/userName/type/title/content/contact/status/reply/createdAt | 独立实体，匿名支持(userName) |
| 8 | **QuizQuestion** | quiz_questions | 否 | question/options/answer/category/correctAnswer/explanation | @JsonIgnore(options)，getOptionList()/setOptionList() JSON转换 |
| 9 | **QuizRecord** | quiz_record | 否 | userId/score/totalQuestions/correctAnswers/createdAt | 独立实体 |
| 10 | **PlantGameRecord** | plant_game_record | 否 | userId/difficulty/score/correctCount/totalCount/createdAt | 独立实体 |
| 11 | **OperationLog** | operation_log | 是 | userId/username/module/operation/type/method/params/ip/duration/success/errorMsg | 继承BaseEntity |
| 12 | **BrowseHistory** | browse_history | 否 | userId/targetType/targetId/createdAt | 独立实体，id为Long类型 |
| 13 | **ChatHistory** | chat_history | 否 | userId/sessionId/role/content/createdAt | 独立实体，id为Long类型 |
| 14 | **SearchHistory** | search_history | 否 | userId/keyword/createdAt | 独立实体，id为Long类型 |
| 15 | **Resource** | resources | 是 | title/category/files/description/viewCount/downloadCount/favoriteCount/popularity | @NotBlank(title)，JSON字段(files) |
| 16 | **Qa** | qa | 是 | category/question/answer/popularity/viewCount/favoriteCount | @NotBlank(question)，人气值体系 |

---

## 二、BaseEntity 基类

```java
@Data
public abstract class BaseEntity {
    @TableId(type = IdType.AUTO)           // 主键自增
    private Integer id;

    @TableField(value = "created_at", fill = FieldFill.INSERT)        // 插入时自动填充
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE) // 更新时自动填充
    private LocalDateTime updatedAt;
}
```

**自动填充机制**：通过 `MetaObjectHandler` 实现类（在config包中），自动设置 createdAt 和 updatedAt。

**继承BaseEntity的实体**（10个）：Plant、Knowledge、Inheritor、User、Comment、OperationLog、Resource、Qa

**不继承BaseEntity的实体**（7个）：Favorite、Feedback、QuizQuestion、QuizRecord、PlantGameRecord、BrowseHistory、ChatHistory、SearchHistory

---

## 三、各Entity字段详解

### 3.1 Plant -- 药用植物

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plants")
public class Plant extends BaseEntity {
    @NotBlank(message = "中文名称不能为空")
    private String nameCn;           // 中文名（必填）
    private String nameDong;         // 侗语名
    private String scientificName;   // 学名
    private String category;         // 分类（如：根茎类、全草类、花叶类）
    private String usageWay;         // 用法（如：内服、外用、熏蒸）
    private String habitat;          // 生长环境
    private String efficacy;         // 功效主治
    private String story;            // 民间故事/传说
    private String images;           // 图片JSON数组 [{"url":"...","name":"...","type":"image/jpeg","size":1234}]
    private String videos;           // 视频JSON数组
    private String documents;        // 文档JSON数组
    private String distribution;     // 产地分布
    private String updateLog;        // 更新日志
    private Integer viewCount;       // 浏览量
    private Integer favoriteCount;   // 收藏量
    private Integer popularity;      // 人气值（浏览+3/次，收藏+10/次）
}
```

**JSON字段格式**（images/videos/documents通用）：
```json
[
  {"url": "/uploads/plants/2024-01-01/xxx.jpg", "name": "钩藤.jpg", "type": "image/jpeg", "size": 123456}
]
```

### 3.2 Knowledge -- 知识库

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge")
public class Knowledge extends BaseEntity {
    @NotBlank(message = "标题不能为空")
    private String title;              // 标题（必填）
    private String type;               // 类型（药方/疗法/文化）
    private String therapyCategory;    // 疗法分类（如：推拿、拔罐、药浴）
    private String diseaseCategory;    // 疾病分类（如：风湿、感冒、跌打损伤）
    private String herbCategory;       // 药材分类
    @NotBlank(message = "内容不能为空")
    private String content;            // 内容（必填）
    private String formula;            // 药方组成
    private String usageMethod;        // 用法用量
    private String steps;              // 操作步骤
    private String images;             // 图片JSON
    private String videos;             // 视频JSON
    private String documents;          // 文档JSON
    private String relatedPlants;      // 关联植物ID列表JSON
    private String updateLog;          // 更新日志
    private Integer popularity;        // 人气值
    private Integer viewCount;         // 浏览量
    private Integer favoriteCount;     // 收藏量
}
```

**多维筛选**：支持 therapyCategory + diseaseCategory + herbCategory 三维组合筛选，是本系统最复杂的搜索逻辑。

### 3.3 Inheritor -- 传承人

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inheritors")
public class Inheritor extends BaseEntity {
    @NotBlank(message = "传承人姓名不能为空")
    private String name;                   // 姓名（必填）
    private String level;                  // 级别（国家级/省级/州级/县级）
    private String bio;                    // 个人简介
    private String specialties;            // 擅长领域
    private Integer experienceYears;       // 从业年限
    private String videos;                 // 视频JSON
    private String images;                 // 图片JSON
    private String documents;              // 文档JSON
    private String representativeCases;    // 代表作品/案例
    private String honors;                 // 荣誉资质
    private String updateLog;              // 更新日志
    private Integer viewCount;             // 浏览量
    private Integer favoriteCount;         // 收藏量
    private Integer popularity;            // 人气值
}
```

### 3.4 User -- 用户

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_BANNED = "banned";

    private String username;
    @TableField("password_hash")
    private String passwordHash;     // BCrypt加密存储
    private String role;             // admin / user
    private String status;           // active / banned

    public boolean isBanned() {
        return STATUS_BANNED.equals(this.status);
    }
}
```

**设计要点**：
- `passwordHash` 使用 `@TableField("password_hash")` 显式映射，因为Java驼峰与数据库下划线不一致
- `isBanned()` 方法封装封禁判断逻辑，Service层直接调用
- `STATUS_ACTIVE` / `STATUS_BANNED` 常量避免硬编码字符串

### 3.5 Comment -- 评论

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comments")
public class Comment extends BaseEntity {
    private Integer userId;            // 评论者ID
    private String username;           // 评论者用户名（冗余存储，避免联表）
    private String targetType;         // 评论对象类型（plant/knowledge/inheritor/qa/resource）
    private Integer targetId;          // 评论对象ID
    private String content;            // 评论内容
    private Integer replyToId;         // 回复的评论ID（null=顶级评论）
    private Integer replyToUserId;     // 回复的用户ID
    private String replyToUsername;    // 回复的用户名（冗余存储）
    private Integer likes;             // 点赞数
    private Integer replyCount;        // 回复数
    private String status;             // 状态（pending/approved/rejected）
}
```

**嵌套回复设计**：通过 `replyToId` 实现评论树。顶级评论 `replyToId = null`，回复评论 `replyToId = 父评论ID`。

**冗余字段**：`username`、`replyToUsername` 冗余存储，避免查询时联表，提高列表查询性能。

### 3.6 Favorite -- 收藏

```java
@Data
@TableName("favorites")
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;           // 用户ID
    private String targetType;        // 收藏对象类型（plant/knowledge/inheritor/qa/resource）
    private Integer targetId;         // 收藏对象ID
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;  // 收藏时间
}
```

**不继承BaseEntity**：收藏只需要 id + createdAt，不需要 updatedAt。

### 3.7 Feedback -- 反馈

```java
@Data
@TableName("feedback")
public class Feedback {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;           // 用户ID（匿名时为null）
    private String userName;          // 用户名（匿名时为"anonymous"）
    private String type;              // 反馈类型
    private String title;             // 反馈标题
    private String content;           // 反馈内容
    private String contact;           // 联系方式
    private String status;            // 状态（pending/resolved）
    private String reply;             // 管理员回复
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

### 3.8 QuizQuestion -- 测验题目

```java
@Data
@TableName("quiz_questions")
public class QuizQuestion {
    @TableId
    private Integer id;
    private String question;          // 题目
    @JsonIgnore
    private String options;           // 选项JSON（不序列化到前端）
    private String answer;            // 用户答案
    private String category;          // 分类
    private String correctAnswer;     // 正确答案
    private String explanation;       // 解析

    // JSON <-> List<String> 转换方法
    public List<String> getOptionList() { ... }
    public void setOptionList(List<String> optionList) { ... }
}
```

**设计要点**：
- `@JsonIgnore` 在 options 字段上：前端不直接看到原始JSON字符串，而是通过 `getOptionList()` 获取解析后的列表
- `getOptionList()` / `setOptionList()`：JSON与List的互转方法，使用静态 ObjectMapper
- `answer` vs `correctAnswer`：answer 是用户提交的答案，correctAnswer 是正确答案

### 3.9 QuizRecord -- 答题记录

```java
@Data
@TableName("quiz_record")
public class QuizRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer score;             // 得分
    private Integer totalQuestions;    // 总题数
    private Integer correctAnswers;    // 正确数
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

### 3.10 PlantGameRecord -- 植物游戏记录

```java
@Data
@TableName("plant_game_record")
public class PlantGameRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String difficulty;         // 难度级别
    private Integer score;             // 得分
    private Integer correctCount;      // 正确数
    private Integer totalCount;        // 总题数
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

### 3.11 OperationLog -- 操作日志

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_log")
public class OperationLog extends BaseEntity {
    private Integer userId;
    private String username;           // 冗余存储
    private String module;             // 操作模块（plant/knowledge/inheritor/user/...）
    private String operation;          // 操作描述
    private String type;               // 操作类型（CREATE/UPDATE/DELETE/QUERY）
    private String method;             // HTTP方法
    private String params;             // 请求参数
    private String ip;                 // 请求IP
    private Integer duration;          // 执行耗时(ms)
    private Boolean success;           // 是否成功
    private String errorMsg;           // 错误信息
}
```

### 3.12 BrowseHistory -- 浏览历史

```java
@Data
@TableName("browse_history")
public class BrowseHistory {
    @TableId(type = IdType.AUTO)
    private Long id;                   // 注意：Long类型，非Integer
    private Integer userId;
    private String targetType;         // 浏览对象类型
    private Integer targetId;          // 浏览对象ID
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

### 3.13 ChatHistory -- 聊天历史

```java
@Data
@TableName("chat_history")
public class ChatHistory {
    @TableId(type = IdType.AUTO)
    private Long id;                   // 注意：Long类型，非Integer
    private Integer userId;
    private String sessionId;          // 会话ID（UUID）
    private String role;               // 角色（user/assistant）
    private String content;            // 消息内容
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

### 3.14 SearchHistory -- 搜索历史

```java
@Data
@TableName("search_history")
public class SearchHistory {
    @TableId(type = IdType.AUTO)
    private Long id;                   // 注意：Long类型，非Integer
    private Integer userId;
    private String keyword;            // 搜索关键词
    private LocalDateTime createdAt;
}
```

### 3.15 Resource -- 学习资源

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resources")
public class Resource extends BaseEntity {
    @NotBlank(message = "资源标题不能为空")
    private String title;
    private String category;           // 资源分类
    private String files;              // 文件JSON数组
    private String description;        // 描述
    private String updateLog;          // 更新日志
    private Integer viewCount;         // 浏览量
    private Integer downloadCount;     // 下载量
    private Integer favoriteCount;     // 收藏量
    private Integer popularity;        // 人气值
}
```

### 3.16 Qa -- 非遗问答

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qa")
public class Qa extends BaseEntity {
    private String category;           // 分类
    @NotBlank(message = "问题不能为空")
    private String question;           // 问题
    private String answer;             // 答案
    private Integer popularity;        // 人气值
    private Integer viewCount;         // 浏览量
    private Integer favoriteCount;     // 收藏量
}
```

---

## 四、设计模式与规范

### 4.1 人气值体系

所有内容实体（Plant、Knowledge、Inheritor、Qa、Resource）都包含三个统计字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `viewCount` | Integer | 浏览量，每次浏览 +1 |
| `favoriteCount` | Integer | 收藏量，收藏 +1，取消收藏 -1 |
| `popularity` | Integer | 人气值，浏览 +3/次，收藏 +10/次 |

人气值权重：收藏(10) >> 浏览(3)，体现"收藏=深度认可"的产品理念。

### 4.2 JSON字段存储

多文件字段（images、videos、documents、files）统一使用 String 类型存储 JSON 数组：

```json
[{"url": "/uploads/xxx/xxx.jpg", "name": "文件名", "type": "image/jpeg", "size": 123456}]
```

前端负责解析和序列化，后端只做存储和读取。QuizQuestion 的 options 字段同理。

### 4.3 冗余字段策略

为避免联表查询，部分字段冗余存储：

| Entity | 冗余字段 | 来源 |
|--------|---------|------|
| Comment | username, replyToUsername | User表 |
| Feedback | userName | User表 |
| OperationLog | username | User表 |

### 4.4 targetType 统一类型标识

Favorite、Comment、BrowseHistory 使用 `targetType` + `targetId` 实现多态关联：

| targetType 值 | 对应实体 |
|---------------|---------|
| plant | Plant |
| knowledge | Knowledge |
| inheritor | Inheritor |
| qa | Qa |
| resource | Resource |

### 4.5 注解使用规范

```java
@TableName("table_name")           // 映射表名
@TableId(type = IdType.AUTO)       // 主键自增
@TableField("column_name")         // 显式映射列名（驼峰与下划线不一致时）
@TableField(fill = FieldFill.INSERT)        // 插入时自动填充
@TableField(fill = FieldFill.INSERT_UPDATE) // 更新时自动填充
@NotBlank(message = "xxx不能为空")  // 校验注解（配合@Valid使用）
@JsonIgnore                        // 序列化时忽略
@Data                              // Lombok: getter/setter/toString/equals/hashCode
@EqualsAndHashCode(callSuper = true) // 继承BaseEntity时必须标注
```

### 4.6 ID类型差异

| 类型 | 使用的实体 | 原因 |
|------|-----------|------|
| Integer | BaseEntity子类 + Favorite/Feedback/QuizQuestion/QuizRecord/PlantGameRecord | 数据量可控，Integer足够 |
| Long | BrowseHistory/ChatHistory/SearchHistory | 历史记录数据量大，Long更安全 |
