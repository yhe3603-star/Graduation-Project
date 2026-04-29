# DTO 层 -- 数据传输对象（快递包裹）

> 本目录存放所有 DTO（Data Transfer Object），用于在前后端之间传输数据。

---

## 一、什么是 DTO？

**类比：快递包裹，只装需要运送的东西**

DTO 全称是 **Data Transfer Object**（数据传输对象），就像快递包裹：

- 你寄快递时，不会把整个房间的东西都寄出去，只寄对方需要的东西
- 同样，DTO 只包含前端需要的数据，不会把数据库的所有字段都暴露出去

```
Entity（整个房间）                DTO（快递包裹）
+------------------+             +------------------+
| id               |             | id               |
| username         |             | username         |
| passwordHash     |  不寄！ --> |                  |  密码不暴露！
| role             |             | role             |
| status           |             | status           |
| createdAt        |             | createdAt        |
+------------------+             +------------------+
  包含所有数据库字段               只包含前端需要的字段
```

---

## 二、为什么用 DTO 而不是直接用 Entity？

### 2.1 安全性：不暴露敏感信息

**最典型的例子：密码**

```java
// Entity：包含密码哈希，绝对不能直接返回给前端！
public class User {
    private Integer id;
    private String username;
    private String passwordHash;   // <-- 敏感信息！不能让前端看到！
    private String role;
    private String status;
}

// DTO：不包含密码，安全地返回给前端
public class UserUpdateDTO {
    private String username;       // <-- 只暴露允许修改的字段
}
```

如果直接返回 Entity，前端就能看到 `passwordHash` 字段（即使值是加密的，也不应该暴露）。

### 2.2 灵活性：不同场景需要不同的字段

同一个 Entity，在不同场景下需要不同的字段组合：

```
场景1：用户登录        --> LoginDTO（需要 username + password + 验证码）
场景2：用户注册        --> RegisterDTO（需要 username + password + 确认密码 + 验证码）
场景3：修改用户信息    --> UserUpdateDTO（只需要 username）
场景4：修改密码        --> ChangePasswordDTO（需要当前密码 + 新密码 + 验证码）
```

如果只用一个 Entity，所有场景都混在一起，既不清晰也不安全。

### 2.3 验证规则：不同场景有不同的校验

```
注册时：密码长度 6-50 位
修改密码时：密码长度 6-20 位（更严格）
登录时：密码只需要不为空
```

用不同的 DTO，每个 DTO 可以定义自己的验证规则。

---

## 三、Entity vs DTO 对比表

| 对比项 | Entity | DTO |
|--------|--------|-----|
| **用途** | 和数据库交互 | 和前端交互 |
| **字段来源** | 对应数据库表的列 | 根据业务需求选择 |
| **包含敏感信息** | 可能包含（如 passwordHash） | 不包含 |
| **校验注解** | 较少（只校验数据库约束） | 较多（校验业务规则） |
| **数量** | 一张表一个 | 一个 Entity 可能有多个 DTO |
| **命名规范** | `Plant`、`User` | `PlantDTO`、`LoginDTO`、`RegisterDTO` |
| **谁使用** | Mapper、Service | Controller、前端 |

```
数据流向：

  前端 --(请求DTO)--> Controller --(Entity)--> Service --(Entity)--> Mapper --> 数据库
  前端 <-(响应DTO)-- Controller <-(Entity)-- Service <-(Entity)-- Mapper <-- 数据库

  进入时：DTO -> Entity（只取需要的字段）
  返回时：Entity -> DTO（只暴露允许的字段）
```

---

## 四、校验注解详解

### 4.1 什么是校验注解？

**类比：快递员检查包裹**

寄快递时，快递员会检查：地址填了没？电话号码格式对不对？包裹超重了吗？
校验注解就是 Java 世界里的"快递员"，在数据进入系统前自动检查：

- 用户名填了没？ -> `@NotBlank`
- 密码够不够长？ -> `@Size(min = 6)`
- 级别是不是合法值？ -> `@Pattern(regexp = "...")`

