# 控制器层 (controller)

本目录存放所有控制器类，负责接收HTTP请求并返回响应。

## 📖 什么是控制器？

控制器是MVC架构中的C（Controller），它是前端和后端交互的桥梁。控制器负责：
- 接收前端发送的HTTP请求
- 解析请求参数
- 调用Service层处理业务逻辑
- 返回响应数据给前端

## 📁 文件列表

| 文件名 | 功能说明 | API路径前缀 |
|--------|----------|-------------|
| `UserController.java` | 用户管理控制器 | `/api/user` |
| `AdminController.java` | 管理员控制器 | `/api/admin` |
| `PlantController.java` | 植物管理控制器 | `/api/plants` |
| `KnowledgeController.java` | 知识库控制器 | `/api/knowledge` |
| `InheritorController.java` | 传承人控制器 | `/api/inheritors` |
| `ResourceController.java` | 资源管理控制器 | `/api/resources` |
| `QaController.java` | 问答控制器 | `/api/qa` |
| `QuizController.java` | 答题控制器 | `/api/quiz` |
| `PlantGameController.java` | 植物游戏控制器 | `/api/plant-game` |
| `CommentController.java` | 评论控制器 | `/api/comments` |
| `FavoriteController.java` | 收藏控制器 | `/api/favorites` |
| `FeedbackController.java` | 反馈控制器 | `/api/feedback` |
| `FileUploadController.java` | 文件上传控制器 | `/api/upload` |
| `CaptchaController.java` | 验证码控制器 | `/api/captcha` |
| `ChatController.java` | AI聊天控制器 | `/api/chat` |
| `StatisticsController.java` | 统计控制器 | `/api/stats` |
| `LeaderboardController.java` | 排行榜控制器 | `/api/leaderboard` |
| `OperationLogController.java` | 操作日志控制器 | `/api/logs` |

## 📦 详细说明

### 1. UserController.java - 用户控制器

**功能:** 处理用户注册、登录、信息管理等

**API接口:**
| 方法 | 路径 | 功能说明 |
|------|------|----------|
| POST | `/api/user/register` | 用户注册 |
| POST | `/api/user/login` | 用户登录 |
| POST | `/api/user/logout` | 用户登出 |
| GET | `/api/user/info` | 获取用户信息 |
| PUT | `/api/user/update` | 更新用户信息 |
| PUT | `/api/user/password` | 修改密码 |

### 2. PlantController.java - 植物控制器

**功能:** 管理药用植物数据

**API接口:**
| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/plants/list` | 获取植物列表（分页） |
| GET | `/api/plants/{id}` | 获取植物详情 |
| GET | `/api/plants/random` | 随机获取植物 |
| POST | `/api/plants/add` | 添加植物（管理员） |
| PUT | `/api/plants/update` | 更新植物（管理员） |
| DELETE | `/api/plants/{id}` | 删除植物（管理员） |

### 3. KnowledgeController.java - 知识库控制器

**功能:** 管理侗族医药知识内容

**API接口:**
| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/knowledge/list` | 获取知识列表 |
| GET | `/api/knowledge/{id}` | 获取知识详情 |
| POST | `/api/knowledge/add` | 添加知识 |
| PUT | `/api/knowledge/update` | 更新知识 |
| DELETE | `/api/knowledge/{id}` | 删除知识 |

### 4. QuizController.java - 答题控制器

**功能:** 处理答题相关功能

**API接口:**
| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/quiz/questions` | 获取答题题目 |
| POST | `/api/quiz/submit` | 提交答案 |
| GET | `/api/quiz/records` | 获取答题记录 |

### 5. FileUploadController.java - 文件上传控制器

**功能:** 处理文件上传

**API接口:**
| 方法 | 路径 | 功能说明 |
|------|------|----------|
| POST | `/api/upload/image` | 上传图片 |
| POST | `/api/upload/video` | 上传视频 |
| POST | `/api/upload/document` | 上传文档 |

## 🎯 控制器规范

### 基本结构
```java
@RestController
@RequestMapping("/api/example")
@RequiredArgsConstructor
public class ExampleController {
    
    private final ExampleService exampleService;
    
    @GetMapping("/list")
    public R<List<Example>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(exampleService.getList(page, size));
    }
    
    @GetMapping("/{id}")
    public R<Example> getById(@PathVariable Integer id) {
        return R.ok(exampleService.getById(id));
    }
    
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public R<String> add(@RequestBody @Valid ExampleDTO dto) {
        exampleService.add(dto);
        return R.ok("添加成功");
    }
}
```

### 统一响应格式
```java
// 成功响应
return R.ok(data);

// 失败响应
return R.fail("错误信息");

// 响应结构
{
    "code": 200,
    "message": "success",
    "data": { ... }
}
```

### 参数校验
```java
@PostMapping("/add")
public R<String> add(@RequestBody @Valid ExampleDTO dto) {
    // @Valid 会自动校验DTO中的注解
}

// DTO中的校验注解
public class ExampleDTO {
    @NotBlank(message = "名称不能为空")
    private String name;
    
    @Min(value = 1, message = "数量必须大于0")
    private Integer count;
}
```

### 最佳实践
1. **单一职责**: 每个控制器只负责一个业务模块
2. **参数校验**: 使用@Valid进行参数校验
3. **权限控制**: 使用@PreAuthorize进行权限验证
4. **统一响应**: 使用R类统一返回格式
5. **异常处理**: 让全局异常处理器处理异常

## 📚 扩展阅读

- [Spring MVC 控制器](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html)
- [RESTful API 设计规范](https://restfulapi.net/)
- [Spring Security 权限控制](https://docs.spring.io/spring-security/reference/)
