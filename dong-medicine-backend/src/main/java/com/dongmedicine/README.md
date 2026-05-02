# Java 源码根包（com.dongmedicine）

> 后端Java代码的"大本营"，包含所有业务逻辑。严格遵循 **Controller → Service → Mapper** 三层架构。

---

## 一、包结构总览

```
com.dongmedicine/
├── DongMedicineBackendApplication.java  # Spring Boot启动类（加载.env + @EnableCaching）
├── controller/          # 控制器层（24个Controller，接收HTTP请求）
├── service/             # 服务层（18个Service接口 + 实现类in impl/）
├── mapper/              # 数据访问层（15个Mapper接口，MyBatis-Plus）
├── entity/              # 实体类（15个Entity + BaseEntity基类）
├── dto/                 # 数据传输对象（23个DTO，请求/响应用）
├── config/              # 配置类（20+个，含health/和logging/子包）
├── mq/                  # RabbitMQ（producer/生产者 + consumer/消费者）
├── websocket/           # WebSocket（ChatWebSocketHandler）
└── common/              # 通用模块
    ├── constant/        #   常量（ApiPaths, RabbitMQConstants, RoleConstants）
    ├── exception/       #   异常体系（BusinessException, ErrorCode, GlobalExceptionHandler）
    └── util/            #   工具类（XssUtils, PageUtils, PasswordValidator等）
```

---

## 二、三层架构（完整版）

```
┌──────────────────────────────────────────────────────────────────┐
│ Controller 层 - 接收HTTP请求                                      │
│ ────────────────────────────────                                  │
│ 注解: @RestController + @RequestMapping + @Validated             │
│ 注入: @RequiredArgsConstructor + private final Service            │
│ 返回: R<T> 统一响应格式                                           │
│                                                                  │
│ 示例: PlantController                                             │
│ @GetMapping("/list")              → public R<Map> list(...)       │
│ @GetMapping("/{id}")              → public R<Plant> detail(...)   │
│ @PostMapping("/{id}/view")        → public R<String> view(...)    │
└───────────────────────────┬──────────────────────────────────────┘
                            │ 调用 service.xxx()
                            ▼
┌──────────────────────────────────────────────────────────────────┐
│ Service 层 - 业务逻辑                                             │
│ ────────────────────────────────                                  │
│ 接口: extends IService<Entity>                                    │
│ 实现: @Service + extends ServiceImpl<Mapper, Entity>              │
│ 缓存: @Cacheable + @CacheEvict                                    │
│ 事务: @Transactional(rollbackFor = Exception.class)              │
│                                                                  │
│ 示例: PlantServiceImpl                                            │
│ advancedSearchPaged() → LambdaQueryWrapper + page()              │
│ incrementViewCount()  → mapper.incrementViewCount(id)            │
│ deleteWithFiles()     → removeById + fileCleanupHelper           │
└───────────────────────────┬──────────────────────────────────────┘
                            │ 调用 mapper / baseMapper
                            ▼
┌──────────────────────────────────────────────────────────────────┐
│ Mapper 层 - 数据访问                                              │
│ ────────────────────────────────                                  │
│ 接口: @Mapper + extends BaseMapper<Entity>                       │
│ 自动方法: insert, deleteById, updateById, selectById,            │
│           selectList, selectPage, selectCount ...                 │
│ 自定义: @Select / @Update 注解                                    │
│                                                                  │
│ 示例: PlantMapper                                                 │
│ incrementViewCount() → @Update("UPDATE plants SET view_count...")│
│ searchByFullText()   → @Select("SELECT * WHERE MATCH...")        │
│ countDistinctCategory() → @Select("SELECT COUNT(DISTINCT...)")   │
└───────────────────────────┬──────────────────────────────────────┘
                            │
                            ▼
                      ┌───────────┐
                      │  MySQL 8  │
                      └───────────┘
```

---

## 三、启动类（DongMedicineBackendApplication）

```java
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableCaching
public class DongMedicineBackendApplication {
    public static void main(String[] args) {
        // 1. 加载.env文件中的环境变量
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));

        // 2. 启动Spring Boot
        SpringApplication.run(DongMedicineBackendApplication.class, args);
    }
}
```

**关键设计点**：
- `exclude = UserDetailsServiceAutoConfiguration.class`：排除Spring Security的默认用户认证（使用Sa-Token替代）
- `@EnableCaching`：启用Spring Cache（Redis + Caffeine降级）
- `Dotenv`：支持.env文件，开发环境无需设置系统环境变量

---

## 四、数据流完整示例：用户登录