### 4.2 常用校验注解一览

| 注解 | 作用 | 示例 |
|------|------|------|
| `@NotBlank` | 不能为空（字符串） | `@NotBlank(message = "用户名不能为空")` |
| `@NotNull` | 不能为 null（任何类型） | `@NotNull(message = "目标ID不能为空")` |
| `@NotEmpty` | 不能为空（集合/数组） | `@NotEmpty(message = "答案列表不能为空")` |
| `@Size(min, max)` | 长度范围 | `@Size(min = 3, max = 20, message = "用户名长度3-20")` |
| `@Pattern(regexp)` | 正则匹配 | `@Pattern(regexp = "^[a-zA-Z0-9_]+$")` |
| `@Valid` | 级联校验（校验对象内部） | `@Valid @NotEmpty List<AnswerDTO> answers` |

### 4.3 @NotBlank vs @NotNull vs @NotEmpty 的区别

```
@NotBlank（字符串专用）：null、""、"   " 都不行，必须有实际内容
  "hello"  -> 通过
  ""       -> 不通过
  "   "    -> 不通过（空格也不行）
  null     -> 不通过

@NotNull（通用）：只要不是 null 就行
  "hello"  -> 通过
  ""       -> 通过（空字符串也算"不是null"）
  "   "    -> 通过（空格也算"不是null"）
  null     -> 不通过

@NotEmpty（集合/数组/字符串）：不能为空，必须有内容
  [1,2,3]  -> 通过
  []       -> 不通过
  null     -> 不通过
```

**选择建议**：
- 字符串字段用 `@NotBlank`（最严格，推荐）
- 数字/对象字段用 `@NotNull`
- 列表/集合字段用 `@NotEmpty`

---

## 五、完整示例：LoginDTO 逐行解读

```java
package com.dongmedicine.dto;

import lombok.Data;                                    // Lombok 注解
import jakarta.validation.constraints.NotBlank;        // 非空校验
import jakarta.validation.constraints.Size;            // 长度校验

@Data   // [1] Lombok：自动生成 getter/setter
public class LoginDTO {

    // [2] 用户名：不能为空，长度3-20
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    // [3] 密码：不能为空，最少6位
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    // [4] 验证码key：不能为空（用于从 Redis 中查找验证码图片）
    @NotBlank(message = "验证码key不能为空")
    private String captchaKey;

    // [5] 验证码：不能为空（用户输入的验证码）
    @NotBlank(message = "请输入验证码")
    private String captchaCode;
}
```

**校验流程**：

```
前端提交数据                    后端校验                        结果
+------------------+         +------------------+         +------------------+
| username: ""     |  ---->  | @NotBlank 失败    |  ---->  | 返回错误：        |
| password: "123"  |         | @Size(min=6) 失败 |         | "用户名不能为空"   |
| captchaKey: "xx" |         |                   |         | "密码长度不能少于6"|
| captchaCode: ""  |         | @NotBlank 失败    |         | "请输入验证码"     |
+------------------+         +------------------+         +------------------+

前端提交数据                    后端校验                        结果
+------------------+         +------------------+         +------------------+
| username: "admin"|  ---->  | 全部通过          |  ---->  | 继续执行登录逻辑   |
| password: "123456"|        |                   |         |                   |
| captchaKey: "abc" |        |                   |         |                   |
| captchaCode: "A3x" |       |                   |         |                   |
+------------------+         +------------------+         +------------------+
```

**Controller 中如何触发校验**：

```java
@PostMapping("/login")
public R<String> login(@Valid @RequestBody LoginDTO loginDTO) {
    //                  ^^^^^ 加 @Valid 才会触发校验！
    // 如果校验失败，Spring 会自动返回 400 错误和错误信息
    // 只有校验通过，才会执行到这里
    return R.ok(jwtToken);
}
```

---

## 六、本项目全部 DTO 一览

### 6.1 请求 DTO（前端发给后端的数据）

