# DTO 数据传输对象目录 (dto)

本目录存放数据传输对象（DTO），用于接收前端请求和返回数据。

## 目录

- [什么是DTO？](#什么是dto)
- [目录结构](#目录结构)
- [DTO列表](#dto列表)
- [开发规范](#开发规范)
- [常用DTO详解](#常用dto详解)

---

## 什么是DTO？

### DTO的概念

**DTO（Data Transfer Object，数据传输对象）** 是用于在不同层之间传输数据的对象。它就像一个"快递包裹"——把需要传输的数据打包好，从前端送到后端，或者从后端送到前端。

### 为什么需要DTO？

```
┌─────────────────────────────────────────────────────────────────┐
│                      不使用DTO                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  前端发送：                                                      │
│  {                                                              │
│    "username": "admin",                                         │
│    "password": "123456",                                        │
│    "confirmPassword": "123456",                                 │
│    "captchaKey": "abc123",                                      │
│    "captchaCode": "1234"                                        │
│  }                                                              │
│                                                                 │
│  直接使用Entity接收：                                            │
│  - Entity没有confirmPassword字段                                │
│  - Entity没有captchaKey/captchaCode字段                         │
│  - Entity可能有敏感字段（如passwordHash）不应该暴露              │
│                                                                 │
│  → 字段不匹配、安全问题                                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       使用DTO                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  前端发送 → RegisterDTO（专门用于注册）                          │
│  {                                                              │
│    "username": "admin",        ← RegisterDTO有这个字段          │
│    "password": "123456",       ← RegisterDTO有这个字段          │
│    "confirmPassword": "123456", ← RegisterDTO有这个字段         │
│    "captchaKey": "abc123",     ← RegisterDTO有这个字段          │
│    "captchaCode": "1234"       ← RegisterDTO有这个字段          │
│  }                                                              │
│                                                                 │
│  Service层处理：                                                 │
│  1. 验证DTO数据                                                  │
│  2. 创建Entity对象                                               │
│  3. 保存到数据库                                                 │
│                                                                 │
│  → 字段匹配、安全可控                                            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### DTO与Entity的区别

| 对比项 | Entity | DTO |
|--------|--------|-----|
| 用途 | 与数据库交互 | 与前端交互 |
| 字段 | 与数据库表对应 | 根据接口需求定义 |
| 验证 | 无 | 有验证注解 |
| 敏感信息 | 可能包含 | 不包含敏感信息 |

---

## 目录结构

```
dto/
│
├── LoginDTO.java                      # 登录请求
├── RegisterDTO.java                   # 注册请求
├── ChangePasswordDTO.java             # 修改密码请求
├── CaptchaDTO.java                    # 验证码响应
│
├── PlantDTO.java                      # 植物数据传输
├── KnowledgeDTO.java                  # 知识数据传输
├── InheritorDTO.java                  # 传承人数据传输
├── ResourceDTO.java                   # 资源数据传输
│
├── CommentDTO.java                    # 评论数据
├── CommentAddDTO.java                 # 添加评论请求
├── FeedbackDTO.java                   # 反馈数据
├── FeedbackReplyDTO.java              # 反馈回复
│
├── QuizQuestionDTO.java               # 测验问题
├── QuizSubmitDTO.java                 # 提交答案
├── AnswerDTO.java                     # 答案数据
├── PlantGameSubmitDTO.java            # 植物游戏提交
│
├── ChatRequest.java                   # AI聊天请求
├── ChatResponse.java                  # AI聊天响应
│
└── LoginVO.java                       # 登录响应
```

---

## DTO列表

### 请求DTO

| DTO | 用途 |
|-----|------|
| LoginDTO | 用户登录请求 |
| RegisterDTO | 用户注册请求 |
| ChangePasswordDTO | 修改密码请求 |
| CommentAddDTO | 添加评论请求 |
| QuizSubmitDTO | 提交测验答案 |
| PlantGameSubmitDTO | 提交游戏结果 |
| ChatRequest | AI聊天请求 |
| FeedbackReplyDTO | 反馈回复 |

### 响应DTO

| DTO | 用途 |
|-----|------|
| LoginVO | 登录响应（包含Token） |
| CaptchaDTO | 验证码响应 |
| QuizResultVO | 测验结果响应 |
| ChatResponse | AI聊天响应 |

### 数据传输DTO

| DTO | 用途 |
|-----|------|
| PlantDTO | 植物数据传输 |
| KnowledgeDTO | 知识数据传输 |
| InheritorDTO | 传承人数据传输 |
| ResourceDTO | 资源数据传输 |
| CommentDTO | 评论数据传输 |
| FeedbackDTO | 反馈数据传输 |
| QuizQuestionDTO | 测验问题数据 |

---

## 开发规范

### 1. 请求DTO规范

```java
/**
 * 用户登录请求DTO
 */
@Data
public class LoginDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度为2-20个字符")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
    
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}
```

### 2. 响应DTO规范

```java
/**
 * 登录响应VO
 */
@Data
@Builder
public class LoginVO {
    
    private String token;       // JWT Token
    
    private Integer userId;     // 用户ID
    
    private String username;    // 用户名
    
    private String role;        // 角色
}
```

### 3. 验证注解说明

| 注解 | 说明 | 示例 |
|------|------|------|
| `@NotBlank` | 字符串不能为空 | `@NotBlank(message = "用户名不能为空")` |
| `@NotNull` | 不能为null | `@NotNull(message = "ID不能为空")` |
| `@Size` | 长度限制 | `@Size(min = 2, max = 20)` |
| `@Min` | 最小值 | `@Min(value = 0)` |
| `@Max` | 最大值 | `@Max(value = 100)` |
| `@Pattern` | 正则验证 | `@Pattern(regexp = "^[a-zA-Z0-9]+$")` |
| `@Email` | 邮箱格式 | `@Email(message = "邮箱格式不正确")` |

---

## 常用DTO详解

### LoginDTO - 登录请求

```java
/**
 * 用户登录请求DTO
 */
@Data
public class LoginDTO {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
    
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}
```

### RegisterDTO - 注册请求

```java
/**
 * 用户注册请求DTO
 */
@Data
public class RegisterDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度为2-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,50}$", 
             message = "密码长度8-50位，必须包含字母和数字")
    private String password;
    
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
    
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}
```

### LoginVO - 登录响应

```java
/**
 * 登录响应VO
 * 返回给前端的登录结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    
    private String token;       // JWT Token
    
    private Integer userId;     // 用户ID
    
    private String username;    // 用户名
    
    private String role;        // 角色
}
```

### QuizSubmitDTO - 提交答案

```java
/**
 * 提交测验答案DTO
 */
@Data
public class QuizSubmitDTO {
    
    @NotNull(message = "答案列表不能为空")
    @Size(min = 1, message = "至少需要提交一个答案")
    private List<AnswerDTO> answers;
    
    @NotNull(message = "每题分数不能为空")
    @Min(value = 1, message = "每题分数至少为1")
    private Integer scorePerQuestion;
}

/**
 * 答案DTO
 */
@Data
public class AnswerDTO {
    
    @NotNull(message = "问题ID不能为空")
    private Integer questionId;
    
    @NotBlank(message = "答案不能为空")
    private String answer;
}
```

### ChatRequest - AI聊天请求

```java
/**
 * AI聊天请求DTO
 */
@Data
public class ChatRequest {
    
    @NotBlank(message = "消息不能为空")
    @Size(max = 2000, message = "消息长度不能超过2000个字符")
    private String message;
    
    private List<Message> history;  // 历史消息
    
    @Data
    public static class Message {
        private String role;      // user 或 assistant
        private String content;   // 消息内容
    }
}
```

---

## 使用示例

### Controller中使用

```java
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    // 使用 @Valid 触发参数验证
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        // 如果验证失败，会自动返回错误信息
        // 这里只需要处理业务逻辑
        return R.ok(userService.login(dto));
    }
    
    @PostMapping("/register")
    public R<String> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return R.ok("注册成功");
    }
}
```

### Service中使用

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void register(RegisterDTO dto) {
        // 1. 验证两次密码是否一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }
        
        // 2. 检查用户名是否存在
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 3. 创建Entity
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole("USER");
        user.setStatus(0);
        
        // 4. 保存
        userMapper.insert(user);
    }
}
```

---

## 最佳实践

### 1. DTO与Entity分离

```java
// ✅ 好的做法：使用DTO接收请求
public void register(RegisterDTO dto) { ... }

// ❌ 不好的做法：直接使用Entity
public void register(User user) { ... }
```

### 2. 使用验证注解

```java
// ✅ 好的做法：使用注解验证
@NotBlank(message = "用户名不能为空")
@Size(min = 2, max = 20, message = "用户名长度为2-20个字符")
private String username;

// ❌ 不好的做法：手动验证
if (username == null || username.length() < 2) {
    throw new BusinessException("用户名不合法");
}
```

### 3. 响应DTO不包含敏感信息

```java
// ✅ 好的做法：不返回敏感信息
public class LoginVO {
    private String token;
    private String username;
    // 不包含passwordHash
}

// ❌ 不好的做法：直接返回Entity
return user;  // 可能包含passwordHash等敏感信息
```

---

**最后更新时间**：2026年4月3日
