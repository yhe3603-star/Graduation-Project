# 控制器目录说明

## 文件夹结构

本目录包含项目的所有控制器类，负责处理HTTP请求和响应。

```
controller/
├── UserController.java         # 用户控制器
├── AdminController.java        # 管理后台控制器
├── PlantController.java        # 药材控制器
├── KnowledgeController.java    # 知识库控制器
├── InheritorController.java    # 传承人控制器
├── ResourceController.java     # 资源控制器
├── QaController.java           # 问答控制器
├── QuizController.java         # 答题控制器
├── PlantGameController.java    # 植物游戏控制器
├── CommentController.java      # 评论控制器
├── FavoriteController.java     # 收藏控制器
├── FeedbackController.java     # 意见反馈控制器
├── FileUploadController.java   # 文件上传控制器
├── ChatController.java         # AI对话控制器
├── LeaderboardController.java  # 排行榜控制器
├── OperationLogController.java # 操作日志控制器
└── README.md                   # 说明文档
```

## 详细说明

### 1. UserController.java - 用户控制器

**请求路径**：`/api/user`

**接口列表**：
| 方法 | 路径 | 权限 | 限流 | 说明 |
|------|------|------|------|------|
| POST | /login | 公开 | 5次/秒 | 用户登录 |
| POST | /register | 公开 | 3次/秒 | 用户注册 |
| GET | /me | 需认证 | - | 获取当前用户信息 |
| POST | /change-password | 需认证 | - | 修改密码 |
| POST | /logout | 需认证 | - | 用户登出 |
| GET | /validate | 需认证 | - | 验证Token有效性 |
| POST | /refresh-token | 需认证 | - | 刷新Token |

