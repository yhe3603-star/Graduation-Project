# Service 层 -- 业务逻辑目录

> Service 是三层架构中的"厨房"层，负责做菜（业务逻辑）。Controller 只管传话，Service 才是真正干活的地方。

---

## 一、什么是 Service 层？

### 生活类比：厨房

```
服务员（Controller）把订单送进厨房
       |
       v
厨师（Service）开始做菜
       |
       +-- 查看配方（业务规则）
       +-- 检查食材是否新鲜（数据校验）
       +-- 从调料架取调料（缓存）
       +-- 让助手去仓库取食材（调用 Mapper）
       +-- 按步骤烹饪（业务逻辑）
       +-- 出菜！
       |
       v
服务员把菜端给顾客
```

**Service 层的核心原则：所有业务逻辑都在这里，Controller 和 Mapper 都不关心业务规则。**

---

## 二、为什么要分离 Controller 和 Service？

### 对比：不分离 vs 分离

**反面教材：所有逻辑塞在 Controller 里**

```java
// 错误示范！Controller 又当服务员又当厨师
@RestController
public class UserController {

    @Autowired private UserMapper userMapper;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        // 1. 校验参数 -- 服务员不该管这个
        if (dto.getUsername() == null || dto.getPassword() == null) {
            return R.badRequest("用户名和密码不能为空");
        }
        // 2. 查数据库 -- 服务员不该直接去仓库
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        // 3. 验证密码 -- 这是业务逻辑，不该在 Controller
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            return R.error("用户名或密码错误");
        }
        // 4. 检查封禁状态 -- 又是业务逻辑
        if ("banned".equals(user.getStatus())) {
            return R.forbidden("该用户已被封禁");
        }
        // 5. 生成Token -- 还是业务逻辑
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
        return R.ok(Map.of("token", token));
    }
}
```

**问题：**
- 如果另一个接口（如管理员接口）也需要验证密码，这段代码就要复制一遍
- 如果密码验证规则变了，要改所有 Controller
- Controller 代码太长，难以阅读和维护
- 无法单独对业务逻辑进行单元测试

**正确做法：Controller 只传话，Service 做业务**

```java
// Controller：简洁明了，只管传话
@PostMapping("/login")
public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
    String token = service.login(dto.getUsername(), dto.getPassword());
    User user = service.getUserByUsername(dto.getUsername());
    return R.ok(Map.of("token", token, "id", user.getId(), "username", user.getUsername(), "role", user.getRole()));
}

// Service：所有业务逻辑集中在这里
@Override
public String login(String username, String password) {
    if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
        throw BusinessException.badRequest("用户名和密码不能为空");
    }
    User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    if (user == null) {
        throw BusinessException.userNotFound();
    }
    if (user.isBanned()) {
        throw BusinessException.forbidden("该用户已被封禁");
    }
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw BusinessException.passwordWrong();
        }
        // Sa-Token 登录
        StpUtil.login(user.getId());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("role", user.getRole());
        return StpUtil.getTokenValue();
    }
```

**好处：**
- `login()` 方法可以被任何 Controller 复用
- 业务逻辑变更只需改 Service
- 可以对 Service 进行独立的单元测试
- Controller 代码简洁，一目了然

---

## 三、如何编写 Service

### 3.1 两步走：接口 + 实现类

**第1步：定义接口（菜单 -- 告诉别人我能做什么）**

```java
// service/PlantService.java
public interface PlantService extends IService<Plant> {

    /**
     * 分页高级搜索植物
     * @param keyword 搜索关键词（可选）
     * @param category 植物分类（可选）
     * @param usageWay 用法方式（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Page<Plant> advancedSearchPaged(String keyword, String category, String usageWay, Integer page, Integer size);

    /**
     * 获取植物详情（包含故事）
     * @param id 植物ID
     * @return 植物详情，不存在则返回null
     */
    Plant getDetailWithStory(Integer id);

    /**
     * 增加浏览次数
     * @param id 植物ID
     */
    void incrementViewCount(Integer id);

    /**
     * 清除植物相关缓存
     */
    void clearCache();
}
```

