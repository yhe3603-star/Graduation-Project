# 控制器层 (controller)

本目录存放所有控制器类，负责接收HTTP请求并返回响应。

## 目录

- [什么是控制器？](#什么是控制器)
- [目录结构](#目录结构)
- [控制器列表](#控制器列表)
- [控制器开发规范](#控制器开发规范)
- [常用控制器详解](#常用控制器详解)

---

## 什么是控制器？

### 控制器的概念

**控制器（Controller）** 是后端应用中处理 HTTP 请求的类。它就像餐厅的"服务员"——接收顾客（用户）的点单（请求），然后告诉厨房（Service层）准备菜品，最后把菜品（响应）端给顾客。

### 控制器的作用

```
┌─────────────────────────────────────────────────────────────────┐
│                        请求处理流程                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  用户发起请求                                                    │
│       │                                                         │
│       ▼                                                         │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Controller 层                         │   │
│  │  ┌─────────────────────────────────────────────────┐    │   │
│  │  │  1. 接收请求（参数验证）                          │    │   │
│  │  │  2. 调用 Service 处理业务逻辑                     │    │   │
│  │  │  3. 封装响应结果                                 │    │   │
│  │  │  4. 返回给用户                                   │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│       │                                                         │
│       ▼                                                         │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                     Service 层                          │   │
│  │              处理具体的业务逻辑                          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 控制器的基本结构

```java
// @RestController - 标记这是一个控制器类
// 相当于 @Controller + @ResponseBody，方法返回值自动转为JSON
@RestController
@RequestMapping("/api/users")  // 设置这个控制器的基础路径
@RequiredArgsConstructor       // Lombok注解，生成构造函数
public class UserController {
    
    // 注入Service
    private final UserService userService;
    
    // @GetMapping - 处理GET请求
    // 访问路径：GET /api/users/list
    @GetMapping("/list")
    public R<List<User>> list() {
        List<User> users = userService.list();
        return R.ok(users);
    }
    
    // @PostMapping - 处理POST请求
    // 访问路径：POST /api/users/add
    @PostMapping("/add")
    public R<String> add(@RequestBody UserDTO dto) {
        userService.add(dto);
        return R.ok("添加成功");
    }
}
```

---

## 目录结构

```
controller/
│
├── AdminController.java              # 管理后台接口
├── AiChatController.java             # AI聊天接口
├── CaptchaController.java            # 验证码接口
├── CommentController.java            # 评论接口
├── FavoriteController.java           # 收藏接口
├── FeedbackController.java           # 反馈接口
├── FileUploadController.java         # 文件上传接口
├── InheritorController.java          # 传承人接口
├── KnowledgeController.java          # 知识库接口
├── LeaderboardController.java        # 排行榜接口
├── PlantController.java              # 药用植物接口
├── PlantGameController.java          # 植物游戏接口
├── QaController.java                 # 问答接口
├── QuizController.java               # 测验接口
├── ResourceController.java           # 学习资源接口
├── UserController.java               # 用户接口
└── HealthController.java             # 健康检查接口
```

---

## 控制器列表

| 控制器 | 基础路径 | 功能描述 |
|--------|----------|----------|
| UserController | `/api/user` | 用户注册、登录、信息管理 |
| CaptchaController | `/api/captcha` | 图形验证码生成 |
| PlantController | `/api/plants` | 药用植物增删改查 |
| KnowledgeController | `/api/knowledge` | 知识库增删改查 |
| InheritorController | `/api/inheritors` | 传承人增删改查 |
| ResourceController | `/api/resources` | 学习资源管理 |
| QaController | `/api/qa` | 问答管理 |
| CommentController | `/api/comments` | 评论管理 |
| FavoriteController | `/api/favorites` | 收藏管理 |
| FeedbackController | `/api/feedback` | 用户反馈 |
| QuizController | `/api/quiz` | 趣味测验 |
| PlantGameController | `/api/plant-game` | 植物识别游戏 |
| LeaderboardController | `/api/leaderboard` | 排行榜 |
| AiChatController | `/api/chat` | AI智能问答 |
| FileUploadController | `/api/upload` | 文件上传 |
| AdminController | `/api/admin` | 管理后台 |
| HealthController | `/actuator` | 健康检查 |

---

## 控制器开发规范

### 1. 类结构规范

```java
@RestController
@RequestMapping("/api/example")
@RequiredArgsConstructor
@Tag(name = "示例管理", description = "示例相关接口")  // Swagger文档
public class ExampleController {
    
    // 1. 依赖注入（使用final + @RequiredArgsConstructor）
    private final ExampleService exampleService;
    
    // 2. 公开接口方法
    @GetMapping("/list")
    @Operation(summary = "获取列表")  // Swagger文档
    public R<List<Example>> list() {
        return R.ok(exampleService.list());
    }
}
```

### 2. 请求映射规范

```java
// GET请求 - 查询数据
@GetMapping("/list")           // 列表查询
@GetMapping("/{id}")           // 详情查询
@GetMapping("/search")         // 搜索查询

// POST请求 - 创建数据
@PostMapping("/add")           // 添加
@PostMapping("/login")         // 登录等操作

// PUT请求 - 更新数据
@PutMapping("/update")         // 更新

// DELETE请求 - 删除数据
@DeleteMapping("/{id}")        // 删除
```

### 3. 参数接收规范

```java
@RestController
@RequestMapping("/api/example")
public class ExampleController {
    
    // @PathVariable - 获取URL路径中的变量
    // 访问：GET /api/example/123
    @GetMapping("/{id}")
    public R<Example> getById(@PathVariable Integer id) {
        return R.ok(exampleService.getById(id));
    }
    
    // @RequestParam - 获取URL查询参数
    // 访问：GET /api/example/search?keyword=张三&page=1
    @GetMapping("/search")
    public R<Page<Example>> search(
        @RequestParam String keyword,                    // 必需参数
        @RequestParam(defaultValue = "1") int page,      // 有默认值
        @RequestParam(required = false) Integer size     // 可选参数
    ) {
        return R.ok(exampleService.search(keyword, page, size));
    }
    
    // @RequestBody - 获取请求体中的JSON数据
    // 前端发送：POST /api/example/add
    // Body: {"name": "张三", "age": 25}
    @PostMapping("/add")
    public R<String> add(@RequestBody @Valid ExampleDTO dto) {
        exampleService.add(dto);
        return R.ok("添加成功");
    }
    
    // @RequestHeader - 获取请求头
    @GetMapping("/info")
    public R<Example> getInfo(@RequestHeader("Authorization") String token) {
        return R.ok(exampleService.getByToken(token));
    }
}
```

### 4. 响应格式规范

```java
// 所有接口统一使用 R<T> 封装响应

// 成功响应
return R.ok(data);              // 返回数据
return R.ok("操作成功");         // 返回消息

// 失败响应
return R.fail("操作失败");       // 返回错误消息
return R.fail(ErrorCode.PARAM_ERROR);  // 返回错误码

// 分页响应
Page<Example> page = exampleService.page(pageNum, pageSize);
return R.ok(page.getRecords(), page.getTotal());
```

### 5. 权限控制规范

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    // 需要登录才能访问
    @GetMapping("/profile")
    public R<User> getProfile() {
        // SecurityUtils 获取当前登录用户
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(userService.getById(userId));
    }
    
    // 需要管理员权限
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/user/{id}")
    public R<String> deleteUser(@PathVariable Integer id) {
        userService.delete(id);
        return R.ok("删除成功");
    }
}
```

---

## 常用控制器详解

### UserController - 用户控制器

处理用户注册、登录、信息管理等请求。

```java
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理")
public class UserController {
    
    private final UserService userService;
    
    // 用户登录
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return R.ok(vo);
    }
    
    // 用户注册
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public R<String> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return R.ok("注册成功");
    }
    
    // 获取当前用户信息
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public R<User> getCurrentUser() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(userService.getById(userId));
    }
    
    // 修改密码
    @PostMapping("/change-password")
    @Operation(summary = "修改密码")
    public R<String> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, dto);
        return R.ok("密码修改成功");
    }
    
    // 退出登录
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public R<String> logout() {
        String token = SecurityUtils.getCurrentToken();
        userService.logout(token);
        return R.ok("退出成功");
    }
}
```

### PlantController - 药用植物控制器

处理药用植物的增删改查请求。

```java
@RestController
@RequestMapping("/api/plants")
@RequiredArgsConstructor
@Tag(name = "药用植物", description = "药用植物管理")
public class PlantController {
    
    private final PlantService plantService;
    
    // 植物列表（支持分类和用法过滤）
    @GetMapping("/list")
    @Operation(summary = "获取植物列表")
    public R<Page<Plant>> list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "12") int size,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String usageWay
    ) {
        return R.ok(plantService.list(page, size, category, usageWay));
    }
    
    // 搜索植物
    @GetMapping("/search")
    @Operation(summary = "搜索植物")
    public R<List<Plant>> search(@RequestParam String keyword) {
        return R.ok(plantService.search(keyword));
    }
    
    // 植物详情
    @GetMapping("/{id}")
    @Operation(summary = "获取植物详情")
    public R<Plant> getById(@PathVariable Integer id) {
        return R.ok(plantService.getById(id));
    }
    
    // 增加浏览次数
    @PostMapping("/{id}/view")
    @Operation(summary = "增加浏览次数")
    public R<String> incrementViewCount(@PathVariable Integer id) {
        plantService.incrementViewCount(id);
        return R.ok("success");
    }
    
    // 随机获取植物（用于游戏）
    @GetMapping("/random")
    @Operation(summary = "随机获取植物")
    public R<List<Plant>> getRandom(
        @RequestParam(defaultValue = "10") int count,
        @RequestParam(required = false) String difficulty
    ) {
        return R.ok(plantService.getRandom(count, difficulty));
    }
}
```

### QuizController - 测验控制器

处理趣味测验相关请求。

```java
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "趣味测验", description = "侗医药知识测验")
public class QuizController {
    
    private final QuizService quizService;
    
    // 获取随机问题
    @GetMapping("/questions")
    @Operation(summary = "获取随机问题")
    public R<List<QuizQuestion>> getQuestions(
        @RequestParam(defaultValue = "10") int count,
        @RequestParam(defaultValue = "10") int scorePerQuestion
    ) {
        return R.ok(quizService.getRandomQuestions(count, scorePerQuestion));
    }
    
    // 提交答案
    @PostMapping("/submit")
    @Operation(summary = "提交答案")
    public R<QuizResultVO> submit(@RequestBody QuizSubmitDTO dto) {
        return R.ok(quizService.submit(dto));
    }
    
    // 获取答题记录
    @GetMapping("/records")
    @Operation(summary = "获取答题记录")
    public R<List<QuizRecord>> getRecords() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return R.ok(quizService.getRecords(userId));
    }
}
```

### AiChatController - AI聊天控制器

处理AI智能问答请求。

```java
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "AI问答", description = "侗医药智能问答")
public class AiChatController {
    
    private final AiChatService aiChatService;
    
    // 发送聊天消息
    @PostMapping
    @Operation(summary = "发送聊天消息")
    @RateLimit(value = 10, key = "chat")  // 限流：每秒最多10次
    public R<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        return R.ok(aiChatService.chat(request));
    }
    
    // 获取聊天统计（管理员）
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取聊天统计")
    public R<ChatStats> getStats() {
        return R.ok(aiChatService.getStats());
    }
}
```

---

## 最佳实践

### 1. 控制器职责

- **只做请求转发**：不包含业务逻辑
- **参数验证**：使用 @Valid 注解验证参数
- **异常处理**：让全局异常处理器处理异常
- **响应封装**：统一使用 R<T> 封装响应

### 2. 安全考虑

```java
// 1. 参数验证
@PostMapping("/add")
public R<String> add(@RequestBody @Valid ExampleDTO dto) {
    // @Valid 会自动验证 DTO 中的注解
}

// 2. XSS防护
@GetMapping("/search")
public R<List<Example>> search(@RequestParam String keyword) {
    // Service层会进行XSS过滤
    return R.ok(exampleService.search(keyword));
}

// 3. SQL注入防护
// 使用MyBatis Plus的LambdaQueryWrapper，自动参数化查询
```

### 3. 性能优化

```java
// 1. 分页查询
@GetMapping("/list")
public R<Page<Example>> list(
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "10") int size
) {
    // 限制每页最大数量
    size = Math.min(size, 100);
    return R.ok(exampleService.page(page, size));
}

// 2. 缓存热门数据
@GetMapping("/hot")
@Cacheable(value = "hotExamples", key = "'list'")
public R<List<Example>> getHot() {
    return R.ok(exampleService.getHot());
}
```

---

**相关文档**

- [Spring MVC 官方文档](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Spring Security 官方文档](https://docs.spring.io/spring-security/reference/)

---

**最后更新时间**：2026年4月3日