```
POST /api/user/login                前端发送JSON: {"username":"admin","password":"Admin123","captchaKey":"xxx","captchaCode":"1234"}
  │
  ├── [Filter] RequestIdFilter → 生成16位requestId，放入MDC
  ├── [Filter] XssFilter → 检查JSON请求体中的XSS攻击
  │
  ├── [Interceptor] SaTokenConfig → 路径/api/user/login在excludePathPatterns中 → 放行
  │
  ├── [AOP] LoggingAspect → 记录请求日志（方法、参数、IP）
  ├── [AOP] RateLimitAspect → @RateLimit(value=5) → Redis计数器检查
  │
  ├── [Controller] UserController.login(@Valid @RequestBody LoginDTO dto)
  │   ├── captchaService.validateCaptchaOrThrow(dto.captchaKey, dto.captchaCode)
  │   │   └── 从Redis取验证码 → 比对 → 删除Redis中的验证码（一次性使用）
  │   └── service.login(dto.username, dto.password)
  │
  ├── [Service] UserServiceImpl.login(username, password)
  │   ├── 参数校验 → 查用户 → 查封禁状态
  │   ├── BCrypt密码验证: passwordEncoder.matches(password, user.passwordHash)
  │   ├── StpUtil.login(user.getId())  → 生成JWT Token
  │   ├── StpUtil.getSession().set("username", user.getUsername())
  │   ├── StpUtil.getSession().set("role", user.getRole())
  │   └── return Map.of("token", token, "id", userId, "username", username, "role", role)
  │
  ├── [Controller] return R.ok(resultMap)
  │
  ├── [AOP] OperationLogAspect → 通过 RabbitMQ 异步记录操作日志
  │
  └── [响应] {"code":200,"msg":"success","data":{"token":"eyJ...","id":1,"username":"admin","role":"admin"},"requestId":"a1b2c3d4"}
```

---

## 五、所有模块清单

### Controller（24个）

| Controller | 路径前缀 | 权限 |
|-----------|---------|------|
| AdminController | /api/admin | @SaCheckRole("admin") |
| BrowseHistoryController | /api/browse-history | @SaCheckLogin（部分） |
| CaptchaController | /api/captcha | 公开 |
| ChatController | /api/chat | 公开 |
| ChatHistoryController | /api/chat-history | @SaCheckLogin |
| CommentController | /api/comments | 公开 + @SaCheckLogin |
| ExportController | /api/admin/export | @SaCheckRole("admin") |
| FavoriteController | /api/favorites | @SaCheckLogin |
| FeedbackController | /api/feedback | 公开 + @SaCheckLogin |
| FileUploadController | /api/upload | @SaCheckRole("admin") |
| InheritorController | /api/inheritors | 公开 |
| KnowledgeController | /api/knowledge | 公开 + @SaCheckLogin |
| LeaderboardController | /api/leaderboard | 公开 |
| MetadataController | /api/metadata | 公开 |
| OperationLogController | /api/admin/logs | @SaCheckRole("admin") |
| PlantController | /api/plants | 公开 |
| PlantGameController | /api/plant-game | 公开 |
| QaController | /api/qa | 公开 |
| QuizController | /api/quiz | 公开 + @SaCheckRole("admin") |
| ResourceController | /api/resources | 公开 |
| SearchController | /api/search | 公开 |
| StatisticsController | /api/stats | 公开 |
| StatsController | /api/stats | 公开 |
| UserController | /api/user | 公开 + @SaCheckLogin |

### Service（18个接口 + 18个实现类）

| Service接口 | 实现类 | 继承IService |
|------------|--------|-------------|
| AiChatService | AiChatServiceImpl | 否 |
| BrowseHistoryService | BrowseHistoryServiceImpl | IService\<BrowseHistory\> |
| CaptchaService | --（直接实现，无接口） | 否 |
| ChatHistoryService | ChatHistoryServiceImpl | 否 |
| CommentService | CommentServiceImpl | IService\<Comment\> |
| FavoriteService | FavoriteServiceImpl | IService\<Favorite\> |
| FeedbackService | FeedbackServiceImpl | IService\<Feedback\> |
| FileUploadService | FileUploadServiceImpl | 否 |
| InheritorService | InheritorServiceImpl | IService\<Inheritor\> |
| KnowledgeService | KnowledgeServiceImpl | IService\<Knowledge\> |
| OperationLogService | OperationLogServiceImpl | IService\<OperationLog\> |
| PlantGameService | PlantGameServiceImpl | 否 |
| PlantService | PlantServiceImpl | IService\<Plant\> |
| PopularityAsyncService | PopularityAsyncServiceImpl | 否 |
| QaService | QaServiceImpl | IService\<Qa\> |
| QuizService | QuizServiceImpl | IService\<QuizQuestion\> |
| RabbitMQOperationLogService | RabbitMQOperationLogServiceImpl | 否 |
| ResourceService | ResourceServiceImpl | IService\<Resource\> |
| UserService | UserServiceImpl | IService\<User\> |