> 为什么要写接口？接口就像"菜单"，Controller 只需要看菜单点菜，不需要知道厨房怎么做。以后换一种做法（比如从 MySQL 换成 MongoDB），只需要改实现类，Controller 不用动。

**第2步：编写实现类（做菜 -- 具体怎么做）**

```java
// service/impl/PlantServiceImpl.java
@Service   // 关键！告诉 Spring 这是一个业务类
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {

    @Autowired
    private PlantMapper plantMapper;

    @Override
    public Page<Plant> advancedSearchPaged(String keyword, String category, String usageWay, Integer page, Integer size) {
        // 具体业务逻辑...
    }

    @Override
    public Plant getDetailWithStory(Integer id) {
        return getById(id);
    }

    @Override
    public void incrementViewCount(Integer id) {
        plantMapper.incrementViewCount(id);
    }

    @Override
    @CacheEvict(value = "plants", allEntries = true)
    public void clearCache() {
        log.info("Plant cache cleared");
    }
}
```

### 3.2 继承关系说明

```
IService<Plant>（MyBatis-Plus提供的接口）
    |   提供基础方法：save(), removeById(), updateById(), getById(), list(), page()...
    |
    v
PlantService（我们定义的接口）
    |   添加自定义方法：advancedSearchPaged(), getDetailWithStory()...
    |
    v
PlantServiceImpl（我们的实现类）
    |   继承 ServiceImpl<PlantMapper, Plant>
    |   实现 PlantService 接口
    |
    v
自动拥有 IService 的所有方法 + 自定义方法
```

> `ServiceImpl<PlantMapper, Plant>` 是 MyBatis-Plus 提供的基类，它帮你实现了 `IService` 接口的所有基础方法。所以你的 `PlantServiceImpl` 自动拥有了 `save()`, `getById()`, `list()`, `page()` 等方法，不用自己写。

---

## 四、核心注解详解

### 4.1 @Service -- "我是厨师"

```java
@Service   // 告诉 Spring：请管理这个类的生命周期，其他类可以通过注入使用它
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {
    // ...
}
```

**不加 @Service 会怎样？** Spring 不会创建这个类的实例，Controller 注入时会报 `NoSuchBeanDefinitionException`。

### 4.2 @Transactional -- "这组操作要么全成功，要么全失败"

**生活类比：** 你在药铺买三味药，要么三味都买到，要么都不买。不能只买到两味就收钱。

```java
@Override
@Transactional(rollbackFor = Exception.class)  // 任何异常都回滚
public void register(String username, String password) {
    // 操作1：检查用户名是否已存在
    // 操作2：加密密码
    // 操作3：保存用户
    // 如果操作3失败了，操作1和2的结果也会被撤销（回滚）
    User user = new User();
    user.setUsername(username);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setRole(RoleConstants.ROLE_USER);
    save(user);  // 如果这里抛异常，前面的操作全部撤销
}
```

**什么时候需要加 @Transactional？**

| 场景 | 需要？ | 例子 |
|------|--------|------|
| 单个数据库操作 | 不需要 | `getById(id)` |
| 多个数据库操作（必须同时成功） | **需要** | 注册=检查+保存 |
| 先查后改 | **需要** | 改密码=验证+更新 |
| 只读操作 | 不需要 | `list()`, `page()` |

> **常见错误：** 在只读方法上加 `@Transactional`，虽然不会出错，但会不必要地占用数据库连接。

### 4.3 @Cacheable -- "先查缓存，缓存没有再查数据库"

**生活类比：** 厨师旁边有个调料架（缓存），做菜时先看调料架上有没有，有就直接用，没有再去仓库（数据库）拿，拿完放一份在调料架上。

```java
@Override
@Cacheable(value = "plants", key = "'list:' + (#keyword ?: 'all') + ':' + (#category ?: 'all')")
public List<Plant> advancedSearch(String keyword, String category, String usageWay) {
    // 第一次调用：执行这个方法，查数据库，结果存入缓存
    // 后续调用：如果参数相同，直接从缓存返回，不执行这个方法
    LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<>();
    // ... 构建查询条件
    return list(qw);
}
```