**登录响应格式**：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "id": 1,
    "username": "admin",
    "role": "admin"
  }
}
```

### 2. AdminController.java - 管理后台控制器

**请求路径**：`/api/admin`

**权限**：需要 `ADMIN` 角色

**接口列表**：

**用户管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /users | 分页获取用户列表 |
| DELETE | /users/{id} | 删除用户 |
| PUT | /users/{id}/role | 更新用户角色 |
| PUT | /users/{id}/ban | 封禁用户 |
| PUT | /users/{id}/unban | 解封用户 |

**传承人管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /inheritors | 分页获取传承人列表 |
| POST | /inheritors | 新增传承人 |
| PUT | /inheritors/{id} | 更新传承人 |
| DELETE | /inheritors/{id} | 删除传承人 |

**知识管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /knowledge | 分页获取知识列表 |
| POST | /knowledge | 新增知识 |
| PUT | /knowledge/{id} | 更新知识 |
| DELETE | /knowledge/{id} | 删除知识 |

**药材管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /plants | 分页获取药材列表 |
| POST | /plants | 新增药材 |
| PUT | /plants/{id} | 更新药材 |
| DELETE | /plants/{id} | 删除药材 |

**问答管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /qa | 分页获取问答列表 |
| POST | /qa | 新增问答 |
| PUT | /qa/{id} | 更新问答 |
| DELETE | /qa/{id} | 删除问答 |

**资源管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /resources | 分页获取资源列表 |
| POST | /resources | 新增资源 |
| PUT | /resources/{id} | 更新资源 |
| DELETE | /resources/{id} | 删除资源 |

**反馈管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /feedback | 分页获取反馈列表 |
| PUT | /feedback/{id}/reply | 回复反馈 |
| DELETE | /feedback/{id} | 删除反馈 |

**评论管理**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /comments | 分页获取评论列表 |
| PUT | /comments/{id}/approve | 审核通过 |
| PUT | /comments/{id}/reject | 审核拒绝 |
| DELETE | /comments/{id} | 删除评论 |

**统计接口**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /stats | 获取统计数据 |
| GET | /stats/plants-distribution | 药材分布统计 |
| GET | /stats/knowledge-popularity | 知识热度统计 |

### 3. PlantController.java - 药材控制器

**请求路径**：`/api/plants`

**接口列表**：
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /list | 公开 | 分页获取药材列表（支持分类、用法筛选） |
| GET | /search | 公开 | 关键词搜索药材 |
| GET | /{id} | 公开 | 获取药材详情 |
| GET | /{id}/similar | 公开 | 获取相似药材 |
| GET | /random | 公开 | 按难度随机获取药材 |
| POST | /{id}/view | 公开 | 增加浏览次数 |

**请求参数**：
- `list`：page, size, category, usageWay, keyword
- `search`：keyword（必填）, page, size
- `random`：difficulty（必填）, limit（1-100）

### 4. KnowledgeController.java - 知识库控制器

**请求路径**：`/api/knowledge`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 分页获取知识列表 |
| GET | /{id} | 获取知识详情 |
| POST | /{id}/view | 增加浏览次数 |

### 5. InheritorController.java - 传承人控制器

**请求路径**：`/api/inheritors`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 分页获取传承人列表 |
| GET | /{id} | 获取传承人详情 |
| POST | /{id}/view | 增加浏览次数 |

### 6. ResourceController.java - 资源控制器

**请求路径**：`/api/resources`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 分页获取资源列表 |
| GET | /{id} | 获取资源详情 |
| GET | /download/{id} | 下载资源 |
| POST | /{id}/view | 增加浏览次数 |

### 7. QaController.java - 问答控制器

**请求路径**：`/api/qa`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 分页获取问答列表 |
| GET | /{id} | 获取问答详情 |
| POST | /{id}/view | 增加浏览次数 |

### 8. QuizController.java - 答题控制器

**请求路径**：`/api/quiz`

**接口列表**：
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /questions | 公开 | 获取随机题目 |
| POST | /submit | 需认证 | 提交答案 |
| GET | /history | 需认证 | 获取答题历史 |
| GET | /list | 需ADMIN | 分页获取题目列表 |
| POST | /add | 需ADMIN | 添加题目 |
| PUT | /update | 需ADMIN | 更新题目 |
| DELETE | /{id} | 需ADMIN | 删除题目 |

### 9. PlantGameController.java - 植物游戏控制器

**请求路径**：`/api/plant-game`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /questions | 获取游戏题目 |
| POST | /submit | 提交游戏答案 |
| GET | /history | 获取游戏历史 |

### 10. CommentController.java - 评论控制器

**请求路径**：`/api/comments`

**接口列表**：
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /list | 公开 | 获取评论列表 |
| POST | / | 需认证 | 添加评论 |
| DELETE | /{id} | 需认证 | 删除评论 |
| POST | /{id}/like | 需认证 | 点赞评论 |

### 11. FavoriteController.java - 收藏控制器

**请求路径**：`/api/favorites`

**权限**：需要认证

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /my | 获取用户收藏列表 |
| POST | /{type}/{id} | 添加收藏 |
| DELETE | /{type}/{id} | 取消收藏 |

**收藏类型**：plant, knowledge, inheritor, resource

### 12. FeedbackController.java - 意见反馈控制器

**请求路径**：`/api/feedback`

**接口列表**：
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /list | 需ADMIN | 获取反馈列表 |
| POST | / | 需认证 | 提交反馈 |
| GET | /{id} | 需认证 | 获取反馈详情 |

### 13. FileUploadController.java - 文件上传控制器

**请求路径**：`/api/upload`

**权限**：需要ADMIN角色

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /image | 上传图片 |
| POST | /video | 上传视频 |
| POST | /document | 上传文档 |
| POST | /file | 上传通用文件 |
| DELETE | /file | 删除文件 |

**支持的文件类型**：
- 图片：jpg, jpeg, png, gif, bmp, webp, svg
- 视频：mp4, avi, mov, wmv, flv, mkv
- 文档：docx, doc, pdf, pptx, ppt, xlsx, xls, txt

### 14. ChatController.java - AI对话控制器

**请求路径**：`/api/chat`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | / | 发送对话消息 |
| GET | /history | 获取对话历史 |

### 15. LeaderboardController.java - 排行榜控制器

**请求路径**：`/api/leaderboard`

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /quiz | 获取答题排行榜 |
| GET | /game | 获取游戏排行榜 |

### 16. OperationLogController.java - 操作日志控制器

**请求路径**：`/api/admin/logs`

**权限**：需要ADMIN角色

**接口列表**：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 分页获取日志列表 |
| DELETE | /{id} | 删除单条日志 |
| DELETE | /batch | 批量删除日志 |
| DELETE | /clear | 清空所有日志 |

## 控制器统计

| 控制器 | 接口数量 | 主要功能 |
|--------|---------|---------|
| UserController | 7 | 用户认证与管理 |
| AdminController | 30+ | 管理后台CRUD操作 |
| PlantController | 6 | 药材信息管理 |
| KnowledgeController | 3 | 知识库管理 |
| InheritorController | 3 | 传承人管理 |
| ResourceController | 4 | 资源管理 |
| QaController | 3 | 问答管理 |
| QuizController | 7 | 答题功能 |
| PlantGameController | 3 | 游戏功能 |
| CommentController | 4 | 评论管理 |
| FavoriteController | 3 | 收藏功能 |
| FeedbackController | 3 | 反馈管理 |
| FileUploadController | 5 | 文件上传 |
| ChatController | 2 | AI对话 |
| LeaderboardController | 2 | 排行榜 |
| OperationLogController | 4 | 日志管理 |
| **总计** | **~90** | - |

## 开发规范

1. **注解使用**：
   - `@RestController`：标记为REST控制器
   - `@RequestMapping`：定义基础路径
   - `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping`：定义HTTP方法
   - `@PreAuthorize`：权限控制
   - `@RateLimit`：接口限流
   - `@Validated`：启用参数验证

2. **命名规范**：
   - 类名使用大驼峰命名法，以Controller结尾
   - 方法名使用小驼峰命名法，动词+名词形式

3. **参数验证**：
   - 使用`@Valid`验证请求体
   - 使用`@NotNull`、`@NotBlank`等验证参数
   - 使用`@RequestParam`设置默认值

4. **响应格式**：
   - 所有响应使用`R<T>`封装
   - 成功：`R.ok(data)`
   - 失败：`R.error(message)`

5. **分页处理**：
   - 使用`PageUtils.getPage(page, size)`创建分页对象
   - 使用`PageUtils.toMap(pageResult)`转换为统一格式

## 分页响应格式

```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "size": 20,
    "current": 1,
    "pages": 5
  }
}
```

---

## 控制器开发模板

### 基础控制器模板

```java
@RestController
@RequestMapping("/api/example")
@Tag(name = "示例管理", description = "示例相关接口")
@RequiredArgsConstructor
@Slf4j
public class ExampleController {