### Entity（15个实体类 + BaseEntity基类）

| Entity | 表名 | 继承 | 核心字段 |
|--------|------|------|---------|
| BaseEntity | -- | -- | id, createdAt, updatedAt |
| Plant | plants | BaseEntity | nameCn, nameDong, efficacy, story, viewCount... |
| Knowledge | knowledge | BaseEntity | title, type, content, formula, therapyCategory... |
| Inheritor | inheritors | BaseEntity | name, level, bio, specialties, honors... |
| Qa | qa | BaseEntity | category, question, answer |
| Resource | resources | BaseEntity | title, category, files, description |
| User | users | BaseEntity | username, passwordHash, role, status |
| Comment | comments | BaseEntity | userId, username, targetType, targetId, content, likes |
| Favorite | favorites | BaseEntity | userId, targetType, targetId |
| Feedback | feedback | 独立（不继承） | userId, type, title, content, status, reply |
| QuizQuestion | quiz_questions | BaseEntity | question, options, answer, category, explanation |
| QuizRecord | quiz_record | 独立（不继承） | userId, score, totalQuestions, correctAnswers |
| PlantGameRecord | plant_game_record | 独立（不继承） | userId, difficulty, score, correctCount, totalCount |
| OperationLog | operation_log | BaseEntity | userId, username, module, type, operation, method |
| BrowseHistory | browse_history | BaseEntity | userId, targetType, targetId |
| ChatHistory | chat_history | BaseEntity | userId, sessionId, role, content |

---

## 六、如何添加新功能模块

以添加"侗族药方"（Recipe）为例，按以下6步操作：

### 第1步：创建Entity
```java
// entity/Recipe.java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recipes")
public class Recipe extends BaseEntity {
    private String nameCn, nameDong;
    private String ingredients;  // JSON字符串
    private String usage, indications, precautions;
}
```

### 第2步：创建Mapper
```java
// mapper/RecipeMapper.java
@Mapper
public interface RecipeMapper extends BaseMapper<Recipe> {
    // 继承BaseMapper就有所有CRUD方法
}
```

### 第3步：创建Service接口
```java
// service/RecipeService.java
public interface RecipeService extends IService<Recipe> {
    Page<Recipe> searchPaged(String keyword, Integer page, Integer size);
    void clearCache();
}
```

### 第4步：创建Service实现类
```java
// service/impl/RecipeServiceImpl.java
@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {
    @Override
    @Cacheable(value = "recipes", key = "'search:' + (#keyword ?: 'all')")
    public Page<Recipe> searchPaged(String keyword, Integer page, Integer size) { ... }

    @Override
    @CacheEvict(value = "recipes", allEntries = true)
    public void clearCache() { }
}
```

### 第5步：创建Controller
```java
// controller/RecipeController.java
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService service;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String keyword) {
        return R.ok(PageUtils.toMap(service.searchPaged(keyword, page, size)));
    }
}
```

### 第6步：注册缓存 + 配置路由权限
```java
// CacheConfig.java中添加
cacheConfigurations.put("recipes", defaultConfig.entryTtl(Duration.ofHours(4)));

// SaTokenConfig.java的excludePathPatterns中添加
"/api/recipes/list", "/api/recipes/search", "/api/recipes/{id}"
```

---

## 七、模块文档导航

| 模块 | 文档 | 内容 |
|------|------|------|
| 控制器 | [controller/README.md](controller/README.md) | 24个Controller完整API端点文档 |
| 服务层 | [service/README.md](service/README.md) | 18个Service + 缓存策略 + 事务管理 |
| 数据层 | [mapper/README.md](mapper/README.md) | 15个Mapper + MyBatis-Plus BaseMapper |
| 实体类 | [entity/README.md](entity/README.md) | 15个Entity + BaseEntity + 注解说明 |
| DTO | [dto/README.md](dto/README.md) | 23个DTO + 参数校验 |
| 配置 | [config/README.md](config/README.md) | 20+配置类完整说明 |
| 消息队列 | [mq/README.md](mq/README.md) | 5个Producer + 5个Consumer |
| WebSocket | [websocket/README.md](websocket/README.md) | AI聊天流式通信 |
| 通用模块 | [common/README.md](common/README.md) | R类 + 异常体系 + 工具类 |