**@Cacheable 参数详解：**

| 参数 | 说明 | 例子 |
|------|------|------|
| `value` | 缓存区域名称（类似文件夹） | `"plants"` |
| `key` | 缓存键（类似文件名），相同key返回相同结果 | `"'list:' + #keyword"` |

**key 中的 SpEL 表达式：**

```
#keyword       --> 方法参数 keyword 的值
#category      --> 方法参数 category 的值
(#keyword ?: 'all')  --> 如果 keyword 为空就用 'all'
```

**缓存的工作流程：**

```
调用 advancedSearch("钩藤", "清热药", null)
       |
       v
检查缓存：plants::list:钩藤:清热药:all
       |
       +-- 缓存命中 --> 直接返回缓存数据，不执行方法体
       |
       +-- 缓存未命中 --> 执行方法体，查数据库
                           |
                           v
                       把结果存入缓存
                           |
                           v
                       返回结果
```

### 4.4 @CacheEvict -- "清除缓存"

**生活类比：** 仓库进了新货，调料架上的旧数据就不准了，需要清掉。

```java
@Override
@CacheEvict(value = "plants", allEntries = true)  // 清除 plants 缓存区的所有条目
public void clearCache() {
    log.info("Plant cache cleared");
}

@Override
@CacheEvict(value = "plants", allEntries = true)  // 删除植物时也要清缓存
public void deleteWithFiles(Integer id) {
    Plant plant = getById(id);
    if (plant == null) return;
    fileCleanupHelper.deleteFilesFromJson(plant.getImages());
    fileCleanupHelper.deleteFilesFromJson(plant.getVideos());
    fileCleanupHelper.deleteFilesFromJson(plant.getDocuments());
    removeById(id);  // 数据库删除
    // 方法执行完后，缓存自动清除
}
```

**什么时候需要清缓存？**

| 操作 | 需要清缓存？ | 原因 |
|------|------------|------|
| 新增植物 | 需要 | 列表数据变了 |
| 修改植物 | 需要 | 详情数据变了 |
| 删除植物 | 需要 | 列表和详情都变了 |
| 查询植物 | 不需要 | 数据没变 |

> **常见错误：** 修改了数据但忘记清缓存，导致前端看到的还是旧数据。

### 4.5 @Async -- "后台慢慢做，不用等"

**生活类比：** 顾客点了菜后，服务员先把菜单送进去，不用等菜做好才去接待下一位顾客。浏览量计数就是这种"不用等"的操作。

```java
// AsyncConfig.java 中定义了专用的线程池
@Bean(name = "viewCountExecutor")
public Executor viewCountExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);     // 核心线程数：2个厨师专门做浏览量计数
    executor.setMaxPoolSize(5);      // 最大线程数：忙时最多5个
    executor.setQueueCapacity(100);  // 排队容量：最多100个任务排队
    executor.setThreadNamePrefix("view-count-");  // 线程名前缀
    executor.initialize();
    return executor;
}
```

```java
// Service 中使用 @Async
@Async("viewCountExecutor")   // 在 viewCountExecutor 线程池中异步执行
public void incrementViewCount(Integer id) {
    plantMapper.incrementViewCount(id);
    // 这个方法不会阻塞主请求，用户不用等浏览量更新完就能收到响应
}
```

**同步 vs 异步对比：**

```
同步（没有 @Async）：
  用户请求 --> 增加浏览量（等50ms） --> 返回响应 --> 用户看到页面
  总耗时：50ms + 业务时间

异步（有 @Async）：
  用户请求 --> 提交浏览量任务 --> 立即返回响应 --> 用户看到页面
                                     |
                                     v（后台线程执行）
                               增加浏览量（50ms）
  总耗时：业务时间（不包含50ms）
```

> **注意：** @Async 方法不能是 private 的，不能在同一个类内部调用（否则不生效）。

---

## 五、项目中的 Service 实例