| 序号 | DTO 类名 | 用途 | 关键字段 | 校验规则 |
|------|---------|------|---------|---------|
| 1 | `LoginDTO` | 用户登录 | username, password, captchaKey, captchaCode | 用户名3-20位，密码>=6位 |
| 2 | `RegisterDTO` | 用户注册 | username, password, confirmPassword, captchaKey, captchaCode | 用户名3-20位只含字母数字下划线，密码6-50位 |
| 3 | `ChangePasswordDTO` | 修改密码 | currentPassword, newPassword, captchaKey, captchaCode | 新密码6-20位 |
| 4 | `UserUpdateDTO` | 修改用户信息 | username | 用户名3-20位（可选） |
| 5 | `PlantDTO` | 新增/编辑植物 | nameCn, nameDong, category, efficacy, story, images... | 中文名必填，最长100字符 |
| 6 | `KnowledgeDTO` | 新增/编辑知识 | title, type, therapyCategory, content, images... | 标题必填，最长200字符 |
| 7 | `InheritorDTO` | 新增/编辑传承人 | name, level, bio, images, videos... | 姓名必填，级别必须是"国家级/省级/市级/县级/自治区级" |
| 8 | `FeedbackDTO` | 提交反馈 | type, title, content, contact | 类型必填最长20，标题必填最长200，内容必填最长2000 |
| 9 | `FeedbackReplyDTO` | 回复反馈 | reply | 回复内容必填，最长1000 |
| 10 | `CommentAddDTO` | 添加评论 | targetType, targetId, content, replyToId... | 目标类型必填，内容必填最长1000 |
| 11 | `QuizSubmitDTO` | 提交测验答案 | answers (List<AnswerDTO>) | 答案列表不能为空，内部级联校验 |
| 12 | `AnswerDTO` | 单个答案 | questionId, answer | 题目ID不能为null，答案不能为空 |
| 13 | `PlantGameSubmitDTO` | 提交植物游戏结果 | difficulty, score, correctCount, totalCount | 所有字段不能为空 |
| 14 | `ChatRequest` | AI 对话请求 | message, history | 消息必填最长2000，历史最多20条 |

### 6.2 响应 DTO（后端返回给前端的数据）

| 序号 | DTO 类名 | 用途 | 关键字段 | 说明 |
|------|---------|------|---------|------|
| 1 | `CaptchaDTO` | 验证码响应 | captchaKey, captchaImage | 包含验证码 key 和 Base64 图片 |
| 2 | `QuizQuestionDTO` | 测验题目响应 | id, q, options | 只返回题目和选项，不返回答案 |
| 3 | `CommentDTO` | 评论响应 | id, userId, username, content, likes... | 评论详情，包含用户名 |
| 4 | `FileUploadResult` | 文件上传结果 | success, fileName, fileUrl, fileSize... | 上传成功/失败信息 |
| 5 | `ChatResponse` | AI 对话响应 | reply, success, error | AI 回复内容或错误信息 |

### 6.3 特殊 DTO 说明

**QuizQuestionDTO** -- 不返回答案和解析（防作弊）：

```java
@Data
public class QuizQuestionDTO {
    private Integer id;           // 题目ID
    private String q;             // 题目内容
    private List<String> options; // 选项列表
    // 注意：没有 answer、correctAnswer、explanation 字段！
    // 前端拿不到答案，防止作弊
}
```

**FileUploadResult** -- 使用了 Builder 模式：

```java
@Data
@Builder          // [1] 建造者模式：链式创建对象
@NoArgsConstructor   // [2] 无参构造器
@AllArgsConstructor  // [3] 全参构造器
public class FileUploadResult {
    private boolean success;
    private String fileName;
    private String fileUrl;
    // ...

    // 静态工厂方法：创建成功结果
    public static FileUploadResult success(...) {
        return FileUploadResult.builder()
                .success(true)
                .fileName(fileName)
                // ... 链式调用，代码更清晰
                .build();
    }

    // 静态工厂方法：创建失败结果
    public static FileUploadResult fail(String message) {
        return FileUploadResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}
```

