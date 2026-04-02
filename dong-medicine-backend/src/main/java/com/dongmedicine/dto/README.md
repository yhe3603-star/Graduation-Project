# 数据传输对象目录 (dto)

本目录存放数据传输对象（Data Transfer Object），用于前后端数据交换。

## 📖 什么是DTO？

DTO（Data Transfer Object）是用于在不同层之间传输数据的对象。它与实体类的区别：
- **实体类**: 与数据库表对应，包含所有字段
- **DTO**: 根据业务需求，只包含需要的字段

## 📁 文件列表

### 请求DTO

| 文件名 | 功能说明 | 使用场景 |
|--------|----------|----------|
| `LoginDTO.java` | 登录请求 | 用户登录 |
| `RegisterDTO.java` | 注册请求 | 用户注册 |
| `UserUpdateDTO.java` | 用户更新请求 | 修改用户信息 |
| `ChangePasswordDTO.java` | 修改密码请求 | 修改密码 |
| `CommentAddDTO.java` | 添加评论请求 | 发表评论 |
| `FeedbackDTO.java` | 反馈请求 | 提交反馈 |
| `ChatRequest.java` | 聊天请求 | AI对话 |
| `QuizSubmitDTO.java` | 答题提交请求 | 提交答案 |
| `PlantGameSubmitDTO.java` | 植物游戏提交请求 | 提交游戏结果 |

### 响应DTO

| 文件名 | 功能说明 | 使用场景 |
|--------|----------|----------|
| `CaptchaDTO.java` | 验证码响应 | 获取验证码 |
| `ChatResponse.java` | 聊天响应 | AI对话返回 |
| `FileUploadResult.java` | 文件上传结果 | 文件上传 |
| `KnowledgeDTO.java` | 知识详情响应 | 知识详情 |
| `PlantDTO.java` | 植物详情响应 | 植物详情 |
| `InheritorDTO.java` | 传承人详情响应 | 传承人详情 |
| `CommentDTO.java` | 评论响应 | 评论列表 |
| `FeedbackReplyDTO.java` | 反馈回复响应 | 反馈回复 |
| `QuizQuestionDTO.java` | 答题题目响应 | 答题题目 |
| `AnswerDTO.java` | 答案响应 | 答案数据 |

## 📦 详细说明

### 1. LoginDTO.java - 登录请求

```java
@Data
public class LoginDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20位")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;
    
    @NotBlank(message = "验证码不能为空")
    private String captcha;
    
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
}
```

### 2. RegisterDTO.java - 注册请求

```java
@Data
public class RegisterDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;
    
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @NotBlank(message = "验证码不能为空")
    private String captcha;
}
```

### 3. QuizSubmitDTO.java - 答题提交请求

```java
@Data
public class QuizSubmitDTO {
    
    @NotNull(message = "答案列表不能为空")
    @Size(min = 1, message = "至少需要提交一个答案")
    private List<AnswerDTO> answers;
    
    @NotNull(message = "每题分数不能为空")
    @Min(value = 1, message = "每题分数必须大于0")
    private Integer scorePerQuestion;
}
```

### 4. QuizQuestionDTO.java - 答题题目响应

```java
@Data
public class QuizQuestionDTO {
    
    private Integer id;
    
    private String q;  // 题目内容
    
    private List<String> options;  // 选项列表
    
    // 注意：不返回正确答案给前端
}
```

## 🎯 DTO规范

### 命名规范
- **请求DTO**: 以`DTO`或`Request`结尾，如`LoginDTO`、`ChatRequest`
- **响应DTO**: 以`DTO`或`Response`结尾，如`KnowledgeDTO`、`ChatResponse`

### 校验注解

| 注解 | 说明 | 示例 |
|------|------|------|
| `@NotNull` | 不能为null | `@NotNull` |
| `@NotBlank` | 不能为空白 | `@NotBlank` |
| `@NotEmpty` | 不能为空 | `@NotEmpty` |
| `@Size` | 长度限制 | `@Size(min=3, max=20)` |
| `@Min` | 最小值 | `@Min(value=1)` |
| `@Max` | 最大值 | `@Max(value=100)` |
| `@Pattern` | 正则匹配 | `@Pattern(regexp="...")` |
| `@Email` | 邮箱格式 | `@Email` |

### 使用示例

**在Controller中使用:**
```java
@PostMapping("/login")
public R<String> login(@RequestBody @Valid LoginDTO dto) {
    // @Valid会自动校验DTO中的注解
    String token = userService.login(dto);
    return R.ok(token);
}
```

**在Service中使用:**
```java
public void register(RegisterDTO dto) {
    // 校验两次密码是否一致
    if (!dto.getPassword().equals(dto.getConfirmPassword())) {
        throw new BusinessException("两次密码不一致");
    }
    
    // DTO转Entity
    User user = new User();
    user.setUsername(dto.getUsername());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setEmail(dto.getEmail());
    
    userMapper.insert(user);
}
```

### DTO与Entity转换
```java
// 使用BeanUtils
User user = new User();
BeanUtils.copyProperties(dto, user);

// 使用MapStruct（推荐）
@Mapper
public interface UserMapper {
    User toEntity(UserDTO dto);
    UserDTO toDTO(User entity);
}
```

### 最佳实践
1. **职责单一**: 每个DTO只服务于一个接口
2. **校验完善**: 使用校验注解进行参数验证
3. **字段最小化**: 只包含需要的字段
4. **文档注释**: 添加字段说明注释
5. **敏感信息**: 响应DTO不包含敏感信息

## 📚 扩展阅读

- [Java Bean Validation](https://beanvalidation.org/)
- [Lombok @Data](https://projectlombok.org/features/Data)
- [MapStruct 对象映射](https://mapstruct.org/)