### 5.1 PlantServiceImpl -- 药用植物服务

```java
@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {

    private static final Logger log = LoggerFactory.getLogger(PlantServiceImpl.class);

    // 常量定义：限制范围
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_SEARCH_LIMIT = 50;

    @Autowired
    private PlantMapper plantMapper;
    @Autowired
    private FileCleanupHelper fileCleanupHelper;

    @Value("${app.search.use-fulltext:true}")   // 从配置文件读取
    private boolean useFullTextSearch;

    // ---- 查询类方法 ----

    @Override
    @Cacheable(value = "plants", key = "'list:' + (#keyword ?: 'all') + ':' + (#category ?: 'all') + ':' + (#usageWay ?: 'all')")
    public List<Plant> advancedSearch(String keyword, String category, String usageWay) {
        // 动态构建查询条件
        LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String escapedKeyword = PageUtils.escapeLike(keyword);  // 防止SQL注入
            qw.and(wrapper -> wrapper
                .like(Plant::getNameCn, escapedKeyword)     // 中文名模糊匹配
                .or().like(Plant::getNameDong, escapedKeyword) // 侗语名模糊匹配
                .or().like(Plant::getEfficacy, escapedKeyword) // 功效模糊匹配
                .or().like(Plant::getStory, escapedKeyword));   // 故事模糊匹配
        }
        if (StringUtils.hasText(category)) {
            qw.eq(Plant::getCategory, category);  // 精确匹配分类
        }
        if (StringUtils.hasText(usageWay)) {
            qw.eq(Plant::getUsageWay, usageWay);  // 精确匹配用法
        }
        qw.orderByAsc(Plant::getNameCn);  // 按中文名排序
        return list(qw);
    }

    @Override
    public List<Plant> search(String keyword, int limit) {
        // 参数校验
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "搜索关键词不能为空");
        }
        if (limit < MIN_PAGE_SIZE || limit > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    String.format("限制数量必须在%d-%d之间", MIN_PAGE_SIZE, MAX_PAGE_SIZE));
        }

        String escapedKeyword = PageUtils.escapeLike(keyword);

        // 优先使用全文搜索（更快），失败则降级到 LIKE 搜索
        try {
            if (useFullTextSearch) {
                List<Plant> results = plantMapper.searchByFullText(keyword, limit);
                if (!results.isEmpty()) {
                    return results;
                }
            }
        } catch (Exception e) {
            log.warn("全文搜索失败，回退到LIKE搜索: {}", e.getMessage());
        }

        return plantMapper.searchByLike(escapedKeyword, limit);
    }

    @Override
    public List<Plant> getSimilarPlants(Integer id) {
        Plant current = getById(id);
        if (current == null) return List.of();
        return list(new LambdaQueryWrapper<Plant>()
                .eq(Plant::getCategory, current.getCategory())  // 同分类
                .ne(Plant::getId, id)                            // 排除自己
                .orderByDesc(Plant::getId)
                .last("LIMIT 4"));                               // 最多4个
    }

    // ---- 写入类方法 ----

    @Override
    public void incrementViewCount(Integer id) {
        try {
            plantMapper.incrementViewCount(id);
        } catch (Exception e) {
            log.error("Failed to increment view count for plant id: {}", id, e);
            // 浏览量更新失败不影响主流程，只记录日志
        }
    }

    @Override
    @CacheEvict(value = "plants", allEntries = true)
    public void clearCache() {
        log.info("Plant cache cleared");
    }

    @Override
    @CacheEvict(value = "plants", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Plant plant = getById(id);
        if (plant == null) return;
        // 先删除关联文件，再删除数据库记录
        fileCleanupHelper.deleteFilesFromJson(plant.getImages());
        fileCleanupHelper.deleteFilesFromJson(plant.getVideos());
        fileCleanupHelper.deleteFilesFromJson(plant.getDocuments());
        removeById(id);
        log.info("Deleted plant {} with associated files", id);
    }
}
```

