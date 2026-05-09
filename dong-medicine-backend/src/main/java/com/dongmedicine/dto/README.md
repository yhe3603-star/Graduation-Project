# DTO 层 -- 数据传输对象（31个DTO）

> DTO（Data Transfer Object）是前后端数据交互的"隔离层"。
> Entity直接映射数据库，DTO面向API请求/响应，两者职责不同，不可混用。
> 核心原则：**Entity不直接暴露给前端，前端不直接操作Entity字段**。

---

## 一、DTO 分类总览

| 分类 | 数量 | 用途 |
|------|------|------|
| 请求DTO（Request） | 18个 | 前端提交数据到后端，带@Valid校验 |
| 响应DTO（Response） | 5个 | 后端返回数据给前端，过滤敏感字段 |
| 创建DTO（Create） | 6个 | 管理员新增数据，防止字段篡改 |
| 更新DTO（Update） | 6个 | 管理员修改数据，包含id字段 |
| 辅助DTO | 3个 | 嵌套结构、文件上传结果等 |

---

## 二、DTO 完整清单

### 2.1 认证相关（4个）

| # | DTO | 类型 | 用途 | 核心字段 |
|---|-----|------|------|---------|
| 1 | **LoginDTO** | 请求 | 用户登录 | username(3-20), password(6+), captchaKey, captchaCode |
| 2 | **RegisterDTO** | 请求 | 用户注册 | username(3-20, 字母数字下划线), password(6-50), confirmPassword, captchaKey, captchaCode |
| 3 | **ChangePasswordDTO** | 请求 | 修改密码 | currentPassword, newPassword(6-20), captchaKey, captchaCode |
| 4 | **CaptchaDTO** | 响应 | 验证码返回 | captchaKey, captchaImage(Base64) |

### 2.2 内容展示DTO（4个）

| # | DTO | 类型 | 用途 | 核心字段 | 特殊方法 |
|---|-----|------|------|---------|---------|
| 5 | **PlantDTO** | 响应 | 植物列表/搜索返回 | id, nameCn, nameDong, category, usageWay, efficacy, story, images, videos, viewCount, favoriteCount, createdAt | `fromEntity(Plant)` |
| 6 | **KnowledgeDTO** | 响应 | 知识列表返回 | id, title, type, therapyCategory, diseaseCategory, content, images, videos, viewCount, favoriteCount, createdAt | `fromEntity(Knowledge)` |
| 7 | **InheritorDTO** | 响应 | 传承人列表返回 | id, name, level(正则校验), bio, images, videos, viewCount, favoriteCount | `fromEntity(Inheritor)` |
| 8 | **CommentDTO** | 响应 | 评论列表返回 | id, userId, username, targetType, targetId, content, replyToId, replyToUserId, replyToUsername, likes, replyCount, status, createTime, updateTime |

### 2.3 内容创建DTO（6个）

| # | DTO | 类型 | 用途 | 核心字段 | 防篡改设计 |
|---|-----|------|------|---------|-----------|
| 9 | **PlantCreateDTO** | 请求 | 管理员新增植物 | nameCn(必填), nameDong, scientificName, category, usageWay, habitat, efficacy, story, images, videos, documents, difficulty | 无id/viewCount/favoriteCount/popularity字段 |
| 10 | **KnowledgeCreateDTO** | 请求 | 管理员新增知识 | title(必填), type, therapyCategory, diseaseCategory, herbCategory, content(必填), formula, usageMethod, steps, images, videos, documents, relatedPlants | 无id/viewCount/favoriteCount/popularity字段 |
| 11 | **InheritorCreateDTO** | 请求 | 管理员新增传承人 | name(必填), level, bio, specialties, experienceYears(必填), videos, images, documents, representativeCases, honors | 无id/viewCount/favoriteCount/popularity字段 |
| 12 | **QaCreateDTO** | 请求 | 管理员新增问答 | category, question(必填), answer(必填) | 无id/popularity/viewCount/favoriteCount字段 |
| 13 | **ResourceCreateDTO** | 请求 | 管理员新增资源 | title(必填), category, files, description | 无id/viewCount/downloadCount/favoriteCount/popularity字段 |
| 14 | **QuizCreateDTO** | 请求 | 管理员新增测验题 | question(必填), options(List\<String\>), answer(必填), category, correctAnswer, explanation | options为List类型，Service层转JSON存储 |

