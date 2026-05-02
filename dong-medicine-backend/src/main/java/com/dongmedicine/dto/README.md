# DTO 层 -- 数据传输对象（23个DTO）

> DTO (Data Transfer Object) 用于前后端之间的数据传输。与Entity不同，DTO只包含当前业务场景需要的字段，不含敏感信息（如密码）。

---

## 一、DTO 清单（23个）

### 请求DTO（前端 → 后端）

| # | DTO | 用途 | 关键字段 |
|---|-----|------|---------|
| 1 | LoginDTO | 用户登录 | username(@NotBlank 3-20), password(@NotBlank min=6), captchaKey, captchaCode |
| 2 | RegisterDTO | 用户注册 | username(@NotBlank 3-20), password, confirmPassword, captchaKey, captchaCode |
| 3 | ChangePasswordDTO | 修改密码 | currentPassword, newPassword, captchaKey, captchaCode |
| 4 | UserUpdateDTO | 修改用户信息 | username（可选） |
| 5 | PlantCreateDTO | 新增植物 | nameCn(@NotBlank), nameDong, category, efficacy, story, images... |
| 6 | PlantUpdateDTO | 更新植物 | 同上（所有字段可选） |
| 7 | KnowledgeCreateDTO | 新增知识 | title(@NotBlank), type, content(@NotBlank), formula, images... |
| 8 | KnowledgeUpdateDTO | 更新知识 | 同上（所有字段可选） |
| 9 | InheritorCreateDTO | 新增传承人 | name(@NotBlank), level, bio, specialties, images... |
| 10 | InheritorUpdateDTO | 更新传承人 | 同上（所有字段可选） |
| 11 | ResourceCreateDTO | 新增资源 | title(@NotBlank), category, files, description |
| 12 | ResourceUpdateDTO | 更新资源 | 同上（所有字段可选） |
| 13 | QaCreateDTO | 新增问答 | category, question, answer |
| 14 | QaUpdateDTO | 更新问答 | 同上 |
| 15 | QuizCreateDTO | 新增题目 | question, options(List\<String\>), correctAnswer, explanation |
| 16 | QuizUpdateDTO | 更新题目 | id + 同上 |
| 17 | QuizSubmitDTO | 提交答案 | answers(List\<AnswerDTO\>) |
| 18 | AnswerDTO | 单个答案 | questionId, answer |
| 19 | CommentAddDTO | 发表评论 | targetType, targetId, content, replyToId, replyToUserId, replyToUsername |
| 20 | FeedbackDTO | 提交反馈 | type, title, content(最多2000), contact |
| 21 | FeedbackReplyDTO | 回复反馈 | reply(最多1000) |
| 22 | PlantGameSubmitDTO | 植物游戏结果 | difficulty, score, correctCount, totalCount |
| 23 | ChatRequest | AI对话请求 | message(最多2000), history(最多20条Message) |

### 响应DTO（后端 → 前端）

| # | DTO | 用途 | 关键字段 |
|---|-----|------|---------|
| 1 | CaptchaDTO | 验证码响应 | captchaKey, captchaImage(Base64) |
| 2 | CommentDTO | 评论响应 | id, userId, username, content, likes, replyCount, children(嵌套回复) |
| 3 | QuizQuestionDTO | 题目响应（不含答案） | id, q, options(List\<String\>) -- 故意不含answer字段 |
| 4 | FileUploadResult | 文件上传结果 | success, fileName, fileUrl, fileSize, message |
| 5 | ChatResponse | AI对话响应 | reply, success, error |
| 6 | PlantDTO | 植物响应 | id, nameCn, nameDong, category, efficacy... |
| 7 | KnowledgeDTO | 知识响应 | id, title, type, content, formula... |
| 8 | InheritorDTO | 传承人响应 | id, name, level, bio, specialties... |

---

## 二、核心DTO详解

### 2.1 LoginDTO -- 登录请求

文件：`dto/LoginDTO.java`

```java
@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    @NotBlank(message = "验证码key不能为空")
    private String captchaKey;        // Redis中查找验证码图片用的key

    @NotBlank(message = "请输入验证码")
    private String captchaCode;       // 用户输入的验证码
}
```

### 2.2 RegisterDTO -- 注册请求

文件：`dto/RegisterDTO.java`