    private final ExampleService exampleService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Example> pageResult = exampleService.page(
            PageUtils.getPage(page, size),
            new LambdaQueryWrapper<Example>()
                .like(StringUtils.hasText(keyword), Example::getName, keyword)
        );
        return R.ok(PageUtils.toMap(pageResult));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情")
    public R<Example> getById(@PathVariable Long id) {
        Example entity = exampleService.getById(id);
        if (entity == null) {
            return R.notFound("数据不存在");
        }
        return R.ok(entity);
    }

    @PostMapping
    @Operation(summary = "新增")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> save(@RequestBody @Valid ExampleDTO dto) {
        exampleService.save(dto);
        return R.ok("新增成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid ExampleDTO dto) {
        exampleService.update(id, dto);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> delete(@PathVariable Long id) {
        exampleService.removeById(id);
        return R.ok("删除成功");
    }
}
```

---

## API版本管理

### 当前版本策略

项目当前使用URL路径版本控制，所有API以`/api`为前缀。

### 版本演进建议

#### 方案一：URL路径版本（推荐）

```java
// V1版本
@RestController
@RequestMapping("/api/v1/plants")
public class PlantControllerV1 {
    // 原有接口
}

// V2版本（新增功能）
@RestController
@RequestMapping("/api/v2/plants")
public class PlantControllerV2 {
    // 增强接口
}
```

#### 方案二：请求头版本

```java
@GetMapping(value = "/plants", headers = "X-API-Version=1")
public R<List<Plant>> getPlantsV1() { }

@GetMapping(value = "/plants", headers = "X-API-Version=2")
public R<List<PlantVO>> getPlantsV2() { }
```

#### 方案三：参数版本

```java
@GetMapping(value = "/plants", params = "version=1")
public R<List<Plant>> getPlantsV1() { }
```

### 版本兼容性规则

| 变更类型 | 是否需要新版本 | 说明 |
|---------|--------------|------|
| 新增接口 | 否 | 向后兼容 |
| 新增可选参数 | 否 | 向后兼容 |
| 新增响应字段 | 否 | 向后兼容 |
| 删除接口 | 是 | 破坏性变更 |
| 修改参数类型 | 是 | 破坏性变更 |
| 删除响应字段 | 是 | 破坏性变更 |
| 修改业务逻辑 | 视情况 | 可能影响客户端 |

---

## 已知限制

| 控制器 | 限制 | 影响 |
|--------|------|------|
| FileUploadController | 单文件最大100MB | 大文件需分片上传 |
| ChatController | 依赖DeepSeek服务 | 服务不可用时无法使用 |
| QuizController | 题目随机算法简单 | 可能出现重复题目 |
| LeaderboardController | 不支持实时更新 | 排行榜有延迟 |
| CommentController | 不支持楼中楼 | 无法嵌套回复 |
| FavoriteController | 不支持批量操作 | 批量收藏需多次请求 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **API文档增强**
   - 添加更多示例
   - 添加错误码说明
   - 添加请求/响应示例

2. **参数验证增强**
   - 添加自定义验证注解
   - 优化错误提示信息

3. **接口优化**
   - 添加批量操作接口
   - 优化分页性能

### 中期改进 (1-2月)

1. **API版本管理**
   - 实现版本路由
   - 添加版本废弃机制
   - 版本迁移指南

2. **性能优化**
   - 接口响应缓存
   - 批量查询优化
   - 异步处理

3. **功能增强**
   - GraphQL支持
   - WebSocket实时推送
   - 接口幂等性

### 长期规划 (3-6月)

1. **微服务拆分**
   - 服务边界划分
   - API网关
   - 服务间通信

2. **国际化支持**
   - 多语言响应
   - 时区处理

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2+ | 框架基础 |
| Spring Security | 6.2+ | 权限控制 |
| SpringDoc OpenAPI | 2.3+ | API文档 |
| MyBatis-Plus | 3.5+ | 数据访问 |
| Hibernate Validator | 8.x | 参数验证 |

---

## 常见问题

### 1. 如何添加新的接口？

```java
// 1. 在Controller中添加方法
@GetMapping("/new-endpoint")
@Operation(summary = "新接口说明")
public R<NewResponse> newEndpoint(@RequestParam String param) {
    return R.ok(service.newMethod(param));
}

// 2. 在Service中添加方法
public NewResponse newMethod(String param) {
    // 业务逻辑
}

// 3. 添加权限控制（如需要）
@PreAuthorize("hasRole('ADMIN')")
```

### 2. 如何处理文件下载？

```java
@GetMapping("/download/{id}")
public void download(@PathVariable Long id, HttpServletResponse response) {
    Resource resource = resourceService.getResource(id);
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", 
        "attachment; filename=" + resource.getFilename());
    try (InputStream is = resource.getInputStream();
         OutputStream os = response.getOutputStream()) {
        IOUtils.copy(is, os);
    }
}
```

### 3. 如何实现接口幂等性？

```java
@PostMapping
@Idempotent(key = "#dto.id", expireTime = 5, timeUnit = TimeUnit.SECONDS)
public R<Void> create(@RequestBody ExampleDTO dto) {
    // 业务逻辑
}
```

### 4. 如何处理异步请求？

```java
@PostMapping("/async")
@Async
public CompletableFuture<R<Void>> asyncOperation(@RequestBody ExampleDTO dto) {
    return CompletableFuture.supplyAsync(() -> {
        service.process(dto);
        return R.ok();
    });
}
```

### 5. 如何添加接口缓存？

```java
@GetMapping("/{id}")
@Cacheable(value = "plants", key = "#id")
public R<Plant> getById(@PathVariable Long id) {
    return R.ok(plantService.getById(id));
}

@PutMapping("/{id}")
@CacheEvict(value = "plants", key = "#id")
public R<Void> update(@PathVariable Long id, @RequestBody PlantDTO dto) {
    plantService.update(id, dto);
    return R.ok();
}
```

### 6. 如何统一处理异常？

```java
// 已由GlobalExceptionHandler处理
// 只需在业务中抛出BusinessException
if (entity == null) {
    throw new BusinessException(ErrorCode.NOT_FOUND);
}
```

---

**最后更新时间**：2026年3月28日