### 2.4 内容更新DTO（6个）

| # | DTO | 类型 | 用途 | 核心字段 | 与CreateDTO区别 |
|---|-----|------|------|---------|----------------|
| 15 | **PlantUpdateDTO** | 请求 | 管理员更新植物 | id(必填) + PlantCreateDTO全部字段 | 多一个@NotNull id字段 |
| 16 | **KnowledgeUpdateDTO** | 请求 | 管理员更新知识 | id(必填) + KnowledgeCreateDTO全部字段 | 多一个@NotNull id字段 |
| 17 | **InheritorUpdateDTO** | 请求 | 管理员更新传承人 | id(必填) + InheritorCreateDTO全部字段 | 多一个@NotNull id字段 |
| 18 | **QaUpdateDTO** | 请求 | 管理员更新问答 | id(必填) + QaCreateDTO全部字段 | 多一个@NotNull id字段 |
| 19 | **ResourceUpdateDTO** | 请求 | 管理员更新资源 | id(必填) + ResourceCreateDTO全部字段 | 多一个@NotNull id字段 |
| 20 | **QuizUpdateDTO** | 请求 | 管理员更新测验题 | id(必填) + QuizCreateDTO全部字段 | 多一个@NotNull id字段 |

### 2.5 互动相关DTO（5个）

| # | DTO | 类型 | 用途 | 核心字段 |
|---|-----|------|------|---------|
| 21 | **CommentAddDTO** | 请求 | 发表评论 | targetType(必填,max50), targetId(必填), content(必填,max1000), replyToId, replyToUserId, replyToUsername |
| 22 | **FeedbackDTO** | 请求 | 提交反馈 | type(必填,max20), title(必填,max200), content(必填,max2000), contact(max100) |
| 23 | **FeedbackReplyDTO** | 请求 | 管理员回复反馈 | reply(必填,max1000) |
| 24 | **UserUpdateDTO** | 请求 | 更新用户信息 | username(3-20) |

### 2.6 答题/游戏相关DTO（5个）

| # | DTO | 类型 | 用途 | 核心字段 |
|---|-----|------|------|---------|
| 25 | **QuizQuestionDTO** | 响应 | 前端获取测验题目 | id, q(题目文本), options(List\<String\>) |
| 26 | **QuizSubmitDTO** | 请求 | 提交测验答案 | answers(List\<AnswerDTO\>, @NotEmpty @Valid) |
| 27 | **AnswerDTO** | 请求 | 单题答案 | questionId(@NotNull), answer |
| 28 | **PlantGameSubmitDTO** | 请求 | 提交植物游戏结果 | difficulty, answers(List\<PlantGameAnswerDTO\>), correctCount, totalCount |
| 29 | **PlantGameAnswerDTO** | 请求 | 植物游戏单题答案 | plantId, userAnswer |

### 2.7 AI聊天相关DTO（2个）

| # | DTO | 类型 | 用途 | 核心字段 |
|---|-----|------|------|---------|
| 30 | **ChatRequest** | 请求 | AI聊天请求 | message(必填,max2000), history(List\<Message\>, max20), 内部类Message(role, content) |
| 31 | **ChatResponse** | 响应 | AI聊天响应 | reply, success, error; 静态工厂方法: `success(reply)`, `error(error)` |

### 2.8 文件上传DTO（1个）

| # | DTO | 类型 | 用途 | 核心字段 |
|---|-----|------|------|---------|
| 32 | **FileUploadResult** | 响应 | 文件上传结果 | success, fileName, originalFileName, filePath, fileUrl, fileType, fileSize, message; 静态工厂方法: `success(...)`, `fail(message)`; @Builder模式 |

---

## 三、设计模式与规范

### 3.1 Create/Update DTO 拆分模式

每个可CRUD的内容实体都有独立的CreateDTO和UpdateDTO：

```
PlantCreateDTO  -- 无id字段，管理员新增时使用
PlantUpdateDTO  -- 有@NotNull id字段，管理员更新时使用
```

**防篡改设计**：CreateDTO和UpdateDTO都不包含以下字段，防止前端篡改：
- `id`（CreateDTO无，UpdateDTO有但@NotNull）
- `viewCount` / `favoriteCount` / `popularity`（统计字段，由系统维护）
- `createdAt` / `updatedAt`（时间字段，由MetaObjectHandler自动填充）
- `updateLog`（更新日志，由Service层自动生成）