**ChatRequest** -- 包含嵌套 DTO：

```java
@Data
public class ChatRequest {
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息长度不能超过2000个字符")
    private String message;

    @Size(max = 20, message = "历史消息数量不能超过20条")
    private List<Message> history;   // <-- 嵌套的内部类

    @Data
    public static class Message {    // <-- 静态内部类
        private String role;
        @Size(max = 2000, message = "历史消息长度不能超过2000个字符")
        private String content;
    }
}
```

---

## 七、DTO 在 Controller 中的使用

### 7.1 请求 DTO：接收前端数据

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    // 用 @RequestBody 接收 JSON 数据，用 @Valid 触发校验
    @PostMapping("/login")
    public R<String> login(@Valid @RequestBody LoginDTO loginDTO) {
    //                       ^^^^^  ^^^^^^^^^^^
    //                       校验    请求DTO
        String token = userService.login(loginDTO);
        return R.ok(token);
    }

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return R.ok();
    }
}
```

### 7.2 响应 DTO：返回给前端数据

```java
@GetMapping("/captcha")
public R<CaptchaDTO> getCaptcha() {
    //                   ^^^^^^^^^ 响应DTO
    CaptchaDTO captcha = captchaService.generateCaptcha();
    return R.ok(captcha);
}
```

### 7.3 Entity 转 DTO 的转换

```java
// PlantDTO 中的静态转换方法
public static PlantDTO fromEntity(Plant plant) {
    if (plant == null) return null;     // 空值保护
    PlantDTO dto = new PlantDTO();
    dto.setId(plant.getId());           // 逐个字段复制
    dto.setNameCn(plant.getNameCn());
    dto.setNameDong(plant.getNameDong());
    dto.setCategory(plant.getCategory());
    dto.setUsageWay(plant.getUsageWay());
    dto.setEfficacy(plant.getEfficacy());
    dto.setStory(plant.getStory());
    dto.setImages(plant.getImages());
    dto.setVideos(plant.getVideos());
    dto.setDocuments(plant.getDocuments());
    dto.setViewCount(plant.getViewCount());
    dto.setFavoriteCount(plant.getFavoriteCount());
    dto.setCreatedAt(plant.getCreatedAt());
    return dto;
    // 注意：没有复制 updateLog、distribution 等前端不需要的字段
}
```

**使用方式**：

```java
// 在 Service 中转换
Plant plant = plantMapper.selectById(id);
PlantDTO dto = PlantDTO.fromEntity(plant);  // Entity -> DTO
return dto;
```

---

## 八、常见错误与解决方案

### 错误 1：忘记加 @Valid

```
现象：DTO 上的 @NotBlank 校验没有生效，空数据也能提交成功
原因：Controller 方法参数上没有加 @Valid 注解
解决：在参数前加 @Valid，如 @Valid @RequestBody LoginDTO loginDTO
```

### 错误 2：混淆 @NotBlank 和 @NotNull

```
错误：用 @NotNull 校验字符串
@NotNull
private String username;

问题："" (空字符串) 和 "   " (空格) 也能通过校验！
正确：字符串应该用 @NotBlank
@NotBlank
private String username;
```

### 错误 3：DTO 直接暴露了敏感字段

```
错误：在 CommentDTO 中暴露了用户密码
public class CommentDTO {
    private String passwordHash;  // <-- 绝对不能有！
}

正确：DTO 只包含前端需要展示的字段
public class CommentDTO {
    private Integer userId;
    private String username;
    private String content;
}
```

### 错误 4：@Size 用在数字类型上

```
错误：@Size 用在 Integer 上
@Size(min = 1, max = 100)
private Integer score;   // @Size 只能用在字符串和集合上！