### 5.2 UserServiceImpl -- 用户服务

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired private UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ---- 登录 ----
    @Override
    public String login(String username, String password) {
        // 1. 参数校验
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw BusinessException.badRequest("用户名和密码不能为空");
        }
        // 2. 查找用户
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        // 3. 检查封禁状态
        if (user.isBanned()) {
            throw BusinessException.forbidden("该用户已被封禁");
        }
        // 4. 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw BusinessException.passwordWrong();
        }
        // 5. Sa-Token 登录，生成 Token
        StpUtil.login(user.getId());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("role", user.getRole());
        return StpUtil.getTokenValue();  // 返回 Token 给前端
    }

    // ---- 注册 ----
    @Override
    @Transactional(rollbackFor = Exception.class)  // 事务：保证数据一致性
    public void register(String username, String password) {
        // 1. 参数校验
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw BusinessException.badRequest("用户名和密码不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new BusinessException(ErrorCode.USERNAME_TOO_SHORT);
        }
        // 2. 密码强度校验
        PasswordValidator.ValidationResult validationResult = PasswordValidator.validate(password);
        if (!validationResult.isValid()) {
            throw BusinessException.passwordTooWeak();
        }
        // 3. 用户名唯一性检查
        if (getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) != null) {
            throw BusinessException.userAlreadyExists();
        }
        // 4. 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));  // BCrypt加密
        user.setRole(RoleConstants.ROLE_USER);
        user.setStatus(User.STATUS_ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        save(user);
    }

    // ---- 修改密码 ----
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)  // 清除用户缓存
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword)) {
            throw BusinessException.badRequest("密码不能为空");
        }
        PasswordValidator.ValidationResult validationResult = PasswordValidator.validate(newPassword);
        if (!validationResult.isValid()) {
            throw BusinessException.passwordTooWeak();
        }
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw BusinessException.passwordWrong();
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    // ---- 获取用户信息 ----
    @Override
    public User getUserInfo(Integer userId) {
        User user = getById(userId);
        if (user == null) {
            throw BusinessException.userNotFound();
        }
        user.setPasswordHash(null);  // 重要！返回给前端前清除密码
        return user;
    }

    // ---- 删除用户 ----
    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Integer userId) {
        Integer currentId = SecurityUtils.getCurrentUserId();
        if (currentId != null && currentId.equals(userId)) {
            throw BusinessException.forbidden("不能删除当前登录账号");
        }
        User target = getById(userId);
        if (target == null) {
            throw BusinessException.userNotFound();
        }
        if (RoleConstants.ROLE_ADMIN.equals(target.getRole()) && countAdmins() <= 1) {
            throw BusinessException.forbidden("不能删除系统唯一的管理员账号");
        }
        removeById(userId);
    }

    // ---- 封禁/解封 ----
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void banUser(Integer userId, String reason) {
        Integer currentId = SecurityUtils.getCurrentUserId();
        if (currentId != null && currentId.equals(userId)) {
            throw BusinessException.forbidden("不能封禁当前登录账号");
        }
        User user = getById(userId);
        if (user == null) throw BusinessException.userNotFound();
        if (RoleConstants.ROLE_ADMIN.equals(user.getRole())) {
            throw BusinessException.forbidden("不能封禁管理员账号");
        }
        user.setStatus(User.STATUS_BANNED);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void unbanUser(Integer userId) {
        User user = getById(userId);
        if (user == null) throw BusinessException.userNotFound();
        user.setStatus(User.STATUS_ACTIVE);
        updateById(user);
    }
}
```

---

## 六、缓存策略详解

### 6.1 多级缓存架构

```
请求到达 Service
       |
       v
[1] 检查 @Cacheable 注解
       |
       +-- 缓存命中 --> 直接返回（最快）
       |
       +-- 缓存未命中
              |
              v
[2] 查询 Redis 缓存
       |
       +-- Redis 命中 --> 返回数据（快）
       |
       +-- Redis 未命中 / Redis 不可用
              |
              v
[3] 查询 MySQL 数据库
       |
       v