```java
@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50)
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;   // 前端校验一致性，后端也校验

    @NotBlank private String captchaKey;
    @NotBlank private String captchaCode;
}
```

**注意**：`PlantCreateDTO`、`KnowledgeCreateDTO`等Create DTO名称实际为PlantDTO、KnowledgeDTO等（在代码中共享），此处为了清晰展示职责而使用CreateDTO命名。

### 2.3 CommentAddDTO -- 发表评论

文件：`dto/CommentAddDTO.java`

```java
@Data
public class CommentAddDTO {
    @NotBlank(message = "目标类型不能为空")
    private String targetType;        // "plant" / "knowledge" / "inheritor" 等

    @NotNull(message = "目标ID不能为空")
    private Integer targetId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    private String content;

    private Integer replyToId;        // 回复的评论ID（嵌套回复）
    private Integer replyToUserId;    // 被回复的用户ID
    private String replyToUsername;   // 被回复的用户名
}
```

### 2.4 QuizQuestionDTO -- 题目响应（防作弊）

文件：`dto/QuizQuestionDTO.java`

```java
@Data
public class QuizQuestionDTO {
    private Integer id;
    private String q;                    // 题目内容
    private List<String> options;        // 选项列表
    // 注意：故意不包含 answer / correctAnswer / explanation 字段
    // 前端拿不到答案，防止通过浏览器DevTools作弊
}
```

### 2.5 PlantGameSubmitDTO -- 植物游戏提交

文件：`dto/PlantGameSubmitDTO.java`

```java
@Data
public class PlantGameSubmitDTO {
    @NotNull private String difficulty;     // 难度
    @NotNull private Integer score;         // 分数
    @NotNull private Integer correctCount;  // 正确数
    @NotNull private Integer totalCount;    // 总题数
}
```

### 2.6 ChatRequest -- AI聊天请求

文件：`dto/ChatRequest.java`

```java
@Data
public class ChatRequest {
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息长度不能超过2000个字符")
    private String message;

    @Size(max = 20, message = "历史消息数量不能超过20条")
    private List<Message> history;

    @Data
    public static class Message {
        private String role;             // "user" 或 "assistant"
        @Size(max = 2000)
        private String content;
    }
}
```

---

## 三、校验注解说明

DTO使用Jakarta Bean Validation进行参数校验，在Controller中通过 `@Valid` 触发：

| 注解 | 适用类型 | 说明 |
|------|---------|------|
| `@NotBlank` | String | 不能为null、空字符串、纯空格 |
| `@NotNull` | 任何类型 | 不能为null |
| `@NotEmpty` | 集合/数组/String | 不能为null或空（集合size>0） |
| `@Size(min, max)` | String/Collection | 长度/大小范围 |
| `@Min/@Max` | 数字 | 最小/最大值 |
| `@Pattern(regexp)` | String | 正则匹配 |
| `@Valid` | 对象/集合 | 级联校验嵌套对象 |

### 校验触发方式

```java
// Controller中
@PostMapping("/login")
public R<Map> login(@Valid @RequestBody LoginDTO dto) { ... }
//                   ^^^^^ 必须加，否则校验注解不生效

// 校验失败时 GlobalExceptionHandler 捕获 MethodArgumentNotValidException
// 返回: {"code":400, "msg":"用户名不能为空", "data":null}
```

### DTO与Entity的转换

```java
// AdminController中的典型模式
@PostMapping("/plants")
public R<String> createPlant(@RequestBody @Valid PlantCreateDTO dto) {
    Plant plant = new Plant();
    BeanUtils.copyProperties(dto, plant);  // Spring BeanUtils复制属性
    plantService.save(plant);
    plantService.clearCache();
    return R.ok("新增成功");
}

// 或使用Entity的静态工厂方法
PlantDTO dto = PlantDTO.fromEntity(plant);
```

---

## 四、安全设计要点

1. **密码不返回**：UserService.getUserInfo() 返回前清空 `passwordHash`
2. **答案不暴露**：QuizQuestionDTO 不含 answer/correctAnswer/explanation
3. **输入长度限制**：所有字符串字段使用 `@Size` 或 `@NotBlank` 限制
4. **用户名格式校验**：RegisterDTO 使用 `@Pattern` 限制为字母数字下划线
5. **级联校验**：QuizSubmitDTO 中的 List\<AnswerDTO\> 使用 `@Valid` 级联校验