正确：数字范围用 @Min 和 @Max
@Min(value = 0)
@Max(value = 100)
private Integer score;
```

### 错误 5：忘记处理校验异常

```
现象：校验失败时返回 500 错误，前端看不到友好的错误提示
原因：没有全局异常处理器
解决：在 GlobalExceptionHandler 中处理 MethodArgumentNotValidException
```

---

## 九、速记口诀

```
DTO 是快递包，只装需要的东西
密码不暴露，安全排第一
不同场景不同 DTO，校验规则各自定
@NotBlank 字符串，@NotNull 通用型
@Size 控长度，@Pattern 正则行
Controller 加 @Valid，校验才能生效
Entity 转 DTO，fromEntity 来帮忙
```

---

## 十、代码审查与改进建议

以下是对 DTO 层代码的审查发现：

### 安全级别

| # | 级别 | 问题 | 涉及 DTO | 说明 |
|---|------|------|---------|------|
| 1 | 安全 | 密码最大长度限制不一致 | `ChangePasswordDTO` / `RegisterDTO` / `PasswordValidator` | `ChangePasswordDTO` 密码 `max=20`，`RegisterDTO` 密码 `max=50`，`PasswordValidator` 中 `MAX_LENGTH=50`，三处密码长度限制不一致。攻击者可通过修改密码接口提交超过20字符的密码（绕过前端校验），导致修改密码时被截断或校验失败。应统一为 `PasswordValidator.MAX_LENGTH=50`，所有 DTO 的 `@Size(max=50)` 保持一致。 |
| 2 | 安全 | `LoginDTO` 密码只有 `min=6` 没有 `max` 限制 | `LoginDTO` | 登录时密码字段只校验了最小长度6位，没有最大长度限制。恶意用户可提交超长密码（如百万字符），BCrypt 加密会消耗大量 CPU 资源，造成拒绝服务攻击（DoS）。应添加 `@Size(max = 50)` 限制，与注册密码长度保持一致。 |
| 3 | 安全 | `ChatRequest.Message` 的 `role` 字段没有验证 | `ChatRequest` | `role` 字段没有任何校验注解，用户可传入 `"system"` 角色，绕过 AI 系统的安全限制提示词，诱导模型输出不当内容。应使用 `@Pattern(regexp = "^(user|assistant)$")` 限制 `role` 只能为 `"user"` 或 `"assistant"`。 |
| 4 | 安全 | `CommentAddDTO` 的 `replyToUsername` 可被客户端伪造 | `CommentAddDTO` | `replyToUsername` 字段由客户端直接提供，恶意用户可伪造被回复人的用户名，造成误导。应从数据库根据 `replyToId` 查询对应的用户名，而非信任客户端提交的值。 |

### 结构级别

| # | 级别 | 问题 | 涉及 DTO | 说明 |
|---|------|------|---------|------|
| 5 | 结构 | 响应 DTO 不应包含验证注解 | `PlantDTO` / `KnowledgeDTO` / `InheritorDTO` 等 | 这些 DTO 同时用于新增/编辑请求和响应返回，包含了 `@NotBlank`、`@Size` 等验证注解。验证注解应只在请求 DTO 上使用，响应 DTO 不需要验证。应将请求和响应 DTO 拆分为两个类（如 `PlantCreateDTO` 和 `PlantResponseDTO`），职责分离更清晰。 |
| 6 | 结构 | `QuizCreateDTO` 同时存在 `answer` 和 `correctAnswer` 两个字段 | `QuizCreateDTO` | 两个字段语义混淆，不清楚哪个是正确答案。应统一为一个字段（如 `correctAnswer`），删除冗余的 `answer` 字段，避免使用时产生歧义。 |
| 7 | 结构 | `PlantGameSubmitDTO` 缺少分数范围校验 | `PlantGameSubmitDTO` | `score`、`correctCount`、`totalCount` 字段只有 `@NotNull` 校验，没有范围限制。客户端可提交不合理的数据（如负数分数、`correctCount > totalCount` 等）。应添加 `@Min(0)` 和逻辑校验确保数据合理性。 |