[4] 结果写入缓存，返回数据（最慢）
```

### 6.2 各缓存区域的过期时间

| 缓存区域 | 过期时间 | 说明 |
|---------|---------|------|
| plants | 6小时 | 药用植物数据变化不频繁 |
| knowledges | 6小时 | 知识数据变化不频繁 |
| inheritors | 6小时 | 传承人数据变化不频繁 |
| resources | 4小时 | 资源数据偶尔更新 |
| users | 30分钟 | 用户信息可能随时变更 |
| quizQuestions | 12小时 | 题目很少变化 |
| searchResults | 5分钟 | 搜索结果变化较快 |
| hotData | 1小时 | 热门数据需要较新 |

### 6.3 Redis 不可用时的降级

```java
// CacheConfig.java 中的降级逻辑
@Bean
@Primary
public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    try {
        connectionFactory.getConnection().ping();  // 尝试连接 Redis
        return createRedisCacheManager(connectionFactory);  // Redis 可用
    } catch (Exception e) {
        log.warn("Redis连接失败，降级到内存缓存: {}", e.getMessage());
        return createFallbackCacheManager();  // 降级到 Caffeine
    }
}
```

---

## 七、本项目的所有 Service

| 服务接口 | 实现类 | 职责 | 关键注解 |
|---------|--------|------|---------|
| PlantService | PlantServiceImpl | 药用植物搜索、详情、缓存 | @Cacheable, @CacheEvict |
| UserService | UserServiceImpl | 注册、登录、改密、封禁 | @Transactional, @CacheEvict, BCrypt |
| KnowledgeService | KnowledgeServiceImpl | 知识库CRUD、分类查询 | @Cacheable, @CacheEvict |
| InheritorService | InheritorServiceImpl | 传承人CRUD、级别筛选 | @Cacheable, @CacheEvict |
| ResourceService | ResourceServiceImpl | 资源CRUD、下载计数 | @Cacheable, @CacheEvict |
| QaService | QaServiceImpl | 问答CRUD、分类查询 | @Cacheable |
| CommentService | CommentServiceImpl | 评论发表、嵌套回复、审核 | @Transactional |
| FavoriteService | FavoriteServiceImpl | 收藏添加/取消、列表 | @Transactional |
| FeedbackService | FeedbackServiceImpl | 反馈提交、管理员回复 | @Transactional |
| QuizService | QuizServiceImpl | 测验题目、评分、记录 | @Cacheable, @Transactional |
| PlantGameService | PlantGameServiceImpl | 植物识别游戏记录 | @Transactional |
| AiChatService | AiChatServiceImpl | DeepSeek AI聊天 | @RateLimit |
| FileUploadService | FileUploadServiceImpl | 文件上传校验+存储 | -- |
| CaptchaService | -- | 验证码生成与验证 | Redis 5分钟过期 |
| OperationLogService | OperationLogServiceImpl | 操作日志管理 | -- |

---

## 八、如何添加一个新的 Service

假设要添加"侗族药方"（Recipe）模块的 Service：

### 第1步：创建 Service 接口

```java
// service/RecipeService.java
package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Recipe;
import java.util.List;

/**
 * 侗族药方服务接口
 */
public interface RecipeService extends IService<Recipe> {

    /**
     * 分页搜索药方
     */
    Page<Recipe> searchPaged(String keyword, Integer page, Integer size);