### 3.2 响应DTO的fromEntity转换

PlantDTO、KnowledgeDTO、InheritorDTO 都提供静态 `fromEntity()` 方法：

```java
public static PlantDTO fromEntity(Plant plant) {
    if (plant == null) return null;
    PlantDTO dto = new PlantDTO();
    dto.setId(plant.getId());
    dto.setNameCn(plant.getNameCn());
    // ... 只复制前端需要的字段
    return dto;
}
```

**作用**：过滤敏感字段（如passwordHash）、控制返回字段范围、避免Entity结构变化影响API契约。

### 3.3 校验注解规范

| 注解 | 用途 | 示例 |
|------|------|------|
| `@NotBlank` | 字符串非空（不能为null/空/纯空格） | `@NotBlank(message = "标题不能为空")` |
| `@NotNull` | 任意类型非null | `@NotNull(message = "ID不能为空")` |
| `@NotEmpty` | 集合非空 | `@NotEmpty(message = "答案列表不能为空")` |
| `@Size(min, max)` | 长度范围 | `@Size(min = 6, max = 20)` |
| `@Pattern(regexp)` | 正则匹配 | `@Pattern(regexp = "^[a-zA-Z0-9_]+$")` |
| `@Valid` | 级联校验 | `@Valid List<AnswerDTO> answers` |

**校验触发**：Controller方法参数加 `@Valid` 或 `@Validated` 注解。

### 3.4 验证码集成

LoginDTO、RegisterDTO、ChangePasswordDTO 都包含验证码字段：

```java
@NotBlank(message = "验证码key不能为空")
private String captchaKey;      // 验证码唯一标识

@NotBlank(message = "请输入验证码")
private String captchaCode;     // 用户输入的验证码
```

Controller层在处理这些请求时，先调用 `CaptchaService.validateCaptchaOrThrow()` 校验验证码。

### 3.5 QuizQuestionDTO 的字段重命名

```java
@Data
public class QuizQuestionDTO {
    private Integer id;
    private String q;              // 前端用q而非question，减少传输量
    private List<String> options;  // 已解析的选项列表，非原始JSON
}
```

### 3.6 PlantGameSubmitDTO 的双模式设计

```java
@Data
public class PlantGameSubmitDTO {
    private String difficulty;                         // 难度
    private List<PlantGameAnswerDTO> answers;          // 推荐模式：答案列表
    private Integer correctCount;                      // 兼容模式：正确数
    private Integer totalCount;                        // 兼容模式：总题数
}
```

- **推荐模式**：提交 `answers` 列表，服务端验证每道题答案
- **兼容模式**：直接提交 `correctCount/totalCount`，服务端做合理性校验

### 3.7 ChatRequest 的嵌套结构

```java
@Data
public class ChatRequest {
    @NotBlank @Size(max = 2000)
    private String message;               // 当前消息

    @Size(max = 20)
    private List<Message> history;        // 历史消息（最多20条）

    @Data
    public static class Message {         // 内部类
        private String role;              // user / assistant
        @Size(max = 2000)
        private String content;
    }
}
```

### 3.8 FileUploadResult 的Builder模式

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult {
    // 字段...
    public static FileUploadResult success(...) { return FileUploadResult.builder()...build(); }
    public static FileUploadResult fail(String message) { return FileUploadResult.builder()...build(); }
}
```

---

## 四、DTO与Entity的映射关系

```
前端请求 -> Controller
  ↓ @Valid校验
CreateDTO / UpdateDTO / 请求DTO
  ↓ Service层转换
Entity（写入数据库）

数据库查询 -> Service
  ↓ fromEntity()转换
响应DTO（PlantDTO / KnowledgeDTO / InheritorDTO / CommentDTO）
  ↓ R<T>封装
前端响应
```

**关键原则**：
1. **请求方向**：前端 -> DTO -> Entity -> 数据库（DTO字段是Entity的子集）
2. **响应方向**：数据库 -> Entity -> DTO -> 前端（DTO过滤敏感字段）
3. **统计字段**（viewCount/favoriteCount/popularity）由系统维护，前端不可直接修改
4. **时间字段**（createdAt/updatedAt）由MetaObjectHandler自动填充，前端不可直接设置