    /**
     * 清除药方缓存
     */
    void clearCache();
}
```

### 第2步：创建 Service 实现类

```java
// service/impl/RecipeServiceImpl.java
package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Recipe;
import com.dongmedicine.mapper.RecipeMapper;
import com.dongmedicine.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service   // 必须加！
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {

    @Autowired
    private RecipeMapper recipeMapper;

    @Override
    @Cacheable(value = "recipes", key = "'search:' + (#keyword ?: 'all') + ':' + #page + ':' + #size")
    public Page<Recipe> searchPaged(String keyword, Integer page, Integer size) {
        Page<Recipe> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Recipe> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String escapedKeyword = PageUtils.escapeLike(keyword);
            qw.and(w -> w.like(Recipe::getNameCn, escapedKeyword)
                         .or().like(Recipe::getIndications, escapedKeyword));
        }
        qw.orderByDesc(Recipe::getCreatedAt);
        return page(pageParam, qw);
    }

    @Override
    @CacheEvict(value = "recipes", allEntries = true)
    public void clearCache() {
        log.info("Recipe cache cleared");
    }
}
```

### 第3步：在 CacheConfig 中注册缓存区域

```java
// CacheConfig.java 的 cacheConfigurations 中添加：
cacheConfigurations.put("recipes", defaultConfig.entryTtl(Duration.ofHours(4)));
```

### 第4步：在 Controller 中使用

```java
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;  // 注入 Service

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String keyword) {
        return R.ok(PageUtils.toMap(recipeService.searchPaged(keyword, page, size)));
    }
}
```

---

## 九、常见错误与避免方法

### 错误1：忘记加 @Service 注解

```java
// 错误！Spring 不会创建这个 Bean，注入时报 NullPointerException
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {
    // ...
}

// 正确！
@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {
    // ...
}
```

### 错误2：修改数据后忘记清缓存

```java
// 错误！数据库已更新，但缓存还是旧数据，前端看到的是过期数据
@Override
public void updateRecipe(Recipe recipe) {
    updateById(recipe);  // 数据库更新了
    // 缓存没清！前端可能看到旧数据
}

// 正确！更新后清除缓存
@Override
@CacheEvict(value = "recipes", allEntries = true)
public void updateRecipe(Recipe recipe) {
    updateById(recipe);
}
```

### 错误3：在同一个类内部调用 @Cacheable 方法

```java
@Service
public class PlantServiceImpl {

    public void someMethod() {
        // 错误！这样调用 @Cacheable 不会生效
        // 因为 Spring AOP 是基于代理的，内部调用绕过了代理
        this.advancedSearch("钩藤", null, null);
    }

    @Cacheable(value = "plants", key = "'search:' + #keyword")
    public List<Plant> advancedSearch(String keyword, String category, String usageWay) {
        // ...
    }
}
```

> **解决方法：** 把 `@Cacheable` 方法放在不同的 Service 中，或者通过注入自身来调用（不推荐）。

### 错误4：@Async 方法是 private 的

```java
// 错误！private 方法 @Async 不生效，Spring AOP 无法代理私有方法
@Async("viewCountExecutor")
private void incrementViewCount(Integer id) { ... }

// 正确！必须是 public
@Async("viewCountExecutor")
public void incrementViewCount(Integer id) { ... }
```

### 错误5：返回给前端时忘记清除敏感信息

```java
// 错误！密码哈希被返回给前端
@Override
public User getUserInfo(Integer userId) {
    return getById(userId);  // 包含 passwordHash！
}

// 正确！返回前清除密码
@Override
public User getUserInfo(Integer userId) {
    User user = getById(userId);
    if (user == null) throw BusinessException.userNotFound();
    user.setPasswordHash(null);  // 清除敏感信息
    return user;
}
```

### 错误6：@Transactional 加在只读方法上

```java
// 不推荐！只读方法不需要事务，加了会占用数据库连接
@Transactional(rollbackFor = Exception.class)
public Plant getById(Integer id) {
    return plantMapper.selectById(id);
}

// 正确！只读方法不加 @Transactional
public Plant getById(Integer id) {
    return plantMapper.selectById(id);
}
```

---

## 十、代码审查与改进建议

以下是对 Service 层代码的审查发现，按严重程度排序：

### 严重级别

| # | 级别 | 问题 | 说明 |
|---|------|------|------|
| 1 | 严重-安全 | `UserServiceImpl.getUserToken()` 允许通过用户ID直接生成登录Token | 完全绕过身份验证，是严重权限提升漏洞。任何知道用户ID的人都可以伪造登录态。 |
| 2 | 严重-安全 | `PlantGameServiceImpl.submit()` 中 `correctCount` 和 `totalCount` 完全由客户端提供 | 恶意用户可提交任意分数，破坏游戏公平性。应在服务端根据题目答案重新计算得分。 |
| 3 | 严重-安全 | `CaptchaService` 使用 `java.util.Random` 而非 `SecureRandom` | 验证码可预测，攻击者可推算出验证码内容，使验证码机制形同虚设。 |

### 高级别

| # | 级别 | 问题 | 说明 |
|---|------|------|------|
| 4 | 高-并发 | `FavoriteServiceImpl.addFavorite()` 先查询后插入不是原子操作 | 并发场景下多个请求可能同时通过唯一性检查，导致重复收藏。应使用数据库唯一索引或分布式锁保证原子性。 |
| 5 | 高-逻辑 | `KnowledgeServiceImpl` 中 `@Async` 自调用失效 | `incrementPopularityAsync()` 在同类内部调用，Spring AOP 基于代理机制，内部调用绕过代理，`@Async` 注解不生效，实际仍为同步执行。 |
| 6 | 高-安全 | `UserServiceImpl.login()` 无登录失败次数限制 | 容易遭受暴力破解攻击。应增加失败计数和锁定机制，如5次失败后锁定账号15分钟。 |
| 7 | 高-安全 | `UserServiceImpl` 中 Sa-Token 的 `kickout` 参数类型与 `login` 参数类型不匹配 | `kickout` 参数为 `Integer` 类型，而 `login` 参数为 `String` 类型，类型不一致可能导致踢人功能异常。 |
| 8 | 高-逻辑 | `ResourceServiceImpl.listByCategoryAndKeywordAndType()` 忽略了 `fileType` 参数 | 方法签名接收 `fileType` 但查询逻辑未使用该参数进行过滤，而缓存 key 却包含了 `fileType`，导致不同 `fileType` 的请求缓存了相同的错误结果。 |

### 中等级别

| # | 级别 | 问题 | 说明 |
|---|------|------|------|
| 9 | 中等-性能 | `OperationLogServiceImpl` 每次 `save` 都触发全表 `COUNT(*)` 查询 | 用于检查日志数量是否超限，高频操作场景下性能极差。应改用定时任务异步清理，或缓存总数。 |
| 10 | 中等-内存 | `CommentServiceImpl.listAllDTO()` 和 `listAllApproved()` 无分页限制 | 查询全部数据转为 DTO 列表，数据量大时可能导致 OOM（内存溢出）。应强制分页或限制最大返回条数。 |
| 11 | 中等-安全 | `FileUploadServiceImpl` 异常信息泄露文件系统路径 | 异常消息中包含服务器文件路径信息，攻击者可利用此信息进行路径遍历攻击。应返回通用错误信息，将详细路径仅记录到日志。 |
| 12 | 中等-逻辑 | `QuizServiceImpl` 答案比较使用 `equals` 过于严格 | 不考虑空格和大小写差异，用户输入 "钩藤 " 或 "钩藤" 应视为相同答案但会被判错。应先 `trim()` 并忽略大小写后再比较。 |
| 13 | 中等-逻辑 | `RabbitMQOperationLogServiceImpl` 日志说"降级为同步保存"但实际只是抛出异常 | 日志信息具有误导性，实际并未实现降级逻辑，RabbitMQ 不可用时日志会丢失。应真正实现同步降级写入数据库。 |

### 低级别

| # | 级别 | 问题 | 说明 |
|---|------|------|------|
| 14 | 低-规范 | 依赖注入方式不一致 | 部分类使用 `@Autowired` 字段注入，部分类使用 `@RequiredArgsConstructor` 构造器注入。应统一为构造器注入（Spring 官方推荐）。 |
| 15 | 低-规范 | 冗余 Mapper 注入 | 多个 `ServiceImpl` 在继承 `ServiceImpl<Mapper, Entity>` 的同时额外 `@Autowired` 注入了相同的 Mapper。`ServiceImpl` 已内置 `baseMapper`，无需重复注入。 |
| 16 | 低-规范 | 缓存策略不一致 | 部分查询方法有 `@Cacheable` 注解，但对应的分页版本方法却没有缓存，导致分页请求每次都查数据库。缓存策略应保持一致。 |
